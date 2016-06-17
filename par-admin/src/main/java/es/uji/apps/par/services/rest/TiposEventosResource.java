package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.services.TiposEventosService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;

@Path("tipoevento")
public class TiposEventosResource extends BaseResource
{
    @InjectParam
    private TiposEventosService tiposEventosService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(new RestResponse(true, tiposEventosService.getTiposEventos(sort, start, limit, userUID),
        		tiposEventosService.getTotalTipusEventos(userUID))).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") String id)
    {
        AuthChecker.canWrite(currentRequest);
        
        tiposEventosService.removeTipoEvento(Integer.parseInt(id));
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(TipoEvento tipoEvento) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
        TipoEvento newTipoEvento = tiposEventosService.addTipoEvento(tipoEvento);
        // TODO crear URI
        return Response.created(URI.create(""))
                .entity(new RestResponse(true, Arrays.asList(newTipoEvento), 1)).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(TipoEvento tipoEvento) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
        tiposEventosService.updateTipoEvento(tipoEvento);
        return Response.ok().entity(new RestResponse(true,Arrays.asList(tipoEvento), 1))
                .build();
    }
}
