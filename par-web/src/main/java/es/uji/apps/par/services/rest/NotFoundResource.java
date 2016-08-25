package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.builders.PublicPageBuilderInterface;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.services.UsersService;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;

@Path("notfound")
public class NotFoundResource extends BaseResource {

    @InjectParam
    private UsersService usersService;

    @Context
    private HttpServletRequest request;

    @InjectParam
    private PublicPageBuilderInterface publicPageBuilderInterface;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Template getNotFound() throws Exception
    {
        Cine cine = usersService.getUserCineByServerName(request.getServerName());

        Locale locale = getLocale();
        String language = locale.getLanguage();
        HTMLTemplate template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + cine.getCodigo() + "/404", locale, APP);

        String url = request.getRequestURL().toString();

        template.put("pagina", publicPageBuilderInterface.buildPublicPageInfo(getBaseUrlPublic(), url, language.toString()));
        template.put("baseUrl", getBaseUrlPublic());
        template.put("lang", language);

        return template;
    }
}
