package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.PreciosPlantilla;
import es.uji.apps.par.services.PlantillasService;
import es.uji.apps.par.services.PreciosPlantillaService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("plantillaprecios")
public class PlantillasPreciosResource extends BaseResource{

	@InjectParam
    private PlantillasService plantillaPreciosService;
	
	@InjectParam
	private PreciosPlantillaService preciosService;
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        if (userUID != null)
        {
            List<Plantilla> all = plantillaPreciosService.getBySala(null, sort, start, limit, userUID);
            return Response.ok().entity(new RestResponse(true, all, all.size())).build();
        }
        else
        {
            return Response.ok().entity(new RestResponse(true, new ArrayList<>(), 0)).build();
        }
    }

    @GET
    @Path("sala/{salaId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBySala(@PathParam("salaId") Long salaId, @QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        if (userUID != null)
        {
            return Response.ok().entity(new RestResponse(true, plantillaPreciosService.getBySala(salaId, sort, start, limit, userUID), plantillaPreciosService.getTotalPlantillaPreciosBySala(salaId, userUID))).build();
        }
        else
        {
            return Response.ok().entity(new RestResponse(true, new ArrayList<>(), 0)).build();
        }
    }

    @GET
    @Path("abonos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPlatnillasAbonos(@QueryParam("sort") String sort)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        List<Plantilla> plantillasPrecio = plantillaPreciosService.getAll(sort, 0, 10000, userUID);
        for (int i = plantillasPrecio.size() - 1; i >= 0; i--) {
            if (plantillasPrecio.get(i).getId() == -1) {
                plantillasPrecio.remove(i);
                break;
            }
        }

        return Response.ok().entity(new RestResponse(true, plantillasPrecio, 0)).build();
    }

    @DELETE
    @Path("editables/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") String id)
    {
        AuthChecker.canWrite(currentRequest);
        
    	plantillaPreciosService.remove(Integer.parseInt(id));
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(Plantilla plantillaPrecios) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
        Plantilla newPlantillaPrecios = plantillaPreciosService.add(plantillaPrecios);
        // TODO crear URI
        return Response.created(URI.create(""))
                .entity(new RestResponse(true, Arrays.asList(newPlantillaPrecios), 1)).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, Plantilla plantillaPrecios) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
    	plantillaPrecios.setId(Long.valueOf(id));
    	plantillaPreciosService.update(plantillaPrecios);
        return Response.ok()
                .entity(new RestResponse(true, Arrays.asList(plantillaPrecios), 1)).build();
    }
    
    @GET
    @Path("{id}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrecios(@PathParam("id") String id, @QueryParam("sort") String sort, @QueryParam("start") int start, 
    		@QueryParam("limit") @DefaultValue("1000") int limit)
    {
        return Response.ok().entity(new RestResponse(true, 
        		preciosService.getPreciosOfPlantilla(Long.valueOf(id), sort, start, limit), 
        		preciosService.getTotalPreciosOfPlantilla(Long.valueOf(id)))).build();
    }
    
    @GET
    @Path("editables")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlantillasEditables(@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(new RestResponse(true, 
        		plantillaPreciosService.getEditables(sort, start, limit, userUID),
        		plantillaPreciosService.getTotalPlantillasEditables(userUID))).build();
    }
    
    @DELETE
    @Path("{plantillaId}/precios/{precioId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removePrecio(@PathParam("plantillaId") String plantillaId, @PathParam("precioId") String precioId)
    {
        AuthChecker.canWrite(currentRequest);
        
    	preciosService.remove(Integer.parseInt(plantillaId), Integer.parseInt(precioId));
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @POST
    @Path("{plantillaId}/precios")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPrecio(@PathParam("plantillaId") String plantillaId, PreciosPlantilla precio) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
        PreciosPlantilla newPrecio = preciosService.add(precio);
        // TODO crear URI
        return Response.created(URI.create(""))
                .entity(new RestResponse(true, Arrays.asList(newPrecio), 1)).build();
    }

    @PUT
    @Path("{plantillaId}/precios/{precioId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePrecio(@PathParam("plantillaId") String plantillaId, 
    		@PathParam("precioId") String precioId, PreciosPlantilla precio) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        
    	precio.setId(Long.valueOf(precioId));
    	preciosService.update(precio);
        return Response.ok()
                .entity(new RestResponse(true, Arrays.asList(precio), 1)).build();
    }
}
