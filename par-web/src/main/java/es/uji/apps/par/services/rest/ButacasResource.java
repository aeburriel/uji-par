package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.services.ButacasService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

@Path("sesion")
public class ButacasResource extends BaseResource
{
    @InjectParam
    private ButacasService butacasService;

    @GET
    @Path("{idSesion}/butacas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getButacasNoAnuladas(@PathParam("idSesion") Long idSesion) throws InterruptedException
    {
        if (!correctApiKey(currentRequest))
        {
            return apiAccessDenied();
        }

        Locale locale = getLocale();
        String language = locale.getLanguage();
        
        List<Butaca> butacas = butacasService.getButacasNoAnuladas(idSesion, language);

        return Response.ok().entity(butacas).build();
    }

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEntradasPresentadas(@PathParam("id") Long sesionId, List<Butaca> butacas)
    {
        if (!correctApiKey(currentRequest))
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
        if (!correctApiKey(currentRequest))
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
