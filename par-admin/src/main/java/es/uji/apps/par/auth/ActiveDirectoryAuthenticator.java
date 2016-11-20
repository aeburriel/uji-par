package es.uji.apps.par.auth;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uji.apps.par.config.Configuration;

public class ActiveDirectoryAuthenticator implements Authenticator {
	private static final String LOGIN_PARAM = "login";
	private static final String PASSWORD_PARAM = "password";

	private static final Logger log = LoggerFactory.getLogger(ActiveDirectoryAuthenticator.class);
	Configuration configuration;

	public ActiveDirectoryAuthenticator() {

	}

	@Override
	public int authenticate(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String login = request.getParameter(LOGIN_PARAM);
		String password = request.getParameter(PASSWORD_PARAM);

		try {
			log.info("Autenticamos usuario " + login);

			if (login != null && loginAdminOk(login, password)) {
				if (tienePermisoParaEntrar(login)) {
					log.info("Usuario " + login + " autenticado y con permiso");
					session.setAttribute(USER_ATTRIBUTE, login);
					return AUTH_OK;
				} else {
					log.info("El usuario " + login + " autentica bien pero no tiene permiso para entrar");
					return AUTH_FAILED;
				}
			} else {
				return AUTH_FAILED;
			}
		} catch (Exception e) {
			log.error("Error autenticando usuario " + login, e);
			return AUTH_FAILED;
		}
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	private boolean tienePermisoParaEntrar(String login) {
		List<String> admins = configuration.getAdminLogin();
		
		if (login != null && admins.contains(login))
			return true;
		return false;
	}

	public boolean loginAdminOk(String login, String password) throws Exception {
		String activeDirectoryIP = configuration.getActiveDirectoryIP();
		String activeDirectoryPort = configuration.getActiveDirectoryPort();
		String activeDirectoryDomain = configuration.getActiveDirectoryDomain();
		String activeDirectoryDC = configuration.getActiveDirectoryDC();
		
		java.util.Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,	"com.sun.jndi.ldap.LdapCtxFactory");
		properties.setProperty(Context.PROVIDER_URL, "ldap://" + activeDirectoryIP + ":" + activeDirectoryPort + "/");
		properties.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
		properties.setProperty(Context.SECURITY_PRINCIPAL, login + "@" + activeDirectoryDomain);
		properties.setProperty(Context.SECURITY_CREDENTIALS, password);
		properties.setProperty(Context.REFERRAL, "follow");
		DirContext ctx = new InitialDirContext(properties);
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = ctx.search(activeDirectoryDC,
				"(sAMAccountName=" + login +  ")",	constraints);
		if (results != null && results.hasMore()) {
			SearchResult sr = (SearchResult) results.next();
			String dn = sr.getName();
			log.info("Usuario " + dn + " autenticado correctamente");
			return true;
		}
		return false;
	}
}