package es.uji.apps.par.services.rest;

import es.uji.apps.par.auth.Authenticator;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.PostConstruct;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("login")
public class LoginResource extends BaseResource
{
	final String PLANTILLA_CAMBIO_PASSWORD = "cambioPassword";

	private Authenticator authClass;

	@PostConstruct
	void inicializa() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		 authClass = (Authenticator) Class.forName(configuration.getAuthClass()).newInstance();
		 authClass.setConfiguration(configuration);
	}

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
        final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        return Response.ok(encryptor.encryptPassword(txtCleanPassword)).build();
    }

    @GET
    @Path("password")
    @Produces(MediaType.TEXT_HTML)
    public Template changePassword() {
        return new HTMLTemplate(Constantes.PLANTILLAS_DIR + PLANTILLA_CAMBIO_PASSWORD, getLocale(), APP);
    }

    @POST
    @Path("password")
    @Produces(MediaType.TEXT_HTML)
    public Template doChangePassword(@FormParam("username") final String username,
            @FormParam("oldpassword") final String password,
            @FormParam("password1") final String password1, @FormParam("password2") final String password2) {
        final Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + PLANTILLA_CAMBIO_PASSWORD, getLocale(), APP);

        if (password1 == null || !password1.equals(password2)) {
            template.put("new_password_mismatch", true);
        } else if (authClass.changePassword(username, password, password1)) {
            template.put("change_ok", true);
        } else {
            template.put("bad_credentials", true);
        }

        return template;
    }
}
