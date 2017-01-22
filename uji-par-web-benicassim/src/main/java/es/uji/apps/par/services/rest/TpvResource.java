package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.services.ComprasService;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.JavaMailService;
import es.uji.apps.par.tpv.TpvInterface;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sis.redsys.api.ApiMacSha256;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

@Component
@Path("tpv")
public class TpvResource extends BaseResource implements TpvInterface {
    private static final Logger log = LoggerFactory.getLogger(TpvResource.class);

    @Autowired
    Configuration configuration;

    @InjectParam
    private ComprasService compras;

    @InjectParam
    private JavaMailService mailService;

    @Context
    HttpServletResponse currentResponse;

    @Context
    HttpServletRequest currentRequest;

    @Context
    private HttpServletRequest request;

    @InjectParam
    private PublicPageBuilderInterface publicPageBuilderInterface;

    @POST
    @Path("resultadosha2")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response leeResultadoSHA2Tpv(@FormParam("Ds_MerchantParameters") String params, @FormParam("Ds_SignatureVersion") String signatureVersion, @FormParam("Ds_Signature") String signature) throws Exception {

        ApiMacSha256 apiMacSha256 = new ApiMacSha256();

        String decodecParams = apiMacSha256.decodeMerchantParameters(params);
        log.info(decodecParams);

        String identificador = apiMacSha256.getParameter("Ds_MerchantData");
        CompraDTO compra = compras.getCompraById(Long.parseLong(identificador));
        if (compra == null) {
            return Response.status(400).build();
        }

        if (!verificaFirmaSHA256(params, signature, signatureVersion, apiMacSha256, compra)) {
            return Response.status(400).build();
        }

        String recibo = apiMacSha256.getParameter("Ds_Order");
        String estado = apiMacSha256.getParameter("Ds_Response");

        asientaCompra(recibo, estado, compra);
        return Response.ok().build();
    }

    private Template checkCompra(CompraDTO compraDTO, String recibo, String estado) throws Exception {
            Template template;

        if (compraDTO.getCaducada())
        {
            // Guardamos código pago de pasarela para luego saber que pago anular
            compras.rellenaCodigoPagoPasarela(compraDTO.getId(), recibo);

            template = paginaError(compraDTO);

            template.put("descripcionError", ResourceProperties.getProperty(getLocale(), "error.datosComprador.compraCaducadaTrasPagar"));

            eliminaCompraDeSesion();
	}
	else if (estado != null && estado.equals("OK"))
	{
            compras.marcaPagadaPasarela(compraDTO.getId(), recibo);
            enviaMail(compraDTO.getEmail(), compraDTO.getUuid(), recibo);

            eliminaCompraDeSesion();

            template = paginaExito(compraDTO, recibo);
	}
	else
	{
            template = paginaError(compraDTO);
	}
	return template;
    }

    private void asientaCompra(String recibo, String estado, CompraDTO compra) {
        log.info("Identificador: " + compra.getId());
        log.info("CADUCADA " + compra.getCaducada());
        log.info("ESTADO " + estado);
        if (compra.getCaducada()) {
            log.info("Compra caducada");
            // Guardamos código pago de pasarela para luego saber que pago anular
            compras.rellenaCodigoPagoPasarela(compra.getId(), recibo);
        } else if (isEstadoOk(estado)) {
            log.info("Compra NO caducada: " + compra.getId());
            compras.marcaPagadaPasarela(compra.getId(), recibo);

            log.info("Marcada como pagada " + compra.getUuid() + " - " + compra.getEmail());
            enviaMail(compra.getEmail(), compra.getUuid(), recibo);
        }
        eliminaCompraDeSesion();
    }

    private boolean isEstadoOk(String estado) {
        int intEstado = Integer.valueOf(estado);
        return (intEstado >= 0 && intEstado <= 99) || intEstado == 900;
    }

    @GET
    @Path("oksha2")
    @Produces(MediaType.TEXT_HTML)
    public Response resultadoSHA2Ok(@QueryParam("Ds_MerchantParameters") String params, @QueryParam("Ds_SignatureVersion") String signatureVersion, @QueryParam("Ds_Signature") String signature) throws Exception {
        ApiMacSha256 apiMacSha256 = new ApiMacSha256();
        String decodecParams = apiMacSha256.decodeMerchantParameters(params);
        log.info(decodecParams);

        String recibo = apiMacSha256.getParameter("Ds_Order");
        String identificador = apiMacSha256.getParameter("Ds_MerchantData");
        CompraDTO compra = compras.getCompraById(Long.parseLong(identificador));
        if (compra == null) {
            return Response.status(400).build();
        }

        if (!verificaFirmaSHA256(params, signature, signatureVersion, apiMacSha256, compra)) {
            return Response.status(400).build();
        }

        if(!compra.getPagada()) {
            // Podríamos no haber recibido confirmación de la pasarela (llamada a leeResultadoSHA2Tpv)
            String estado = apiMacSha256.getParameter("Ds_Response");
            asientaCompra(recibo, estado, compra);
        }

        return getResponseResultadoOk(recibo, compra);
    }

    private Response getResponseResultadoOk(String recibo, CompraDTO compra) throws Exception {
        Template template;

        if (compra.getCaducada()) {
            template = paginaError(compra);
            template.put("descripcionError", ResourceProperties.getProperty(getLocale(), "error.datosComprador.compraCaducadaTrasPagar"));
        } else {
            template = paginaExito(compra, recibo);
        }

        return Response.ok(template).build();
    }

    @GET
    @Path("kosha2")
    @Produces(MediaType.TEXT_HTML)
    public Response resultadoSHA2Ko(@QueryParam("Ds_MerchantParameters") String params, @QueryParam("Ds_SignatureVersion") String signatureVersion, @QueryParam("Ds_Signature") String signature) throws Exception {
        ApiMacSha256 apiMacSha256 = new ApiMacSha256();
        String decodecParams = apiMacSha256.decodeMerchantParameters(params);
        log.info(decodecParams);

        String identificador = apiMacSha256.getParameter("Ds_MerchantData");
        CompraDTO compra = compras.getCompraById(Long.parseLong(identificador));
        if (compra == null) {
            return Response.status(400).build();
        }

        if (!verificaFirmaSHA256(params, signature, signatureVersion, apiMacSha256, compra)) {
            return Response.status(400).build();
        }

        return getResponseResultadoKo(compra);
    }

    private Response getResponseResultadoKo(CompraDTO compra) throws Exception {
        Template template = paginaError(compra);

        return Response.ok(template).build();
    }

    private void eliminaCompraDeSesion() {
        currentRequest.getSession().removeAttribute(EntradasService.BUTACAS_COMPRA);
        currentRequest.getSession().removeAttribute(EntradasService.UUID_COMPRA);
    }

    private Template paginaExito(CompraDTO compra, String recibo) throws Exception {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "compraValida", locale, APP);
        String url = request.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(configurationSelector.getUrlPublic(), url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("referencia", recibo);
        template.put("email", compra.getEmail());
        template.put("url", getBaseUrlPublic() + "/rest/compra/" + compra.getUuid() + "/pdf");
        template.put("urlComoLlegar", configurationSelector.getUrlComoLlegar());
        template.put("lang", language);

        return template;
    }

    private HTMLTemplate paginaError(CompraDTO compra) throws Exception {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        HTMLTemplate template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "compraIncorrecta", locale, APP);
        String url = request.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("urlReintentar", getBaseUrlPublic() + "/rest/entrada/" + compra.getParSesion().getId());
        template.put("lang", language);

        return template;
    }

    private void enviaMail(String email, String uuid, String recibo) {
        String urlEntradas = String.format("%s/rest/compra/%s/pdf", configurationSelector.getUrlPublic(), uuid);

        String titulo = ResourceProperties.getProperty(new Locale("ca"), "mail.entradas.titulo") + " | " +
                ResourceProperties.getProperty(new Locale("es"), "mail.entradas.titulo");
        String texto = ResourceProperties.getProperty(new Locale("ca"), "mail.entradas.texto", recibo, urlEntradas) + "\n\n" +
                ResourceProperties.getProperty(new Locale("es"), "mail.entradas.texto", recibo, urlEntradas);

        mailService.anyadeEnvio(configurationSelector.getMailFrom(), email, titulo, texto, uuid, configurationSelector.getUrlPublic(), configurationSelector.getUrlPieEntrada());
    }

    @Override
    public Response testTPV(long identificadorCompra) throws Exception {
        return null;
    }

    @Override
    public Response compraGratuita(long identificador) throws Exception {
        CompraDTO compra = compras.getCompraById(identificador);
        Template template = checkCompra(compra, "COMPRA_GRATUITA_" + identificador, "OK");
        return Response.ok(template).build();
    }

    private static boolean verificaFirmaSHA256(final String mensaje, final String firma, final String version, final ApiMacSha256 apiMacSha256, final CompraDTO compra) throws Exception {
        if (!version.equals("HMAC_SHA256_V1")) {
            log.info("Algoritmo de firma no reconocido: " + version);
            return false;
        }
        TpvsDTO parTpv = compra.getParSesion().getParEvento().getParTpv();

        String firmaCalculada = apiMacSha256.createMerchantSignatureNotif(parTpv.getSecret(), mensaje);
        if (!firmaCalculada.equals(firma)) {
            log.info("Recibida respuesta TPV no auténtica");
            return false;
        }
        return true;
    }
}
