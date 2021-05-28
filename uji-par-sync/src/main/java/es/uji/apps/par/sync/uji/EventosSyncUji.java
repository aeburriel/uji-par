package es.uji.apps.par.sync.uji;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.TipoEventoDTO;
import es.uji.apps.par.sync.parse.RssParser;
import es.uji.apps.par.sync.rss.jaxb.Item;
import es.uji.apps.par.sync.rss.jaxb.Rss;
import es.uji.apps.par.sync.utils.SyncUtils;
import es.uji.apps.par.utils.Utils;

@Service("syncUji")
public class EventosSyncUji implements EventosSync
{
    public static Logger log = Logger.getLogger(EventosSyncUji.class);

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TiposEventosDAO tipoEventosDAO;
    
    RssParser rssParser;
    
    public EventosSyncUji()
    {
        rssParser = new RssParser();
    }

    @Override
    public void sync(InputStream rssInputStream) throws JAXBException, MalformedURLException, IOException
    {
        Rss rss = rssParser.parse(rssInputStream);

        for (Item item : rss.getChannel().getItems())
        {
            if (item.getEsquema() != null && item.getEsquema().equals("paranimf"))
            {
                syncEvento(item);
            }
        }
    }

    private void syncEvento(Item item) throws MalformedURLException, IOException
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
            eventosDAO.updateEventoDTO(evento);
        }
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
            byte[] imagen = SyncUtils.getImageFromUrl(urlImagen);
            
            if (imagen != null && imagen.length > 0) {
            	evento.setImagen(imagen);
            	evento.setImagenSrc(urlImagen);
            	evento.setImagenContentType(item.getEnclosures().get(0).getType());
            }
        }

        if (Utils.VALENCIANO.getLanguage().equals(item.getIdioma()))
        {
            evento.setTituloVa(item.getTitle());
            evento.setCaracteristicasVa(item.getResumen());
            evento.setDuracionVa(item.getDuracio());
            evento.setDescripcionVa(item.getContenido());

            if (item.getTipo() != null)
            {
                String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
                TipoEventoDTO tipoEvento = tipoEventosDAO.getTipoEventoByNombreVa(tipo);

                if (tipoEvento == null)
                    logeaTipoNoEncontrado(evento, tipo, item.getIdioma());
                else
                    evento.setParTiposEvento(tipoEvento);
            }
        }
        else if (Utils.CASTELLANO.getLanguage().equals(item.getIdioma()))
        {
            evento.setTituloEs(item.getTitle());
            evento.setCaracteristicasEs(item.getResumen());
            evento.setDuracionEs(item.getDuracio());
            evento.setDescripcionEs(item.getContenido());

            if (item.getTipo() != null)
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
