package es.uji.apps.par.sync.uji;

import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.TipoEventoDTO;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.sync.parse.RssParser;
import es.uji.apps.par.sync.rss.jaxb.Item;
import es.uji.apps.par.sync.rss.jaxb.Rss;
import es.uji.apps.par.sync.utils.SyncUtils;
import es.uji.apps.par.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

@Service("syncUji")
public class EventosSyncUji implements EventosSync {
    private static final Logger log = LoggerFactory.getLogger(EventosSyncUji.class);

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TiposEventosDAO tipoEventosDAO;

    @Autowired
    private TpvsDAO tpvsDAO;

    @Autowired
    UsersService usersService;

    RssParser rssParser;

    public EventosSyncUji() {
        rssParser = new RssParser();
    }

    public void sync(InputStream rssInputStream, String userUID) throws JAXBException, IOException, InstantiationException, IllegalAccessException {
        Rss rss = rssParser.parse(rssInputStream);

        for (Item item : rss.getChannel().getItems()) {
            try {
                if (item.getTipo() != null && appContext.getBean(item.getTipo()) != null)
                    syncEventoTipo(item, userUID);
            } catch (BeansException e) {
                if (item.getEsquema() != null && item.getEsquema().equals("paranimf")) {
                    syncEvento(item, userUID);
                }
            }
        }
    }

    private void syncEvento(Item item, String userUID) throws IOException {
        EventoDTO evento = eventosDAO.getEventoByRssId(item.getContenidoId(), userUID);

        if (evento == null) {
            log.info(String.format("RSS insertando nuevo evento: %s - \"%s\"", item.getContenidoId(), item.getTitle()));

            Cine cine = usersService.getUserCineByUserUID(userUID);

            evento = new EventoDTO();
            evento.setParTpv(tpvsDAO.getTpvDefault());
            evento.setRssId(item.getContenidoId());
            evento.setParCine(Cine.cineToCineDTO(cine));
        } else {
            log.info(String.format("RSS actualizando evento existente: %s - \"%s\"", evento.getRssId(),
                    evento.getTituloVa()));
        }

        updateDatosEvento(item, evento, userUID);

        if (evento.getParTiposEvento() != null) {
            eventosDAO.updateEventoDTO(evento);
        }
    }

    private void syncEventoTipo(Item item, String userUID) throws IOException, IllegalAccessException, InstantiationException {
        EventosTipoSync eventosTipoSync = (EventosTipoSync)appContext.getBean(item.getTipo());
        EventoDTO evento = eventosDAO.getEventoByRssId(item.getContenidoId(), userUID);

        if (evento == null) {
            Cine cine = usersService.getUserCineByUserUID(userUID);
            eventosTipoSync.createNewTipoEvento(item, Cine.cineToCineDTO(cine));
        } else {
            eventosTipoSync.updateTipoEvento(evento, item);
        }
    }

    private void updateDatosEvento(Item item, EventoDTO evento, String userUID) throws IOException {
        if (item.getSeientsNumerats() != null) {
            evento.setAsientosNumerados(item.getSeientsNumerats().equals("si") ? true : false);
        }

        if (item.getEnclosures() != null && item.getEnclosures().size() > 0) {
            String urlImagen = item.getEnclosures().get(0).getUrl();

            byte[] imagen = SyncUtils.getImageFromUrl(urlImagen);
            if (imagen != null)
                evento.setImagen(imagen);

            evento.setImagenSrc(urlImagen);
            evento.setImagenContentType(item.getEnclosures().get(0).getType());
        }

        if (item.getIdioma().equals("ca")) {
            evento.setTituloVa(item.getTitle());
            evento.setCaracteristicasVa(item.getResumen());
            evento.setDuracionVa(item.getDuracio());
            evento.setDescripcionVa(item.getContenido());

            if (item.getTipo() != null) {
                String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
                TipoEventoDTO tipoEvento = tipoEventosDAO.getTipoEventoByNombreVa(tipo, userUID);

                if (tipoEvento == null)
                    logeaTipoNoEncontrado(evento, tipo, item.getIdioma());
                else
                    evento.setParTiposEvento(tipoEvento);
            }
        } else if (item.getIdioma().equals("es")) {
            evento.setTituloEs(item.getTitle());
            evento.setCaracteristicasEs(item.getResumen());
            evento.setDuracionEs(item.getDuracio());
            evento.setDescripcionEs(item.getContenido());

            if (item.getTipo() != null) {
                String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
                TipoEventoDTO tipoEvento = tipoEventosDAO.getTipoEventoByNombreEs(tipo, userUID);

                if (tipoEvento == null)
                    logeaTipoNoEncontrado(evento, tipo, item.getIdioma());
                else
                    evento.setParTiposEvento(tipoEvento);
            }
        } else {
            log.error(String.format("Idioma desconocido: \"%s\" - Título: %s", item.getIdioma(), item.getTitle()));
        }

        // Para que no de error en BD
        if (evento.getTituloEs() == null) {
            evento.setTituloEs(evento.getTituloVa());
        }

        // Para que no de error en BD
        if (evento.getTituloVa() == null) {
            evento.setTituloVa(evento.getTituloEs());
        }
    }

    private void logeaTipoNoEncontrado(EventoDTO evento, String tipo, String idioma) {
        log.error(String.format("No se ha encontrado el tipo \"%s\" para evento: %d - %s - idioma: %s", tipo,
                evento.getId(), evento.getTituloVa(), idioma));
    }

}
