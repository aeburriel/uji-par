package es.uji.apps.par.services.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.Constantes;
import es.uji.apps.par.EventoNoEncontradoException;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.PreciosSesion;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.services.EventosService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.ImageUtils;
import es.uji.apps.par.utils.Utils;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;

@Path("evento")
public class EventosResource extends BaseResource
{
    @InjectParam
    private EventosService eventosService;

    @InjectParam
    private SesionesService sesionesService;

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public Template getEvento(@PathParam("id") Integer id) throws Exception
    {
        Evento evento = eventosService.getEvento(id);
        List<Sesion> sesiones = sesionesService.getSesiones(id);

        Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "eventoDetalle", getLocale());

        String tipoEvento, titulo, companyia, duracion, caracteristicas, premios, interpretes, descripcion;

        if (getLocale().getLanguage().equals("ca"))
        {
            tipoEvento = evento.getParTipoEvento().getNombreVa();
            titulo = evento.getTituloVa();
            companyia = evento.getCompanyiaVa();
            duracion = evento.getDuracionVa();
            caracteristicas = evento.getCaracteristicasVa();
            premios = evento.getPremiosVa();
            interpretes = evento.getInterpretesVa();
            descripcion = evento.getDescripcionVa();
        }
        else
        {
            tipoEvento = evento.getParTipoEvento().getNombreEs();
            titulo = evento.getTituloEs();
            companyia = evento.getCompanyiaEs();
            duracion = evento.getDuracionEs();
            caracteristicas = evento.getCaracteristicasEs();
            premios = evento.getPremiosEs();
            interpretes = evento.getInterpretesEs();
            descripcion = evento.getDescripcionEs();
        }

        template.put("tipoEvento", tipoEvento);
        template.put("titulo", titulo);
        template.put("companyia", companyia);
        template.put("duracion", duracion);
        template.put("caracteristicas", caracteristicas);
        template.put("premios", premios);
        template.put("interpretes", interpretes);
        template.put("descripcion", descripcion);

        template.put("evento", evento);
        template.put("sesiones", sesiones);
        template.put("sesionesPlantilla", getSesionesPlantilla(sesiones));

        return template;
    }

    private List<Map<String, Object>> getSesionesPlantilla(List<Sesion> sesiones)
    {
        List<Map<String, Object>> sesionesPlantilla = new ArrayList<Map<String, Object>>();

        for (Sesion sesion : sesiones)
        {
            Map<String, Object> datos = new HashMap<String, Object>();

            datos.put("texto", getFechaSesion(sesion));
            
            datos.put("id", sesion.getId());
            datos.put("enPlazoVentaInternet", sesion.getEnPlazoVentaInternet());
            datos.put("canalInternet", sesion.getCanalInternet());
            datos.put("fechaInicioVentaOnline", sesion.getFechaFinVentaOnline());

            datos.put(
                    "textoFechasInternet",
                    ResourceProperties.getProperty(getLocale(), "venta.plazoInternet",
                            DateUtils.dateToSpanishString(sesion.getFechaInicioVentaOnline()),
                            DateUtils.dateToSpanishString(sesion.getFechaFinVentaOnline())));
            
            if (sesion.getEnPlazoVentaInternet())
                datos.put("clase", "comprarEntradas");
            else
                datos.put("clase", "");
                    

            sesionesPlantilla.add(datos);
        }

        return sesionesPlantilla;
    }

    private String getFechaSesion(Sesion sesion)
    {
        Date fecha = sesion.getFechaCelebracion();
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM HH.mm", getLocale());
        
        String diaSemana = ResourceProperties.getProperty(getLocale(), "dia.abreviado." + fecha.getDay());
        
        return diaSemana + " " + format.format(fecha) + " / " + getPrecioSesion(sesion) + " euros";
    }
    
    private String getPrecioSesion(Sesion sesion)
    {
        Map<String, PreciosSesion> preciosSesion = sesionesService.getPreciosSesionPorLocalizacion(sesion.getId());
        
        if (preciosSesion != null && preciosSesion.get("platea1") != null)
        {
            return Utils.formatEuros(preciosSesion.get("platea1").getPrecio());
        }
        else
        {
            return "-";
        }
    }

    @GET
    @Path("{id}/imagen")
    public Response getImagenEvento(@PathParam("id") Integer eventoId)
    {
        try
        {
            Evento evento = eventosService.getEvento(eventoId);

            return Response.ok(evento.getImagen()).type(evento.getImagenContentType()).build();
        }
        catch (EventoNoEncontradoException e)
        {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{id}/imagenEntrada")
    public Response getImagenEntrada(@PathParam("id") Integer eventoId) throws IOException
    {
        try
        {
            Evento evento = eventosService.getEvento(eventoId);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ImageUtils.changeDpi(evento.getImagen(), bos, 3);

            return Response.ok(bos.toByteArray()).type(evento.getImagenContentType()).build();
        }
        catch (EventoNoEncontradoException e)
        {
            return Response.noContent().build();
        }
    }

}
