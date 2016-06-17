package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.Abonado;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.services.AbonadosService;
import es.uji.apps.par.services.AbonosService;
import es.uji.apps.par.services.SesionesService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;

@Path("abono")
public class AbonosResource extends BaseResource
{
    @InjectParam
    private AbonosService abonosService;

    @InjectParam
    private AbonadosService abonadosService;

    @InjectParam
    private SesionesService sesionesService;

    @Context
    HttpServletRequest currentRequest;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        String userUID = AuthChecker.getUserUID(currentRequest);

        return Response.ok().entity(new RestResponse(true, abonosService.getAbonos(sort, start, limit, userUID),
                abonosService.getTotalAbonos(userUID))).build();
    }

    @GET
    @Path("{id}/sesiones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSesiones(@PathParam("id") Long abonoId, @QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        return Response.ok().entity(new RestResponse(true, sesionesService.getSesionesAbono(abonoId, sort, start, limit),
                sesionesService.getTotalSesionesAbono(abonoId))).build();
    }

    @GET
    @Path("{id}/abonados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbonados(@PathParam("id") Long abonoId, @QueryParam("sort") String sort, @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit)
    {
        return Response.ok().entity(new RestResponse(true, abonadosService.getAbonadosPorAbono(abonoId, sort, start, limit),
                abonadosService.getTotalAbonadosPorAbono(abonoId))).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAbono(@PathParam("id") Long id)
    {
        AuthChecker.canWrite(currentRequest);

        abonosService.removeAbono(id);

        return Response.ok().entity(new RestResponse(true)).build();
    }

    @DELETE
    @Path("{idAbono}/abonados/{idAbonado}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAbonado(@PathParam("idAbono") Long idAbono, @PathParam("idAbonado") Long idAbonado)
    {
        AuthChecker.canWrite(currentRequest);

        abonosService.removeAbonado(idAbonado);
        return Response.ok().entity(new RestResponse(true)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(Abono abono) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);

        Abono newAbono = abonosService.addAbono(abono);
        return Response.created(URI.create(""))
                .entity(new RestResponse(true, Arrays.asList(newAbono), 1)).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Abono abono) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);

        abonosService.updateAbono(abono);

        return Response.ok().entity(new RestResponse(true,Arrays.asList(abono), 1))
                .build();
    }

    @PUT
    @Path("abonado/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Abonado abonado) throws GeneralPARException
    {
        AuthChecker.canWrite(currentRequest);

        abonadosService.updateAbonado(abonado);

        return Response.ok().entity(new RestResponse(true,Arrays.asList(abonado), 1))
                .build();
    }
}