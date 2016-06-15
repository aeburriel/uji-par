package es.uji.apps.par.builders;

import es.uji.apps.par.db.TpvsDTO;

import javax.persistence.EntityManager;

public class TpvBuilder
{
	private TpvsDTO tpv;

	public TpvBuilder(String nombre)
	{
		tpv = new TpvsDTO();
		tpv.setNombre(nombre);
	}

	public TpvsDTO build(EntityManager entityManager)
	{
		entityManager.persist(tpv);

		return tpv;
	}
}