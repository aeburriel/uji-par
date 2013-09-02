package es.uji.apps.par.services;

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

    public List<Evento> getEventos(String sort, int start, int limit)
    {
       return getEventos(false, sort, start, limit);
    }
    
    public List<Evento> getEventosActivos(String sort, int start, int limit)
    {
       return getEventos(true, sort, start, limit);
    }
    
    private List<Evento> getEventos(boolean activos, String sort, int start, int limit)
    {
        if (activos)
            return eventosDAO.getEventosActivos(sort, start, limit);
        else
            return eventosDAO.getEventos(sort, start, limit);
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
        if (evento.getParTiposEvento() == null)
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

	public int getTotalEventosActivos() {
		return eventosDAO.getTotalEventosActivos();
	}

	public int getTotalEventos() {
		return eventosDAO.getTotalEventos();
	}
}
