package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.UsersService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
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

        Response response = Response.ok(bos.toByteArray())
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .header("Content-Disposition","attachment; filename =\"entrada_" + uuidCompra + ".pdf\"")
                .build();

        return response;
    }
}
