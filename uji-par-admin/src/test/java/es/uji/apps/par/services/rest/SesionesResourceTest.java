package es.uji.apps.par.services.rest;

import java.util.Calendar;
import java.util.HashMap;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.util.Log4jConfigListener;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

import es.uji.apps.par.CampoRequeridoException;
import es.uji.apps.par.CommonExceptionMapper;
import es.uji.apps.par.DateUtils;
import es.uji.apps.par.FechasInvalidasException;
import es.uji.apps.par.ResponseMessage;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.TipoEvento;

public class SesionesResourceTest extends JerseyTest
{
    private WebResource resource;

    public SesionesResourceTest()
    {
        super(
                new WebAppDescriptor.Builder(
                        "es.uji.apps.par.services.rest;com.fasterxml.jackson.jaxrs.json;es.uji.apps.par")
                        .contextParam("contextConfigLocation",
                                "classpath:applicationContext-db-test.xml")
                        .contextParam("log4jConfigLocation",
                                "src/main/webapp/WEB-INF/log4j.properties")
                        .contextParam("webAppRootKey", "paranimf-fw-uji.root")
                        .contextListenerClass(Log4jConfigListener.class)
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
    public void getSesiones()
    {
        ClientResponse response = resource.path("evento").path("1").path("sesiones")
                .get(ClientResponse.class);
        RestResponse serviceResponse = response.getEntity(RestResponse.class);

        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertNotNull(serviceResponse.getData());
    }

    private Sesion preparaSesion()
    {
        Sesion parSesion = new Sesion();
        parSesion.setFechaCelebracion("01/01/2012");
        parSesion.setFechaInicioVentaOnline("01/01/2012");
        parSesion.setFechaFinVentaOnline("01/01/2012");
        parSesion.setHoraCelebracion("12:30");
        parSesion.setCanalInternet("1");
        /*
         * ParEventoDTO parEventoDTO = new ParEventoDTO(); parEventoDTO.setId(1);
         * parSesion.setEvento(parEventoDTO);
         */

        return parSesion;
    }
    
    private String getFieldFromRestResponse(RestResponse restResponse, String field)
    {
        return ((HashMap) restResponse.getData().get(0)).get(field).toString();
    }
    
    @Test public void addSesionWithoutFechaCelebracion() { 
    	Sesion sesion = preparaSesion();
    	sesion.setFechaCelebracion(null);
    	
    	ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus()); 
    	
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(CampoRequeridoException.CAMPO_OBLIGATORIO + "Fecha de celebración",
    	                resultatOperacio.getMessage());
    }

    
    @Test public void addSesionWithoutHoraCelebracion() { 
    	Sesion sesion = preparaSesion();
    	sesion.setHoraCelebracion(null);
     
    	ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus()); 
    	
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(CampoRequeridoException.CAMPO_OBLIGATORIO + "Hora de celebración",
    	                resultatOperacio.getMessage());
    }
    
    private TipoEvento addTipoEvento()
    {
        TipoEvento parTipoEvento = new TipoEvento("prueba");
        ClientResponse response = resource.path("tipoevento").post(ClientResponse.class,
                parTipoEvento);
        RestResponse serviceResponse = response.getEntity(RestResponse.class);

        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(serviceResponse.getSuccess());
        Assert.assertNotNull(serviceResponse.getData());
        int id = Integer.valueOf(((HashMap) serviceResponse.getData().get(0)).get("id").toString());
        parTipoEvento.setId(id);
        return parTipoEvento;
    }
    
    private FormDataMultiPart preparaEvento(TipoEvento tipoEvento)
    {
        FormDataMultiPart f = new FormDataMultiPart();
        f.field("tituloEs", "titulo");
        f.field("tipoEvento", String.valueOf(tipoEvento.getId()));
        f.field("asientosNumerados", "1");

        return f;
    }
    
    @Test 
    public void addSesion() {
    	TipoEvento parTipoEvento = addTipoEvento();
        FormDataMultiPart parEvento = preparaEvento(parTipoEvento);
        ClientResponse response = resource.path("evento").type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .post(ClientResponse.class, parEvento);
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        RestResponse restResponse = response.getEntity(new GenericType<RestResponse>()
        {
        });
        Assert.assertTrue(restResponse.getSuccess());
        Assert.assertNotNull(getFieldFromRestResponse(restResponse, "id"));
        Assert.assertEquals(parEvento.getField("tituloEs").getValue(),
                getFieldFromRestResponse(restResponse, "tituloEs"));
        
    	Sesion sesion = preparaSesion();
     
    	response = resource.path("evento").path(getFieldFromRestResponse(restResponse, "id")).path("sesiones").post(ClientResponse.class, sesion);
    	Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus()); 
    	
    	restResponse = response.getEntity(new GenericType<RestResponse>()
    	{
    	});
    	Assert.assertTrue(restResponse.getSuccess());
    	Assert.assertNotNull(getFieldFromRestResponse(restResponse, "id"));
    	
    	Assert.assertEquals(String.valueOf(DateUtils.addStartEventTimeToDate(sesion.getFechaCelebracion(), sesion.getHoraCelebracion()).getTime()),
            getFieldFromRestResponse(restResponse, "fechaCelebracion"));
    }
    
    @Test
    public void addSesionWithFechaEndVentaAnteriorFechaStartVenta() {
    	Sesion sesion = preparaSesion();
    	sesion.setFechaInicioVentaOnline("02/12/2012");
    	sesion.setFechaFinVentaOnline("01/12/2012");
    	
    	ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(FechasInvalidasException.FECHA_INICIO_VENTA_POSTERIOR_FECHA_FIN_VENTA,
            resultatOperacio.getMessage());
    }
    
    @Test 
    public void addSesionWithoutFechaInicioVentaOnline() {
        Sesion sesion = preparaSesion();
        sesion.setFechaInicioVentaOnline(null);
        
        ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus()); 
    	
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(CampoRequeridoException.CAMPO_OBLIGATORIO + "Fecha de inicio de la venta online",
            resultatOperacio.getMessage());
    }
    
    @Test 
    public void addSesionWithoutFechaFinVentaOnline() {
    	Sesion sesion = preparaSesion();
        sesion.setFechaFinVentaOnline(null);
        
        ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus()); 
    	
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(CampoRequeridoException.CAMPO_OBLIGATORIO + "Fecha de fin de la venta online",
            resultatOperacio.getMessage());
    }
     
    @Test 
    public void addSesionWithFechaEndVentaPosteriorFechaCelebracion() {
    	Sesion sesion = preparaSesion();
    	sesion.setFechaCelebracion("01/12/2012");
    	sesion.setFechaInicioVentaOnline("01/11/2012");
    	sesion.setFechaFinVentaOnline("03/12/2012");
    	
    	ClientResponse response = resource.path("evento").path("1").path("sesiones").post(ClientResponse.class, sesion);
    	ResponseMessage resultatOperacio = response.getEntity(new GenericType<ResponseMessage>()
        {
        });
        Assert.assertEquals(FechasInvalidasException.FECHA_FIN_VENTA_POSTERIOR_FECHA_CELEBRACION,
            resultatOperacio.getMessage());
    }
}