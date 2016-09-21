package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.EventoNoEncontradoException;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.services.EventosService;
import es.uji.apps.par.services.LocalizacionesService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.utils.DateUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("evento")
public class EventosResource
{
    @InjectParam
    private EventosService eventosService;

    @InjectParam
    private SesionesService sesionesService;

    @InjectParam
    private UsersService usersService;
    
    @InjectParam
    private LocalizacionesService localizacionesService;
    
    @Context
    HttpServletRequest currentRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("activos") boolean activos, 
    		@QueryParam("sort") @DefaultValue("[{\"property\":\"tituloVa\",\"direction\":\"ASC\"}]") String sort, 
    		@QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        List<Evento> eventos;
        int total = 0;
        
        if (activos) {
            eventos = eventosService.getEventosActivos(sort, start, limit, userUID);
            total = eventosService.getTotalEventosActivos(userUID);
        }
        else {
            eventos = eventosService.getEventos(sort, start, limit, userUID);
            total = eventosService.getTotalEventos(userUID);
        }
                
        return Response.ok().entity(new RestResponse(true, eventos, total)).build();
    }

    @GET
    @Path("{id}/imagen")
    public Response getImagenEvento(@PathParam("id") Long eventoId)
    {
        try
        {
            String userUID = AuthChecker.getUserUID(currentRequest);
            Evento evento = eventosService.getEvento(eventoId, userUID);

            return Response.ok(evento.getImagen()).type(evento.getImagenContentType()).build();
        }
        catch (EventoNoEncontradoException e)
        {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/imagenPubli")
    public Response getImagenPubliEvento(@PathParam("id") Long eventoId)
    {
        try
        {
            String userUID = AuthChecker.getUserUID(currentRequest);
            Evento evento = eventosService.getEvento(eventoId, userUID);

            return Response.ok(evento.getImagenPubli()).type(evento.getImagenPubliContentType()).build();
        }
        catch (EventoNoEncontradoException e)
        {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/sesiones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesiones(@PathParam("id") Long eventoId, @QueryParam("activos") boolean activos,
    		@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        List<Sesion> sesiones = new ArrayList<>();
        int total = 0;

        if (userUID != null)
        {
            if (activos)
            {
                sesiones = sesionesService.getSesionesActivasConVendidasDateEnSegundos(eventoId, sort, start, limit, userUID);
                total = sesionesService.getTotalSesionesActivas(eventoId, userUID);
            }
            else
            {
                sesiones = sesionesService.getSesionesConVendidasDateEnSegundos(eventoId, sort, start, limit, userUID);
                total = sesionesService.getTotalSesiones(eventoId, userUID);
            }
        }

        return Response.ok().entity(new RestResponse(true, sesiones, total)).build();
    }

    @GET
    @Path("{id}/sesiones/sala/{idSala}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesionesPorSala(@PathParam("id") Long eventoId, @PathParam("idSala") Long salaId,
                                @QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(sesionesService.getSesionesPorSala(eventoId, salaId, sort, userUID)).build();
    }
    
    @GET
    @Path("sesionesficheros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesionesCinePorFechas(@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin,
    		@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

    	List<Sesion> sesiones = sesionesService.getSesionesICAAPorFechas(fechaInicio, fechaFin, sort, userUID);
        return Response.ok().entity(new RestResponse(true, sesiones, sesiones.size())).build();
    }
    
    @GET
    @Path("sesionesficheros/todo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesionesPorFechas(@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin,
    		@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

    	List<Sesion> sesiones = sesionesService.getSesionesPorFechas(fechaInicio, fechaFin, sort, userUID);
        return Response.ok().entity(new RestResponse(true, sesiones, sesiones.size())).build();
    }
    
    @PUT
    @Path("sesionesficheros/{sesionId}/incidencia/{incidenciaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setIncidencia(@PathParam("sesionId") long sesionId, @PathParam("incidenciaId") int incidenciaId) {
    	sesionesService.setIncidencia(sesionId, incidenciaId);
    	
    	return Response.ok().entity(new RestResponse(true)).build();
    }
    
    @GET
    @Path("{eventoId}/sesiones/{sesionId}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPreciosSesion(@PathParam("eventoId") Integer eventoId, @PathParam("sesionId") Long sesionId, 
    		@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(new RestResponse(true, 
        		sesionesService.getPreciosSesion(sesionId, sort, start, limit, true, userUID),
        		sesionesService.getTotalPreciosSesion(sesionId)))
                .build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({"text/html"})
    public String addEvento(@FormDataParam("tituloEs") String tituloEs,
            @FormDataParam("descripcionEs") String descripcionEs,
            @FormDataParam("companyiaEs") String companyiaEs,
            @FormDataParam("interpretesEs") String interpretesEs,
            @FormDataParam("duracionEs") String duracionEs,
            @FormDataParam("dataBinary") byte[] dataBinary,
            @FormDataParam("dataBinary") FormDataContentDisposition dataBinaryDetail,
            @FormDataParam("dataBinary") FormDataBodyPart imagenBodyPart,
            @FormDataParam("dataBinaryPubli") byte[] dataBinaryPubli,
            @FormDataParam("dataBinaryPubli") FormDataContentDisposition dataBinaryDetailPubli,
            @FormDataParam("dataBinaryPubli") FormDataBodyPart imagenBodyPartPubli,
            @FormDataParam("premiosEs") String premiosEs,
            @FormDataParam("caracteristicasEs") String caracteristicasEs,
            @FormDataParam("comentariosEs") String comentariosEs,
            @FormDataParam("tipoEvento") Integer tipoEventoId,
            @FormDataParam("tpv") Integer tpvId,
            @FormDataParam("tituloVa") String tituloVa,
            @FormDataParam("descripcionVa") String descripcionVa,
            @FormDataParam("companyiaVa") String companyiaVa,
            @FormDataParam("interpretesVa") String interpretesVa,
            @FormDataParam("duracionVa") String duracionVa,
            @FormDataParam("premiosVa") String premiosVa,
            @FormDataParam("caracteristicasVa") String caracteristicasVa,
            @FormDataParam("comentariosVa") String comentariosVa,
            @FormDataParam("porcentajeIVA") BigDecimal porcentajeIVA,
            @FormDataParam("retencionSGAE") BigDecimal retencionSGAE,
            @FormDataParam("ivaSGAE") BigDecimal ivaSGAE,
            @FormDataParam("asientosNumerados") Boolean asientosNumerados,
            @FormDataParam("rssId") String rssId,
            @FormDataParam("expediente") String expediente,
            @FormDataParam("codigoDistribuidora") String codigoDistribuidora,
            @FormDataParam("nombreDistribuidora") String nombreDistribuidora,
            @FormDataParam("nacionalidad") String nacionalidad,
            @FormDataParam("vo") String vo,
            @FormDataParam("metraje") String metraje,
            @FormDataParam("subtitulos") String subtitulos,
			@FormDataParam("multisesion") String checkMultisesion,
			@FormDataParam("jsonEventosMultisesion") String jsonEventosMultisesion,
            @FormDataParam("formato") String formato,
            @FormDataParam("promotor") String promotor,
            @FormDataParam("nifPromotor") String nifPromotor) throws GeneralPARException
    {
        String nombreArchivo = (dataBinaryDetail != null) ? dataBinaryDetail.getFileName() : "";
        String mediaType = (imagenBodyPart != null) ? imagenBodyPart.getMediaType().toString() : "";

        String nombreArchivoPubli = (dataBinaryDetailPubli != null) ? dataBinaryDetailPubli.getFileName() : "";
        String mediaTypePubli = (imagenBodyPartPubli != null) ? imagenBodyPartPubli.getMediaType().toString() : "";

        String userUID = AuthChecker.getUserUID(currentRequest);
        Cine cine = usersService.getUserCineByUserUID(userUID);

        Evento evento = new Evento(rssId, tituloEs, descripcionEs, companyiaEs, interpretesEs, duracionEs,
                premiosEs, caracteristicasEs, comentariosEs, tituloVa, descripcionVa, companyiaVa,
                interpretesVa, duracionVa, premiosVa, caracteristicasVa, comentariosVa, dataBinary,
                nombreArchivo, mediaType, dataBinaryPubli,
                nombreArchivoPubli, mediaTypePubli, tipoEventoId, tpvId, porcentajeIVA, retencionSGAE, ivaSGAE, asientosNumerados,
                expediente, codigoDistribuidora, nombreDistribuidora, nacionalidad, vo, metraje, subtitulos, formato, cine, promotor, nifPromotor);

		if (checkMultisesion != null && checkMultisesion.equalsIgnoreCase("on"))
			evento.setEventosMultisesion(jsonEventosMultisesion);

        Evento newEvento = eventosService.addEvento(evento);

        // TODO -> crear URL
        /*return Response.created(URI.create(""))
        .entity(new RestResponse(true, Arrays.asList(newEvento), 1)).build();*/

        // devolvemos el json como text/html ya que un content-type json hace que el explorer no lo acepte al hacer la petición vía submit en lugar de ajax-request

        return "{success: 'true'}";
    }

    @POST
    @Path("{id}/sesiones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSesion(@PathParam("id") Integer eventoId, Sesion sesion) throws GeneralPARException
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        Sesion newSesion = sesionesService.addSesion(eventoId, sesion, userUID);
        //TODO -> crear URL
        return Response.created(URI.create("")).entity(new RestResponse(true, Arrays.asList(newSesion), 1))
                .build();
    }

    @POST
    @Path("{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, @FormDataParam("tituloEs") String tituloEs,
            @FormDataParam("descripcionEs") String descripcionEs,
            @FormDataParam("companyiaEs") String companyiaEs,
            @FormDataParam("interpretesEs") String interpretesEs,
            @FormDataParam("duracionEs") String duracionEs,
            @FormDataParam("dataBinary") byte[] dataBinary,
            @FormDataParam("dataBinary") FormDataContentDisposition dataBinaryDetail,
            @FormDataParam("dataBinary") FormDataBodyPart imagenBodyPart,
            @FormDataParam("dataBinaryPubli") byte[] dataBinaryPubli,
            @FormDataParam("dataBinaryPubli") FormDataContentDisposition dataBinaryDetailPubli,
            @FormDataParam("dataBinaryPubli") FormDataBodyPart imagenBodyPartPubli,
            @FormDataParam("premiosEs") String premiosEs,
            @FormDataParam("caracteristicasEs") String caracteristicasEs,
            @FormDataParam("comentariosEs") String comentariosEs,
            @FormDataParam("tipoEvento") Integer tipoEventoId,
            @FormDataParam("tpv") Integer tpvId,
            @FormDataParam("tituloVa") String tituloVa,
            @FormDataParam("descripcionVa") String descripcionVa,
            @FormDataParam("companyiaVa") String companyiaVa,
            @FormDataParam("interpretesVa") String interpretesVa,
            @FormDataParam("duracionVa") String duracionVa,
            @FormDataParam("premiosVa") String premiosVa,
            @FormDataParam("caracteristicasVa") String caracteristicasVa,
            @FormDataParam("comentariosVa") String comentariosVa,
            @FormDataParam("porcentajeIVA") BigDecimal porcentajeIVA,
            @FormDataParam("retencionSGAE") BigDecimal retencionSGAE,
            @FormDataParam("ivaSGAE") BigDecimal ivaSGAE,
            @FormDataParam("asientosNumerados") Boolean asientosNumerados,
            @FormDataParam("rssId") String rssId,
            @FormDataParam("expediente") String expediente,
            @FormDataParam("codigoDistribuidora") String codigoDistribuidora,
            @FormDataParam("nombreDistribuidora") String nombreDistribuidora,
            @FormDataParam("nacionalidad") String nacionalidad,
            @FormDataParam("vo") String vo,
            @FormDataParam("metraje") String metraje,
            @FormDataParam("subtitulos") String subtitulos,
			@FormDataParam("multisesion") String checkMultisesion,
			@FormDataParam("jsonEventosMultisesion") String jsonEventosMultisesion,
            @FormDataParam("formato") String formato,
            @FormDataParam("promotor") String promotor,
            @FormDataParam("nifPromotor") String nifPromotor) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        String userUID = AuthChecker.getUserUID(currentRequest);
        
        String nombreArchivo = (dataBinaryDetail != null) ? dataBinaryDetail.getFileName() : "";
        String mediaType = (imagenBodyPart != null) ? imagenBodyPart.getMediaType().toString() : "";

        String nombreArchivoPubli = (dataBinaryDetailPubli != null) ? dataBinaryDetailPubli.getFileName() : "";
        String mediaTypePubli = (imagenBodyPartPubli != null) ? imagenBodyPartPubli.getMediaType().toString() : "";

        Cine cine = usersService.getUserCineByUserUID(userUID);

        Evento evento = new Evento(rssId, tituloEs, descripcionEs, companyiaEs, interpretesEs, duracionEs,
                premiosEs, caracteristicasEs, comentariosEs, tituloVa, descripcionVa, companyiaVa,
                interpretesVa, duracionVa, premiosVa, caracteristicasVa, comentariosVa, dataBinary,
                nombreArchivo, mediaType, dataBinaryPubli,
                nombreArchivoPubli, mediaTypePubli, tipoEventoId, tpvId, porcentajeIVA, retencionSGAE, ivaSGAE, asientosNumerados,
                expediente, codigoDistribuidora, nombreDistribuidora, nacionalidad, vo, metraje, subtitulos, formato, cine, promotor, nifPromotor);

		if (checkMultisesion != null && checkMultisesion.equalsIgnoreCase("on"))
			evento.setEventosMultisesion(jsonEventosMultisesion);

        evento.setId(id);
        eventosService.updateEvento(evento, userUID);

        // no devolvemos el evento porque al enviar la imagen colgaba el navegador durante un tiempo
        return Response.ok()
                .entity(new RestResponse(true/* , Collections.singletonList(evento) */)).build();
    }

    @PUT
    @Path("{id}/sesiones/{sesionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer eventoId,
            @PathParam("sesionId") Integer sesionId, Sesion sesion) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        String userUID = AuthChecker.getUserUID(currentRequest);

        sesion.setId(sesionId);
        sesionesService.updateSesion(eventoId, sesion, userUID);
        return Response.ok().entity(new RestResponse(true, Arrays.asList(sesion), 1)).build();
    }
    
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") Integer id)
    {
        AuthChecker.canWrite(currentRequest);
        
        eventosService.removeEvento(id);
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @DELETE
    @Path("{id}/sesiones/{sesionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") Integer eventoId,
            @PathParam("sesionId") Integer sesionId)
    {
        AuthChecker.canWrite(currentRequest);
        
        sesionesService.removeSesion(sesionId);
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @DELETE
    @Path("{id}/imagen")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeImagen(@PathParam("id") Integer eventoId)
    {
        AuthChecker.canWrite(currentRequest);
        
        eventosService.removeImagen(eventoId);
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @DELETE
    @Path("{id}/imagenPubli")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeImagenPubli(@PathParam("id") Integer eventoId)
    {
        AuthChecker.canWrite(currentRequest);

        eventosService.removeImagenPubli(eventoId);
        return Response.ok().entity(new RestResponse(true)).build();
    }
    
    @GET
    @Path("sesion/{sesionId}/localizacion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocalizacionesSesion(@PathParam("sesionId") Long sesionId)
    {
        return Response.ok().entity(
        	localizacionesService.getLocalizacionesSesion(sesionId)
        ).build();
    }

	@GET
	@Path("peliculas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeliculas()
	{
		return Response.ok().entity(eventosService.getPeliculas()).build();
	}

	@GET
	@Path("{id}/peliculas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeliculasEvento(@PathParam("id") long eventoId)
	{
		return Response.ok().entity(eventosService.getPeliculas(eventoId)).build();
	}

    @GET
    @Path("{eventoId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNumeroSesionesMismaHoraYSala(@QueryParam("sesionid") Long sesionId,
          @QueryParam("sala") long salaId, @QueryParam("fecha") String fechaCelebracion) throws ParseException {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(sesionesService.getNumeroSesionesMismaHoraYSala(sesionId, salaId,
                DateUtils.spanishStringWithHourstoDate(fechaCelebracion), userUID)).build();
    }
}
