package es.uji.apps.par.services;

import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.model.Tarifa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TarifasService
{
	private static final String TARIFA_INVITACION = "Invitació";

    @Autowired
    private TarifasDAO tarifasDAO;

	public List<Tarifa> getAll(String sort, int start, int limit, String userUID) {
		List<TarifaDTO> tarifasDTO = tarifasDAO.getAll(sort, start, limit, userUID);
		List<Tarifa> listaTarifas = new ArrayList<Tarifa>();
		
		for (TarifaDTO tarifaDTO: tarifasDTO) {
			Tarifa tarifa = new Tarifa(tarifaDTO);
			listaTarifas.add(tarifa);
		}
		return listaTarifas;
	}

	public int getTotalTarifas(String userUID) {
		List<TarifaDTO> all = tarifasDAO.getAll("", 0, Integer.MAX_VALUE, userUID);

		return all != null ? all.size() : 0;
	}

	public Tarifa add(Tarifa tarifa) {
		TarifaDTO tarifaDTO = tarifasDAO.add(Tarifa.toDTO(tarifa));
		tarifa.setId(tarifaDTO.getId());
		return tarifa;
	}

	public Tarifa update(Tarifa tarifa) {
		TarifaDTO tarifaDTO = Tarifa.toDTO(tarifa);
		tarifasDAO.update(tarifaDTO);
		return tarifa;
	}

	public void removeTarifa(Integer id) {
		Tarifa tarifa = new Tarifa(id);
		tarifasDAO.removeTarifa(tarifa);
	}

	/**
	 * Devuelve la tarifa invitación
	 * @param userUID
	 * @return la tarifa o null si no existe
	 */
	public TarifaDTO getTarifaInvitacion(final String userUID) {
		// Obtenemos la tarifa Invitación
		for (final TarifaDTO tarifa : tarifasDAO.getAll(userUID)) {
			if (TARIFA_INVITACION.equals(tarifa.getNombre())) {
				return tarifa;
			}
		}

		return null;
	}
}
