package es.uji.apps.par.builders;

import es.uji.apps.par.db.CineDTO;

import javax.persistence.EntityManager;

public class CineBuilder
{
	private CineDTO cine;

	public CineBuilder(String nombre)
	{
		cine = new CineDTO();
		cine.setNombre(nombre);
	}

	public CineDTO build(EntityManager entityManager)
	{
		entityManager.persist(cine);

		return cine;
	}
}