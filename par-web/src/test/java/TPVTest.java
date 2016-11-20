import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.TpvsDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import java.util.Locale;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = {"/applicationContext-db.xml"})
public class TPVTest extends BaseResourceTest {
    private WebResource resource;

;

    @Autowired
    TpvsDAO tpvsDAO;

	@Autowired
	Configuration configuration;

    private TpvsDTO tpvDefault;

    public TPVTest() {
        super(new WebAppDescriptor.Builder(
                "es.uji.apps.par.services.rest;com.fasterxml.jackson.jaxrs.json;es.uji.apps.par")
                .contextParam("contextConfigLocation", "classpath:applicationContext-test.xml")
                .contextParam("webAppRootKey", "paranimf-fw-uji.root")
                .contextListenerClass(ContextLoaderListener.class).clientConfig(clientConfiguration())
                .requestListenerClass(RequestContextListener.class).servletClass(SpringServlet.class).build());

        this.client().addFilter(new LoggingFilter());
        this.resource = resource();
    }

    private static ClientConfig clientConfiguration() {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        return config;
    }

    @Override
    public void setUp() throws Exception {
        tpvDefault = tpvsDAO.getTpvDefault();
        Assert.assertNotNull(tpvDefault);
    }

    @Ignore
    public String getCALang() {
        Locale locale = new Locale("ca");
        return locale.getLanguage();
    }

    @Ignore
    public String getESLang() {
        Locale locale = new Locale("es");
        return locale.getLanguage();
    }

    @Test
    public void getTPVCaLangCode() {
        String language = getCALang();
        if (language.equals("ca")) {
            Assert.assertEquals(configuration.getTpvLangCaCode(), "003");
            Assert.assertEquals(configuration.getTpvLangCaCode(), tpvDefault.getLangCaCode());
        } else {
            Assert.fail("Idioma incorrecto detectado");
        }
    }

    @Test
    public void getTPVEsLangCode() {
        String language = getESLang();
        if (!language.equals("ca")) {
            Assert.assertEquals(configuration.getTpvLangEsCode(), "001");
            Assert.assertEquals(configuration.getTpvLangEsCode(), tpvDefault.getLangEsCode());
        } else {
            Assert.fail("Idioma incorrecto detectado");
        }
    }

    @Test
    public void getTPVOrderPrefix() {
        Assert.assertEquals(configuration.getTpvOrderPrefixCodeCajamar(), "0000");
        Assert.assertEquals(configuration.getTpvOrderPrefixCodeCajamar(), tpvDefault.getOrderPrefix());
    }

    @Test
    public void getTPVValues() {
        Assert.assertEquals(configuration.getTpvCurrency(), tpvDefault.getCurrency());
        Assert.assertEquals(configuration.getTpvCode(), tpvDefault.getCode());
        Assert.assertEquals(configuration.getTpvNombre(), tpvDefault.getNombre());
        Assert.assertEquals(configuration.getTpvTerminal(), tpvDefault.getTerminal());
        Assert.assertEquals(configuration.getTpvTransaction(), tpvDefault.getTransactionCode());

    }

    @Test
    public void getTPVUrls() {
        Assert.assertNotNull(tpvDefault.getUrl());
        Assert.assertNotNull("", tpvDefault.getWsdlUrl());

        Assert.assertFalse(tpvDefault.getUrl().equals(tpvDefault.getWsdlUrl()));
    }
}
