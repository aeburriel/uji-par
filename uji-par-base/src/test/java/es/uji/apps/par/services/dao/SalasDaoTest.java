package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Usuario;
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
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class SalasDaoTest extends BaseDAOTest
{
    @Autowired
    UsuariosDAO usuariosDAO;

    @Autowired
    SalasDAO salasDao;

    @Autowired
    CinesDAO cinesDao;

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuarioConSala1;
    UsuarioDTO usuarioConSala2;

    @Before
    public void setUp()
    {
        //Usuario1 -> Sala1

        //Usuario2 -> Sala2

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        SalaDTO sala1 = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        usuarioConSala1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .withSala(sala1)
                .build(entityManager);


        CineDTO cine2 = new CineBuilder("Cine 2")
                .build(entityManager);

        SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
                .build(entityManager);

        usuarioConSala2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
                .withSala(sala2)
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    public void getSalas()
    {
        List<Sala> salas = salasDao.getSalas(usuarioConSala1.getUsuario());

        Assert.assertEquals(1, salas.size());
    }

    @Test
    @Transactional
    public void insertaUna()
    {
        Cine cine = new Cine("a", "cine 1", "12345678F", "Real nº 1", "1", "2", "12000", "AB SL", "123", "964123456",
                new BigDecimal(21));
        Sala sala = new Sala("b", "sala 1", 4, 3, 2, "asd", "qwe", "subtitulado", cine);
        Usuario usuario = new Usuario(usuarioConSala1);

        cinesDao.addCine(cine);
        salasDao.addSala(sala);
        usuariosDAO.addSalaUsuario(sala, usuario);

        List<Sala> salas = salasDao.getSalas(usuarioConSala1.getUsuario());

        Assert.assertEquals(2, salas.size());
    }
}
