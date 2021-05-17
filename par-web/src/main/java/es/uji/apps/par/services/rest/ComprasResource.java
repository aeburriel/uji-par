package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.utils.Utils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.ByteArrayOutputStream;

@Path("compra")
public class ComprasResource extends BaseResource
{
    @InjectParam
    private EntradasService entradasService;

    @InjectParam
    private UsersService usersService;

    @Context
    HttpServletResponse currentResponse;

    @GET
    @Path("{id}/pdf")
    @Produces("application/pdf")
    public Response datosEntrada(@PathParam("id") String uuidCompra) throws Exception
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        entradasService.generaEntrada(uuidCompra, bos, user.getUsuario(), configurationSelector.getUrlPublicSinHTTPS(), configurationSelector.getUrlPieEntrada());

        final ResponseBuilder builder = Response.ok(bos.toByteArray())
                .header("Content-Disposition","attachment; filename =\"entrada_" + uuidCompra + ".pdf\"");

        return Utils.noCache(builder).build();
    }
}
