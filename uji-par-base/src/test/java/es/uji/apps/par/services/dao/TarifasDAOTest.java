package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.CineBuilder;
import es.uji.apps.par.builders.SalaBuilder;
import es.uji.apps.par.builders.TarifaBuilder;
import es.uji.apps.par.builders.UsuarioBuilder;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.SalaDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.db.UsuarioDTO;
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
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class TarifasDAOTest
{
	private static final String SORT = "";
	private static final int START = 0;
	private static final int LIMIT = 100;

	@Autowired
    TarifasDAO tarifasDAO;

    @Autowired
    SalasDAO salasDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuarioConCine1;
    UsuarioDTO usuarioConCine2;
    TarifaDTO tarifa1;

    @Before
    public void setUp()
    {
        //Usuario1 -> Cine1 -> Sala1 -> Tarifa1

        //Usuario2 -> Cine2 -> Sala2 -> Tarifa2

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        SalaDTO sala1 = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        tarifa1 = new TarifaBuilder("Tarifa 1", cine1).build(entityManager);

        usuarioConCine1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .withSala(sala1)
                .build(entityManager);


        CineDTO cine2 = new CineBuilder("Cine 2")
                .build(entityManager);

        SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
                .build(entityManager);

        new TarifaBuilder("Tarifa 2", cine2)
                .build(entityManager);

        usuarioConCine2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
                .withSala(sala2)
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

	@Test
    @Transactional
    public void getTarifasUsuarioConCine1()
    {
        List<TarifaDTO> tarifas = tarifasDAO.getAll(TarifasDAOTest.SORT, TarifasDAOTest.START, TarifasDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(tarifas);
        Assert.assertTrue(tarifas.size() == 1);
        Assert.assertTrue(tarifas.get(0).getNombre().equals("Tarifa 1"));
    }

    @Test
    @Transactional
    public void getTarifasUsuarioConCine2()
    {
        List<TarifaDTO> tarifas = tarifasDAO.getAll(TarifasDAOTest.SORT, TarifasDAOTest.START, TarifasDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(tarifas);
        Assert.assertTrue(tarifas.size() == 1);
        Assert.assertTrue(tarifas.get(0).getNombre().equals("Tarifa 2"));
    }

    @Test
    @Transactional
    public void getTarifa1UsuarioConCine1()
    {
        TarifaDTO tarifaDTO = tarifasDAO.get(Long.valueOf(tarifa1.getId()).intValue(), usuarioConCine1.getUsuario());

        Assert.assertNotNull(tarifaDTO);
        Assert.assertTrue(tarifaDTO.getId() == tarifa1.getId());
        Assert.assertTrue(tarifaDTO.getNombre().equals(tarifa1.getNombre()));
    }

    @Test
    @Transactional
    public void getTarifa1UsuarioConCine2()
    {
        TarifaDTO tarifaDTO = tarifasDAO.get(Long.valueOf(tarifa1.getId()).intValue(), usuarioConCine2.getUsuario());

        Assert.assertNull(tarifaDTO);
    }
}
