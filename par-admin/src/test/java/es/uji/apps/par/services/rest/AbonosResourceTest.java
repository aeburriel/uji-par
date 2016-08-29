package es.uji.apps.par.services.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.model.*;
import org.junit.Assert;
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

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
@Transactional
public class AbonosResourceTest extends BaseResourceTest
{
    @Autowired
    SalasDAO salasDAO;

    @Autowired
    CinesDAO cinesDAO;

    @Autowired
    PlantillasDAO plantillasDAO;

    @Autowired
    SesionesDAO sesionesDAO;

    @Autowired
    UsuariosDAO usuariosDAO;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    private WebResource resource;

    public AbonosResourceTest()
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

    public Cine getCine(){
        Cine cine = new Cine();
        cine.setNombre("Prueba");
        cine = cinesDAO.addCine(cine);

        return cine;
    }

    public Tpv getTpv(Cine cine){
        Tpv tpv = new Tpv();
        tpv.setNombre("tpv");

        tpv.setId(tpvsDAO.addTpv(tpv, cine.getId()).getId());

        return tpv;
    }

    private Sala getSala(Cine cine) {
        Sala sala = new Sala();
        sala.setAsientos(10);
        sala.setAsientosDiscapacitados(2);
        sala.setAsientosNoReservados(3);
        sala.setCine(cine);
        sala = salasDAO.addSala(sala);

        return sala;
    }

    private Usuario getUsuario(Sala sala) {
        Usuario usuario = new Usuario();
        usuario.setUsuario("usuario");
        usuario.setNombre("Usuario");
        usuario.setMail("mail@text.com");

        usuario = usuariosDAO.addUser(usuario);
        usuariosDAO.addSalaUsuario(sala, usuario);

        return usuario;
    }

    private Plantilla getPlantilla(Cine cine)
    {
        Plantilla plantilla = new Plantilla("Prueba");
        plantilla.setSala(getSala(cine));

        return plantillasDAO.add(plantilla);
    }

    private Evento getEvento(Cine cine)
    {
        Evento evento = new Evento();
        evento.setCine(cine);
        evento.setTituloEs("Titulo");
        evento.setTituloVa("Titol");
        evento.setParTpv(getTpv(cine));

        return eventosDAO.addEvento(evento);
    }

    private Sesion getSesion(Plantilla plantilla, Cine cine, Usuario usuario) {
        Sesion sesion = new Sesion();
        sesion.setFechaCelebracion("01/01/2012");
        sesion.setFechaInicioVentaOnline("01/01/2012");
        sesion.setFechaFinVentaOnline("01/01/2012");
        sesion.setHoraCelebracion("12:30");
        sesion.setHoraInicioVentaOnline("12:00");
        sesion.setHoraFinVentaOnline("12:30");
        sesion.setEvento(getEvento(cine));

        sesion.setPlantillaPrecios(plantilla);
        if (plantilla != null) {
            sesion.setSala(plantilla.getSala());
        }

        return sesionesDAO.addSesion(sesion, usuario.getUsuario());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
    {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void addAbono()
    {
        Cine cine = getCine();
        Plantilla plantilla = getPlantilla(cine);
        Usuario usuario = getUsuario(plantilla.getSala());
        Sesion sesion = getSesion(plantilla, cine, usuario);

        Abono abono = new Abono();
        abono.setNombre("Abono 2");
        abono.setPlantillaPrecios(plantilla);
        abono.setSesiones("[{sesion:{id:" + sesion.getId() + "}}]");

        ClientResponse response = resource.path("abono").type("application/json").post(ClientResponse.class, abono);

        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertTrue(restResponse.getSuccess());
    }

    @Test
    public void addAbonoSinSesiones()
    {
        Cine cine = getCine();
        Plantilla plantilla = getPlantilla(cine);

        Abono abono = new Abono();
        abono.setNombre("Abono 2");
        abono.setPlantillaPrecios(plantilla);

        ClientResponse response = resource.path("abono").type("application/json").post(ClientResponse.class, abono);

        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertFalse(restResponse.getSuccess());
    }
}
