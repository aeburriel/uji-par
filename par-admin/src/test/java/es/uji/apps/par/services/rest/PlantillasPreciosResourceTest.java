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
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.ResultatOperacio;
import es.uji.apps.par.model.Sala;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import javax.ws.rs.core.Response.Status;
import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class PlantillasPreciosResourceTest extends BaseResourceTest
{
	private WebResource resource;

    @Autowired
    SalasDAO salasDAO;

    @Autowired
    CinesDAO cinesDAO;

	public PlantillasPreciosResourceTest()
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
        this.resource = resource().path("plantillaprecios");
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

    private Plantilla preparaPlantilla()
    {
        Plantilla plantilla = new Plantilla("Prueba");
        plantilla.setSala(getSala());

        return plantilla;
    }

    @Test
    public void getPlantillaPrecios()
    {
        ClientResponse response = resource.get(ClientResponse.class);
        RestResponse serviceResponse = response.getEntity(RestResponse.class);

        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertNotNull(serviceResponse.getData());
    }

    @Test
    public void addPlantillaWithoutNombre()
    {
        Plantilla plantilla = preparaPlantilla();
        plantilla.setNombre(null);

        ClientResponse response = resource.type("application/json").post(ClientResponse.class, plantilla);
        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        ResultatOperacio resultatOperacio = response.getEntity(ResultatOperacio.class);
        Assert.assertEquals(CampoRequeridoException.REQUIRED_FIELD + "Nombre",
                resultatOperacio.getDescripcio());
    }

    @SuppressWarnings("rawtypes")
    private String getFieldFromRestResponse(RestResponse restResponse, String field)
    {
        return ((HashMap) restResponse.getData().get(0)).get(field).toString();
    }

    private Sala getSala() {
        Cine cine = getCine();

        Sala sala = new Sala();
        sala.setAsientos(10);
        sala.setAsientosDiscapacitados(2);
        sala.setAsientosNoReservados(3);
        sala.setCine(cine);
        sala = salasDAO.addSala(sala);

        return sala;
    }

    public Cine getCine(){
        Cine cine = new Cine();
        cine.setNombre("Prueba");
        cine = cinesDAO.addCine(cine);

        return cine;
    }

    @Test
	@Ignore
    public void addPlantilla()
    {
        Plantilla plantillaPrecios = preparaPlantilla();

        ClientResponse response = resource.type("application/json").post(ClientResponse.class, plantillaPrecios);
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });
        Assert.assertTrue(restResponse.getSuccess());
        Assert.assertNotNull(getFieldFromRestResponse(restResponse, "id"));
        Assert.assertEquals(plantillaPrecios.getNombre(),
                getFieldFromRestResponse(restResponse, "nombre"));
    }

    @Test
    @Ignore
    public void updatePlantilla()
    {
        Plantilla plantilla = preparaPlantilla();
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, plantilla);
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        String id = getFieldFromRestResponse(restResponse, "id");
        Assert.assertNotNull(id);

        plantilla.setNombre("Prueba2");

        response = resource.path(String.valueOf(plantilla.getId())).type("application/json").put(ClientResponse.class, plantilla);
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertEquals(plantilla.getNombre(),
                getFieldFromRestResponse(restResponse, "nombre"));
    }

    @Test
	@Ignore
    public void updatePlantillaAndRemoveNombre()
    {
        Plantilla plantilla = preparaPlantilla();
        ClientResponse response = resource.type("application/json").post(ClientResponse.class, plantilla);
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        String id = getFieldFromRestResponse(restResponse, "id");
        Assert.assertNotNull(id);

        plantilla.setNombre("");
        response = resource.path(id).type("application/json").put(ClientResponse.class, plantilla);
        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        ResultatOperacio parResponseMessage = response.getEntity(ResultatOperacio.class);
        Assert.assertEquals(CampoRequeridoException.REQUIRED_FIELD + "Nombre",
                parResponseMessage.getDescripcio());
    }
}
