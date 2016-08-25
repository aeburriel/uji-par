package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.services.EventosSyncService;
import es.uji.apps.par.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;

@Path("sync")
public class SyncResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(SyncResource.class);

    @InjectParam
    EventosSyncService eventosSyncService;

    @InjectParam
    private UsersService usersService;

	@InjectParam
	Configuration configuration;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response syncronizaDesdeURL() throws JAXBException, IOException, ParseException {
        String urlRssException = null;
        String syncUrlsHeaderToken = configuration.getSyncUrlsHeaderToken();
        String syncUrlsToken = configuration.getSyncUrlsToken();

        for (String urlRss : configuration.getSyncUrlsRss()) {
            urlRssException = urlRss;

            try {
                URL url = new URL(urlRss);
                URLConnection urlConnection = url.openConnection();
                if (syncUrlsHeaderToken != null && syncUrlsToken != null)
                    urlConnection.setRequestProperty(syncUrlsHeaderToken, syncUrlsToken);

                Usuario user = usersService.getUserByServerName(currentRequest.getServerName());
                eventosSyncService.sync(urlConnection.getInputStream(), user.getUsuario());
            } catch (Exception e) {
                if (urlRssException != null)
                    log.error(String.format("Error sincronizando eventos desde %s: %s", urlRssException, e.getMessage()));
                else
                    log.error(String.format("Error en la sincronización: no existe la propiedad %s", Configuration.SYNC_URL_TIPO));
            }
        }

        return Response.ok().build();
    }
}