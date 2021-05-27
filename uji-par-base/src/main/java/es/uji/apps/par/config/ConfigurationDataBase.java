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
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getUrlPublic();
	}

	public String getUrlPublicSinHTTPS()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

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
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getNombre();
	}

	public String getMailFrom()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getMailFrom();
	}

	public String getUrlComoLlegar()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getUrlComoLlegar();
	}

	public String getUrlCondicionesPrivacidad()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getUrlPrivacidad();
	}

	public String getUrlPieEntrada()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getUrlPieEntrada();
	}

	public String getLogoReport()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getLogoReport();
	}

	@Override
	public String getNombreMunicipio()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return cine.getNombreMunicipio();
	}

	public String getApiKey()
	{
		return usuariosDAO.getApiKeyByServerName(this.currentRequest.getServerName());
	}

	@Override
	public String getLangsAllowed()
	{
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

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
			final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

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
		final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

		return (cine.getShowButacasQueHanEntradoEnDistintoColor() != null && cine.getShowButacasQueHanEntradoEnDistintoColor()) ?
				true : false;
	}

	@Override
	public boolean showIVA() {
		try {
			final Cine cine = usuariosDAO.getCineByRequest(this.currentRequest);

			return cine.getShowIVA();
		}
		catch (IllegalStateException e)
		{
		}

		return true;
	}
}
