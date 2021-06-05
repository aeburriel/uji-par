package es.uji.apps.par.services.rest;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.core.InjectParam;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.config.ConfigurationSelector;
import es.uji.apps.par.exceptions.ResponseMessage;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.utils.Utils;

public class BaseResource
{
    protected static final String APP = "par";
    protected static final String LANG = "language";

    @Context
    HttpServletRequest currentRequest;

	@InjectParam
	Configuration configuration;

    @InjectParam
    protected ConfigurationSelector configurationSelector;

    protected Locale getLocale()
    {
    	HttpSession session = currentRequest.getSession();
    	String lang = (String) session.getAttribute(LANG);
    	
    	return getLocale(lang);
    }

    protected void setLocale(String lang)
    {
        if (Utils.esIdiomaValido(lang)) {
            HttpSession session = currentRequest.getSession();
            session.setAttribute(LANG, lang);
        }
    }
    
    protected Locale getLocale(String lang)
    {
        String idiomaFinal = configurationSelector.getIdiomaPorDefecto();
        if (lang != null && lang.length() > 0)
        {
        	HttpSession session = currentRequest.getSession();
        	session.setAttribute(LANG, lang);
        	idiomaFinal = lang;
        }
        else if (currentRequest != null)
        {
            if (currentRequest.getCookies() != null)
            {
                for (Cookie cookie : currentRequest.getCookies())
                {
                    if (cookie != null && "uji-lang".equals(cookie.getName()))
                    {
                        String idiomaCookie = cookie.getValue();

                        if (Utils.esIdiomaValido(idiomaCookie))
                        {
                            idiomaFinal = idiomaCookie;
                            break;
                        }
                    }
                }
            }

            String idiomaParametro = currentRequest.getParameter("idioma");
            if (idiomaParametro != null)
            {
                if (Utils.esIdiomaValido(idiomaParametro))
                {
                    idiomaFinal = idiomaParametro;
                }
            }
        }

        return new Locale(idiomaFinal);
    }

    protected String getBaseUrlPublic()
    {
        return configurationSelector.getUrlBase(currentRequest);
    }

    public Response errorResponse(String messageProperty, Object... values)
    {
        String errorMessage = getProperty(messageProperty, values);
        return Response.status(409).entity(new ResponseMessage(false, errorMessage)).build();
    }

    public Response jsonTextResponse(final boolean status, final String messageProperty, final Object... values)
    {
        final String message;
        if (messageProperty != null && !messageProperty.isEmpty()) {
            message = getProperty(messageProperty, values);
        } else {
            message = "";
        }

        return Response.ok().entity(new ResponseMessage(status, message)).build();
    }

    public String getProperty(String messageProperty, Object... values)
    {
        return ResourceProperties.getProperty(getLocale(), messageProperty, values);
    }

    protected boolean correctApiKey(HttpServletRequest request)
    {
        String requestApiKey = request.getParameter("key");
        String userApiKey = configurationSelector.getApiKey();

        return userApiKey.equals(requestApiKey);
    }

    protected Response apiAccessDenied()
    {
        return Response.status(Status.UNAUTHORIZED).build();
    }
}
