package es.uji.apps.par.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class ConfigurationProperties implements ConfigurationSelector
{
	private static final String URL_PUBLIC = "uji.par.urlPublic";
	private static final String URL_PUBLIC_SIN_HTTPS = "uji.par.urlPublicSinHTTPS";

	private static final Logger log = LoggerFactory.getLogger(ConfigurationProperties.class);

	private Properties properties;

	@Autowired
	public ConfigurationProperties(ConfigurationInterface configurationInterface) throws IOException
	{
		properties = new Properties();
		properties.load(configurationInterface.getPathToFile());
	}

	private String getProperty(String propertyName)
	{
		String value = properties.getProperty(propertyName);
		try
		{
			value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
		catch (NullPointerException e)
		{
			log.info("Propiedad " + propertyName + " nula");
			throw e;
		}

		return value.trim();
	}

	public String getUrlPublic()
	{
		return getProperty(URL_PUBLIC);
	}

	public String getUrlPublicSinHTTPS()
	{
		return getProperty(URL_PUBLIC_SIN_HTTPS);
	}
}
