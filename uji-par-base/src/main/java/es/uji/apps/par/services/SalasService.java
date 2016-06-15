package es.uji.apps.par.services;

import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.model.Sala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalasService
{
    @Autowired
    private SalasDAO salasDAO;

    public List<Sala> getSalas(String userUID)
    {
        return salasDAO.getSalas(userUID);
    }
}
