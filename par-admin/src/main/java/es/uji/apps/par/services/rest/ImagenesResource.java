package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.drawer.MapaDrawerInterface;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
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
        ByteArrayOutputStream os = mapaDrawer.generaImagenAbono(abonoId, seccion, muestraReservadas != null && muestraReservadas.equals("true"));

        Response response = Response.ok(os.toByteArray())
                .header("Cache-Control", "no-cache, no-store, must-revalidate").header("Pragma", "no-cache")
                .header("Expires", "0").build();

        return response;
    }
}