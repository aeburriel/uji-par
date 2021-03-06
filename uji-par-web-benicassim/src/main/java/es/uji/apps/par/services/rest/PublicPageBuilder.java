package es.uji.apps.par.services.rest;

import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.config.Configuration;
import es.uji.commons.web.template.model.Pagina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class PublicPageBuilder implements PublicPageBuilderInterface {
	@Autowired
	Configuration configuration;

	public Pagina buildPublicPageInfo(String urlBase, String url, String idioma, String htmlTitle) throws ParseException
	{
		Pagina pagina = new Pagina(urlBase, url, idioma, htmlTitle);
		pagina.setSubTitulo("");
		return pagina;
	}
}
