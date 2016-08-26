package es.uji.apps.par.builders;

import es.uji.commons.web.template.model.Pagina;

import java.text.ParseException;

public interface PublicPageBuilderInterface {
	public Pagina buildPublicPageInfo(String urlBase, String url, String idioma, String htmlTitle) throws ParseException;
}
