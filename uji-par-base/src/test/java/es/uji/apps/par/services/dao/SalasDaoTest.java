package es.uji.apps.par.services.dao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Sala;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class SalasDaoTest extends BaseDAOTest
{
    @Autowired
    SalasDAO salasDao;

    @Autowired
    CinesDAO cinesDao;

    @Before
    public void before()
    {
    }

    @Test
    @Transactional
    public void getSalas()
    {
        List<Sala> salas = salasDao.getSalas();

        Assert.assertEquals(0, salas.size());
    }

    @Test
    @Transactional
    public void insertaUna()
    {
        Cine cine = new Cine("a", "cine 1", "12345678F", "Real nº 1", "1", "2", "12000", "AB SL", "123", "964123456",
                new BigDecimal(21));
        Sala sala = new Sala("b", "sala 1", 4, 3, 2, "asd", "qwe", "subtitulado", cine);

        cinesDao.addCine(cine);
        salasDao.addSala(sala);

        List<Sala> salas = salasDao.getSalas();

        Assert.assertEquals(1, salas.size());
        Assert.assertTrue(salas.get(0).getId() != 0);
        Assert.assertEquals("sala 1", salas.get(0).getNombre());
    }

}