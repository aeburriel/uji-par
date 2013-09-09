package es.uji.apps.par.services.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.services.ButacasService;

@Path("sesion")
public class ButacasResource extends BaseResource
{
    public static Logger log = Logger.getLogger(ButacasResource.class);
    
    @InjectParam
    private ButacasService butacasService;

    @GET
    @Path("{idSesion}/butacas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventos(@PathParam("idSesion") Long idSesion) throws InterruptedException
    {
        List<Butaca> butacas = butacasService.getButacasNoAnuladas(idSesion);
        
        return Response.ok().entity(butacas).build();
    }

 }