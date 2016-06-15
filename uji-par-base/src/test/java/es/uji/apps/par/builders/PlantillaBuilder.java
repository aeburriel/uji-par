package es.uji.apps.par.builders;

import es.uji.apps.par.db.PlantillaDTO;
import es.uji.apps.par.db.SalaDTO;

import javax.persistence.EntityManager;

public class PlantillaBuilder
{
	private PlantillaDTO plantilla;

	public PlantillaBuilder(String nombre, SalaDTO sala)
	{
		plantilla = new PlantillaDTO();
		plantilla.setNombre(nombre);
		plantilla.setSala(sala);
	}

	public PlantillaDTO build(EntityManager entityManager)
	{
		entityManager.persist(plantilla);

		return plantilla;
	}
}