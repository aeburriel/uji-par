package es.uji.apps.par;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.config.ConfigurationSelector;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;
import es.uji.commons.web.template.model.GrupoMenu;
import es.uji.commons.web.template.model.ItemMenu;
import es.uji.commons.web.template.model.Menu;
import es.uji.commons.web.template.model.Pagina;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.text.ParseException;
import java.util.Locale;

@Provider
public class CommonExceptionMapper implements ExceptionMapper<Exception>
{
	private static final Logger log = LoggerFactory.getLogger(CommonExceptionMapper.class);
    
    @Context
    HttpServletRequest currentRequest;

	@Autowired
	Configuration configuration;

	@Autowired
	ConfigurationSelector configurationSelector;
    
    protected static final String APP = "par";
    protected static final String LANG = "language";
    
    protected Locale getLocale()
    {
    	HttpSession session = currentRequest.getSession();
    	String lang = (String) session.getAttribute(LANG);
    	
    	return getLocale(lang);
    }
    
    protected Locale getLocale(String lang)
    {
        String idiomaFinal = "ca";

        if (lang != null && lang.length() > 0)
        {
        	HttpSession session = currentRequest.getSession();
        	session.setAttribute(LANG, lang);
        	idiomaFinal = lang;
        }
        else if (currentRequest != null)
        {
            if (currentRequest.getCookies() != null)
            {
                for (Cookie cookie : currentRequest.getCookies())
                {
                    if (cookie != null && "uji-lang".equals(cookie.getName()))
                    {
                        String idiomaCookie = cookie.getValue();

                        if (esIdiomaValido(idiomaCookie))
                        {
                            idiomaFinal = idiomaCookie;
                            break;
                        }
                    }
                }
            }

            String idiomaParametro = currentRequest.getParameter("idioma");

            if (idiomaParametro != null)
            {
                if (esIdiomaValido(idiomaParametro))
                {
                    idiomaFinal = idiomaParametro;
                }
            }
        }

        return new Locale(idiomaFinal);
    }

    private boolean esIdiomaValido(String idioma)
    {
        return idioma.equals("es") || idioma.equals("ca");
    }

    @Override
    public Response toResponse(Exception exception)
    {
        log.error(exception.getMessage(), exception);
        Locale locale = getLocale();
        String language = locale.getLanguage();

		Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "error", locale, APP);
		Pagina pagina = null;

		try {
			pagina = new Pagina(configurationSelector.getUrlPublic(), configurationSelector.getUrlPublic(), language, configuration.getHtmlTitle());
			pagina.setTitulo(configurationSelector.getHtmlTitle());
			pagina.setSubTitulo("");
		} catch (ParseException e) {

		}
		template.put("idioma", language);
		template.put("baseUrl", configurationSelector.getUrlPublic());
		template.put("pagina", pagina);

		Menu menu = new Menu();

		GrupoMenu grupo = new GrupoMenu("Comunicació");
		grupo.addItem(new ItemMenu("Noticies", "http://www.uji.es/"));
		grupo.addItem(new ItemMenu("Investigació", "http://www.uji.es/"));
		menu.addGrupo(grupo);
		pagina.setMenu(menu);

		return Response.ok(template).build();
    }
}