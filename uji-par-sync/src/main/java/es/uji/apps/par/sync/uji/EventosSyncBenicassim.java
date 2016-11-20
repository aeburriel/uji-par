package es.uji.apps.par.sync.uji;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.PlantillasDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.PlantillaDTO;
import es.uji.apps.par.db.TipoEventoDTO;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.sync.parse.RssParser;
import es.uji.apps.par.sync.rss.jaxb.Item;
import es.uji.apps.par.sync.rss.jaxb.Rss;
import es.uji.apps.par.sync.rss.jaxb.Sesion;
import es.uji.apps.par.sync.utils.SyncUtils;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;

@Service("syncBenicassim")
public class EventosSyncBenicassim implements EventosSync
{
    public static Logger log = Logger.getLogger(EventosSyncBenicassim.class);

    @Autowired
    EventosDAO eventosDAO;
    
    @Autowired
    SesionesDAO sesionesDao;
    
    @Autowired
    PlantillasDAO plantillasDao;

    @Autowired
    TiposEventosDAO tipoEventosDAO;
    
    @Autowired
    SalasDAO salasDAO;

    @Autowired
    RssParser rssParser;

	@Autowired
	Configuration configuration;

    @Override
    public void sync(InputStream rssInputStream) throws JAXBException, MalformedURLException, IOException, ParseException
    {
        Rss rss = rssParser.parse(rssInputStream);

        for (Item item : rss.getChannel().getItems())
        {
            syncEvento(item);
        }
    }

    @Transactional
    private void syncEvento(Item item) throws MalformedURLException, IOException, ParseException
    {
        EventoDTO evento = eventosDAO.getEventoByRssId(item.getContenidoId());

        if (evento == null)
        {
            log.info(String.format("RSS insertando nuevo evento: %s - \"%s\"", item.getContenidoId(), item.getTitle()));

            evento = new EventoDTO();
            evento.setRssId(item.getContenidoId());
        }
        else
        {
            log.info(String.format("RSS actualizando evento existente: %s - \"%s\"", evento.getRssId(),
                    evento.getTituloVa()));
        }

        updateDatosEvento(item, evento);
        
        if (evento.getParTiposEvento() != null)
        {
            evento = eventosDAO.updateEventoDTO(evento);

            insertaSesiones(item, evento);
        }
    }

    private void insertaSesiones(Item item, EventoDTO evento) throws ParseException
    {
        for (Sesion sesionRss : item.getSesiones().getSesiones())
        {
            es.uji.apps.par.model.Sesion sesion = sesionesDao.getSesionByRssId(sesionRss.getId());
            
            if (sesion == null)
            {
                sesion = new es.uji.apps.par.model.Sesion();
                sesion.setEvento(Evento.eventoDTOtoEvento(evento));
                
                sesion.setPlantillaPrecios(getPlantillaParaItem(item));
                sesion.setRssId(sesionRss.getId());
                
                // Por ahora los metemos en la primera sala que exista (CAMBIAR)
                sesion.setSala(salasDAO.getSalas().get(0));
                
                // Inicio venta online sumando X horas (según config) a las 00:00 del día en el que se crea la sesión
                Calendar inicioVentaOnline = Calendar.getInstance();
                inicioVentaOnline.set(Calendar.HOUR_OF_DAY, 0);
                inicioVentaOnline.set(Calendar.MINUTE, 0);
                inicioVentaOnline.set(Calendar.SECOND, 0);
                inicioVentaOnline.set(Calendar.MILLISECOND, 0);
                
                inicioVentaOnline.add(Calendar.HOUR_OF_DAY, configuration.getSyncHorasInicioVentaOnline());
                
                sesion.setFechaInicioVentaOnlineWithDate(inicioVentaOnline.getTime());
            }
            
            Date fechaCelebracion = DateUtils.databaseWithSecondsToDate(sesionRss.getFecha());
            sesion.setFechaCelebracion(DateUtils.dateToSpanishString(fechaCelebracion));
            sesion.setHoraCelebracion(sesionRss.getFecha().split(" ")[1]);

            // Fin venta online 1 hora antes
            Calendar finVentaOnline = Calendar.getInstance();
            finVentaOnline.setTime(fechaCelebracion);
            finVentaOnline.add(Calendar.HOUR, -1);
            sesion.setFechaFinVentaOnline(DateUtils.dateToSpanishString(finVentaOnline.getTime()));
            sesion.setHoraFinVentaOnline(DateUtils.getHourAndMinutesWithLeadingZeros(finVentaOnline.getTime()));
            
            if (sesion.getId() == 0)
                sesionesDao.addSesion(sesion);
            else
                sesionesDao.updateSesion(sesion);
        }
    }

    private Plantilla getPlantillaParaItem(Item item)
    {
        List<PlantillaDTO> plantillas = plantillasDao.get(false, "", 0, 100);
        
        return Plantilla.plantillaPreciosDTOtoPlantillaPrecios(plantillas.get(0));
    }

    private void updateDatosEvento(Item item, EventoDTO evento) throws MalformedURLException, IOException
    {
        if (item.getSeientsNumerats() != null)
        {
            evento.setAsientosNumerados(item.getSeientsNumerats().equals("si") ? true : false);
        }

        if (item.getEnclosures() != null && item.getEnclosures().size() > 0)
        {
            String urlImagen = item.getEnclosures().get(0).getUrl();
            evento.setImagen(SyncUtils.getImageFromUrl(urlImagen));
            evento.setImagenSrc(urlImagen);
            evento.setImagenContentType(item.getEnclosures().get(0).getType());
        }

        if (item.getIdioma().equals("ca"))
        {
            evento.setTituloVa(item.getTitle());
            evento.setCaracteristicasVa(item.getResumen());
            evento.setDuracionVa(item.getDuracio());
            evento.setDescripcionVa(item.getContenido());

            if (item.getTipo() != null && !item.getTipo().equals(""))
            {
                String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
                TipoEventoDTO tipoEvento = tipoEventosDAO.getTipoEventoByNombreVa(tipo);

                if (tipoEvento == null)
                    logeaTipoNoEncontrado(evento, tipo, item.getIdioma());
                else
                    evento.setParTiposEvento(tipoEvento);
            }
        }
        else if (item.getIdioma().equals("es"))
        {
            evento.setTituloEs(item.getTitle());
            evento.setCaracteristicasEs(item.getResumen());
            evento.setDuracionEs(item.getDuracio());
            evento.setDescripcionEs(item.getContenido());

            if (item.getTipo() != null && !item.getTipo().equals(""))
            {
                String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
                TipoEventoDTO tipoEvento = tipoEventosDAO.getTipoEventoByNombreEs(tipo);

                if (tipoEvento == null)
                    logeaTipoNoEncontrado(evento, tipo, item.getIdioma());
                else
                    evento.setParTiposEvento(tipoEvento);
            }
        }
        else
        {
            log.error(String.format("Idioma desconocido: \"%s\" - Título: %s", item.getIdioma(), item.getTitle()));
        }

        // Para que no de error en BD
        if (evento.getTituloEs() == null)
        {
            evento.setTituloEs(evento.getTituloVa());
        }

        // Para que no de error en BD
        if (evento.getTituloVa() == null)
        {
            evento.setTituloVa(evento.getTituloEs());
        }
    }

    private void logeaTipoNoEncontrado(EventoDTO evento, String tipo, String idioma)
    {
        log.error(String.format("No se ha encontrado el tipo \"%s\" para evento: %d - %s - idioma: %s", tipo,
                evento.getId(), evento.getTituloVa(), idioma));
    }

}
