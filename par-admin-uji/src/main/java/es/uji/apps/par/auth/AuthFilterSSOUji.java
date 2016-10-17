package es.uji.apps.par.auth;

import es.uji.apps.par.services.UJIPerfilesService;
import es.uji.commons.sso.AccessManager;
import es.uji.commons.sso.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.regex.Pattern;

public class AuthFilterSSOUji implements Filter
{
	private static final Logger log = LoggerFactory.getLogger(AuthFilterSSOUji.class);
    private static final Pattern excluded =  Pattern.compile(".*/login|.*/logout|.*/.*\\.png|.*\\.jpg|.*\\.js|.*\\.css|.*/sync");

    private FilterConfig filterConfig;
    private String returnScheme;
    private String returnHost;
    private String returnPort;
    private String defaultUserId;
    private String defaultUserName;
    private String authToken;

    @Autowired
    UJIPerfilesService ujiPerfilesService;

    @Context
    ServletContext ctx;
    @Override
    public void init(FilterConfig config) throws ServletException
    {
        filterConfig = config;
        ctx = config.getServletContext();
        
        returnScheme = filterConfig.getInitParameter("returnScheme");
        returnHost = filterConfig.getInitParameter("returnHost");
        returnPort = filterConfig.getInitParameter("returnPort");
        defaultUserId = filterConfig.getInitParameter("defaultUserId");
        defaultUserName = filterConfig.getInitParameter("defaultUserName");
        authToken = filterConfig.getInitParameter("authToken");
    }

    public void destroy()
    {
    }

    private boolean isExcluded(String url)
    {
        return excluded.matcher(url).matches();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        HttpServletRequest sRequest = (HttpServletRequest) request;

        String headerAuthToken = sRequest.getHeader("X-UJI-AuthToken");
        if (headerAuthToken != null) {
            if (authToken != null && headerAuthToken.equals(authToken)) {
                chain.doFilter(request, response);
                return;
            }
        }

        User user = AccessManager.getConnectedUser(sRequest);
        boolean isUserValid = false;

        if (sRequest.getSession().getAttribute("user") == null) {
            sRequest.getSession().setAttribute("user", user.getName());
            isUserValid = ujiPerfilesService.hasPerfil("ADMIN", user.getId());
            sRequest.getSession().setAttribute("isUserValid", isUserValid);
        } else {
            isUserValid = (Boolean) sRequest.getSession().getAttribute("isUserValid");
        }

        if (isExcluded(sRequest.getRequestURI()))
        {
            chain.doFilter(request, response);
            return;
        }
        
        if (user != null && isUserValid)
        {
        	log.info("Ya autenticados " + sRequest.getRequestURI());
            chain.doFilter(request, response);
        }
        else
        {
        	HttpServletResponse sResponse = (HttpServletResponse) response;
        	redirectToEmptyPage(sResponse);
        }
    }

	private void redirectToEmptyPage(HttpServletResponse sResponse) throws IOException {
		//sResponse.sendRedirect(configuration.getUrlAdmin() + "/rest/login");
		sResponse.sendError(403);
	}
}