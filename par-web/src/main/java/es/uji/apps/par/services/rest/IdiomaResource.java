package es.uji.apps.par.services.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("idioma")
public class IdiomaResource extends BaseResource {

    @GET
    @Path("{lang}")
    @Produces(MediaType.TEXT_HTML)
    public Response index(@PathParam("lang") String lang) throws Exception {
        if (lang != null) {
            setLocale(lang);
        }

        String referrer = currentRequest.getHeader("referer");
        return Response.seeOther(new URI(referrer)).build();
    }
}
