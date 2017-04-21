package es.uji.apps.par.config;

import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.utils.Utils;
import org.apache.commons.lang3.NotImplementedException;
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

		return Utils.sinHTTPS(cine.getUrlPublic());
	}

	public String getUrlPublicLimpio()
	{
		return getUrlPublic();
	}

	public String getUrlAdmin()
	{
		throw new NotImplementedException("getUrlAdmin");
	}

	public String getHtmlTitle()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getNombre();
	}

	public String getMailFrom()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getMailFrom();
	}

	public String getUrlComoLlegar()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getUrlComoLlegar();
	}

	public String getUrlCondicionesPrivacidad()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getUrlPrivacidad();
	}

	public String getUrlPieEntrada()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getUrlPieEntrada();
	}

	public String getLogoReport()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getLogoReport();
	}

	@Override
	public String getNombreMunicipio()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return cine.getNombreMunicipio();
	}

	public String getApiKey()
	{
		return usuariosDAO.getApiKeyByServerName(this.currentRequest.getServerName());
	}

	@Override
	public String getLangsAllowed()
	{
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		String langsAllowed = cine.getLangs();

		if (langsAllowed != null && langsAllowed.length() > 0)
			return langsAllowed;
		return "[{'locale':'es', 'alias': 'Español'}]";
	}

	@Override
	public boolean getLocalizacionEnValenciano() {
		String langsAllowed = getLangsAllowed();
		return (langsAllowed.toUpperCase().contains("VALENCI") || langsAllowed.toUpperCase().contains("CATAL"));
	}

	@Override
	public String getIdiomaPorDefecto()
	{
		try {
			String serverName = this.currentRequest.getServerName();
			Cine cine = usuariosDAO.getUserCineByServerName(serverName);

			String defaultLang = cine.getDefaultLang();
			if (defaultLang != null && defaultLang.length() > 0)
				return defaultLang;
		}
		catch (IllegalStateException e)
		{
		}

		return "es";
	}

	@Override
	public boolean showButacasHanEntradoEnDistintoColor() {
		Cine cine = usuariosDAO.getUserCineByServerName(this.currentRequest.getServerName());

		return (cine.getShowButacasQueHanEntradoEnDistintoColor() != null && cine.getShowButacasQueHanEntradoEnDistintoColor()) ?
				true : false;
	}

	@Override
	public boolean showIVA() {
		try {
			String serverName = this.currentRequest.getServerName();
			Cine cine = usuariosDAO.getUserCineByServerName(serverName);

			return cine.getShowIVA();
		}
		catch (IllegalStateException e)
		{
		}

		return true;
	}
}
