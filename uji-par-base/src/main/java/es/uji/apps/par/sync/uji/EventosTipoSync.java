package es.uji.apps.par.sync.uji;

import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.sync.rss.jaxb.Item;

import java.net.MalformedURLException;

public interface EventosTipoSync {
    void createNewTipoEvento(Item item, CineDTO cineDTO) throws MalformedURLException;

    void updateTipoEvento(EventoDTO evento, Item item) throws MalformedURLException;
}
