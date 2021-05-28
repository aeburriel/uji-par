package es.uji.apps.par.services.rest;
import com.mysema.commons.lang.Pair;
import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.butacas.DatosButaca;
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
import es.uji.apps.par.utils.Cart;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;
import es.uji.apps.par.utils.UserCarts;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("entrada")
public class EntradasResource extends BaseResource {
	public static final String ID_SELECTOR_CARTS = "selectorCarrito";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Logger log = LoggerFactory.getLogger(EntradasResource.class);

    @InjectParam
    private SesionesService sesionesService;

    @InjectParam
    private ButacasService butacasService;

    @InjectParam
    private ButacasVinculadasService butacasVinculadasService;

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
	public Response datosEntrada(@PathParam("id") final Long sesionId) throws Exception {
		final String selector = initUserCart();

		currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + sesionId + "/" + selector);
		return null;
	}

    @GET
    @Path("{id}/{selector}")
    @Produces(MediaType.TEXT_HTML)
    public Response datosEntrada(@PathParam("id") final Long sesionId, @PathParam("selector") final String selector) throws Exception {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
        Sesion sesion;
        try {
            sesion = sesionesService.getSesion(sesionId, user.getUsuario());
        } catch (SesionNoEncontradaException e) {
            return Response.status(404).build();
        }

        // Si el selector no es válido, redirigimos al inicio del proceso de compra
        final Cart cart = getUserCarts().getCart(selector);
        if (cart == null) {
            currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + sesionId);
            return null;
        }

        if (sesion.getCanalInternet() && (sesion.getAnulada() == null || sesion.getAnulada() == false)) {
            if (Utils.isAsientosNumerados(sesion.getEvento())) {
                return paginaSeleccionEntradasNumeradas(sesionId, cart, null, null, null, user.getUsuario());
            } else {
                return paginaSeleccionEntradasNoNumeradas(sesionId, cart , null, user.getUsuario());
            }
        } else
            return paginaFueraDePlazo(sesionId, user.getUsuario());
    }

    private Response paginaSeleccionEntradasNumeradas(final long sesionId, final Cart cart, final List<Butaca> butacasSeleccionadas,
                                                      final List<Butaca> butacasOcupadas, final String error, final String userUID) throws Exception {
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

        template.put("butacasSesion", cart.getButacas());
        template.put("uuidCompra", cart.getSelector()); // uuidCompra es, en realidad, el selector

        if (Utils.VALENCIANO.equals(locale)) {
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

    private Response paginaSeleccionEntradasNoNumeradas(final long sesionId, final Cart cart, final String error, final String userUID) throws Exception {
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

        template.put("uuidCompra", cart.getSelector()); // uuidCompra es, en realidad, el selector

        if (Utils.VALENCIANO.equals(locale)) {
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
    @Path("{id}/{selector}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response compraEntradaHtml(@PathParam("id") final Long sesionId, @PathParam("selector") final String selector,
                                      @FormParam("butacasSeleccionadas") final String butacasSeleccionadasJSON,
                                      @FormParam("platea1Normal") final String platea1Normal, @FormParam("platea1Descuento") final String platea1Descuento,
                                      @FormParam("platea2Normal") final String platea2Normal, @FormParam("platea2Descuento") final String platea2Descuento,
                                      @FormParam("b_t") final String strButacas) throws Exception {
        final Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
        final Sesion sesion = sesionesService.getSesion(sesionId, user.getUsuario());
        final Cart cart = getUserCarts().getCart(selector);
        if (cart == null) {
            return Response.status(403).build();
        }

        if (Utils.isAsientosNumerados(sesion.getEvento())) {
            return compraEntradaNumeradaHtml(sesionId, cart, butacasSeleccionadasJSON, user.getUsuario());
        } else {
            return compraEntradaNoNumeradaHtml(sesionId, cart, strButacas, user.getUsuario());
        }
    }

    private Response compraEntradaNumeradaHtml(final Long sesionId, final Cart cart, final String butacasSeleccionadasJSON, final String userUID)
            throws Exception {
        ResultadoCompra resultadoCompra;
        List<Butaca> butacasSeleccionadas = Butaca.parseaJSON(butacasSeleccionadasJSON);

        try {
            Map<String, List<Butaca>> butacasExistentes = new HashMap<String, List<Butaca>>();
            for (Butaca butacaSeleccionada : butacasSeleccionadas) {
                String localizacion = butacaSeleccionada.getLocalizacion();
                if (!butacasExistentes.containsKey(localizacion)) {
                    byte[] encoded = Files.readAllBytes(Paths.get(configuration.getPathJson(localizacion)));
                    butacasExistentes.put(localizacion, Butaca.parseaJSON(new String(encoded, "UTF-8")));
                }

                if (!existeButaca(butacasExistentes.get(localizacion), butacaSeleccionada)) {
                    throw new CompraButacaNoExistente();
                }
            }

            final String uuidCompra = cart.getUuid();
            if (uuidCompra == null) {
                return Response.status(403).build();
            }

            resultadoCompra = comprasService.realizaCompraInternet(sesionId, butacasSeleccionadas, uuidCompra, userUID);
        } catch (FueraDePlazoVentaInternetException e) {
            log.error("Fuera de plazo", e);
            return paginaFueraDePlazo(sesionId, userUID);
        } catch (ButacaOcupadaException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.ocupadas");
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas, null, error, userUID);
        } catch (CompraSinButacasException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.noSeleccionadas");
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas, null, error, userUID);
        } catch (CompraInvitacionPorInternetException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.invitacionPorInternet");
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas, null, error, userUID);
        } catch (CompraButacaDescuentoNoDisponible e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraDescuentoNoDisponible");
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas, null, error, userUID);
        } catch (CompraButacaNoExistente e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraButacaNoExistente");
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas, null, error, userUID);
        }

        if (resultadoCompra.getCorrecta()) {
            cart.setButacas(butacasSeleccionadasJSON);
            cart.setUuid(resultadoCompra.getUuid());

            currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + cart.getSelector() + "/datosComprador");
            return null;
        } else {
            return paginaSeleccionEntradasNumeradas(sesionId, cart, butacasSeleccionadas,
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

    private Response compraEntradaNoNumeradaHtml(final Long sesionId, final Cart cart, final String butacasSeleccionadasJSON, final String userUID) throws Exception {
        ResultadoCompra resultadoCompra;
        List<Butaca> butacasSeleccionadas = Butaca.parseaJSON("[" + butacasSeleccionadasJSON + "]");

        try {
            final String uuidCompra = cart.getUuid();
            if (uuidCompra == null) {
                return Response.status(403).build();
            }

            resultadoCompra = comprasService.realizaCompraInternet(sesionId, butacasSeleccionadas, uuidCompra, userUID);
        } catch (FueraDePlazoVentaInternetException e) {
            log.error("Fuera de plazo", e);
            return paginaFueraDePlazo(sesionId, userUID);
        } catch (ButacaOcupadaException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.ocupadas");
            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, error, userUID);
        } catch (CompraSinButacasException e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.noSeleccionadas");
            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, error, userUID);
        } catch (CompraButacaDescuentoNoDisponible e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.seleccionEntradas.compraDescuentoNoDisponible");
            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, error, userUID);
        } catch (NoHayButacasLibresException e) {
            String error = "";
            try {
                error = ResourceProperties.getProperty(getLocale(), "error.noHayButacasParaLocalizacion") + " " +
                        ResourceProperties.getProperty(getLocale(), "localizacion." + e.getLocalizacion());
            } catch (Exception ex) {
                error = ResourceProperties.getProperty(getLocale(), "error.noHayButacasParaLocalizacion");
            }

            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, error, userUID);
        } catch (Exception e) {
            String error = ResourceProperties.getProperty(getLocale(), "error.errorGeneral");
            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, error, userUID);
        }

        if (resultadoCompra.getCorrecta()) {
            cart.setUuid(resultadoCompra.getUuid());

            currentResponse.sendRedirect(getBaseUrlPublicLimpio() + "/rest/entrada/" + cart.getSelector() + "/datosComprador");
            return null;
        } else {
            return paginaSeleccionEntradasNoNumeradas(sesionId, cart, "", userUID);
        }
    }

    @GET
    @Path("{selector}/datosComprador")
    @Produces(MediaType.TEXT_HTML)
    public Response rellenaDatosComprador(@PathParam("selector") final String selector, String nombre, String apellidos,
                                          String direccion, String poblacion, String cp, String provincia, String telefono, String email,
                                          String infoPeriodica, String condicionesPrivacidad, String error) throws Exception {
        final String uuidCompra = getUserCarts().getUuid(selector);
        if (uuidCompra == null) {
            return Response.status(403).build();
        }

        CompraDTO compra = comprasService.getCompraByUuid(uuidCompra);

        Locale locale = getLocale();
        String language = locale.getLanguage();
        String codigo;
        if (compra != null) {
            codigo = compra.getParSesion().getParSala().getParCine().getCodigo();
        } else {
            codigo = Constantes.CODIGO_CINE_DEFECTO;
        }
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + codigo + "/datosComprador", locale, APP);
        String urlBase = getBaseUrlPublic();
        String url = currentRequest.getRequestURL().toString();
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("idioma", language);
        template.put("lang", language);

        template.put("nombre", nombre);
        template.put("apellidos", apellidos);
        template.put("direccion", direccion);
        template.put("poblacion", poblacion);
        template.put("cp", cp);
        template.put("provincia", provincia);
        template.put("telefono", telefono);
        template.put("email", email);
        template.put("condicionesPrivacidad", condicionesPrivacidad);
        template.put("urlCondicionesPrivacidad", compra.getParSesion().getParEvento().getParCine().getUrlPrivacidad());

        if (compra != null) {
            if (Utils.VALENCIANO.equals(locale))
                template.put("tipoEvento", compra.getParSesion().getParEvento().getParTiposEvento().getNombreVa());
            else
                template.put("tipoEvento", compra.getParSesion().getParEvento().getParTiposEvento().getNombreEs());
            template.put("eventoId", compra.getParSesion().getParEvento().getId());

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
    @Path("{selector}/datosComprador")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response guardaDatosComprador(@PathParam("selector") final String selector,
                                         @FormParam("nombre") String nombre, @FormParam("apellidos") String apellidos,
                                         @FormParam("direccion") String direccion, @FormParam("poblacion") String poblacion,
                                         @FormParam("cp") String cp, @FormParam("provincia") String provincia,
                                         @FormParam("telefono") String telefono, @FormParam("email") String email,
                                         @FormParam("infoPeriodica") String infoPeriodica,
                                         @FormParam("condicionesPrivacidad") String condicionesPrivacidad) throws Exception {
        final String uuidCompra = getUserCarts().getUuid(selector);
        if (uuidCompra == null) {
            return Response.status(403).build();
        }

        if (nombre == null || nombre.isEmpty()) {
            return rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono,
                    email, infoPeriodica, condicionesPrivacidad,
                    ResourceProperties.getProperty(getLocale(), "error.datosComprador.nombre"));
        }

        if (apellidos == null || apellidos.isEmpty()) {
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

        if (email == null || email.isEmpty()) {
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

        if (infoPeriodica == null || infoPeriodica.isEmpty()) {
            infoPeriodica = "no";
        }

        if (condicionesPrivacidad == null || condicionesPrivacidad.isEmpty()) {
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
        // En este punto no permitimos volver atrás, pues el TPV prohíbe reutilizar los códigos de pedido
        getUserCarts().clearCart(selector);

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
                Utils.VALENCIANO.equals(locale) ? parTpv.getLangCaCode() : parTpv.getLangEsCode(),
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
        String Idioma = Utils.VALENCIANO.equals(locale) ? parTpv.getLangCaCode() : parTpv.getLangEsCode();
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

        if (Utils.VALENCIANO.equals(locale))
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

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/compraFinalizada", locale, APP);
        String urlBase = getBaseUrlPublic();
        String url = currentRequest.getRequestURL().toString();
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(urlBase, url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("idioma", language);
        template.put("lang", language);

        return Response.ok(template).build();
    }

    @GET
    @Path("{id}/accesibles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response butacasAccesibles(@PathParam("id") Integer idSesion) {
    	try {
    		List<DatosButaca> butacas = butacasVinculadasService.getButacasAccesibles(idSesion, false);
    		return Response.ok().entity(new RestResponse(true, butacas, butacas.size())).build();
    	} catch (Exception e) {
    		return Response.status(404).build();
    	}
    }

    @GET
    @Path("{id}/acompanantes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response butacasAcompanantes(@PathParam("id") Integer idSesion) {
    	try {
    		List<DatosButaca> butacas = butacasVinculadasService.getButacasAcompanantes(idSesion);
    		return Response.ok().entity(new RestResponse(true, butacas, butacas.size())).build();
    	} catch (Exception e) {
    		return Response.status(404).build();
    	}
    }

    @POST
    @Path("{id}/ocupadas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Butaca> estadoButaca(@PathParam("id") Integer idSesion, EstadoButacasRequest params) throws Exception {
        // pararms.getUuidCompra() es, en realidad, el selector
        final String uuidCompra = getUserCarts().getUuid(params.getUuidCompra());
        return butacasService.estanOcupadas(idSesion, params.getButacas(), uuidCompra);
    }

    @POST
    @Path("{id}/valida")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validaButacas(@PathParam("id") final Integer idSesion, final EstadoButacasRequest params) {
        // pararms.getUuidCompra() es, en realidad, el selector
        final String uuidCompra = getUserCarts().getUuid(params.getUuidCompra());

        boolean status = false;
        String message = null;
        try {
            butacasService.validaButacas(idSesion, params.getButacas(), uuidCompra);
            status = true;
        } catch (final CompraDistanciamientoSocial e) {
            message = "error.seleccionEntradas.distanciamientoSocial";
        }
        return jsonTextResponse(status, message, configuration.getAforoDistanciamientoSocialUFLimite());
    }

    /*
    @GET
    @Path("{id}/compra/{fila}/{butaca}/{localizacion}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Compra getCompra(@PathParam("id") long idSesion, @PathParam("fila") String fila, @PathParam("butaca") String butaca,
            @PathParam("localizacion") String localizacion) throws Exception {
        return butacasService.getCompra(idSesion, localizacion, fila, butaca);
    }
    */

    @GET
    @Path("{id}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPreciosSesion(@PathParam("id") Long sesionId)
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        try {
            return Response.ok().entity(new RestResponse(true, sesionesService.getPreciosSesion(sesionId, user.getUsuario()),
                    sesionesService.getTotalPreciosSesion(sesionId))).build();
        } catch (SesionNoEncontradaException e) {
            return Response.status(404).build();
        }
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

        Sesion sesion;
        try {
            sesion = sesionesService.getSesion(sesionId, user.getUsuario());
        } catch (SesionNoEncontradaException e) {
            return Response.status(404).build();
        }
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

        if (Utils.VALENCIANO.equals(locale)) {
            template.put("titulo", sesion.getEvento().getTituloVa());
        } else {
            template.put("titulo", sesion.getEvento().getTituloEs());
        }

        template.put("butacasSesion", "[]");

        return Response.ok().entity(template).header("Content-Type", "text/html; charset=utf-8").build();
    }

	/**
	 * Devuelve el almacén de carritos de la compra de la sesión
	 * Si no existe, lo crea
	 * 
	 * @return UserCarts
	 */
    private UserCarts getUserCarts() {
		final HttpSession httpSession = currentRequest.getSession();
		UserCarts userCarts;

		synchronized(httpSession) {
			userCarts = (UserCarts) httpSession.getAttribute(EntradasResource.ID_SELECTOR_CARTS);
			if (userCarts == null) {
				userCarts = new UserCarts();
				httpSession.setAttribute(EntradasResource.ID_SELECTOR_CARTS, userCarts);
			}
		}
		return userCarts;
	}

	/**
	 * Inicializa nuevo carrito de la compra
	 * 
	 * @return el selector asignado
	 */
    private String initUserCart() {
		return getUserCarts().newCart().getSelector();
	}

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public class InfoSesion {
        private String titulo_es;
        private String titulo_va;
        private String descripcion_es;
        private String descripcion_va;
        private String duracion;
        private boolean numerada;
        private String fecha;
        private String hora;
        private long disponibles;

        public InfoSesion(final Evento evento, final Sesion sesion) {
            this.titulo_es = evento.getTituloEs();
            this.titulo_va = evento.getTituloVa();
            this.descripcion_es = evento.getDescripcionEs();
            this.descripcion_va = evento.getDescripcionVa();
            this.duracion = evento.getDuracionEs();
            this.numerada = evento.getAsientosNumerados();

            this.fecha = DateUtils.dateToSpanishString(sesion.getFechaCelebracion());
            this.hora = sesion.getHoraCelebracion();

            final long sesionId = sesion.getId();
            this.disponibles = sesionesService.getAforoTotal(sesionId) - sesionesService.getAforoOcupado(sesionId);
        }
    }

    @GET
    @Path("{id}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfoSesion(@PathParam("id") final Long idSesion) {
        try {
            final Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            final Sesion sesion = sesionesService.getSesion(idSesion.longValue(), user.getUsuario());
            final Evento evento = sesion.getEvento();

            final InfoSesion info = new InfoSesion(evento, sesion);

            return Response.ok().entity(info).build();
        } catch (SesionNoEncontradaException e) {

        }
        return Response.status(404).build();
    }
}
