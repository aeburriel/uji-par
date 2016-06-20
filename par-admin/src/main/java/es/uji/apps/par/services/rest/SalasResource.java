package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.services.EventosService;
import es.uji.apps.par.services.SalasService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("sala")
public class SalasResource
{
    @InjectParam
    private SalasService salasService;

    @InjectParam
    private EventosService eventosService;

    @Context
    HttpServletRequest currentRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll()
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        List<Sala> salas = salasService.getSalas(userUID);
                
        return Response.ok().entity(new RestResponse(true, salas, salas.size())).build();
    }

    @GET
    @Path("evento/{eventoId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableByEvent(@PathParam("eventoId") Long eventoId)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        List<Sala> salas = salasService.getSalasDisponiblesParaEvento(userUID, eventoId);

        return Response.ok().entity(new RestResponse(true, salas, salas.size())).build();
    }
}
