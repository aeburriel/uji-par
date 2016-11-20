package es.uji.apps.par.services.rest;

import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.config.Configuration;
import es.uji.commons.web.template.model.GrupoMenu;
import es.uji.commons.web.template.model.ItemMenu;
import es.uji.commons.web.template.model.Menu;
import es.uji.commons.web.template.model.Pagina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class UJIPublicPageBuilder implements PublicPageBuilderInterface {
	@Autowired
	Configuration configuration;

	public Pagina buildPublicPageInfo(String urlBase, String url, String idioma) throws ParseException
	{
		Menu menu = new Menu();

		GrupoMenu grupo = new GrupoMenu("Comunicació");
		grupo.addItem(new ItemMenu("Noticies", "http://www.uji.es/"));
		grupo.addItem(new ItemMenu("Investigació", "http://www.uji.es/"));
		menu.addGrupo(grupo);

		Pagina pagina = new Pagina(urlBase, url, idioma, configuration.getHtmlTitle());
		pagina.setSubTitulo("");
		pagina.setMenu(menu);

		return pagina;
	}
}
