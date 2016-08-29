package es.uji.apps.par.services;

import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.model.Tpv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TpvsService {

    @Autowired
    private TpvsDAO tpvsDao;

    public List<Tpv> getTpvs(String userUID, String sortParameter, int start, int limit) {
        List<Tpv> tpvs = new ArrayList<Tpv>();
        List<TpvsDTO> tpvsDto = tpvsDao.getTpvs(userUID, sortParameter, start, limit);

        for (TpvsDTO tpvDto : tpvsDto) {
            tpvs.add(new Tpv(tpvDto));
        }

        return tpvs;
    }
}