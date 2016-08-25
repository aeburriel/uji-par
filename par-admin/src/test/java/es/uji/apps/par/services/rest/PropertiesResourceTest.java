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
import es.uji.apps.par.model.Cine;
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

import javax.ws.rs.core.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
@Transactional
public class PropertiesResourceTest extends BaseResourceTest
{
    @Autowired
    CinesDAO cinesDAO;

    private WebResource resource;

    public PropertiesResourceTest()
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
        cine.setUrlPublic("https://ejemplo.de.url");
        cine = cinesDAO.addCine(cine);

        return cine;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
    {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void getPublicUrlFromProperties()
    {
        getCine();

        ClientResponse response = resource.path("index/public/properties").get(ClientResponse.class);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertNotNull(restResponse.getData());
        Assert.assertTrue(restResponse.getTotal() > 0);
        Assert.assertEquals(restResponse.getData().get(0), "http://localhost:9003/par-public");
    }
}
