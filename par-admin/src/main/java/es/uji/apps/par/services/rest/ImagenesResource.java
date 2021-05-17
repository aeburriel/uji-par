package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.drawer.MapaDrawerInterface;
import es.uji.apps.par.utils.Utils;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.ByteArrayOutputStream;

@Path("imagenes")
public class ImagenesResource extends BaseResource
{
    @InjectParam
    private MapaDrawerInterface mapaDrawer;

    @Context
    ServletContext context;

    @GET
    @Path("butacas/{abonoId}/{seccion}")
    @Produces("image/jpeg")
    public Response datosEntrada(@PathParam("abonoId") Long abonoId, @PathParam("seccion") String seccion,
                                 @QueryParam("muestraReservadas") String muestraReservadas) throws Exception {
        String userUID = AuthChecker.getUserUID(currentRequest);
        ByteArrayOutputStream os = mapaDrawer.generaImagenAbono(abonoId, seccion, muestraReservadas != null && muestraReservadas.equals("true"), userUID);

        final ResponseBuilder builder = Response.ok(os.toByteArray());

        return Utils.noCache(builder).build();
    }
}