package es.uji.apps.par.services.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.services.ButacasService;

@Path("sesion")
public class ButacasResource extends BaseResource
{
    @InjectParam
    private ButacasService butacasService;

    @Context
    private HttpServletRequest request;
    
    @GET
    @Path("{idSesion}/butacas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getButacasNoAnuladas(@PathParam("idSesion") Long idSesion) throws InterruptedException
    {
        if (!correctApiKey(request))
        {
            return apiAccessDenied();
        }
        
        List<Butaca> butacas = butacasService.getButacasNoAnuladas(idSesion);

        return Response.ok().entity(butacas).build();
    }

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEntradasPresentadas(@PathParam("id") Long sesionId, List<Butaca> butacas)
    {
        if (!correctApiKey(request))
        {
            return apiAccessDenied();
        }
        
        butacasService.updatePresentadas(butacas);
        
        return Response.ok().build();
    }

    @POST
    @Path("{id}/online")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEntradaPresentada(@PathParam("id") Long sesionId, Butaca butaca)
    {
        if (!correctApiKey(request))
        {
            return apiAccessDenied();
        }

        long update = butacasService.updatePresentada(butaca);

        RestResponse response = new RestResponse();
        if (update > 0) {
            response.setSuccess(true);
            return Response.ok(response).build();
        }
        else {
            response.setSuccess(false);
            return Response.ok(response).build();
        }
    }

}
