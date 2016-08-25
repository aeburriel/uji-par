package es.uji.apps.par.services.rest;

import es.uji.apps.par.auth.Authenticator;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.jasypt.util.password.BasicPasswordEncryptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("login")
public class LoginResource extends BaseResource
{
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Template index(@QueryParam("error") boolean error) throws Exception
    {
        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "login", getLocale(), APP);
        template.put("urlPublic", configurationSelector.getUrlPublic());
		Boolean errorLogin = (Boolean) currentRequest.getSession().getAttribute(Authenticator.ERROR_LOGIN);

		if (errorLogin != null && errorLogin)
        	template.put("error", true);

        return template;
    }
    
    @GET
    @Path("generatepassword")
    @Produces(MediaType.TEXT_PLAIN)
    public Response generatePassword(@QueryParam("password") String txtCleanPassword) throws Exception
    {
    	BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        return Response.ok(encryptor.encryptPassword(txtCleanPassword)).build();
    }
}
