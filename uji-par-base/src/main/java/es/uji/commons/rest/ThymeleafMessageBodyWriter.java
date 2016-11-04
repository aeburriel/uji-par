package es.uji.commons.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.PDFTemplate;
import es.uji.commons.web.template.Template;

@Provider
public class ThymeleafMessageBodyWriter implements MessageBodyWriter<Template>
{
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return type.equals(HTMLTemplate.class) || type.equals(PDFTemplate.class);
    }

    @Override
    public long getSize(Template template, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(Template template, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException
    {
        try
        {
            byte[] content = template.process();

            if (content != null && content.length > 0)
            {
                entityStream.write(content);
                entityStream.flush();
            }
        }
        catch (Exception e)
        {
            throw new WebApplicationException(e);
        }
    }
}
