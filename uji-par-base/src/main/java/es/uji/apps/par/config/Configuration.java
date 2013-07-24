package es.uji.apps.par.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Configuration
{
    private static final String URL_PUBLIC = "uji.par.urlPublic";

    public static Logger log = Logger.getLogger(Configuration.class);

    private static String fileName = "/etc/uji/par/app.properties";
    private Properties properties;
    private static Configuration instance;

    static
    {
        try
        {
            instance = new Configuration();
        }
        catch (IOException e)
        {
            log.error(e.toString());
        }
    }

    private Configuration() throws IOException
    {
        String filePath = fileName;
        File f = new File(filePath);

        properties = new Properties();
        FileInputStream fis = new FileInputStream(f);
        properties.load(fis);
    }

    public static void reinitConfig() throws IOException
    {
        instance = null;
        instance = new Configuration();
    }

    private static String getProperty(String propertyName)
    {
        return (String) instance.properties.getProperty(propertyName);
    }

    public static String getUrlPublic()
    {
        return getProperty(URL_PUBLIC);
    }
}