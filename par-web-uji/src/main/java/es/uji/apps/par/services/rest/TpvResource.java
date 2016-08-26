package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.services.ComprasService;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.MailService;
import es.uji.apps.par.tpv.TpvInterface;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

@Component
@Path("tpv")
public class TpvResource extends BaseResource implements TpvInterface
{
	private static final Logger log = LoggerFactory.getLogger(TpvResource.class);

    @InjectParam
    private ComprasService compras;

    @InjectParam
    private MailService mailService;

    @Context
    HttpServletResponse currentResponse;

    @Context
    HttpServletRequest currentRequest;
    
    @Context
    private HttpServletRequest request;

	@InjectParam
	private PublicPageBuilderInterface publicPageBuilderInterface;

    @POST
    @Path("resultado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response leeResultadoTpv(@FormParam("tpv_estado") String estado, @FormParam("tpv_recibo") String recibo,
            @FormParam("tpv_identificador") String identificador, @FormParam("tpv_importe") String importe,
            @FormParam("tpv_firma") String firma) throws Exception
    {


        String msg = String.format(
                "Resultado pago TPV: tpv_estado=%s, tpv_recibo=%s, tpv_identificador=%s, tpv_importe=%s, tpv_firma=%s",
                estado, recibo, identificador, importe, firma);
        log.info(msg);

        CompraDTO compra = compras.getCompraById(Long.parseLong(identificador));
		Template template = checkCompra(compra, recibo, estado);
        return Response.ok(template).build();
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
			enviaMail(compraDTO.getEmail(), compraDTO.getUuid());

			eliminaCompraDeSesion();

			template = paginaExito(compraDTO, recibo);
		}
		else
		{
			template = paginaError(compraDTO);
		}
		return template;
	}

    private void eliminaCompraDeSesion()
    {
        currentRequest.getSession().removeAttribute(EntradasService.BUTACAS_COMPRA);
        currentRequest.getSession().removeAttribute(EntradasService.UUID_COMPRA);
    }

    private Template paginaExito(CompraDTO compra, String recibo) throws Exception
    {
    	Locale locale = getLocale();
        String language = locale.getLanguage();
        
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "compraValida", locale, APP);
        String url = request.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(configurationSelector.getUrlPublic(), url,
				language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("referencia", recibo);
        template.put("email", compra.getEmail());
        template.put("url", getBaseUrlPublic() + "/rest/compra/" + compra.getUuid() + "/pdf");
        template.put("urlComoLlegar", configurationSelector.getUrlComoLlegar());
        template.put("lang", language);

        return template;
    }

    private HTMLTemplate paginaError(CompraDTO compra) throws Exception
    {
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

    private void enviaMail(String email, String uuid)
    {
        String urlEntradas = String.format("%s/rest/compra/%s/pdf", getBaseUrlPublic(), uuid);

        String titulo = ResourceProperties.getProperty(getLocale(), "mail.entradas.titulo");
        String texto = ResourceProperties.getProperty(getLocale(), "mail.entradas.texto", urlEntradas);

        mailService.anyadeEnvio(email, titulo, texto, uuid, configurationSelector.getUrlPublic());
    }

	@Override
	public Response testTPV(long identificador) throws Exception {
		CompraDTO compra = compras.getCompraById(identificador);
		Template template = checkCompra(compra, "RECIBO_TEST", "OK");
		return Response.ok(template).build();
	}

    @Override
    public Response compraGratuita(long identificador) throws Exception {
        CompraDTO compra = compras.getCompraById(identificador);
        Template template = checkCompra(compra, "COMPRA_GRATUITA_" + identificador, "OK");
        return Response.ok(template).build();
    }
}
