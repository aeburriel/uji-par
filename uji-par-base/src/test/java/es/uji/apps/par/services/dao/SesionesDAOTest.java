package es.uji.apps.par.services.dao;

import com.mysema.query.Tuple;
import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.model.Tpv;
import es.uji.apps.par.utils.Pair;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class SesionesDAOTest
{
    private static final String SORT = "";
    private static final int START = 0;
    private static final int LIMIT = 100;

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

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuarioConCine1;
    UsuarioDTO usuarioConCine2;
    SalaDTO sala1;
    EventoDTO evento1;
    EventoDTO evento2;
    EventoDTO evento3;
    EventoDTO evento4;
    SesionDTO sesion1;
    SesionDTO sesion2;
    SesionDTO sesion5;
    SesionDTO sesion7;
    final String RSS_ID = "100";
    final String RSS_ID_SESION = "200";

    @Before
    public void setUp()
    {
        //Usuario1 -> Cine1 -> Evento1 -> Sesion1 (Sala1)
        //   "          "   ->    "    -> Sesion2 (Sala1)
        //   "          "   -> Evento2

        //Usuario2 -> Cine2 -> Evento3 -> Sesion3 (Sala2)
        //   -     ->   "   ->    "    -> Sesion4 (Sala2)
        //   -     ->   "   ->    "    -> Sesion5 (Sala2)

        //   -     ->  null -> Evento4 -> Sesion6 (Sala3)
        //   -     ->  null -> Evento4 -> Sesion7 (Sala3)

        TipoEventoDTO tipoEvento = new TipoEventoBuilder("tipo", "tipo", false)
                .build(entityManager);

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        sala1 = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        evento1 = new EventoBuilder("Evento 1", "Esdeveniment 1", cine1, tipoEvento)
                .withRssId(RSS_ID)
                .build(entityManager);

        sesion1 = new SesionBuilder("Sesión 1", sala1, evento1)
                .withRssId(RSS_ID_SESION)
                .withCompra("Juan", BigDecimal.valueOf(25.0), 4)
                .build(entityManager);

        sesion2 = new SesionBuilder("Sesión 2", sala1, evento1)
                .withCompra("Juan", BigDecimal.valueOf(10.0), 2)
                .withCompra("Pepe", BigDecimal.valueOf(10.0), 3)
                .build(entityManager);

        evento2 = new EventoBuilder("Evento 2", "Esdeveniment 2", cine1, tipoEvento)
                .build(entityManager);

        usuarioConCine1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .withSala(sala1)
                .build(entityManager);



        CineDTO cine2 = new CineBuilder("Cine 2")
                .build(entityManager);

        SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
                .build(entityManager);

        evento3 = new EventoBuilder("Evento 3", "Esdeveniment 3", cine2, tipoEvento)
                .withSesion("Sesión 3", sala2)
                .withSesion("Sesión 4", sala2)
                .build(entityManager);

        sesion5 = new SesionBuilder("Sesión 5", sala2, evento3)
                .build(entityManager);

        usuarioConCine2 = new UsuarioBuilder("User 2", "user2@test.com", "user2")
                .withSala(sala2)
                .build(entityManager);


        SalaDTO sala3 = new SalaBuilder("Sala 3", cine2)
                .build(entityManager);

        evento4 = new EventoBuilder("Evento 4", "Esdeveniment 4", null, tipoEvento)
                .withSesion("Sesión 6", sala3)
                .build(entityManager);

        sesion7 = new SesionBuilder("Sesión 7", sala3, evento4)
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

    private Sesion preparaSesion()
    {
    	TipoEvento tipoEvento = new TipoEvento("tipo evento");
    	tipoEvento = tiposEventosDAO.addTipoEvento(tipoEvento);

		Tpv tpv = new Tpv();
		tpv.setNombre("Test TPV");
		if (tpvsDAO.getTpvDefault() == null)
			tpvsDAO.addTpv(tpv, true);

		TpvsDTO tpvDefault = tpvsDAO.getTpvDefault();
		tpv.setId(tpvDefault.getId());
    	Evento evento = new Evento("Evento", tipoEvento);
		evento.setParTpv(tpv);
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

    @Test
    @Transactional
    public void getSesiones()
    {
        List<SesionDTO> sesionesEvento1 = sesionesDAO.getSesiones(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 2);

        List<SesionDTO> sesionesEvento2 = sesionesDAO.getSesiones(evento2.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento2);
        Assert.assertTrue(sesionesEvento2.size() == 0);

        List<SesionDTO> sesionesEvento3 = sesionesDAO.getSesiones(evento3.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento3);
        Assert.assertTrue(sesionesEvento3.size() == 3);
    }

    @Test
    @Transactional
    public void getSesionesUsuariosSinSalaCorrespondiente()
    {
        List<SesionDTO> sesionesEvento1 = sesionesDAO.getSesiones(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 0);

        List<SesionDTO> sesionesEvento2 = sesionesDAO.getSesiones(evento2.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento2);
        Assert.assertTrue(sesionesEvento2.size() == 0);

        List<SesionDTO> sesionesEvento3 = sesionesDAO.getSesiones(evento3.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento3);
        Assert.assertTrue(sesionesEvento3.size() == 0);

        List<SesionDTO> sesionesEvento4 = sesionesDAO.getSesiones(evento4.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento4);
        Assert.assertTrue(sesionesEvento4.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionesPorFechas()
    {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        List<SesionDTO> sesionesICAAPorFechasUsuarioConCine1 = sesionesDAO.getSesionesPorFechas(yesterday, today, SesionesDAOTest.SORT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesICAAPorFechasUsuarioConCine1);
        Assert.assertTrue(sesionesICAAPorFechasUsuarioConCine1.size() == 2);

        List<SesionDTO> sesionesICAAPorFechasUsuarioConCine2 = sesionesDAO.getSesionesPorFechas(yesterday, today, SesionesDAOTest.SORT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesICAAPorFechasUsuarioConCine2);
        Assert.assertTrue(sesionesICAAPorFechasUsuarioConCine2.size() == 3);
    }

    @Test
    @Transactional
    public void getSesionesConIds()
    {
        List<Long> sesiones = new ArrayList<>();
        sesiones.add(sesion1.getId());
        sesiones.add(sesion2.getId());
        sesiones.add(sesion5.getId());
        sesiones.add(sesion7.getId());

        List<SesionDTO> sesionesUsuarioConCine1 = sesionesDAO.getSesiones(sesiones, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesUsuarioConCine1);
        Assert.assertTrue(sesionesUsuarioConCine1.size() == 2);

        List<SesionDTO> sesionesUsuarioConCine2 = sesionesDAO.getSesiones(sesiones, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesUsuarioConCine2);
        Assert.assertTrue(sesionesUsuarioConCine2.size() == 1);
    }

    @Test
    @Transactional
    public void getSesionesActivas()
    {
        List<SesionDTO> sesionesEvento1 = sesionesDAO.getSesionesActivas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 2);

        List<SesionDTO> sesionesEvento2 = sesionesDAO.getSesionesActivas(evento2.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento2);
        Assert.assertTrue(sesionesEvento2.size() == 0);

        List<SesionDTO> sesionesEvento3 = sesionesDAO.getSesionesActivas(evento3.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento3);
        Assert.assertTrue(sesionesEvento3.size() == 3);
    }

    @Test
    @Transactional
    public void getSesionesActivasUsuariosSinSalaCorrespondiente()
    {
        List<SesionDTO> sesionesEvento1 = sesionesDAO.getSesionesActivas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 0);

        List<SesionDTO> sesionesEvento2 = sesionesDAO.getSesionesActivas(evento2.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento2);
        Assert.assertTrue(sesionesEvento2.size() == 0);

        List<SesionDTO> sesionesEvento3 = sesionesDAO.getSesionesActivas(evento3.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento3);
        Assert.assertTrue(sesionesEvento3.size() == 0);

        List<SesionDTO> sesionesEvento4 = sesionesDAO.getSesionesActivas(evento4.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento4);
        Assert.assertTrue(sesionesEvento4.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionesPorRSSId()
    {
        List<SesionDTO> sesionesEventoRSS = sesionesDAO.getSesionesPorRssId(RSS_ID, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEventoRSS);
        Assert.assertTrue(sesionesEventoRSS.size() == 2);
        Assert.assertTrue(sesionesEventoRSS.contains(sesion1));
        Assert.assertTrue(sesionesEventoRSS.contains(sesion2));
    }

    @Test
    @Transactional
    public void getSesionesPorRSSIdUsuarioSinSalaCorrespondiente()
    {
        List<SesionDTO> sesionesEventoRSS = sesionesDAO.getSesionesPorRssId(RSS_ID, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEventoRSS);
        Assert.assertTrue(sesionesEventoRSS.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionPorRSSId()
    {
        Sesion sesionByRssId = sesionesDAO.getSesionByRssId(RSS_ID_SESION, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionByRssId);
        Assert.assertTrue(sesionByRssId.getId() == sesion1.getId());
    }

    @Test
    @Transactional
    public void getSesionPorRSSIdUsuarioSinSalaCorrespondiente()
    {
        Sesion sesionByRssId = sesionesDAO.getSesionByRssId(RSS_ID_SESION, usuarioConCine2.getUsuario());

        Assert.assertNull(sesionByRssId);
    }

    @Test
    @Transactional
    public void getSesionesConVentas()
    {
        List<Tuple> sesionesEvento1 = sesionesDAO.getSesionesConButacasVendidas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 2);

        SesionDTO sesion = sesionesEvento1.get(0).get(0, SesionDTO.class);
        if (sesion.getId() == sesion1.getId())
        {
            Assert.assertTrue(sesionesEvento1.get(0).get(1, Long.class) == 4);
            Assert.assertTrue(sesionesEvento1.get(1).get(1, Long.class) == 5);
        }
        else
        {
            Assert.assertTrue(sesionesEvento1.get(0).get(1, Long.class) == 5);
            Assert.assertTrue(sesionesEvento1.get(1).get(1, Long.class) == 4);
        }
    }

    @Test
    @Transactional
    public void getSesionesConVentasUsuarioSinSalaCorrespondiente()
    {
        List<Tuple> sesionesEvento1 = sesionesDAO.getSesionesConButacasVendidas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionesActivasConVentas()
    {
        List<Tuple> sesionesEvento1 = sesionesDAO.getSesionesActivasConButacasVendidas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 2);

        SesionDTO sesion = sesionesEvento1.get(0).get(0, SesionDTO.class);
        if (sesion.getId() == sesion1.getId())
        {
            Assert.assertTrue(sesionesEvento1.get(0).get(1, Long.class) == 4);
            Assert.assertTrue(sesionesEvento1.get(1).get(1, Long.class) == 5);
        }
        else
        {
            Assert.assertTrue(sesionesEvento1.get(0).get(1, Long.class) == 5);
            Assert.assertTrue(sesionesEvento1.get(1).get(1, Long.class) == 4);
        }
    }

    @Test
    @Transactional
    public void getSesionesActivasConVentasUsuarioSinSalaCorrespondiente()
    {
        List<Tuple> sesionesEvento1 = sesionesDAO.getSesionesActivasConButacasVendidas(evento1.getId(), SesionesDAOTest.SORT, SesionesDAOTest.START, SesionesDAOTest.LIMIT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesEvento1);
        Assert.assertTrue(sesionesEvento1.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionesSala()
    {
        List<SesionDTO> sesionesPorSala1 = sesionesDAO.getSesionesPorSala(evento1.getId(), sala1.getId(), SesionesDAOTest.SORT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesPorSala1);
        Assert.assertTrue(sesionesPorSala1.size() == 2);
    }

    @Test
    @Transactional
    public void getSesionesSalaUsuarioSinSalaCorrespondiente()
    {
        List<SesionDTO> sesionesPorSala1 = sesionesDAO.getSesionesPorSala(evento1.getId(), sala1.getId(), SesionesDAOTest.SORT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesPorSala1);
        Assert.assertTrue(sesionesPorSala1.size() == 0);
    }

    @Test
    @Transactional
    public void getSesion()
    {
        SesionDTO sesion = sesionesDAO.getSesion(sesion1.getId(), usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesion);
        Assert.assertTrue(sesion.getId() == sesion1.getId());

        sesion = sesionesDAO.getSesion(sesion2.getId(), usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesion);
        Assert.assertTrue(sesion.getId() == sesion2.getId());
    }

    @Test
    @Transactional
    public void getSesionUsuarioSinSalaCorrespondiente()
    {
        SesionDTO sesion = sesionesDAO.getSesion(sesion1.getId(), usuarioConCine2.getUsuario());

        Assert.assertNull(sesion);

        sesion = sesionesDAO.getSesion(sesion2.getId(), usuarioConCine2.getUsuario());

        Assert.assertNull(sesion);
    }

    @Test
    @Transactional
    public void getTotalSesiones()
    {
        int totalSesionesActivas = sesionesDAO.getTotalSesionesActivas(evento1.getId(), usuarioConCine1.getUsuario());

        Assert.assertTrue(totalSesionesActivas == 2);

        int totalSesiones = sesionesDAO.getTotalSesiones(evento1.getId(), usuarioConCine1.getUsuario());

        Assert.assertTrue(totalSesiones == 2);
    }

    @Test
    @Transactional
    public void getTotalSesionesUsuarioSinSalaCorrespondiente()
    {
        int totalSesionesActivas = sesionesDAO.getTotalSesionesActivas(evento1.getId(), usuarioConCine2.getUsuario());

        Assert.assertTrue(totalSesionesActivas == 0);

        int totalSesiones = sesionesDAO.getTotalSesiones(evento1.getId(), usuarioConCine2.getUsuario());

        Assert.assertTrue(totalSesiones == 0);
    }

    @Test
    @Transactional
    public void getSesionesOrdenadas()
    {
        List<Sesion> sesiones = new ArrayList<>();
        sesiones.add(Sesion.SesionDTOToSesion(sesion2));
        sesiones.add(Sesion.SesionDTOToSesion(sesion1));

        List<SesionDTO> sesionesOrdenadas = sesionesDAO.getSesionesOrdenadas(sesiones, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesOrdenadas);
        Assert.assertTrue(sesionesOrdenadas.size() == 2);
        Assert.assertTrue(sesionesOrdenadas.get(0).getId() == sesion1.getId());
        Assert.assertTrue(sesionesOrdenadas.get(1).getId() == sesion2.getId());
    }

    @Test
    @Transactional
    public void getSesionesOrdenadasUsuarioSinSalaCorrespondiente()
    {
        List<Sesion> sesiones = new ArrayList<>();
        sesiones.add(Sesion.SesionDTOToSesion(sesion2));
        sesiones.add(Sesion.SesionDTOToSesion(sesion1));

        List<SesionDTO> sesionesOrdenadas = sesionesDAO.getSesionesOrdenadas(sesiones, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesOrdenadas);
        Assert.assertTrue(sesionesOrdenadas.size() == 0);
    }

    @Test
    @Transactional
    public void getSesionesCinePorFechas()
    {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        List<SesionDTO> sesionesCinePorFechasUsuarioCine1 = sesionesDAO.getSesionesCinePorFechas(yesterday, today, SesionesDAOTest.SORT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesCinePorFechasUsuarioCine1);
        Assert.assertTrue(sesionesCinePorFechasUsuarioCine1.size() == 2);

        List<SesionDTO> sesionesCinePorFechasUsuarioCine2 = sesionesDAO.getSesionesCinePorFechas(yesterday, today, SesionesDAOTest.SORT, usuarioConCine2.getUsuario());

        Assert.assertNotNull(sesionesCinePorFechasUsuarioCine2);
        Assert.assertTrue(sesionesCinePorFechasUsuarioCine2.size() == 3);
    }

    @Test
    @Transactional
    public void getSesionesICAAPorFechas()
    {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        List<SesionDTO> sesionesICAAPorFechas = sesionesDAO.getSesionesICAAPorFechas(yesterday, today, SesionesDAOTest.SORT, usuarioConCine1.getUsuario());

        Assert.assertNotNull(sesionesICAAPorFechas);
        Assert.assertTrue(sesionesICAAPorFechas.size() == 0);
    }

    @Test
    @Transactional
    public void getCantidadSesionesMismaFechaYLocalizacion()
    {
        Pair cantidadSesionesMismaFechaYLocalizacion = sesionesDAO.getCantidadSesionesMismaFechaYLocalizacion(new Timestamp(new Date().getTime()), sala1.getId(), evento1.getId(), usuarioConCine1.getUsuario());

        Assert.assertNotNull(cantidadSesionesMismaFechaYLocalizacion);
        Assert.assertTrue(cantidadSesionesMismaFechaYLocalizacion.getFirst() == 0);
        Assert.assertTrue(cantidadSesionesMismaFechaYLocalizacion.getSecond() == 0);
    }

    @Test
    @Transactional
    public void getCantidadSesionesMismaFechaYLocalizacionUsuarioSinSalaCorrespondiente()
    {
        Pair cantidadSesionesMismaFechaYLocalizacion = sesionesDAO.getCantidadSesionesMismaFechaYLocalizacion(new Timestamp(new Date().getTime()), sala1.getId(), evento1.getId(), usuarioConCine2.getUsuario());

        Assert.assertNotNull(cantidadSesionesMismaFechaYLocalizacion);
        Assert.assertTrue(cantidadSesionesMismaFechaYLocalizacion.getFirst() == 0);
        Assert.assertTrue(cantidadSesionesMismaFechaYLocalizacion.getSecond() == 0);
    }

    @Test
    @Transactional
    public void isSesionReprogramada()
    {
        boolean sesionReprogramada = sesionesDAO.isSesionReprogramada(new Timestamp(new Date().getTime()), sala1.getId(), evento1.getId(), usuarioConCine1.getUsuario());

        Assert.assertFalse(sesionReprogramada);
    }

    @Test
    @Transactional
    public void isSesionReprogramadaUsuarioSinSalaCorrespondiente()
    {
        boolean sesionReprogramada = sesionesDAO.isSesionReprogramada(new Timestamp(new Date().getTime()), sala1.getId(), evento1.getId(), usuarioConCine2.getUsuario());

        Assert.assertFalse(sesionReprogramada);
    }

    @Test
    @Transactional
    public void getTotalPreciosSesion()
    {
        int totalPreciosSesion = sesionesDAO.getTotalPreciosSesion(sesion1.getId());

        Assert.assertTrue(totalPreciosSesion == 0);
    }
}
