package es.uji.apps.par.config;

public interface ConfigurationSelector
{
	String getUrlPublic();
	String getUrlPublicSinHTTPS();
	String getUrlPublicLimpio();
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
