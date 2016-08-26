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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import javax.ws.rs.core.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@ContextConfiguration(locations = { "/applicationContext-db2-test.xml" })
@Transactional
public class DataBasePropertiesResourceTest extends BaseResourceTest
{
    String WEB = "ejemplo.de.url";
    String HTML_TITLE = "CINE NOMBRE";
    String CONDICIONES_URL = "http://example.com/condiciones.html";
    String COMO_LLEGAR_URL = "http://example.com/documento.pdf";
    String PIE_ENTRADA_URL = "http://example.com/example.jpg";
    String MAIL_FROM = "mailFrom";
    String LOGO_REPORT = "logo-vertical-color.svg";
    String PUBLIC_URL = String.format("https://%s", WEB);
    String PUBLIC_URL_SIN_HTTPS = String.format("http://%s", WEB);
    String PUBLIC_URL_LIMPIO = PUBLIC_URL;

    private WebResource resource;

    public DataBasePropertiesResourceTest()
    {
        super(
                new WebAppDescriptor.Builder(
                        "es.uji.apps.par.services.rest;com.fasterxml.jackson.jaxrs.json;es.uji.apps.par")
                        .contextParam("contextConfigLocation",
                                "classpath:applicationContext-db2-test.xml")
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
    public void getPublicUrlFromDataBase()
    {
        ClientResponse response = resource.path("index/public/properties").get(ClientResponse.class);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });

        Assert.assertNotNull(restResponse.getData());
        Assert.assertTrue(restResponse.getTotal() == 9);
        Assert.assertEquals(restResponse.getData().get(0), PUBLIC_URL);
        Assert.assertEquals(restResponse.getData().get(1), PUBLIC_URL_SIN_HTTPS);
        Assert.assertEquals(restResponse.getData().get(2), PUBLIC_URL_LIMPIO);
        Assert.assertEquals(restResponse.getData().get(3), HTML_TITLE);
        Assert.assertEquals(restResponse.getData().get(4), CONDICIONES_URL);
        Assert.assertEquals(restResponse.getData().get(5), COMO_LLEGAR_URL);
        Assert.assertEquals(restResponse.getData().get(6), PIE_ENTRADA_URL);
        Assert.assertEquals(restResponse.getData().get(7), MAIL_FROM);
        Assert.assertEquals(restResponse.getData().get(8), LOGO_REPORT);
    }
}
