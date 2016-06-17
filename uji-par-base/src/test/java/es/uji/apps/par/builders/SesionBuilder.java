package es.uji.apps.par.builders;

import es.uji.apps.par.db.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SesionBuilder
{
	private SesionDTO sesion;
	private List<CompraDTO> compras;

	public SesionBuilder(String nombre, SalaDTO sala, EventoDTO evento)
	{
		compras = new ArrayList<>();

		Date date = new Date();

		sesion = new SesionDTO();
		sesion.setNombre(nombre);
		sesion.setParSala(sala);
		sesion.setParEvento(evento);
		sesion.setFechaCelebracion(new Timestamp(date.getTime()));
	}

	public SesionBuilder withRssId(String rssId)
	{
		sesion.setRssId(rssId);

		return this;
	}

	public SesionBuilder withCompra(String nombre, BigDecimal importe, Integer numeroButacas)
	{
		CompraDTO compraDTO = new CompraDTO();
		compraDTO.setNombre(nombre);
		compraDTO.setEmail(String.format("%s@test.com", nombre.toLowerCase()));
		compraDTO.setAnulada(false);
		compraDTO.setCaducada(false);
		compraDTO.setPagada(true);
		compraDTO.setReserva(false);
		compraDTO.setImporte(importe);
		compraDTO.setInfoPeriodica(true);

		List<ButacaDTO> butacas = new ArrayList<>();
		for (int i = 0; i < numeroButacas; i++)
		{
			ButacaDTO butaca = new ButacaDTO();
			butaca.setFila("1");
			butaca.setNumero(String.valueOf(i));
			butaca.setAnulada(false);

			butacas.add(butaca);
		}
		compraDTO.setParButacas(butacas);

		compras.add(compraDTO);

		return this;
	}

	public SesionDTO build(EntityManager entityManager)
	{
		entityManager.persist(sesion);

		for (CompraDTO compra : compras)
		{
			List<ButacaDTO> butacas = compra.getParButacas();

			compra.setParButacas(null);
			compra.setParSesion(sesion);
			entityManager.persist(compra);

			for (ButacaDTO butaca : butacas)
			{
				butaca.setParSesion(sesion);
				butaca.setParCompra(compra);
				entityManager.persist(butaca);
			}
		}
		sesion.setParCompras(compras);

		return sesion;
	}
}