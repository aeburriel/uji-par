package es.uji.apps.par.services;

import es.uji.apps.par.dao.AbonosDAO;
import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.model.PreciosPlantilla;
import es.uji.apps.par.model.PreciosSesion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AbonosService {

    @Autowired
    private AbonadosService abonadosService;

    @Autowired
    private AbonosDAO abonosDAO;

    @Autowired
    private PreciosPlantillaService preciosPlantillaService;

    public List<Abono> getAbonos(String sortParameter, int start, int limit, String userUID)
    {
        return abonosDAO.getAbonos(sortParameter, start, limit, userUID);
    }

    public void removeAbono(Long id)
    {
        int totalAbonadosPorAbono = abonadosService.getTotalAbonadosPorAbono(id);
        if (totalAbonadosPorAbono > 0)
        {
            throw new GeneralPARException(GeneralPARException.ABONO_CON_ABONADOS_CODE);
        }
        else
        {
            abonosDAO.removeAbono(id);
        }
    }

    public void removeAbonado(Long id)
    {
        abonadosService.removeAbonado(id);
    }

    public Abono addAbono(Abono abono) throws CampoRequeridoException
    {
        checkRequiredFields(abono);
        return abonosDAO.addAbono(abono);
    }

    public void updateAbono(Abono abono) throws CampoRequeridoException
    {
        int totalAbonadosPorAbono = abonadosService.getTotalAbonadosPorAbono(abono.getId());
        if (totalAbonadosPorAbono > 0) {
            abonosDAO.updateCamposActualizablesConAbonadosAbono(abono);
        }
        else {
            checkRequiredFields(abono);
            abonosDAO.updateAbono(abono);
        }
    }

    private void checkRequiredFields(Abono abono) throws CampoRequeridoException
    {
        if (abono.getNombre() == null || abono.getNombre().isEmpty())
            throw new CampoRequeridoException("Nombre");

        if (abono.getPlantillaPrecios() == null )
            throw new CampoRequeridoException("Plantilla");

        if (abono.getSesiones() == null || abono.getSesiones().size() == 0)
            throw new CampoRequeridoException("Sesiones");
    }

    public int getTotalAbonos(String userUID)
    {
        return abonosDAO.getTotalAbonos(userUID);
    }

    public Abono getAbono(Long abonoId, String userUID)
    {
        return abonosDAO.getAbono(abonoId, userUID);
    }

    public List<PreciosSesion> getPreciosAbono(Long abonoId, String userUID) {
        List<PreciosSesion> listaPreciosSesion = new ArrayList<PreciosSesion>();

        Abono abono = getAbono(abonoId, userUID);
        List<PreciosPlantilla> preciosPlantilla = preciosPlantillaService.getPreciosOfPlantilla(abono.getPlantillaPrecios().getId(), "", 0, 100);

        for(PreciosPlantilla precioPlantilla: preciosPlantilla) {
            listaPreciosSesion.add(new PreciosSesion(precioPlantilla));
        }

        return listaPreciosSesion;
    }
}
