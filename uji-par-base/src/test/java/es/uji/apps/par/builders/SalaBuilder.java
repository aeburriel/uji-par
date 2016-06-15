package es.uji.apps.par.builders;

import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.SalaDTO;

import javax.persistence.EntityManager;

public class SalaBuilder
{
	private SalaDTO sala;

	public SalaBuilder(String nombre, CineDTO cine)
	{
		sala = new SalaDTO();
		sala.setNombre(nombre);
		sala.setParCine(cine);
	}

	public SalaDTO build(EntityManager entityManager)
	{
		entityManager.persist(sala);

		return sala;
	}
}