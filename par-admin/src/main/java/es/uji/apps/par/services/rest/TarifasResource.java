package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Tarifa;
import es.uji.apps.par.services.TarifasService;
import es.uji.apps.par.services.UsersService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Path("tarifa")
public class TarifasResource
{
    @InjectParam
    private UsersService usersService;

    @InjectParam
    private TarifasService tarifasService;
    
    @Context
    HttpServletRequest currentRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("sort") @DefaultValue("[{\"property\":\"nombre\",\"direction\":\"ASC\"}]") String sort, 
    		@QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        List<Tarifa> tarifas = tarifasService.getAll(sort, start, limit, userUID);
        int tarifasSize = tarifasService.getTotalTarifas(userUID);
        return Response.ok().entity(new RestResponse(true, tarifas, tarifasSize)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTarifa(Tarifa tarifa) throws GeneralPARException
    {
        String userUID = AuthChecker.getUserUID(currentRequest);
        Cine cine = usersService.getUserCineByUserUID(userUID);

        tarifa.setCine(cine);
        tarifa = tarifasService.add(tarifa);

        // TODO -> crear URL
        return Response.created(URI.create(""))
                .entity(new RestResponse(true, Arrays.asList(tarifa), 1)).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Tarifa tarifa) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);
        tarifa = tarifasService.update(tarifa);

        return Response.ok()
                .entity(new RestResponse(true, Collections.singletonList(tarifa), 1)).build();
    }
    
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") Integer id)
    {
    	AuthChecker.canWrite(currentRequest);
        tarifasService.removeTarifa(id);
        return Response.ok().entity(new RestResponse(true)).build();
    }
}
