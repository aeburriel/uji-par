package es.uji.apps.par.auth;

import javax.servlet.http.HttpServletRequest;

public class AuthChecker
{
    public static void canWrite(HttpServletRequest request) throws RuntimeException
    {
        Boolean readonly = (Boolean) request.getSession().getAttribute(Authenticator.READONLY_ATTRIBUTE);
        
        if (readonly != null && readonly.booleanValue())
        {
            throw new RuntimeException("Usuario sólo lectura ha intentado guardar");
        }
    }

	public static String getUserUID(HttpServletRequest request) {
		return (String) request.getSession().getAttribute(Authenticator.USER_ATTRIBUTE);
	}
}
