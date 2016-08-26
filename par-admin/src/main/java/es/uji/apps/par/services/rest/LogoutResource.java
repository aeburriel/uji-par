package es.uji.apps.par.services.rest;

import es.uji.apps.par.auth.Authenticator;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("logout")
public class LogoutResource extends BaseResource
{
    @Context
    HttpServletResponse currentResponse;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response logout() throws Exception
    {
        currentRequest.getSession().removeAttribute(Authenticator.USER_ATTRIBUTE);
        currentRequest.getSession().removeAttribute(Authenticator.READONLY_ATTRIBUTE);
		currentRequest.getSession().removeAttribute(Authenticator.ERROR_LOGIN);

        String redirect = this.currentRequest.getRequestURL().toString().replaceFirst("logout$", "login");

        currentResponse.sendRedirect(redirect);
        return null;
    }
}
