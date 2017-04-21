package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.sanselan.ImageReadException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.exceptions.EventoNoEncontradoException;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.DisponiblesLocalizacion;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.EventoParaSync;
import es.uji.apps.par.model.PreciosSesion;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.services.ButacasService;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.EventosService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.ImageUtils;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;

@Path("evento")
public class EventosResource extends BaseResource {

    @InjectParam
    private EventosService eventosService;

    @InjectParam
    private SesionesService sesionesService;

    @InjectParam
    private ButacasService butacasService;

    @InjectParam
    private UsersService usersService;

    @InjectParam
    private PublicPageBuilderInterface publicPageBuilderInterface;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventos() {
        if (!correctApiKey(currentRequest)) {
            return apiAccessDenied();
        }

        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
        List<Evento> eventos = eventosService.getEventosConSesiones(user.getUsuario());

        imagenesANull(eventos);

        return Response.ok().entity(new RestResponse(true, eventos, 0)).build();
    }

    @GET
    @Path("activos/online")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventosActivosParaVentaOnline() {
        if (!correctApiKey(currentRequest)) {
            return apiAccessDenied();
        }

        List<EventoParaSync> eventos;

        eventos = eventosService.getEventosActivosParaVentaOnline(configurationSelector.getUrlPublic());
        return Response.ok().entity(eventos).build();
    }

    private void imagenesANull(List<Evento> eventos) {
        for (Evento evento : eventos) {
            evento.setImagen(null);
            evento.setImagenPubli(null);
        }
    }

    @GET
    @Path("listado")
    @Produces(MediaType.TEXT_HTML)
    public Template getListadoEventos() throws Exception {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            List<Evento> eventosActivos = eventosService.getEventosActivos("[{\"property\":\"fechaPrimeraSesion\",\"direction\":\"ASC\"}]", 0, 1000, user.getUsuario());

            List<Evento> eventosActivosConSesiones = new ArrayList<>();
            for (Evento eventosActivo : eventosActivos) {
                if (eventosActivo.getFechaPrimeraSesion() != null)
                {
                    eventosActivosConSesiones.add(eventosActivo);
                }
            }

            return getTemplateEventos(eventosActivosConSesiones);
        } catch (EventoNoEncontradoException e) {
            return getTemplateEventoNoEncontrado();
        }
    }

    @GET
    @Path("{contenidoId}")
    @Produces(MediaType.TEXT_HTML)
    public Template getEvento(@PathParam("contenidoId") Long contenidoId) throws Exception {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEventoByRssId(contenidoId, user.getUsuario());
			List<Sesion> sesiones = sesionesService.getSesiones(evento.getId(), user.getUsuario());
			evento.setSesiones(sesiones);

            return getTemplateEvento(evento, user.getUsuario(), sesiones);
        } catch (EventoNoEncontradoException e) {
            return getTemplateEventoNoEncontrado();
        }
    }

    @GET
    @Path("id/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Template getEventoById(@PathParam("id") Long id) throws Exception {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEvento(id, user.getUsuario());
			List<Sesion> sesiones = sesionesService.getSesiones(evento.getId(), user.getUsuario());
			evento.setSesiones(sesiones);

            return getTemplateEvento(evento, user.getUsuario(), sesiones);
        } catch (EventoNoEncontradoException e) {
            return getTemplateEventoNoEncontrado();
        }
    }

    private Template getTemplateEventoNoEncontrado() throws MalformedURLException, ParseException {
        Cine cine = usersService.getUserCineByServerName(currentRequest.getServerName());

        Locale locale = getLocale();
        String language = locale.getLanguage();
        HTMLTemplate template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + cine.getCodigo() + "/eventoNoEncontrado", locale, APP);

        String url = currentRequest.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());
        template.put("lang", language);

        return template;
    }

    private Template getTemplateEventos(List<Evento> eventos) throws MalformedURLException, ParseException {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        borrarEntradasSeleccionadasConAnterioridad();

        Cine cine = usersService.getUserCineByServerName(currentRequest.getServerName());
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + cine.getCodigo() + "/eventosListado", getLocale(), APP);

        String url = currentRequest.getRequestURL().toString();
        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("eventos", eventos);
        template.put("lang", language);

        return template;
    }

    private Template getTemplateEvento(Evento evento, String userUID, List<Sesion> sesiones) throws
			MalformedURLException, ParseException {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        borrarEntradasSeleccionadasConAnterioridad();

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + evento.getSesiones().get(0).getSala().getCine()
				.getCodigo() + "/eventoDetalle", getLocale(), APP);

        String tipoEvento, titulo, companyia, duracion, caracteristicas, premios, descripcion;
        if (language.equals("ca")) {
            tipoEvento = evento.getParTiposEvento().getNombreVa();
            titulo = evento.getTituloVa();
            companyia = evento.getCompanyiaVa();
            duracion = evento.getDuracionVa();
            caracteristicas = evento.getCaracteristicasVa();
            premios = evento.getPremiosVa();
            descripcion = evento.getDescripcionVa();
        } else {
            tipoEvento = evento.getParTiposEvento().getNombreEs();
            titulo = evento.getTituloEs();
            companyia = evento.getCompanyiaEs();
            duracion = evento.getDuracionEs();
            caracteristicas = evento.getCaracteristicasEs();
            premios = evento.getPremiosEs();
            descripcion = evento.getDescripcionEs();
        }

        String url = currentRequest.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString(), configurationSelector.getHtmlTitle()));
        template.put("baseUrl", getBaseUrlPublic());

        template.put("tipoEvento", tipoEvento);
        template.put("titulo", titulo);
        template.put("companyia", companyia);
        template.put("duracion", duracion);
        template.put("caracteristicas", caracteristicas);
        template.put("premios", premios);
        template.put("descripcion", descripcion);

        template.put("evento", evento);
        template.put("sesiones", sesiones);
        template.put("sesionesPlantilla", getSesionesPlantilla(sesiones, userUID));
        template.put("lang", language);

        return template;
    }

    private void borrarEntradasSeleccionadasConAnterioridad() {
        currentRequest.getSession().removeAttribute(EntradasService.BUTACAS_COMPRA);
    }

    private List<Map<String, Object>> getSesionesPlantilla(List<Sesion> sesiones, String userUID) {
        DatabaseHelper databaseHelper = DatabaseHelperFactory.newInstance(configuration);
        List<Map<String, Object>> sesionesPlantilla = new ArrayList<Map<String, Object>>();
        Calendar cal = Calendar.getInstance();


        for (Sesion sesion : sesiones) {
            if (sesion.getFechaCelebracion().before(cal.getTime()))
                continue;

            if (!configuration.showSesionesSinVentaInternet() && !sesion.getCanalInternet())
                continue;

            Map<String, Object> datos = new HashMap<String, Object>();

            List<DisponiblesLocalizacion> disponiblesNoNumerada = butacasService.getDisponiblesNoNumerada(sesion.getId());
            List<PreciosSesion> preciosSesion = sesionesService.getPreciosSesion(sesion.getId(), userUID);
            int disponibles = 0;
            if (disponiblesNoNumerada != null) {
                for (DisponiblesLocalizacion disponiblesLocalizacion : disponiblesNoNumerada) {
                    for (PreciosSesion precioSesion : preciosSesion) {
                        if (disponiblesLocalizacion.getLocalizacion().equals(precioSesion.getLocalizacion().getCodigo()))
                        {
                            disponibles += disponiblesLocalizacion.getDisponibles();
                            break;
                        }
                    }
                }
            }
            datos.put("disponibles", disponibles);
            datos.put("textoDisponibles", ResourceProperties.getProperty(getLocale(), "venta.disponibles", disponibles));

            datos.put("texto", getFechaSesion(sesion));

            datos.put("id", sesion.getId());
            datos.put("sala", sesion.getSala().getNombre());
            datos.put("enPlazoVentaInternet", sesion.getEnPlazoVentaInternet());
            datos.put("canalInternet", databaseHelper.booleanToNumber(sesion.getCanalInternet()));
            datos.put("fechaInicioVentaOnline", sesion.getFechaInicioVentaOnline());
            datos.put("versionLinguistica", sesion.getVersionLinguistica());

            if (sesion.getFechaInicioVentaOnline() != null && sesion.getFechaFinVentaOnline() != null)
                datos.put(
                        "textoFechasInternet",
                        ResourceProperties.getProperty(getLocale(), "venta.plazoInternet",
                                DateUtils.dateToSpanishStringWithHour(sesion.getFechaInicioVentaOnline()),
                                DateUtils.dateToSpanishStringWithHour(sesion.getFechaFinVentaOnline())));

            if (sesion.getEnPlazoVentaInternet())
                datos.put("clase", "contieneBoton");


            sesionesPlantilla.add(datos);
        }

        return sesionesPlantilla;
    }

    private String getFechaSesion(Sesion sesion) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sesion.getFechaCelebracion());
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM HH.mm", getLocale());

        String diaSemana = ResourceProperties.getProperty(getLocale(), "dia.abreviado." + cal.get(Calendar.DAY_OF_WEEK));

        return diaSemana + " " + format.format(cal.getTime());
    }

    @GET
    @Path("{id}/imagen")
    public Response getImagenEvento(@PathParam("id") Long eventoId) throws IOException {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEvento(eventoId, user.getUsuario());

			byte[] imagen = (evento.getImagen() != null) ? evento.getImagen() : eventosService.getImagenSustitutivaSiExiste();
			String contentType = (evento.getImagenContentType() != null) ? evento.getImagenContentType() : eventosService.getImagenSustitutivaContentType();

            return Response.ok(imagen).type(contentType).build();
        } catch (EventoNoEncontradoException e) {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/imagenPubli")
    public Response getImagenPubliEvento(@PathParam("id") Long eventoId) throws IOException {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEvento(eventoId, user.getUsuario());

            byte[] imagenPubli = (evento.getImagenPubli() != null) ? evento.getImagenPubli() : eventosService.getImagenPubliSustitutivaSiExiste();
            String contentType = (evento.getImagenPubliContentType() != null) ? evento.getImagenPubliContentType() : eventosService.getImagenPubliSustitutivaContentType();

            return Response.ok(imagenPubli).type(contentType).build();
        } catch (EventoNoEncontradoException e) {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/imagenEntrada")
    @Produces("image/jpeg")
    public Response getImagenEntrada(@PathParam("id") Long eventoId, @QueryParam("width") int width) throws IOException, ImageReadException {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEvento(eventoId, user.getUsuario());

            byte[] imagen = (evento.getImagen() != null) ? evento.getImagen() : eventosService.getImagenSustitutivaSiExiste();

            if (width <= 0) {
                width = 300;
            }

            if (imagen != null) {
                return Response.ok(ImageUtils.scaleEncodedImage(imagen, width).toByteArray()).build();
            } else {
                return Response.status(404).build();
            }
        } catch (EventoNoEncontradoException e) {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/imagenPubliEntrada")
    public Response getImagenPubliEntrada(@PathParam("id") Long eventoId) throws IOException, ImageReadException {
        try {
            Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
            Evento evento = eventosService.getEvento(eventoId, user.getUsuario());

            byte[] imagenPubli = (evento.getImagenPubli() != null) ? evento.getImagenPubli() : eventosService.getImagenPubliSustitutivaSiExiste();
            String contentType = (evento.getImagenPubliContentType() != null) ? evento.getImagenPubliContentType() : eventosService.getImagenPubliSustitutivaContentType();

            if (imagenPubli != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(imagenPubli.length);
                bos.write(imagenPubli, 0, imagenPubli.length);
                return Response.ok(bos.toByteArray()).type(contentType).build();
            } else {
                return Response.status(404).build();
            }
        } catch (EventoNoEncontradoException e) {
            return Response.noContent().build();
        }
    }

	@GET
	@Path("condicionesprivacidad")
	@Produces(MediaType.TEXT_HTML)
	public Template getCondicionesPrivacidad() throws ParseException {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        Cine cine = usersService.getUserCineByServerName(currentRequest.getServerName());
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + cine.getCodigo() + "/condicionesPrivacidad", getLocale()
				, APP);

		String url = currentRequest.getRequestURL().toString();

		template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString(),
				configurationSelector.getHtmlTitle()));
		template.put("baseUrl", getBaseUrlPublic());

		return template;
	}
}
