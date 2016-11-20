package es.uji.apps.par.services;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

@Service
public class PinpadWebService implements PinpadDataService
{
	private static final Logger log = LoggerFactory.getLogger(PinpadWebService.class);

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    @Override
    public String consultaEstado(String id, String secret)
    {
        Client client = createClient();

        String sha1String = Utils.sha1(id + secret);

        String url = String.format("https://e-ujier.uji.es/pls/www/!pinpad.estado?tpv_identificador=%s&tpv_hash=%s",
                id, sha1String);

        log.info("antes consultaEstado: " + url);

        WebResource webResource = client.resource(url);

        ClientResponse response = webResource.get(ClientResponse.class);

        log.info("status consultaEstado: " + url + ", status: " + response.getStatus());

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Error respuesta HTTP : " + response.getStatus());
        }

        String output = response.getEntity(String.class);

        log.info(String.format("body consultaEstado: \"%s\", body:\"%s\"", url, output));

        return output;
    }

    @Override
    public String realizaPago(String id, BigDecimal importe, String concepto, String secret)
    {
        Client client = createClient();

        String importeEnviar = Utils.monedaToCents(importe);
        String sha1String = Utils.sha1(id + importeEnviar + secret);

        String conceptoEncoded;
        try
        {
            conceptoEncoded = URLEncoder.encode(concepto, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error en URLEncoder.encode: " + concepto);
        }

        String url = String
                .format("https://e-ujier.uji.es/pls/www/!pinpad.cobrar?tpv_identificador=%s&tpv_concepto=%s&tpv_importe=%s&tpv_hash=%s",
                        id, conceptoEncoded, importeEnviar, sha1String);

        log.info("antes realizaPago: " + url);

        WebResource webResource = client.resource(url);

        ClientResponse response = webResource.get(ClientResponse.class);

        log.info(String.format("status realizaPago: \"%s\", status:\"%s\"", url, response.getStatus()));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Error respuesta HTTP: " + response.getStatus());
        }

        String output = response.getEntity(String.class);

        log.info(String.format("body realizaPago: \"%s\", body:\"%s\"", url, output));

        return output;
    }

    private Client createClient()
    {
        Client client = Client.create();

        client.setConnectTimeout(CONNECT_TIMEOUT);
        client.setReadTimeout(READ_TIMEOUT);

        return client;
    }
}
