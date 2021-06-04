package es.uji.apps.par.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.password.StrongPasswordEncryptor;

import com.google.common.base.Strings;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.model.Usuario;

public class DatabaseAuthenticator implements Authenticator {
	private static final String LOGIN_PARAM = "login";
    private static final String PASSWORD_PARAM = "password";

    private final StrongPasswordEncryptor encryptor;
	private Configuration configuration;

	public DatabaseAuthenticator() {
		encryptor = new StrongPasswordEncryptor();
	}

	@Override
	public int authenticate(HttpServletRequest request) {
		try {
			final boolean result = doLogin(request);
			return result ? AUTH_OK : AUTH_FAILED;
		} catch (EncryptionOperationNotPossibleException e) {
			return AUTH_FAILED_INTERNAL_ERROR;
		}
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	private void addAuthAttributes(final HttpServletRequest request, final boolean error, final boolean readonly) {
		final HttpSession session = request.getSession();

		if (error) {
			if (request.getParameter(LOGIN_PARAM) != null) {
				session.setAttribute(ERROR_LOGIN, true);
			}
		} else {
			session.setAttribute(USER_ATTRIBUTE, request.getParameter(LOGIN_PARAM));
			if (readonly) {
				session.setAttribute(READONLY_ATTRIBUTE, true);
			}
		}
	}

	private void authFail(final HttpServletRequest request) {
		addAuthAttributes(request, true, false);
	}

	private void authReadonly(final HttpServletRequest request) {
		addAuthAttributes(request, false, true);
	}

	private void authAdmin(final HttpServletRequest request) {
		addAuthAttributes(request, false, false);
	}

	private boolean doLogin(final HttpServletRequest request) {
		final String login = request.getParameter(LOGIN_PARAM);
		final String password = request.getParameter(PASSWORD_PARAM);

		if (Strings.isNullOrEmpty(login) || Strings.isNullOrEmpty(password)) {
			authFail(request);
			return false;
		}

		final Usuario usuario;
		try {
			usuario = configuration.usuariosDAO.getUserById(login);
		} catch (NullPointerException e) {
			authFail(request);
			return false;
		}

		boolean correct = encryptor.checkPassword(password, usuario.getPassword());

		if (correct) {
			if (usuario.isReadonly()) {
				authReadonly(request);
			} else {
				authAdmin(request);
			}
			return true;
		} else {
			authFail(request);
			return false;
		}
    }
}
