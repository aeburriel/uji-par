package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.butacas.EstadoButacasRequest;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.SesionAbono;
import es.uji.apps.par.services.AbonosService;
import es.uji.apps.par.services.ButacasService;
import es.uji.commons.web.template.HTMLTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Path("entrada")
public class EntradasResource extends BaseResource {

    @InjectParam
    private AbonosService abonosService;

    @InjectParam
    private ButacasService butacasService;

    @Context
    HttpServletRequest currentRequest;

    @POST
    @Path("{abonoId}/ocupadas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Butaca> estadoButaca(@PathParam("abonoId") Long abonoId, EstadoButacasRequest params) throws Exception
    {
        Abono abono = abonosService.getAbono(abonoId);

        List<Long> sesionIds = new ArrayList<>();
        for (SesionAbono sesion:abono.getSesiones())
        {
            sesionIds.add(sesion.getSesion().getId());
        }

        return butacasService.estanOcupadas(sesionIds, params.getButacas(), params.getUuidCompra());
    }

    @GET
    @Path("{id}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPreciosSesion(@PathParam("id") Long abonoId)
    {
        return Response.ok().entity(abonosService.getPreciosAbono(abonoId)).build();
    }

    @GET
    @Path("butacasAbonoFragment/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response butacasAbonoFragment(@PathParam("id") long abonoId, @QueryParam("reserva") String reserva,
                                         @QueryParam("if") String isAdmin) throws Exception {
        Locale locale = getLocale();
        String language = locale.getLanguage();

        Abono abono = abonosService.getAbono(abonoId);

        HTMLTemplate template;
        List<SesionAbono> sesiones = abono.getSesiones();
        if (sesiones != null && sesiones.size() > 0) {
            Sesion sesion = sesiones.get(0).getSesion();
            template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + sesion.getSala().getCine().getCodigo() + "/" + sesion.getSala().getHtmlTemplateName() + "Abono", locale, APP);
        }
        else {
            template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "butacasFragmentAbono", locale, APP);
        }

        template.put("baseUrl", getBaseUrlPublic());
        template.put("idioma", language);
        template.put("lang", language);
        template.put("abono", abono);
        template.put("ocultaComprar", "true");
        template.put("gastosGestion", 0.0);
        template.put("modoReserva", reserva != null && reserva.equals("true"));
        template.put("estilopublico", "false");
        template.put("muestraReservadas", true);
        template.put("modoAdmin", true);
        Calendar cal = Calendar.getInstance();
        template.put("millis", cal.getTime().getTime());

        List<String> titulos = new ArrayList<>();
        for (SesionAbono sesion: sesiones)
        {
            if (language.equals("ca")) {
                titulos.add(sesion.getSesion().getEvento().getTituloVa());
            } else {
                titulos.add(sesion.getSesion().getEvento().getTituloEs());
            }
        }
        template.put("titulos", titulos);
        template.put("butacasSesion", "[]");

        return Response.ok().entity(template).header("Content-Type", "text/html; charset=utf-8").build();
    }
}