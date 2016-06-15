package es.uji.apps.par.services.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.model.Tpv;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import javax.ws.rs.core.Response.Status;
import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class EventosResourceTest extends BaseResourceTest
{
    private WebResource resource;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    public EventosResourceTest()
    {
        super(
                new WebAppDescriptor.Builder(
                        "es.uji.apps.par.services.rest;com.fasterxml.jackson.jaxrs.json;es.uji.apps.par")
                        .contextParam("contextConfigLocation",
                                "classpath:applicationContext-db-test.xml")
                        .contextParam("webAppRootKey", "paranimf-fw-uji.root")
                        .contextListenerClass(ContextLoaderListener.class)
                        .clientConfig(clientConfiguration())
                        .requestListenerClass(RequestContextListener.class)
                        .servletClass(SpringServlet.class).build());

        this.client().addFilter(new LoggingFilter());
        this.resource = resource();
    }

    private static ClientConfig clientConfiguration()
    {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        return config;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
    {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    @Ignore //Ignorado porque tiene SQL Oracle
    public void getEventos()
    {
        ClientResponse response = resource.path("evento").get(ClientResponse.class);
        RestResponse serviceResponse = response.getEntity(RestResponse.class);

        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertNotNull(serviceResponse.getData());
    }

    private String getFieldFromRestResponse(RestResponse restResponse, String field)
    {
        return ((HashMap) restResponse.getData().get(0)).get(field).toString();
    }

    private TipoEvento addTipoEvento()
    {
        TipoEvento parTipoEvento = new TipoEvento("prueba");
        ClientResponse response = resource.path("tipoevento").type("application/json").post(ClientResponse.class, parTipoEvento);
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        RestResponse serviceResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertNotNull(serviceResponse.getData());
        int id = Integer.valueOf(((HashMap) serviceResponse.getData().get(0)).get("id").toString());
        parTipoEvento.setId(id);
        return parTipoEvento;
    }

    private FormDataMultiPart preparaEvento(Evento evento)
    {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("tituloEs", evento.getTituloEs());
        form.field("tituloVa", evento.getTituloVa());
        form.field("porcentajeIVA", "1.0");
        form.field("retencionSGAE", "2.0");
        form.field("ivaSGAE", "3.0");
        form.field("tipoEvento", String.valueOf(evento.getTipoEvento()));

        return form;
    }

    @Test
    @Transactional
    public void addEvento() {
        Tpv tpv = new Tpv();
        if (tpvsDAO.getTpvDefault() == null) {
            tpv = addTpvDefault();
        }

        TipoEvento parTipoEvento = addTipoEvento();

        Evento evento = new Evento();
        evento.setTituloEs("Nombre ES");
        evento.setTituloVa("Nombre CA");
        evento.setTipoEvento(parTipoEvento.getId());
        evento.setParTpv(tpv);

        evento = eventosDAO.addEvento(evento);

        Assert.assertNotNull(evento.getId());
        Assert.assertNotNull(evento.getParTpv());
    }

    @Test
    @Transactional
    public void addEventoSinTpv() {
        if (tpvsDAO.getTpvDefault() == null) {
            addTpvDefault();
        }

        TipoEvento parTipoEvento = addTipoEvento();

        Evento evento = new Evento();
        evento.setTituloEs("Nombre ES");
        evento.setTituloVa("Nombre CA");
        evento.setTipoEvento(parTipoEvento.getId());

        evento = eventosDAO.addEvento(evento);

        Assert.assertNotNull(evento.getId());
        Assert.assertNotNull(evento.getParTpv());
    }

    private Tpv addTpvDefault() {
        tpvsDAO.addTpvDefault();
        TpvsDTO tpvDto = tpvsDAO.getTpvDefault();
        return new Tpv(tpvDto);
    }

    /*@Test
    @Transactional
    @Ignore
    public void updateEvento()
    {
        if (tpvsDAO.getTpvDefault() == null) {
            addTpvDefault();
        }

        TipoEvento parTipoEvento = addTipoEvento();

        Evento evento = new Evento();
        evento.setTituloEs("Nombre ES");
        evento.setTituloVa("Nombre CA");
        evento.setTipoEvento(parTipoEvento.getId());

        evento = eventosDAO.addEvento(evento);

        long id = evento.getId();

        FormDataMultiPart parEvento = preparaEvento(evento);

        ClientResponse response = resource.path("evento").path(Long.toString(id)).type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, parEvento);
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        RestResponse serviceResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });
        EventoDTO eventoBD = eventosDAO.getEventoById(id);
        List<Evento> eventos = eventosDAO.getEventos("", 0, 1000);
        
        RestResponse restResponse = resource.path("evento").get(RestResponse.class);
        
        Assert.assertEquals(parEvento.getField("porcentajeIVA"), eventoBD.getPorcentajeIva());
    }*/
}
