package es.uji.apps.par.builders;

import es.uji.apps.par.db.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventoBuilder
{
	private EventoDTO evento;
	private List<SesionDTO> sesiones;

	public EventoBuilder(String tituloEs, String tituloVa, CineDTO cine, TipoEventoDTO tipoEvento)
	{
		sesiones = new ArrayList<>();

		evento = new EventoDTO();
		evento.setTituloEs(tituloEs);
		evento.setTituloVa(tituloVa);
		evento.setParTiposEvento(tipoEvento);
		evento.setParCine(cine);
		evento.setRetencionSgae(BigDecimal.ZERO);
		evento.setIvaSgae(BigDecimal.ZERO);
		evento.setPorcentajeIva(BigDecimal.ZERO);
	}


	public EventoBuilder withRssId(String rssId)
	{
		evento.setRssId(rssId);

		return this;
	}

	public EventoBuilder withTpv( TpvsDTO tpv)
	{
		evento.setParTpv(tpv);

		return this;
	}

	public EventoBuilder withSesion(String nombre, SalaDTO sala)
	{
		Date date = new Date();

		SesionDTO sesionDTO = new SesionDTO();
		sesionDTO.setNombre(nombre);
		sesionDTO.setParSala(sala);
		sesionDTO.setFechaCelebracion(new Timestamp(date.getTime()));

		sesiones.add(sesionDTO);

		return this;
	}

	public EventoDTO build(EntityManager entityManager)
	{
		if (evento.getParTpv() == null)
		{
			evento.setParTpv(new TpvBuilder("tpv").build(entityManager));
		}

		entityManager.persist(evento);

		for (SesionDTO sesion : sesiones)
		{
			sesion.setParEvento(evento);
			entityManager.persist(sesion);
		}

		return evento;
	}
}