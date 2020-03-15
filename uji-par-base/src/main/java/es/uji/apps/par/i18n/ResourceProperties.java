package es.uji.apps.par.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

public class ResourceProperties
{
    private static final String[] LENGUAJES = new String[]{"es", "ca"};

    private static Map<String,PropertyResourceBundle> properties;
    
    public static String getProperty(Locale locale, String propertyName, Object ... objects)
    {
    	String language = LENGUAJES[0];
        if (properties == null)
            initProperties();
        
        if (locale != null)
        	language = locale.getLanguage();
        
        if (objects.length == 0) 
            return properties.get(language).getString(propertyName);
        else 
            return String.format(properties.get(language).getString(propertyName), objects);
    }

    private static void initProperties()
    {
        properties = new HashMap<String, PropertyResourceBundle>();
        
        for (final String lenguaje:LENGUAJES)
        {
            try
            {
            	final String path = "/etc/uji/par/i18n/properties_" + lenguaje + ".properties";
            	final InputStream inputStream = Files.newInputStream(Paths.get(path));
            	final InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            	try
            	{
            		properties.put(lenguaje, new PropertyResourceBundle(reader));
            	}
            	finally {
            		reader.close();
            		inputStream.close();
            	}
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static String getProperty(String messageProperty, Locale locale, Object... values)
    {
        return ResourceProperties.getProperty(locale, messageProperty, values);
    }
}
