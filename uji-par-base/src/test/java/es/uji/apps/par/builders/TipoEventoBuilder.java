package es.uji.apps.par.builders;

import es.uji.apps.par.db.TipoEventoDTO;

import javax.persistence.EntityManager;

public class TipoEventoBuilder
{
	private TipoEventoDTO tipoEvento;

	public TipoEventoBuilder(String nombreEs, String nombreVa, boolean icaa)
	{
		tipoEvento = new TipoEventoDTO();
		tipoEvento.setNombreEs(nombreEs);
		tipoEvento.setNombreVa(nombreVa);
		tipoEvento.setExportarICAA(icaa);
	}

	public TipoEventoDTO build(EntityManager entityManager)
	{
		entityManager.persist(tipoEvento);

		return tipoEvento;
	}
}