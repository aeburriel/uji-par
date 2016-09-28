package es.uji.apps.par.mails;

import es.uji.commons.web.template.HTMLTemplate;

import java.util.Map;

public interface MailTemplate
{
    String compose(HTMLTemplate template, Map<String, Object> properties) throws Exception;
}
