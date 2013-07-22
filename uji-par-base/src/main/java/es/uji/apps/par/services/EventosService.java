package es.uji.apps.par.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.CampoRequeridoException;
import es.uji.apps.par.EventoNoEncontradoException;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.model.Evento;

@Service
public class EventosService
{
    @Autowired
    private EventosDAO eventosDAO;

    public List<Evento> getEventos()
    {
       return getEventos(false);
    }
    
    public List<Evento> getEventosActivos()
    {
       return getEventos(true);
    }
    
    private List<Evento> getEventos(boolean activos)
    {
        List<Evento> listaParEvento = new ArrayList<Evento>();
        
        List<EventoDTO> eventos;
        if (activos)
            eventos = eventosDAO.getEventosActivos();
        else
            eventos = eventosDAO.getEventos();
        
        for (EventoDTO eventoDB : eventos)
        {
            listaParEvento.add(new Evento(eventoDB, false));
        }
        
        return listaParEvento;
    }

    public void removeEvento(Integer id)
    {
        eventosDAO.removeEvento(id);
    }

    public Evento addEvento(Evento evento) throws CampoRequeridoException
    {
        checkRequiredFields(evento);
        return eventosDAO.addEvento(evento);
    }

    private void checkRequiredFields(Evento evento) throws CampoRequeridoException
    {
        if (evento.getTituloEs() == null || evento.getTituloEs().isEmpty())
            throw new CampoRequeridoException("Título");
        if (evento.getParTipoEvento() == null)
            throw new CampoRequeridoException("Tipo de evento");
    }

    public void updateEvento(Evento evento) throws CampoRequeridoException
    {
        checkRequiredFields(evento);
        eventosDAO.updateEvento(evento);
    }

    public Evento getEvento(Integer eventoId) throws EventoNoEncontradoException
    {
        List<EventoDTO> listaEventosDTO = eventosDAO.getEventoDTO(eventoId.longValue());

        if (listaEventosDTO.size() > 0)
            return new Evento(listaEventosDTO.get(0), true);
        else
            throw new EventoNoEncontradoException(eventoId);
    }

    public void removeImagen(Integer eventoId)
    {
        eventosDAO.deleteImagen(eventoId);
    }
}
