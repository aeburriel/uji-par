package es.uji.apps.par.services.dao;

import com.mysema.query.Tuple;
import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.ClientesDAO;
import es.uji.apps.par.db.*;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = {"/applicationContext-db-test.xml"})
@Transactional
public class ClientesDAOTest extends BaseDAOTest
{
    private static final String SORT = "[{\"property\":\"nombre\", \"direction\":\"ASC\"}]";
    private static final int START = 0;
    private static final int LIMIT = 100;

    @Autowired
    ClientesDAO clientesDAO;

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
                .withCompra("Luis", BigDecimal.valueOf(25.0), 4)
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
                .withCompra("Rafa", BigDecimal.valueOf(10.0), 2)
                .withCompra("Miguel", BigDecimal.valueOf(10.0), 3)
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
    public void getClientes()
    {
        List<Tuple> clientes = clientesDAO.getClientes(ClientesDAOTest.SORT, ClientesDAOTest.START, ClientesDAOTest.LIMIT, usuarioConSala1.getUsuario());

        int totalClientes = clientesDAO.getTotalClientes(usuarioConSala1.getUsuario());

        assertNotNull(clientes);
        assertEquals(3, clientes.size());
        assertEquals(totalClientes, clientes.size());
    }
}
