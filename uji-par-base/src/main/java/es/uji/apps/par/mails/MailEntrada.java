package es.uji.apps.par.mails;

import es.uji.commons.web.template.HTMLTemplate;

import java.util.Map;

public class MailEntrada implements MailTemplate
{
    @Override
    public String compose(HTMLTemplate template, Map<String, Object> properties) throws Exception
    {
        for (String propertyKey : properties.keySet())
        {
            template.put(propertyKey, properties.get(propertyKey));
        }

        return new String(template.process());
    }
}
