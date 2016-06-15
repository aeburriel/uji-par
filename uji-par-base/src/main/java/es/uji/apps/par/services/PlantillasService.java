package es.uji.apps.par.services;

import es.uji.apps.par.dao.PlantillasDAO;
import es.uji.apps.par.db.PlantillaDTO;
import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.model.Plantilla;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlantillasService {
	
	@Autowired
    private PlantillasDAO plantillasPreciosDAO;

	private List<Plantilla> get(boolean filtrarEditables, String sortParameter, int start, int limit, String userUID) {
		List<Plantilla> plantillaPrecios = new ArrayList<Plantilla>();
		
		for (PlantillaDTO plantillaDTO: plantillasPreciosDAO.get(filtrarEditables, sortParameter, start, limit, userUID)) {
			plantillaPrecios.add(new Plantilla(plantillaDTO));
		}
		return plantillaPrecios;
	}

	public void remove(int id) {
		plantillasPreciosDAO.remove(id);
	}

	public Plantilla add(Plantilla plantillaPrecios) throws CampoRequeridoException {
		checkRequiredFields(plantillaPrecios);
		return plantillasPreciosDAO.add(plantillaPrecios);
	}

	private void checkRequiredFields(Plantilla plantillaPrecios) throws CampoRequeridoException {
		if (plantillaPrecios.getNombre() == null || plantillaPrecios.getNombre().isEmpty())
			throw new CampoRequeridoException("Nombre");
	}

	public void update(Plantilla plantillaPrecios) throws CampoRequeridoException {
		checkRequiredFields(plantillaPrecios);
		plantillasPreciosDAO.update(plantillaPrecios);
	}

	public List<Plantilla> getEditables(String sortParameter, int start, int limit, String userUID) {
		boolean filtrarEditables = true;
		return get(filtrarEditables, sortParameter, start, limit, userUID);
	}
	
	public List<Plantilla> getAll(String sortParameter, int start, int limit, String userUID) {
		boolean filtrarEditables = false;
		return get(filtrarEditables, sortParameter, start, limit, userUID);
	}

	public int getTotalPlantillaPrecios(String userUID) {
		return plantillasPreciosDAO.getTotalPlantillaPrecios(userUID);
	}

	public int getTotalPlantillasEditables(String userUID) {
		return plantillasPreciosDAO.getTotalPlantillasEditables(userUID);
	}
}
