package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.model.Tpv;
import es.uji.apps.par.services.TpvsService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("tpv")
public class TpvsResource {
    @InjectParam
    private TpvsService tpvsService;

    @Context
    HttpServletRequest currentRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        List<Tpv> tpvs = tpvsService.getTpvs(userUID, sort, start, limit);

        return Response.ok().entity(new RestResponse(true, tpvs, tpvs.size())).build();
    }
}
