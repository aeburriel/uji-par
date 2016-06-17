package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.AbonosDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.model.Plantilla;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class AbonosDAOTest
{
	private static final String SORT = "[{\"property\":\"nombre\", \"direction\":\"ASC\"}]";
	private static final int START = 0;
	private static final int LIMIT = 100;

    @Autowired
	AbonosDAO abonosDAO;

	@PersistenceContext
	protected EntityManager entityManager;

	UsuarioDTO usuarioConSala1;
	UsuarioDTO usuarioConSala2;
	EventoDTO evento1;
	SesionDTO sesion1;
	SesionDTO sesion2;
	PlantillaDTO plantilla;
	AbonoDTO abono1;
	final String RSS_ID = "100";

	@Before
	public void setUp()
	{
		//Usuario1 -> Cine1 -> Evento1 -> Sesion1 (Sala1)
		//   "          "   ->    "    -> Sesion2 (Sala1)

		//Usuario2 -> Cine2 -> Evento3 -> Sesion3 (Sala2)

		//   -     ->  null -> Evento4 -> Sesion4 (Sala2)

		TipoEventoDTO tipoEvento = new TipoEventoBuilder("tipo", "tipo", false)
				.build(entityManager);


		CineDTO cine1 = new CineBuilder("Cine 1")
				.build(entityManager);

		SalaDTO sala1 = new SalaBuilder("Sala 1", cine1)
				.build(entityManager);

		plantilla = new PlantillaBuilder("Plantilla 1", sala1)
				.build(entityManager);

		evento1 = new EventoBuilder("Evento 1", "Esdeveniment 1", cine1, tipoEvento)
				.withRssId(RSS_ID)
				.build(entityManager);

		sesion1 = new SesionBuilder("Sesión 1", sala1, evento1)
				.withCompra("Juan", BigDecimal.valueOf(25.0), 4)
				.build(entityManager);

		sesion2 = new SesionBuilder("Sesión 2", sala1, evento1)
				.withCompra("Juan", BigDecimal.valueOf(10.0), 2)
				.withCompra("Pepe", BigDecimal.valueOf(10.0), 3)
				.build(entityManager);

		abono1 = new AbonoBuilder("Abono 1", plantilla)
				.withSesion(sesion1)
				.withSesion(sesion2)
				.build(entityManager);

		usuarioConSala1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
				.withSala(sala1)
				.build(entityManager);

		CineDTO cine2 = new CineBuilder("Cine 2")
				.build(entityManager);

		SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
				.build(entityManager);

		PlantillaDTO plantilla2 = new PlantillaBuilder("Plantilla 2", sala2)
				.build(entityManager);

		EventoDTO evento3 = new EventoBuilder("Evento 3", "Esdeveniment 3", cine2, tipoEvento)
				.withSesion("Sesión 3", sala2)
				.build(entityManager);

		SesionDTO sesion3 = new SesionBuilder("Sesión 3", sala2, evento3)
				.build(entityManager);

		new AbonoBuilder("Abono 2", plantilla2)
				.withSesion(sesion3)
				.build(entityManager);

		usuarioConSala2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
				.withSala(sala2)
				.build(entityManager);

		new EventoBuilder("Evento 4", "Esdeveniment 4", null, tipoEvento)
				.withSesion("Sesión 4", sala2)
				.build(entityManager);

		entityManager.flush();
		entityManager.clear();
	}

    @Test
    public void addAbono()
    {
        Abono abono = new Abono();
		abono.setNombre("Abono 2");
		abono.setPlantillaPrecios(Plantilla.plantillaPreciosDTOtoPlantillaPrecios(plantilla));
		abono.setSesiones("[{sesion:{id:" + sesion2.getId() + "}}]");

		abono = abonosDAO.addAbono(abono);

		Assert.assertNotNull(abono);
		Assert.assertNotNull(abono.getId());
		Assert.assertNotNull(abono.getSesiones());
		Assert.assertEquals(1, abono.getSesiones().size());
    }

	@Test
	public void addAbonoSinSesiones()
	{
		Abono abono = new Abono();
		abono.setNombre("Abono 2");
		abono.setPlantillaPrecios(Plantilla.plantillaPreciosDTOtoPlantillaPrecios(plantilla));

		abono = abonosDAO.addAbono(abono);

		Assert.assertNull(abono);
	}

	@Test
	public void getAbono()
	{
		Abono abono = abonosDAO.getAbono(abono1.getId(), usuarioConSala1.getUsuario());

		Assert.assertNotNull(abono);
		Assert.assertEquals(abono1.getId(), abono.getId());
	}

    @Test
    public void deleteAbono()
    {
		long anuladoId = abonosDAO.removeAbono(abono1.getId());

		Assert.assertEquals(abono1.getId(), anuladoId);

		Abono abono = abonosDAO.getAbono(anuladoId, usuarioConSala1.getUsuario());

		Assert.assertNull(abono);
    }

    @Test
    public void updateEvento()
    {
		String updateName = "Abono actualizado 1";

		Abono abono = Abono.AbonoDTOToAbono(abono1);
		abono.setNombre(updateName);

		Abono updatedAbono = abonosDAO.updateAbono(abono);

		Assert.assertEquals(abono1.getId(), updatedAbono.getId());
		Assert.assertNotEquals(abono1.getNombre(), updatedAbono.getNombre());
		Assert.assertEquals(updateName, updatedAbono.getNombre());
    }

	@Test
	public void updateCamposActualizablesEvento()
	{
		String updateName = "Abono actualizado 1";

		Abono abono = Abono.AbonoDTOToAbono(abono1);
		abono.setNombre(updateName);

		Abono updatedAbono = abonosDAO.updateCamposActualizablesConAbonadosAbono(abono);

		Assert.assertEquals(abono1.getId(), updatedAbono.getId());
		Assert.assertNotEquals(abono1.getNombre(), updatedAbono.getNombre());
		Assert.assertEquals(updateName, updatedAbono.getNombre());
	}

	@Test
	public void getAbonosUsuarioConAbono1()
	{
		List<Abono> abonos = abonosDAO.getAbonos(AbonosDAOTest.SORT, AbonosDAOTest.START, AbonosDAOTest.LIMIT, usuarioConSala1.getUsuario());

		Assert.assertEquals(1, abonos.size());
		Assert.assertEquals(abono1.getId(), abonos.get(0).getId());

		int totalAbonos = abonosDAO.getTotalAbonos(usuarioConSala1.getUsuario());

		Assert.assertEquals(1, totalAbonos);
		Assert.assertEquals(abonos.size(), totalAbonos);
	}
}
