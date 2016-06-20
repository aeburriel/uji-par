package es.uji.apps.par.services;

import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.model.Sala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalasService
{
    @Autowired
    private SalasDAO salasDAO;

    @Autowired
    private EventosDAO eventosDAO;

    public List<Sala> getSalas(String userUID)
    {
        return salasDAO.getSalas(userUID);
    }

    public List<Sala> getSalasDisponiblesParaEvento(String userUID, Long eventoId)
    {
        List<Sala> salas = new ArrayList<>();

        EventoDTO eventoById = eventosDAO.getEventoById(eventoId, userUID);
        List<Sala> allSalas = getSalas(userUID);
        if (eventoById.getAsientosNumerados())
        {
            for (Sala sala : allSalas)
            {
                if (sala.isAsientosNumerados())
                {
                    salas.add(sala);
                }
            }
            return salas;
        }
        else
        {
            return allSalas;
        }
    }
}
