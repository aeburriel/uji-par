package es.uji.apps.par.services.rest;

import com.mysema.commons.lang.Pair;
import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.butacas.EstadoButacasRequest;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.exceptions.*;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.*;
import es.uji.apps.par.services.*;
import es.uji.apps.par.tpv.HmacSha256TPVInterface;
import es.uji.apps.par.tpv.IdTPVInterface;
import es.uji.apps.par.tpv.SHA1TPVInterface;
import es.uji.apps.par.tpv.TpvInterface;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("entrada")
public class EntradasResource extends BaseResource {
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Logger log = LoggerFactory.getLogger(EntradasResource.class);

    @InjectParam
    private SesionesService sesionesService;

    @InjectParam
    private ButacasService butacasService;

    @InjectParam
    private ComprasService comprasService;

    @InjectParam
    private LocalizacionesService localizacionesService;

    @InjectParam
    private UsersService usersService;

    @Context
    HttpServletResponse currentResponse;

    @InjectParam
    private TpvInterface tpvInterface;

    @InjectParam
    private HmacSha256TPVInterface hmacSha256TPVInterface;

    @InjectParam
    private SHA1TPVInterface sha1TPVInterface;

    @InjectParam
    private IdTPVInterface idTPVInterface;

    @InjectParam
    private PublicPageBuilderInterface publicPageBuilderInterface;

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response datosEntrada(@PathParam("id") Long sesionId) throws Exception {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
        Sesion sesion = sesionesService.getSesion(sesionId, user.getUsuario());
        if (sesion.getCanalInternet() && (sesion.getAnulada() == null || sesion.getAnulada() == false)) {
            currentRequest.getSession().setAttribute(EntradasService.ID_SESION, sesionId);

            if (Utils.isAsientosNumerados(sesion.getEvento())) {
                return paginaSeleccionEntradasNumeradas(sesionId, null, null, null, user.getUsuario());
            } else {
                return paginaSeleccionEntradasNoNumeradas(sesionId, null, user.getUsuario());
            }
        } else
            return paginaFueraDePlazo(sesionId, user.getUsuario());
    }

    private Response paginaSeleccionEntradasNumeradas(long sesionId, List<Butaca> butacasSeleccionadas,
                                                      List<Butaca> butacasOcupadas, String error, String userUID) throws Exception {
        Sesion sesion = sesionesService.getSesion(sesionId, userUID);
        String urlBase = getBaseUrlPublic();
        String url = currentRequest.getRequestURL().toString();

        if (!sesion.getEnPlazoVentaInternet())
            return paginaFueraDePlazo(sesionId, userUID);

        Evento evento = sesion.getEvento();

        Locale locale = getLocale();
        String language = locale.getLanguage();
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/seleccionEntrada", locale, APP);
        template.put("evento", evento);
        template.put("sesion", sesion);
        template.put("idioma", language);
        template.put("baseUrl", getBaseUrlPublic());
        template.put("fecha", DateUtils.dateToSpanishString(sesion.getFechaCelebracion()));
        template.put("hora", sesion.getHoraCelebracion());
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("tipoEventoEs", sesion.getEvento().getParTiposEvento().getNombreEs());
        template.put("butacasFragment", Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/" + sesion.getSala().getHtmlTemplateName());
		Calendar cal = Calendar.getInstance();
		template.put("millis", cal.getTime().getTime());
        //template.put("tarifas", sesionesService)

        template.put("estilosOcupadas", butacasService.estilosButacasOcupadas(sesionId, localizacionesService.getLocalizacionesSesion(sesionId), false));

        if (error != null && !error.equals("")) {
            template.put("error", error);
        }

        String butacasSesion = (String) currentRequest.getSession().getAttribute(EntradasService.BUTACAS_COMPRA);
        if (butacasSesion == null) {
            template.put("butacasSesion", "[]");
        } else {
            template.put("butacasSesion", butacasSesion);
        }

        String uuidCompra = (String) currentRequest.getSession().getAttribute(EntradasService.UUID_COMPRA);
        if (uuidCompra != null) {
            template.put("uuidCompra", uuidCompra);
        }

        if (language.equals("ca")) {
            template.put("titulo", evento.getTituloVa());
            template.put("tipoEvento", evento.getParTiposEvento().getNombreVa());
        } else {
            template.put("titulo", evento.getTituloEs());
            template.put("tipoEvento", evento.getParTiposEvento().getNombreEs());
        }

        if (butacasSeleccionadas != null)
            template.put("butacasSeleccionadas", butacasSeleccionadas);

        List<PreciosSesion> precios = sesionesService.getPreciosSesion(sesion.getId(), userUID);

        for (PreciosSesion precio : precios) {
            template.put("precioNormal_" + precio.getLocalizacion().getCodigo(), precio.getPrecio());
            template.put("precioDescuento_" + precio.getLocalizacion().getCodigo(), precio.getDescuento());
        }

        template.put("gastosGestion", Float.parseFloat(configuration.getGastosGestion()));
        template.put("lang", language);

        ResponseBuilder builder = Response.ok(template);

        return Utils.noCache(builder).build();
    }

    private Response paginaSeleccionEntradasNoNumeradas(long sesionId, String error, String userUID) throws Exception {
        Sesion sesion = sesionesService.getSesion(sesionId, userUID);
        String urlBase = getBaseUrlPublic();

        if (!sesion.getEnPlazoVentaInternet())
            return paginaFueraDePlazo(sesionId, userUID);

        Evento evento = sesion.getEvento();

        Locale locale = getLocale();
        String language = locale.getLanguage();
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/seleccionEntradaNoNumerada", locale, APP);
        template.put("evento", evento);
        template.put("sesion", sesion);
        template.put("idioma", language);
        template.put("baseUrl", getBaseUrlPublic());
        template.put("fecha", DateUtils.dateToSpanishString(sesion.getFechaCelebracion()));
        template.put("hora", sesion.getHoraCelebracion());
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, urlBase, language.toString(), configurationSelector.getHtmlTitle()));
        Calendar cal = Calendar.getInstance();
        template.put("millis", cal.getTime().getTime());
        List<Tarifa> tarifas = new ArrayList<Tarifa>();

        if (sesion.getPlantillaPrecios() != null && sesion.getPlantillaPrecios().getId() != -1)
            tarifas = sesionesService.getTarifasPublicasConPrecioConPlantilla(sesionId);
        else
            tarifas = sesionesService.getTarifasPublicasConPrecioSinPlantilla(sesionId);
        //List<PreciosSesion> preciosSesion = sesionesService.getPreciosSesion(sesionId);

        template.put("tarifas", tarifas);
        //template.put("preciosSesion", preciosSesion);

        if (error != null && !error.equals("")) {
            template.put("error", error);
        }

        String uuidCompra = (String) currentRequest.getSession().getAttribute(EntradasService.UUID_COMPRA);
        if (uuidCompra != null) {
            template.put("uuidCompra", uuidCompra);
        }

        if (language.equals("ca")) {
            template.put("titulo", evento.getTituloVa());
        } else {
            template.put("titulo", evento.getTituloEs());
        }

        List<PreciosSesion> preciosSesion = sesionesService.getPreciosSesionPublicos(sesion.getId(), userUID);
        template.put("preciosSesion", preciosSesion);

        for (PreciosSesion precio : preciosSesion) {
            String codigoLocalizacion = precio.getLocalizacion().getCodigo();

            //template.put("precioNormal_" + codigoLocalizacion, precio.getPrecio());
            //template.put("precioDescuento_" + codigoLocalizacion, precio.getDescuento());

            // Hay algunos casos en los que no se permite descuento
            template.put("descuentoNoDisponible_" + codigoLocalizacion, comprasService.esButacaDescuentoNoDisponible("descuento", evento, precio));
        }

        Map<String, Map<Long, PreciosSesion>> preciosSesionLocalizacion = sesionesService.getPreciosSesionPublicosPorLocalizacion(sesion.getId(), userUID);
        template.put("preciosSesionLocalizacion", preciosSesionLocalizacion);

        template.put("gastosGestion", Float.parseFloat(configuration.getGastosGestion()));
        template.put("lang", language);

        ResponseBuilder builder = Response.ok(template);

        return Utils.noCache(builder).build();
    }

    @POST
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response compraEntradaHtml(@PathParam("id") Long sesionId,
                                      @FormParam("butacasSeleccionadas") String butacasSeleccionadasJSON,
                                      @FormParam("platea1Normal") String platea1Normal, @FormParam("platea1Descuento") String platea1Descuento,
                                      @FormParam("platea2Normal") String platea2Normal, @FormParam("platea2Descuento") String platea2Descuento,
                                      @FormParam("uuidCompra") String uuidCompra, @FormParam("b_t") String strButacas) throws Exception {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
        Sesion sesion = sesionesService.getSesion(sesionId, user.getUsuario());

        if (Utils.isAsientosNumerados(sesion.getEvento())) {
            return compraEntradaNumeradaHtml(sesionId, butacasSeleccionadasJSON, uuidCompra, user.getUsuario());
        } else {
            return compraEntradaNoNumeradaHtml(sesionId, strButacas, uuidCompra, user.getUsuario());
        }
    }

    private Response compraEntradaNumeradaHtml(Long sesionId, String butacasSeleccionadasJSON, String uuidCompra, String userUID)
            throws Exception {
        ResultadoCompra resultadoCompra;
        List<Butaca> butacasSeleccionadas = Butaca.parseaJSON(butacasSeleccionadasJSON);

        try {
            Map<String, List<Butaca>> butacasExistentes = new HashMap<String, List<Butaca>>();
            for (Butaca butacaSeleccionada : butacasSeleccionadas) {
                String localizacion = butacaSeleccionada.getLocalizacion();
                if (!butacasExistentes.containsKey(localizacion)) {
                    byte[] encoded = Files.readAllBytes(Paths.get(configuration.getPathJson() + localizacion + ".json"));
                    butacasExistentes.put(localizacion, Butaca.parseaJSON(new String(encoded, "UTF-8")));
                }

                if (!existeButaca(butacasExistentes.get(localizacion), butacaSeleccionada)) {
                    throw new CompraButacaNoExistente();
                }
            }

            resultadoCompra = comprasService.realizaCompraInternet(sesionId, butacasSeleccionadas, uuidCompra, userUID);
        } catch (FueraDePlazoVentaInternetException e) {
            log.error("Fuera de plazo", e);
            return paginaFueraDePlazo(sesionId, userUID);
        } catch (ButacaOcupadaException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.ocupadas");
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas, null, error, userUID);
        } catch (CompraSinButacasException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.noSeleccionadas");
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas, null, error, userUID);
        } catch (CompraInvitacionPorInternetException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.invitacionPorInternet");
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas, null, error, userUID);
        } catch (CompraButacaDescuentoNoDisponible e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraDescuentoNoDisponible");
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas, null, error, userUID);
        } catch (CompraButacaNoExistente e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraButacaNoExistente");
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas, null, error, userUID);
        }

        if (resultadoCompra.getCorrecta()) {
            currentRequest.getSession().setAttribute(EntradasService.BUTACAS_COMPRA, butacasSeleccionadasJSON);
            currentRequest.getSession().setAttribute(EntradasService.UUID_COMPRA, resultadoCompra.getUuid());

            currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + resultadoCompra.getUuid() + "/datosComprador");
            return null;
        } else {
            return paginaSeleccionEntradasNumeradas(sesionId, butacasSeleccionadas,
                    resultadoCompra.getButacasOcupadas(), null, userUID);
        }
    }


    private boolean existeButaca(List<Butaca> butacas, Butaca butacaSeleccionada) {
        for (Butaca butaca : butacas) {
            if (butacaSeleccionada.getFila().equals(butaca.getFila()) && butacaSeleccionada.getNumero().equals(butaca.getNumero())) {
                return true;
            }
        }
        return false;
    }

    private Response compraEntradaNoNumeradaHtml(Long sesionId, String butacasSeleccionadasJSON, String uuidCompra, String userUID) throws Exception {
        ResultadoCompra resultadoCompra;
        List<Butaca> butacasSeleccionadas = Butaca.parseaJSON("[" + butacasSeleccionadasJSON + "]");

        try {
            resultadoCompra = comprasService.realizaCompraInternet(sesionId, butacasSeleccionadas, uuidCompra, userUID);
        } catch (FueraDePlazoVentaInternetException e) {
            log.error("Fuera de plazo", e);
            return paginaFueraDePlazo(sesionId, userUID);
        } catch (ButacaOcupadaException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.ocupadas");
            return paginaSeleccionEntradasNoNumeradas(sesionId, error, userUID);
        } catch (CompraSinButacasException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.noSeleccionadas");
            return paginaSeleccionEntradasNoNumeradas(sesionId, error, userUID);
        } catch (CompraButacaDescuentoNoDisponible e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraDescuentoNoDisponible");
            return paginaSeleccionEntradasNoNumeradas(sesionId, error, userUID);
        } catch (NoHayButacasLibresException e) {
            String error = "";
            try {
                error = ResourceProperties.getProperty(getLocale(), "error.noHayButacasParaLocalizacion") + " " +
                        ResourceProperties.getProperty(getLocale(), "localizacion." + e.getLocalizacion());
            } catch (Exception ex) {
                error = ResourceProperties.getProperty(getLocale(), "error.noHayButacasParaLocalizacion");
            }

            return paginaSeleccionEntradasNoNumeradas(sesionId, error, userUID);
        } catch (Exception e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.errorGeneral");
            return paginaSeleccionEntradasNoNumeradas(sesionId, error, userUID);
        }

        if (resultadoCompra.getCorrecta()) {
            currentRequest.getSession().setAttribute(EntradasService.UUID_COMPRA, resultadoCompra.getUuid());

            currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + resultadoCompra.getUuid() + "/datosComprador");
            return null;
        } else {
            return paginaSeleccionEntradasNoNumeradas(sesionId, "", userUID);
        }
    }

    @GET
    @Path("{uuidCompra}/datosComprador")
    @Produces(MediaType.TEXT_HTML)
    public Response rellenaDatosComprador(@PathParam("uuidCompra") String uuidCompra, String nombre, String apellidos,
                                          String direccion, String poblacion, String cp, String provincia, String telefono, String email,
                                          String infoPeriodica, String condicionesPrivacidad, String error) throws Exception {
        CompraDTO compra = comprasService.getCompraByUuid(uuidCompra);

        Locale locale = getLocale();
        String language = locale.getLanguage();
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + compra.getParSesion().getParSala().getParCine().getCodigo() + "/datosComprador", locale, APP);
        String urlBase = getBaseUrlPublic();
        String url = currentRequest.getRequestURL().toString();
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("idioma", language);
        template.put("lang", language);

        template.put("uuidCompra", uuidCompra);

        template.put("nombre", nombre);
        template.put("apellidos", apellidos);
        template.put("direccion", direccion);
        template.put("poblacion", poblacion);
        template.put("cp", cp);
        template.put("provincia", provincia);
        template.put("telefono", telefono);
        template.put("email", email);
        template.put("condicionesPrivacidad", condicionesPrivacidad);
        template.put("urlCondicionesPrivacidad", configurationSelector.getUrlCondicionesPrivacidad());

        if (compra != null) {
            if (language.equals("ca"))
                template.put("tipoEvento", compra.getParSesion().getParEvento().getParTiposEvento().getNombreVa());
            else
                template.put("tipoEvento", compra.getParSesion().getParEvento().getParTiposEvento().getNombreEs());

            if (infoPeriodica == null || infoPeriodica.equals(""))
                infoPeriodica = "no";

            template.put("infoPeriodica", infoPeriodica);
        } else
            error = ResourceProperties.getProperty(locale, "error.compraCaducada");
        ;

        if (error != null && !error.equals("")) {
            template.put("error", error);
        }

        return Response.ok(template).build();
    }

    @POST
    @Path("{uuidCompra}/datosComprador")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response guardaDatosComprador(@PathParam("uuidCompra") String uuidCompra,
                                         @FormParam("nombre") String nombre, @FormParam("apellidos") String apellidos,
                                         @FormParam("direccion") String direccion, @FormParam("poblacion") String poblacion,
                                         @FormParam("cp") String cp, @FormParam("provincia") String provincia,
                                         @FormParam("telefono") String telefono, @FormParam("email") String email,
                                         @FormParam("infoPeriodica") String infoPeriodica,
                                         @FormParam("condicionesPrivacidad") String condicionesPrivacidad) throws Exception {
        if (nombre == null || nombre.equals("")) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.nombre"));
        }

        if (apellidos == null || apellidos.equals("")) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.apellidos"));
        }

        if (telefono == null || telefono.length() < 9)
        {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.telefono"));
        }

        if (email == null || email.equals("")) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.email"));
        } else {
            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                        email, infoPeriodica, condicionesPrivacidad,
                        ResourceProperties.getProperty(getLocale(), "error.datosComprador.emailIncorrecto"));
            }
        }

        if (infoPeriodica == null || infoPeriodica.equals("")) {
            infoPeriodica = "no";
        }

        if (condicionesPrivacidad == null || condicionesPrivacidad.equals("")) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.condicionesPrivacidad"));
        }

        comprasService.rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia,
                telefono, email, infoPeriodica);

        CompraDTO compra = comprasService.getCompraByUuid(uuidCompra);

        if (compra.getCaducada()) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.compraCaducada"));
        }

        Locale locale = getLocale();
        String language = locale.getLanguage();

        if (configuration.isDebug())
            return tpvInterface.testTPV(compra.getId());
        else if (compra.getImporte().equals(BigDecimal.ZERO)) {
            return tpvInterface.compraGratuita(compra.getId());
        } else {
            Template template;
            TpvsDTO parTpv = compra.getParSesion().getParEvento().getParTpv();

            String tpvSignatureMethod = parTpv.getSignatureMethod();
            if (tpvSignatureMethod != null && tpvSignatureMethod.equals(SignatureTPV.HMAC_SHA256_V1.toString())) {
                template = getSha2Template(locale, parTpv, compra, email, language);
            } else if (tpvSignatureMethod != null && tpvSignatureMethod.equals(SignatureTPV.CECA_SHA1.toString())) {
                template = getCecaSha1Template(locale, parTpv, compra, email, language);
            } else {
                template = getSha1Template(locale, parTpv, compra, email, language);
            }

            String urlPago = parTpv.getUrl();
            if (urlPago != null)
                template.put("urlPago", urlPago);

            return Response.ok(template).build();
        }
    }

    private Template getSha2Template(Locale locale, TpvsDTO parTpv, CompraDTO compra, String email, String language) throws Exception {

        String identificador = idTPVInterface.getFormattedId(compra.getId());
        String urlOk = getBaseUrlPublic() + "/rest/tpv/oksha2";
        String urlKo = getBaseUrlPublic() + "/rest/tpv/kosha2";

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "tpv_sha2", locale, APP);
        Pair<String, String> parametrosYFirma = hmacSha256TPVInterface.getParametrosYFirma(
                Utils.monedaToCents(compra.getImporte()),
                parTpv.getOrderPrefix() + identificador,
                parTpv.getCode(),
                parTpv.getCurrency(),
                parTpv.getTransactionCode(),
                parTpv.getTerminal(),
                parTpv.getWsdlUrl(),
                urlOk,
                urlKo,
                email,
                language.equals("ca") ? parTpv.getLangCaCode() : parTpv.getLangEsCode(),
                identificador,
                StringUtils.stripAccents(compra.getParSesion().getParEvento().getTituloVa().toUpperCase()),
                parTpv.getNombre(),
                parTpv.getSecret());

        template.put("params", parametrosYFirma.getFirst());
        template.put("signature", parametrosYFirma.getSecond());
        return template;
    }

    private Template getCecaSha1Template(Locale locale, TpvsDTO parTpv, CompraDTO compra, String email, String language) {
        String Clave_encriptacion = parTpv.getSecret();

        String compraId = idTPVInterface.getFormattedId(compra.getId());

        String AcquirerBIN = parTpv.getOrderPrefix();
        String MerchantID = parTpv.getCode();
        String TerminalID = parTpv.getTerminal();

        String Sign_Param = Utils.sha1(Clave_encriptacion + compraId + AcquirerBIN + MerchantID + TerminalID);
        String params = "?order=" + compraId + "&uuid=" + Sign_Param;
        String URL_OK = getBaseUrlPublic() + "/rest/tpv/ceca/ok" + params;
        String URL_NOK = getBaseUrlPublic() + "/rest/tpv/ceca/ko" + params;

        String Cifrado = "SHA1";
        String Num_operacion = compraId;
        String Importe = Utils.monedaToCents(compra.getImporte());
        String TipoMoneda = parTpv.getCurrency();
        String Exponente = parTpv.getTransactionCode();
        String Pago_soportado = "SSL";
        String Idioma = language.equals("ca") ? parTpv.getLangCaCode() : parTpv.getLangEsCode();
        String Descripcion = StringUtils.stripAccents(compra.getParSesion().getParEvento().getTituloVa().toUpperCase());

        String url = parTpv.getWsdlUrl();

        String Firma = Utils.sha1(Clave_encriptacion + MerchantID + AcquirerBIN + TerminalID + Num_operacion + Importe + TipoMoneda + Exponente + Cifrado + URL_OK + URL_NOK);
        log.info("Sha1 para envio generado " + Firma);

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "tpv_ceca", locale, APP);
        template.put("AcquirerBIN", AcquirerBIN);
        template.put("MerchantID", MerchantID);
        template.put("TerminalID", TerminalID);
        template.put("URL_OK", URL_OK);
        template.put("URL_NOK", URL_NOK);
        template.put("Firma", Firma);
        template.put("Cifrado", Cifrado);
        template.put("Num_operacion", Num_operacion);
        template.put("Importe", Importe);
        template.put("TipoMoneda", TipoMoneda);
        template.put("Exponente", Exponente);
        template.put("Pago_soportado", Pago_soportado);
        template.put("Idioma", Idioma);
        template.put("Descripcion", Descripcion);

        template.put("urlPago", url);

        return template;
    }

    private Template getSha1Template(Locale locale, TpvsDTO parTpv, CompraDTO compra, String email, String language) {
        String importe = Utils.monedaToCents(compra.getImporte());
        String identificador = idTPVInterface.getFormattedId(compra.getId());
        String order = parTpv.getOrderPrefix() + identificador;
        String tpvCode = parTpv.getCode();
        String tpvCurrency = parTpv.getCurrency();
        String tpvTransaction = parTpv.getTransactionCode();
        String tpvTerminal = parTpv.getTerminal();
        String tpvNombre = parTpv.getNombre();
        String secret = parTpv.getSecret();
        String url = parTpv.getWsdlUrl();
        String urlOk = getBaseUrlPublic() + "/rest/tpv/ok";
        String urlKo = getBaseUrlPublic() + "/rest/tpv/ko";
        String concepto = StringUtils.stripAccents(compra.getParSesion().getParEvento().getTituloVa().toUpperCase());

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "tpv", locale, APP);
        template.put("idioma", language);
        template.put("lang", language);
        template.put("baseUrl", getBaseUrlPublic());
        template.put("identificador", identificador);
        template.put("concepto", concepto);
        template.put("importe", importe);
        template.put("correo", email);
        template.put("url", url);
        template.put("hash", Utils.sha1(identificador + importe + email + url + secret));
        template.put("order", order);
        template.put("urlOk", urlOk);
        template.put("urlKo", urlKo);

        if (language.equals("ca"))
            template.put("langCode", parTpv.getLangCaCode());
        else
            template.put("langCode", parTpv.getLangEsCode());

        if (tpvCode != null && tpvCurrency != null && tpvTransaction != null && tpvTerminal != null && tpvNombre != null) {
            String date = new SimpleDateFormat("YYMMddHHmmss").format(new Date());
            template.put("date", date);
            template.put("currency", tpvCurrency);
            template.put("code", tpvCode);
            template.put("terminal", tpvTerminal);
            template.put("transaction", tpvTransaction);
            template.put("nombre", tpvNombre);

            String shaEnvio = sha1TPVInterface.getFirma(importe, parTpv.getOrderPrefix(), identificador, tpvCode, tpvCurrency, tpvTransaction, url, secret, date);

            template.put("hashcajamar", shaEnvio);
            log.info("Sha1 para envio generado " + shaEnvio);
        }

        return template;
    }

    private Response paginaFueraDePlazo(Long sesionId, String userUID) throws Exception {
        Sesion sesion = sesionesService.getSesion(sesionId, userUID);

        Locale locale = getLocale();
        String language = locale.getLanguage();

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "compraFinalizada", locale, APP);
        String urlBase = getBaseUrlPublic();
        String url = currentRequest.getRequestURL().toString();
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("idioma", language);
        template.put("lang", language);

        return Response.ok(template).build();
    }

    @POST
    @Path("{id}/ocupadas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Butaca> estadoButaca(@PathParam("id") Integer idSesion, EstadoButacasRequest params) throws Exception {
        return butacasService.estanOcupadas(idSesion, params.getButacas(), params.getUuidCompra());
    }

    @GET
    @Path("{id}/compra/{fila}/{butaca}/{localizacion}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Compra getCompra(@PathParam("id") long idSesion, @PathParam("fila") String fila, @PathParam("butaca") String butaca,
            @PathParam("localizacion") String localizacion) throws Exception {
        return butacasService.getCompra(idSesion, localizacion, fila, butaca);
    }

    @GET
    @Path("{id}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPreciosSesion(@PathParam("id") Long sesionId)
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        return Response.ok().entity(new RestResponse(true, sesionesService.getPreciosSesion(sesionId, user.getUsuario()),
                sesionesService.getTotalPreciosSesion(sesionId))).build();
    }

    @GET
    @Path("butacasFragment/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response butacasFragment(@PathParam("id") long sesionId, @QueryParam("reserva") String reserva,
                                    @QueryParam("if") String isAdmin) throws Exception
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        Locale locale = getLocale();
        String language = locale.getLanguage();

        Sesion sesion = sesionesService.getSesion(sesionId, user.getUsuario());
        HTMLTemplate template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/" + sesion.getSala().getHtmlTemplateName(), locale, APP);

        template.put("baseUrl", getBaseUrlPublic());
        template.put("idioma", language);
        template.put("lang", language);
        template.put("sesion", sesion);
        template.put("fecha", DateUtils.dateToSpanishString(sesion.getFechaCelebracion()));
        template.put("hora", sesion.getHoraCelebracion());
        template.put("ocultaComprar", "true");
        template.put("gastosGestion", 0.0);
        template.put("modoReserva", reserva != null && reserva.equals("true"));
        template.put("estilopublico", "false");
        template.put("muestraReservadas", true);
        template.put("modoAdmin", true);
        template.put("tipoEventoEs", sesion.getEvento().getParTiposEvento().getNombreEs());
        Calendar cal = Calendar.getInstance();
        template.put("millis", cal.getTime().getTime());
        List<Tarifa> tarifas = new ArrayList<Tarifa>();

        template.put("estilosOcupadas", butacasService.estilosButacasOcupadas(sesionId, localizacionesService.getLocalizacionesSesion(sesionId), isAdmin.equals("true")));

        if (sesion.getPlantillaPrecios() != null && sesion.getPlantillaPrecios().getId() != -1)
            tarifas = sesionesService.getTarifasConPrecioConPlantilla(sesionId);
        else
            tarifas = sesionesService.getTarifasConPrecioSinPlantilla(sesionId);

        template.put("tarifas", tarifas);

        if (language.equals("ca")) {
            template.put("titulo", sesion.getEvento().getTituloVa());
        } else {
            template.put("titulo", sesion.getEvento().getTituloEs());
        }

        template.put("butacasSesion", "[]");

        return Response.ok().entity(template).header("Content-Type", "text/html; charset=utf-8").build();
    }

}
