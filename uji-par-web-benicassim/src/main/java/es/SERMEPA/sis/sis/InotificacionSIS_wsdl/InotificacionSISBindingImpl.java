package es.SERMEPA.sis.sis.InotificacionSIS_wsdl;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.config.ConfigurationSelector;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.tpvmodel.Message;
import es.uji.apps.par.utils.DateUtils;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sis.redsys.api.ApiMacSha256;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.Locale;

public class InotificacionSISBindingImpl implements InotificacionSISPortType {
    private static final Logger log = LoggerFactory.getLogger(InotificacionSISBindingImpl.class);
    private static final int PEDIDO_REPETIDO = 913;
    private static Connection conn = null;

    @Autowired
    Configuration configuration;

    @Autowired
    ConfigurationSelector configurationSelector;

    private void initializeConnectionIfNeeded() throws SQLException {
        if (conn == null)
            conn = DriverManager.getConnection(configuration.getJdbUrl(), configuration.getDBUser(), configuration.getDBPassword());
    }

    private void closeConnection() throws SQLException {
        if (conn != null)
            conn.close();

        conn = null;
    }

    public java.lang.String procesaNotificacionSIS(java.lang.String XML) throws java.rmi.RemoteException {
        String msg = String.format("Llega al soap con el XML %s", XML);
        log.info(msg);

        try {
            boolean estadoOk = false;
            CompraDTO compra = null;
            Message message = xmlToJaxb(XML);
            String identificador = message.getRequest().getDs_MerchantData();
            String estado = message.getRequest().getDs_Response();
            String recibo = message.getRequest().getDs_Order();

            initializeConnectionIfNeeded();

            PreparedStatement stmt = conn.prepareStatement("select pt.secret from par_compras pc join par_sesiones ps on pc.sesion_id=ps.id join par_eventos pe on ps.evento_id=pe.id join par_tpvs pt on pe.tpv_id=pt.id where pc.id= ?");
            stmt.setLong(1, Long.valueOf(identificador));
            ResultSet res = stmt.executeQuery();
            String secret = null;
            if (res.next())
                secret = res.getString("secret");

            stmt = conn.prepareStatement("select * from par_compras where id = ?");
            stmt.setLong(1, Long.valueOf(identificador));
            res = stmt.executeQuery();

            if (res.next())
                compra = CompraDTO.resultsetToDTO(res);
            closeConnection();

            // Comparamos la firma experada con la que nos llega (así sabemos si es urlSafe o no)
            boolean isRequestSignatureValidate = true;

            ApiMacSha256 apiMacSha256 = new ApiMacSha256();
            boolean urlSafe = false;
            String requestSignatureSOAP = getRequestSignatureSOAP(XML);
            try {
                String urlSafeRequestSignatureSOAP = apiMacSha256.createMerchantSignatureNotifSOAPRequest(secret, XML);

                urlSafe = isUrlSafe(requestSignatureSOAP, urlSafeRequestSignatureSOAP);
            } catch (Exception e) {
                isRequestSignatureValidate = false;
            }

            if (secret != null && compra != null) {
                if (compra.getCaducada())
                {
                    log.info("Compra caducada");
                    // Guardamos código pago de pasarela para luego saber que pago anular
                    rellenaReciboTPVEnCompra(compra.getId(), recibo);
                }
                else if (isEstadoOk(estado) && isRequestSignatureValidate)
                {
                    estadoOk = true;
                    log.info("Compra NO caducada: " + compra.getId());
                    marcaCompraComoPagadaEnPasarela(compra.getId(), recibo);
                    log.info("Marcada como pagada " + compra.getId() + " - " + compra.getUuid() + " - " + compra.getEmail());
                    enviaMail(compra.getEmail(), compra.getUuid(), recibo);
                } else {
                    if (!isRequestSignatureValidate) {
                        log.error("Compra: " + compra.getId() + ". No hemos podido validar la firma devuelta por el banco " + XML);
                    }

                    log.info("Compra no pagada: " + compra.getId() + ". El banco nos da el estado " + estado);
                    if ((compra.getPagada() == null || !compra.getPagada())
                            && (compra.getCodigoPagoPasarela() == null || compra.getCodigoPagoPasarela().equals("")))
                        borraCompraPorNoPagada(compra.getId(), estado, compra.getUuid());
                    else {
                        log.info("La compra " + compra.getId() + " ya estaba pagada, con codigo de pasarela "
                                + compra.getCodigoPagoPasarela() + " no la borramos");
                    }
                }

                try {
                    String numPedido = apiMacSha256.getOrderNotifSOAP(XML);
                    String response = getResponse(estadoOk);
                    String signature = apiMacSha256.createMerchantSignatureNotifSOAPResponse(secret, response, numPedido);
                    if (!urlSafe) {
                        signature = getUrlUnsafeSignature(signature);
                    }
                    log.info("Y obtenemos la firma " + signature);

                    return getMessageRespuesta(signature, estadoOk);
                } catch (Exception e) {
                    log.error("Error generando la firma HMAC SHA256", e);
                }
            } else {
                log.error("Se intenta marcar como pagada una compra que no existe. Devolvemos error");
            }
        } catch (SQLException e) {
            log.error("Error de base de datos", e);
        } catch (JAXBException e) {
            log.error("Error en el JAXB", e);
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                log.error("Error cerrando la conexion SQL", e);
            }
        }
        return null;
    }

    private void borraCompraPorNoPagada(long id, String estado, String uuid) throws SQLException {
        log.info("Borramos la compra por no pagada y la movemos a la tabla de borradas " + id + " - estado " + estado + " - uuid " + uuid);
        initializeConnectionIfNeeded();
        String sql = "begin transaction;" +
                "insert into par_compras_borradas (compra_id, sesion_id, nombre, apellidos, direccion, poblacion, " +
                "cp, provincia, tfno, email, info_periodica, fecha, taquilla, importe, codigo_pago_tarjeta, pagada, uuid," +
                "codigo_pago_pasarela, reserva, desde, hasta, observaciones_reserva, anulada, recibo_pinpad, caducada," +
                "referencia_pago, compra_uuid, fecha_borrado, codigo_error_banco) " +

                "select id, sesion_id, nombre, apellidos, direccion, poblacion, cp, provincia, tfno, email, info_periodica, " +
                "fecha, taquilla, importe, codigo_pago_tarjeta, pagada, uuid, codigo_pago_pasarela, reserva, desde, hasta, " +
                "observaciones_reserva, anulada, recibo_pinpad, caducada, referencia_pago, uuid, now(), ? " +
                "from par_compras where id = ?;" +

                "insert into par_butacas_borradas (butaca_id, sesion_id, localizacion_id, compra_id, fila, numero, tipo, precio, " +
                "anulada, presentada, fecha_borrada) " +
                "select id, sesion_id, localizacion_id, compra_id, fila, numero, tipo, precio, anulada, presentada, now() " +
                "from par_butacas where compra_id = ?; " +

                "delete from par_compras where id = ?;" +

                "commit;";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, estado);
        stmt.setLong(2, id);
        stmt.setLong(3, id);
        stmt.setLong(4, id);
        stmt.execute();
        closeConnection();
        int intEstado = Integer.valueOf(estado);

        if (intEstado == PEDIDO_REPETIDO)
            enviaMailAdmin(id, estado, uuid);
    }

    private String getResponse(boolean estadoOperacion) {
        String Ds_Response_Merchant = "";

        if (estadoOperacion)
            Ds_Response_Merchant = "OK";
        else
            Ds_Response_Merchant = "KO";
        String respuestaAPinyon = "<Response Ds_Version=\"0.0\"><Ds_Response_Merchant>" + Ds_Response_Merchant + "</Ds_Response_Merchant></Response>";
        return respuestaAPinyon;
    }

    private void marcaCompraComoPagadaEnPasarela(long id, String recibo) throws SQLException {
        initializeConnectionIfNeeded();
        PreparedStatement stmt;

        if (configuration.isIdEntrada()) {
            stmt = conn.prepareStatement("update PAR_COMPRAS set CODIGO_PAGO_PASARELA = ?, " +
                    "PAGADA = true where id = ?;select updateEntradaId(?, ?);commit;");
            stmt.setString(1, recibo);
            stmt.setLong(2, id);
            stmt.setInt(3, new Long(id).intValue());
            stmt.setInt(4, new Long(configuration.getIdEntrada()).intValue());
        }
        else {
            stmt = conn.prepareStatement("update PAR_COMPRAS set CODIGO_PAGO_PASARELA = ?, PAGADA = true where id = ?;commit;");
            stmt.setString(1, recibo);
            stmt.setLong(2, id);
        }
        stmt.execute();
        closeConnection();
    }

    private void rellenaReciboTPVEnCompra(long id, String recibo) throws SQLException {
        initializeConnectionIfNeeded();
        PreparedStatement stmt = conn.prepareStatement("update PAR_COMPRAS set CODIGO_PAGO_PASARELA = ? where id = ?;commit");
        stmt.setString(1, recibo);
        stmt.setLong(2, id);
        stmt.execute();
        closeConnection();
    }

    private Message xmlToJaxb(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Message message = (Message) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(xml)));

        return message;
    }

    private String getMessageRespuesta(String signatura, boolean estadoOperacion) throws JAXBException {
        String Ds_Response_Merchant;

        if (estadoOperacion)
            Ds_Response_Merchant = "OK";
        else
            Ds_Response_Merchant = "KO";

        String respuestaAPinyon = "<Message><Response Ds_Version=\"0.0\"><Ds_Response_Merchant>" + Ds_Response_Merchant + "</Ds_Response_Merchant></Response>" +
                "<Signature>" + signatura + "</Signature></Message>";

        log.info("Enviamos respuesta " + respuestaAPinyon);
        return respuestaAPinyon;
    }

    private boolean isEstadoOk(String estado)
    {
        int intEstado = Integer.valueOf(estado);
        return (intEstado >= 0 && intEstado <= 99) || intEstado == 900;
    }

    private void enviaMail(String email, String uuid, String recibo) throws SQLException
    {
        initializeConnectionIfNeeded();
        String urlEntradas = String.format("%s/rest/compra/%s/pdf", configurationSelector.getUrlPublic(), uuid);

        String titulo = ResourceProperties.getProperty(new Locale("ca"), "mail.entradas.titulo") + " | " +
                ResourceProperties.getProperty(new Locale("es"), "mail.entradas.titulo");
        String texto = ResourceProperties.getProperty(new Locale("ca"), "mail.entradas.texto", recibo, urlEntradas) + "\n\n" +
                ResourceProperties.getProperty(new Locale("es"), "mail.entradas.texto", recibo, urlEntradas);

        PreparedStatement stmt = conn.prepareStatement("insert into PAR_MAILS (DE, PARA, TITULO, TEXTO, FECHA_CREADO, COMPRA_UUID) values (?, ?, ?, ?, ?, ?);commit;");
        stmt.setString(1, configurationSelector.getMailFrom());
        stmt.setString(2, email);
        stmt.setString(3, titulo);
        stmt.setString(4, texto);
        stmt.setTimestamp(5, new java.sql.Timestamp(DateUtils.getCurrentDate().getTime()));
        stmt.setString(6, uuid);
        stmt.execute();
        closeConnection();
    }

    private void enviaMailAdmin(long id, String estadoBanco, String uuid) throws SQLException
    {
        initializeConnectionIfNeeded();
        PreparedStatement stmt = conn.prepareStatement("insert into PAR_MAILS (DE, PARA, TITULO, TEXTO, FECHA_CREADO, COMPRA_UUID) " +
                "values (?, ?, ?, ?, ?, ?);commit;");
        stmt.setString(1, configurationSelector.getMailFrom());
        stmt.setString(2, "debug@wifi.benicassim.es");
        stmt.setString(3, "Pedido repetido en venta de entradas online de Benicàssim. Revisar por si hay error");
        stmt.setString(4, "Revisar la entrada con id " + id + " respuesta del banco " + estadoBanco + " y uuid " + uuid);
        stmt.setTimestamp(5, new java.sql.Timestamp(DateUtils.getCurrentDate().getTime()));
        stmt.setString(6, uuid);
        stmt.execute();
        closeConnection();
    }

    public static String getRequestSignatureSOAP(String datos) {
        int posReqIni = datos.indexOf("<Signature");
        int posReqFin = datos.indexOf("</Signature>");
        int tamReqIni = "<Signature>".length();
        return datos.substring(posReqIni + tamReqIni, posReqFin);
    }

    public static boolean isUrlSafe(String expectedSignature, String urlSafeSignature) throws Exception {
        if (expectedSignature.equals(urlSafeSignature)) {
            return true;
        }
        else {
            String urlUnsafeSignature = getUrlUnsafeSignature(urlSafeSignature);
            if (urlUnsafeSignature != null && urlUnsafeSignature.equals(expectedSignature)) {
                return false;
            }
        }
        throw new Exception("No se puede comprobar que la firma es UrlSafe");
    }

    private static String getUrlUnsafeSignature(String urlSafeSignature) {
        byte[] data = urlSafeSignature.getBytes();
        byte[] encode = Arrays.copyOf(data, data.length);

        for (int i = 0; i < encode.length; ++i) {
            if (encode[i] == 45) {
                encode[i] = 43;
            } else if (encode[i] == 95) {
                encode[i] = 47;
            }
        }
        try {
            return new String(encode, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            log.error("Error reemplazando caracteres urlSafe en la firma");
        }
        return null;
    }
}