package es.uji.apps.par.services.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.TipoEvento;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class SesionesDAOTest
{

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @Autowired
    SesionesDAO sesionesDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    @Before
    public void setup()
    {
        tpvsDAO.addTpvDefault();
    }

    private Sesion preparaSesion()
    {
    	TipoEvento tipoEvento = new TipoEvento("tipo evento");
    	tipoEvento = tiposEventosDAO.addTipoEvento(tipoEvento);
    	
    	Evento evento = new Evento("Evento", tipoEvento);
    	evento = eventosDAO.addEvento(evento);
    	
        Sesion parSesion = new Sesion();
        parSesion.setEvento(evento);
        parSesion.setFechaCelebracion("01/01/2012");

        return parSesion;
    }

    @Test
    @Transactional
    public void addSesionAndDeleteIt()
    {
        Sesion parSesion = preparaSesion();
        Assert.assertEquals(0, parSesion.getId());
        sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesion.getId());
        Assert.assertNotSame(0, parSesion.getId());
    }

    @Test
    @Transactional
    public void addSesionWithoutFechaCelebracion()
    {
        Sesion parSesion = preparaSesion();
        parSesion.setFechaCelebracionWithDate(null);
        SesionDTO parSesionDTO = sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesionDTO.getId());
    }

    @Test
    @Transactional
    public void addSesionWithFechaEndVentaAnteriorFechaStartVenta()
    {
        Sesion parSesion = preparaSesion();
        parSesion.setFechaInicioVentaOnline("01/01/2012");
        parSesion.setFechaFinVentaOnline("01/01/2011");
        SesionDTO parSesionDTO = sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesionDTO.getId());
    }

    @Test
    @Transactional
    public void addSesionWithFechaEndVentaPosteriorFechaCelebracion()
    {
        Sesion parSesion = preparaSesion();
        parSesion.setFechaInicioVentaOnline("01/01/2012");
        parSesion.setFechaFinVentaOnline("02/01/2012");
        SesionDTO parSesionDTO = sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesionDTO.getId());
    }

    @Test
    @Transactional
    public void addSesionWithFechaStartVentaPosteriorFechaCelebracion()
    {
        Sesion parSesion = preparaSesion();
        parSesion.setFechaInicioVentaOnline("02/01/2012");
        SesionDTO parSesionDTO = sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesionDTO.getId());
    }

    @Test
    @Transactional
    public void addSesionWithoutHoraCelebracion()
    {
        Sesion parSesion = preparaSesion();
        SesionDTO parSesionDTO = sesionesDAO.persistSesion(Sesion.SesionToSesionDTO(parSesion));
        Assert.assertNotNull(parSesionDTO.getId());
    }
}
