package es.uji.apps.par.config;

public interface ConfigurationSelector
{
	String getUrlPublic();
	String getUrlPublicSinHTTPS();
	String getUrlPublicLimpio();
	String getHtmlTitle();
	String getMailFrom();
	String getUrlComoLlegar();
	String getUrlCondicionesPrivacidad();
	String getUrlPieEntrada();
	String getLogoReport();
	String getApiKey();
}
