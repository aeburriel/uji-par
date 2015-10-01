package es.uji.apps.par.services.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.services.EventosSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("sync")
public class SyncResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(SyncResource.class);

    @InjectParam
    EventosSyncService eventosSyncService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response syncronizaDesdeURL() throws JAXBException, IOException, ParseException {
        String urlRssException = null;
        String syncUrlsHeaderToken = Configuration.getSyncUrlsHeaderToken();
        String syncUrlsToken = Configuration.getSyncUrlsToken();
        try {
            for (String urlRss : Configuration.getSyncUrlsRss()) {
                urlRssException = urlRss;

                URL url = new URL(urlRss);
                URLConnection urlConnection = url.openConnection();
                if (syncUrlsHeaderToken != null && syncUrlsToken != null)
                    urlConnection.setRequestProperty(syncUrlsHeaderToken, syncUrlsToken);

                eventosSyncService.sync(urlConnection.getInputStream());
            }
        } catch (Exception e) {
            if (urlRssException != null)
                log.error(String.format("Error sincronizando eventos desde %s", e.getMessage()));
            else
                log.error(String.format("Error en la sincronización: no existe la propiedad %s", Configuration.SYNC_URL_TIPO));
        }

        return Response.ok().build();
    }
}