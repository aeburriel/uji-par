package es.uji.apps.par.config;

import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.model.Cine;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class ConfigurationDataBase implements ConfigurationSelector
{
	UsuariosDAO usuariosDAO;

	HttpServletRequest currentRequest;

	@Autowired
	public ConfigurationDataBase(UsuariosDAO usuariosDAO, HttpServletRequest currentRequest)
	{
		this.usuariosDAO = usuariosDAO;
		this.currentRequest = currentRequest;
	}

	public String getUrlPublic()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getUrlPublic();
	}

	public String getUrlPublicSinHTTPS()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());
		String urlPublicSinHTTPS = cine.getUrlPublic().replaceFirst("^https://", "http://");

		return urlPublicSinHTTPS;
	}
}
