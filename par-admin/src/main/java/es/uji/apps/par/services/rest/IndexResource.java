package es.uji.apps.par.services.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.auth.Authenticator;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;

import java.util.Locale;

@Path("index")
public class IndexResource extends BaseResource
{
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Template index(@QueryParam("lang") String lang) throws Exception
    {
        if (lang != null)
        {
            setLocale(lang);
        }

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "admin", getLocale(), APP);

        template.put("user", currentRequest.getSession().getAttribute(Authenticator.USER_ATTRIBUTE));
        template.put("urlPublic", Configuration.getUrlPublic());
		template.put("allowMultisesion", Configuration.getAllowMultisesion());
        template.put("payModes", Configuration.getPayModes(getLocale()));
        template.put("lang", getLocale().getLanguage());
        template.put("langsAllowed", Configuration.getLangsAllowed());

        Boolean b = (Boolean) currentRequest.getSession().getAttribute(Authenticator.READONLY_ATTRIBUTE);
        boolean readOnlyUser = (b == null || !b)?false:true;
        template.put("readOnlyUser", readOnlyUser);

        template.put("menuHtml", getMenu(readOnlyUser));
        template.put("controllers", getControllers(readOnlyUser));
        template.put("screens", getScreens(readOnlyUser));
        template.put("views", getViews(readOnlyUser));

        return template;
    }

    private String getMenu(boolean readOnlyUser) {
        Locale locale = getLocale();

        String menuHtml = "<div class=\"nav\">" +
                "    <div class=\"paranimf_logo\"></div>" +
                "    <ul>" +
                "        <li id=\"menuTiposEventos\">" +
                "            <a href=\"javascript:muestraMenu('TiposEventos')\"><img src=\"../resources/images/menu/tiposeventos.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.tipoEvento") + "</span></a>" +
                "        </li>" +
                "        <li id=\"menuEventos\">" +
                "            <a href=\"javascript:muestraMenu('Eventos')\"><img src=\"../resources/images/menu/Movie.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.eventoSesiones") + "</span></a>" +
                "        </li>" +
                "        <li id=\"menuPlantillasPrecios\">" +
                "            <a href=\"javascript:muestraMenu('PlantillasPrecios')\"><img src=\"../resources/images/menu/coin.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.plantillaPrecios") + "</span></a>" +
                "        </li>" +
                "        <li id=\"menuTarifas\">" +
                "            <a href=\"javascript:muestraMenu('Tarifas')\"><img src=\"../resources/images/menu/coin.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.tarifas") + "</span></a>" +
                "        </li>";

        if (!readOnlyUser) {
            menuHtml += "        <li id=\"menuTaquilla\">" +
                    "            <a href=\"javascript:muestraMenu('Taquilla')\"><img src=\"../resources/images/menu/settings.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.taquilla") + "</span></a>" +
                    "        </li>";
        }

        menuHtml += "        <li id=\"menuComprasReservas\">" +
                "            <a href=\"javascript:muestraMenu('ComprasReservas')\"><img src=\"../resources/images/menu/settings.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.comprasReservas") + "</span></a>" +
                "        </li>";

        if (Configuration.isMenuAbono()) {
            menuHtml += "        <li id=\"menuAbonos\">" +
                    "            <a href=\"javascript:muestraMenu('Abonos')\"><img src=\"../resources/images/menu/abonos.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.abonos") + "</span></a>" +
                    "        </li>";
        }

        menuHtml += "        <li id=\"menuInformes\">" +
                "            <a href=\"javascript:muestraMenu('Informes')\"><img src=\"../resources/images/menu/informes.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.informes") + "</span></a>" +
                "        </li>    " +
                "        <li id=\"menuClientes\">" +
                "            <a href=\"javascript:muestraMenu('Clientes')\"><img src=\"../resources/images/menu/email.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.clientes") + "</span></a>" +
                "        </li>    ";

        if (Configuration.isMenuICAA()) {
            menuHtml += "        <li id=\"menuGenerarFicheros\">" +
                    "            <a href=\"javascript:muestraMenu('GenerarFicheros')\"><img src=\"../resources/images/menu/Save.png\" width=\"24\"/><span>" + ResourceProperties.getProperty(locale, "menu.icaa") + "</span></a>" +
                    "        </li>";
        }

        menuHtml += "    </ul></div>";

        return menuHtml;
    }

    private String getControllers(boolean readOnlyUser) {
        String controllers = "['Dashboard', 'Usuarios', 'TiposEventos', 'Eventos', 'PlantillasPrecios', 'ComprasReservas', 'Informes', 'Tarifas', 'Clientes'";

        if (!readOnlyUser) {
            controllers += ", 'Taquilla'";
        }

        if (Configuration.isMenuAbono()) {
            controllers += ", 'Abonos'";
        }

        if (Configuration.isMenuICAA()) {
            controllers += ", 'GenerarFicheros'";
        }

        controllers += "]";

        return controllers;
    }

    private String getScreens(boolean readOnlyUser) {
        String screens = "[{'Dashboard': 0, 'Usuarios': 1, 'TiposEventos': 2, 'Eventos': 3, 'PlantillasPrecios': 4,  'ComprasReservas': 5, 'Informes': 6, 'Tarifas': 7, 'Clientes': 8";

        int i = 9;
        if (!readOnlyUser) {
            screens += ", 'Taquilla': " + i;
            i++;
        }

        if (Configuration.isMenuAbono()) {
            screens += ", 'Abonos': " + i;
            i++;
        }

        if (Configuration.isMenuICAA()) {
            screens += ", 'GenerarFicheros': " + i;
            i++;
        }

        screens += "}]";

        return screens;
    }

    private String getViews(boolean readOnlyUser) {
        String views = "[{border: false, xtype: 'dashboard'}, {xtype: 'gridUsuarios'}, {xtype: 'gridTiposEventos'}, {xtype: 'panelEventos'}, {xtype: 'panelPlantillas'}, {xtype: 'panelComprasReservas'}, {xtype: 'panelInformes'}, {xtype: 'panelTarifas'}, {xtype: 'gridClientes'}";

        if (!readOnlyUser) {
            views += ", {xtype: 'panelTaquilla'}";
        }

        if (Configuration.isMenuAbono()) {
            views += ", {xtype: 'panelAbonos'}";
        }

        if (Configuration.isMenuICAA()) {
            views += ", {xtype: 'panelSesionesFicheros'}";
        }

        views += "]";

        return views;
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public Template indexPost() throws Exception
    {
        return index(null);
    }
}
