package es.uji.apps.par.config;

import javax.servlet.http.HttpServletRequest;

public interface ConfigurationSelector
{
	String getUrlBase(final HttpServletRequest request);
	String getUrlPublic();
	String getUrlPublicSinHTTPS();
	String getUrlAdmin();
	String getHtmlTitle();
	String getMailFrom();
	String getUrlComoLlegar();
	String getUrlCondicionesPrivacidad();
	String getUrlPieEntrada();
	String getLogoReport();
	String getNombreMunicipio();
	String getApiKey();
	String getLangsAllowed();
	boolean getLocalizacionEnValenciano();
	String getIdiomaPorDefecto();
	boolean showButacasHanEntradoEnDistintoColor();
	boolean showIVA();
}
