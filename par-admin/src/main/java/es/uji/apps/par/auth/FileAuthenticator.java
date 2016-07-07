package es.uji.apps.par.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasypt.util.password.BasicPasswordEncryptor;

import es.uji.apps.par.config.Configuration;

public class FileAuthenticator implements Authenticator
{
    private static final String LOGIN_PARAM = "login";
    private static final String PASSWORD_PARAM = "password";
    
    private BasicPasswordEncryptor encryptor;

    public FileAuthenticator()
    {
        encryptor = new BasicPasswordEncryptor();
    }
	Configuration configuration;

    @Override
    public int authenticate(HttpServletRequest request)
    {
        HttpSession session = request.getSession();

        if (loginAdminOk(request))
        {
            session.setAttribute(USER_ATTRIBUTE, request.getParameter(LOGIN_PARAM));
			session.setAttribute(ERROR_LOGIN, false);
            return AUTH_OK;
        }
        else if (loginUserReadonlyOk(request))
        {
            session.setAttribute(USER_ATTRIBUTE, request.getParameter(LOGIN_PARAM));
            session.setAttribute(READONLY_ATTRIBUTE, true);
			session.setAttribute(ERROR_LOGIN, false);
            return AUTH_OK;
        }
        else
        {
			if (request.getParameter(LOGIN_PARAM) != null)
				session.setAttribute(ERROR_LOGIN, true);
            return AUTH_FAILED;
        }
    }

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	private boolean loginAdminOk(HttpServletRequest request)
    {
        return loginOk(request, configuration.getAdminLogin(), configuration.getAdminPassword());
    }

    private boolean loginUserReadonlyOk(HttpServletRequest request)
    {
        return loginOk(request, configuration.getUserReadonlyLogin(), configuration.getUserReadonlyPassword());
    }
    
    private boolean loginOk(HttpServletRequest request, List<String> logins, List<String> passwordsCifrado)
    {
        String loginFromRequest = request.getParameter(LOGIN_PARAM);

        if (loginFromRequest != null && logins.contains(loginFromRequest))
        {
        	int indexLogin = logins.indexOf(loginFromRequest);
        	return encryptor.checkPassword(request.getParameter(PASSWORD_PARAM), passwordsCifrado.get(indexLogin));
        }
        else
        {
        	return false;
        }
    }
    
    public static void main(String[] args)
    {
        BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        
        System.out.println(encryptor.encryptPassword("x"));
    }
}
