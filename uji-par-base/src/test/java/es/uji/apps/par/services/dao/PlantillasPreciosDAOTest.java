package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.CineBuilder;
import es.uji.apps.par.builders.PlantillaBuilder;
import es.uji.apps.par.builders.SalaBuilder;
import es.uji.apps.par.builders.UsuarioBuilder;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.PlantillaDTO;
import es.uji.apps.par.db.SalaDTO;
import es.uji.apps.par.db.UsuarioDTO;
import es.uji.apps.par.model.Sala;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.PlantillasDAO;
import es.uji.apps.par.model.Plantilla;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class PlantillasPreciosDAOTest {
	private static final String SORT = "";
	private static final int START = 0;
	private static final int LIMIT = 100;

	@Autowired
	PlantillasDAO plantillasPreciosDAO;

    @Autowired
    SalasDAO salasDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuarioConPlantilla1;
    UsuarioDTO usuarioConPlantilla2;
    SalaDTO sala1;

    @Before
    public void setUp()
    {
        //Usuario1 -> Sala1 -> Plantilla1

        //Usuario2 -> Sala2 -> Plantilla2

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        sala1 = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        new PlantillaBuilder("Plantilla 1", sala1)
                .build(entityManager);

        usuarioConPlantilla1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .withSala(sala1)
                .build(entityManager);


        CineDTO cine2 = new CineBuilder("Cine 2")
                .build(entityManager);

        SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
                .build(entityManager);

        new PlantillaBuilder("Plantilla 2", sala2)
                .build(entityManager);

        usuarioConPlantilla2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
                .withSala(sala2)
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

    private Plantilla preparaPlantillaConSala1()
    {
        Plantilla plantilla = new Plantilla("Nombre Localizacion");
        plantilla.setSala(Sala.salaDTOtoSala(sala1));
        return plantilla;
    }
	
	@Test
    @Transactional
    public void getPlantillasPrecios()
    {
        List<PlantillaDTO> plantillas = plantillasPreciosDAO.get(true, PlantillasPreciosDAOTest.SORT, PlantillasPreciosDAOTest.START, PlantillasPreciosDAOTest.LIMIT, usuarioConPlantilla1.getUsuario());

        Assert.assertNotNull(plantillas);
        Assert.assertTrue(plantillas.size() == 1);
    }

    @Test
    @Transactional
    public void addPlantilla()
    {
        Plantilla parPlantillaPrecio = preparaPlantillaConSala1();
        Plantilla plantilla = plantillasPreciosDAO.add(parPlantillaPrecio);

        Assert.assertNotNull(plantilla.getId());

        List<PlantillaDTO> plantillas = plantillasPreciosDAO.get(true, PlantillasPreciosDAOTest.SORT, PlantillasPreciosDAOTest.START, PlantillasPreciosDAOTest.LIMIT, usuarioConPlantilla1.getUsuario());

        Assert.assertNotNull(plantillas);
        Assert.assertTrue(plantillas.size() == 2);
    }

    @Test
    @Transactional
    public void updatePlantillaPrecio()
    {
        Plantilla plantilla = plantillasPreciosDAO.add(preparaPlantillaConSala1());

        Assert.assertNotNull(plantilla.getId());

        plantilla.setNombre("Prueba2");
        Plantilla plantillaActualizada = plantillasPreciosDAO.update(plantilla);
        Assert.assertEquals(plantilla.getId(), plantillaActualizada.getId());
    }
}
