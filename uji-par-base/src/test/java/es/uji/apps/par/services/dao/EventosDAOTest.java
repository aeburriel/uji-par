package es.uji.apps.par.services.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.TipoEvento;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class EventosDAOTest
{
	private static final String SORT = "[{\"property\":\"tituloVa\", \"direction\":\"ASC\"}]";
	private static final int START = 0;
	private static final int LIMIT = 100;

    @Autowired
    EventosDAO eventosDAO;
    
    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    @Before
    public void setup()
    {
        tpvsDAO.addTpvDefault();
    }

    @Test
	//TODO dice que usuario no tiene privilegios
	@Ignore
    @Transactional
    public void getEventos()
    {
        Assert.assertNotNull(eventosDAO.getEventos(EventosDAOTest.SORT, EventosDAOTest.START, EventosDAOTest.LIMIT));
    }

    @Test
    @Transactional
    public void addEvento()
    {
        Evento parEvento = preparaEvento();
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
    }

    @Test
    @Transactional
    public void deleteEvento()
    {
    	Evento parEvento = preparaEvento();
    	parEvento = eventosDAO.addEvento(parEvento);
        Assert.assertEquals(1, eventosDAO.removeEvento(parEvento.getId()));
    }

    private Evento preparaEvento()
    {
    	TipoEvento tipoEvento = tiposEventosDAO.addTipoEvento(new TipoEvento("Tipo evento"));
        return new Evento("Nombre", tipoEvento);
    }

    @Test
    @Transactional
    public void updateEvento()
    {
    	Evento parEvento = preparaEvento();
    	parEvento = eventosDAO.addEvento(parEvento);

        parEvento.setTituloEs("Prueba2");
        Evento eventoActualizado = eventosDAO.updateEvento(parEvento);
        Assert.assertEquals(parEvento.getId(), eventoActualizado.getId());
    }

    @Test
    @Transactional
    public void addEventoConIdiomas()
    {
        Evento parEvento = preparaEvento();
        parEvento.setCaracteristicasVa("valencia");
        parEvento.setComentariosEs("comentarios");
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
    }

    @Test
    @Transactional
    public void deleteImagen()
    {
        Evento parEvento = preparaEvento();
        parEvento.setCaracteristicasVa("valencia");
        parEvento.setComentariosEs("comentarios");
        parEvento.setImagen("hola".getBytes());
        parEvento.setImagenSrc("hola");
        parEvento.setImagenContentType("");
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
        eventosDAO.deleteImagen(parEvento.getId());

        /*
         * ParEventoDTO eventoDespuesDeBorrarLaImagen =
         * eventosDAO.getEventoDTO(parEvento.getId()).get(0);
         * Assert.assertNull(eventoDespuesDeBorrarLaImagen.getImagen());
         */
    }
}
