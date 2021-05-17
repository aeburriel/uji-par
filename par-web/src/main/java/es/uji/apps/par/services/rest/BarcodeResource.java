package es.uji.apps.par.services.rest;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.services.BarcodeService;
import es.uji.apps.par.utils.Utils;

@Path("barcode")
public class BarcodeResource extends BaseResource
{
    @InjectParam
    private BarcodeService barcodeService;

    @Context
    HttpServletResponse currentResponse;

    @GET
    @Path("{text}")
    @Produces("image/png")
    public Response datosEntrada(@PathParam("text") String text, @QueryParam("size") String size) throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        barcodeService.generaBarcodeQr(text, size, bos);

        final ResponseBuilder builder = Response.ok(bos.toByteArray());
        
        return Utils.noCache(builder).build();
    }

}
