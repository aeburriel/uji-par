package es.uji.apps.par.services.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.db.CompraDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class ComprasDAOTest extends BaseDAOTest
{
    @Autowired
    ComprasDAO comprasDAO;

    @Before
    public void before()
    {
    }

    @Test
    @Transactional
    public void guardaCompraOk()
    {
        CompraDTO compraDTO = comprasDAO.guardaCompra("Pepe", "Perez", "964123456", "pepe@example.com");

        assertNotNull(compraDTO.getId());
    }
}
