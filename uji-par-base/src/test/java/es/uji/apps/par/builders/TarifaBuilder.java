package es.uji.apps.par.builders;

import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.TarifaDTO;

import javax.persistence.EntityManager;

public class TarifaBuilder
{
	private TarifaDTO tarifa;

	public TarifaBuilder(String nombre, CineDTO cine)
	{
		tarifa = new TarifaDTO();
		tarifa.setNombre(nombre);
		tarifa.setParCine(cine);
	}

	public TarifaDTO build(EntityManager entityManager)
	{
		entityManager.persist(tarifa);

		return tarifa;
	}
}