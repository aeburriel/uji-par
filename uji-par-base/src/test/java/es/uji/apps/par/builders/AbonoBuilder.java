package es.uji.apps.par.builders;

import es.uji.apps.par.db.AbonoDTO;
import es.uji.apps.par.db.PlantillaDTO;
import es.uji.apps.par.db.SesionAbonoDTO;
import es.uji.apps.par.db.SesionDTO;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class AbonoBuilder
{
	private AbonoDTO abono;
	private List<SesionDTO> sesiones;

	public AbonoBuilder(String nombre, PlantillaDTO plantilla)
	{
		sesiones = new ArrayList<>();

		abono = new AbonoDTO();
		abono.setNombre(nombre);
		abono.setAnulado(false);
		abono.setParPlantilla(plantilla);
	}

	public AbonoBuilder withSesion(SesionDTO sesion)
	{
		sesiones.add(sesion);

		return this;
	}

	public AbonoDTO build(EntityManager entityManager)
	{
		entityManager.persist(abono);

		List<SesionAbonoDTO> sesionAbonoDTOList = new ArrayList<>();
		for (SesionDTO sesion : sesiones)
		{
			SesionAbonoDTO sesionAbono = new SesionAbonoDTO();
			sesionAbono.setParAbono(abono);
			sesionAbono.setParSesion(sesion);

			entityManager.persist(sesionAbono);

			sesionAbonoDTOList.add(sesionAbono);
		}
		abono.setParSesiones(sesionAbonoDTOList);

		return abono;
	}
}