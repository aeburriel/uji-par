package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.TipoEvento;
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
@Transactional
public class TiposEventosDAOTest
{
	private static final String SORT = "";
	private static final int START = 0;
	private static final int LIMIT = 100;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuarioConSala1;
    UsuarioDTO usuarioConSala2;
    TipoEventoDTO tipoEvento1;

    @Before
    public void setUp()
    {
        //Usuario1 -> Cine1 -> TipoEvento1

        //Usuario2 -> Cine2 -> TipoEvento2

        //   -     ->  null -> TipoEvento3

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        tipoEvento1 = new TipoEventoBuilder("tipo1", "tipo1", false, cine1)
                .build(entityManager);

        SalaDTO sala1 = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        usuarioConSala1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .withSala(sala1)
                .build(entityManager);

        CineDTO cine2 = new CineBuilder("Cine 2")
                .build(entityManager);

        new TipoEventoBuilder("tipo2", "tipo2", false, cine2)
                .build(entityManager);

        SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
                .build(entityManager);

        usuarioConSala2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
                .withSala(sala2)
                .build(entityManager);

        new TipoEventoBuilder("tipo3", "tipo3", false, null)
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void addTipoEvento()
    {
        TipoEvento parTipoEvento = preparaTipoEvento();
        TipoEvento tipoEvento = tiposEventosDAO.addTipoEvento(parTipoEvento);

        Assert.assertNotNull(tipoEvento.getId());
    }

    private TipoEvento preparaTipoEvento()
    {
        return new TipoEvento("NombreTipoEvento");
    }

    @Test
    public void updateTipoEvento()
    {
    	TipoEvento parTipoEvento = preparaTipoEvento();
        parTipoEvento = tiposEventosDAO.addTipoEvento(parTipoEvento);
        
        parTipoEvento.setNombreEs("Prueba2");
        TipoEvento tipoEventoActualizado = tiposEventosDAO.updateTipoEvento(parTipoEvento);
        Assert.assertEquals(parTipoEvento.getId(), tipoEventoActualizado.getId());
    }

    @Test
    public void addTipoEventoConIdiomas()
    {
        TipoEvento parTipoEvento = preparaTipoEvento();
        parTipoEvento.setNombreVa("valencia");
        TipoEvento tipoEvento = tiposEventosDAO.addTipoEvento(parTipoEvento);

        Assert.assertNotNull(tipoEvento.getId());
    }

    @Test
    public void getTiposEventosUsuarioConSala1()
    {
        List<TipoEvento> tiposEventos = tiposEventosDAO.getTiposEventos(TiposEventosDAOTest.SORT, TiposEventosDAOTest.START, TiposEventosDAOTest.LIMIT, usuarioConSala1.getUsuario());

        int totalTipusEventos = tiposEventosDAO.getTotalTipusEventos(usuarioConSala1.getUsuario());

        Assert.assertNotNull(tiposEventos);
        Assert.assertEquals(2, tiposEventos.size());
        Assert.assertEquals(totalTipusEventos, tiposEventos.size());
    }

    @Test
    public void getTipoEventoByNombreEsUsuarioConSala1()
    {
        TipoEventoDTO tipoEvento = tiposEventosDAO.getTipoEventoByNombreEs(tipoEvento1.getNombreEs(), usuarioConSala1.getUsuario());

        Assert.assertNotNull(tipoEvento);
        Assert.assertEquals(tipoEvento1.getNombreEs(), tipoEvento.getNombreEs());
    }

    @Test
    public void getTipoEventoByNombreEsUsuarioConSala2()
    {
        TipoEventoDTO tipoEvento = tiposEventosDAO.getTipoEventoByNombreEs(tipoEvento1.getNombreEs(), usuarioConSala2.getUsuario());

        Assert.assertNull(tipoEvento);
    }

    @Test
    public void getTipoEventoByNombreVaUsuarioConSala1()
    {
        TipoEventoDTO tipoEvento = tiposEventosDAO.getTipoEventoByNombreEs(tipoEvento1.getNombreVa(), usuarioConSala1.getUsuario());

        Assert.assertNotNull(tipoEvento);
        Assert.assertEquals(tipoEvento1.getNombreVa(), tipoEvento.getNombreVa());
    }

    @Test
    public void getTipoEventoByNombreVaUsuarioConSala2()
    {
        TipoEventoDTO tipoEvento = tiposEventosDAO.getTipoEventoByNombreEs(tipoEvento1.getNombreVa(), usuarioConSala2.getUsuario());

        Assert.assertNull(tipoEvento);
    }
}
