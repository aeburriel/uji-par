package es.uji.apps.par.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import es.uji.apps.par.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.pinpad.EstadoPinpad;
import es.uji.apps.par.pinpad.ResultadoPagoPinpad;

@Service
public class Pinpad
{
	private static final Logger log = LoggerFactory.getLogger(Pinpad.class);
	public static final List<String> CODIGOS_ERROR = Arrays.asList("5", "7", "8", "9");

    @Autowired
    PinpadDataService dataService;

	@Autowired
	Configuration configuration;

    public Pinpad()
    {
    }

    public Pinpad(PinpadDataService dataService)
    {
        this.dataService = dataService;
    }

    public void setDataService(PinpadDataService dataService)
    {
        this.dataService = dataService;
    }

    public EstadoPinpad getEstadoPinpad(String id)
    {
        try
        {
            log.info(String.format("llamando consultaEstado(\"%s\")...", id));
            String resultado = dataService.consultaEstado(id, configuration.getSecret());
            log.info(String.format("llamado consultaEstado(\"%s\") -> \"%s\"", id, resultado));

            return parseEstado(resultado);
        }
        catch (Exception e)
        {
            log.error("Error obteniendo estado pinpad", e);
            return new EstadoPinpad(true, e.getMessage());
        }
    }

    public ResultadoPagoPinpad realizaPago(String id, BigDecimal importe, String concepto)
    {
        try
        {
            log.info(String.format("llamando realizaPago(\"%s\", \"%s\", \"%s\")...", id, importe.floatValue(), concepto));
            String resultado = dataService.realizaPago(id, importe, concepto, configuration.getSecret());
            log.info(String.format("llamado realizaPago(\"%s\", \"%s\", \"%s\") -> \"%s\"", id, importe, concepto, resultado));
            
            return parseResultadoPago(resultado);
        }
        catch (Exception e)
        {
            log.error("Error realizando pago pinpad", e);
            return new ResultadoPagoPinpad(true, e.getMessage());
        }
    }

    private ResultadoPagoPinpad parseResultadoPago(String textoResultado)
    {
        ResultadoPagoPinpad resultado = new ResultadoPagoPinpad();

        if (textoResultado.equals(""))
        {
            resultado.setError(true);
        }
        else
        {
            resultado.setError(false);
            resultado.setCodigo(textoResultado.trim());
        }

        return resultado;
    }

    private EstadoPinpad parseEstado(String resultado)
    {
        EstadoPinpad estado = new EstadoPinpad(false);

        String[] vecMensaje = resultado.split("\n", 1);
        String lineaEstado = vecMensaje[0];
        String[] vecLineaEstado = lineaEstado.split("-", 3);

        String codigoAccion = vecLineaEstado[0];
		String recibo = vecLineaEstado[1];
        String mensaje = vecLineaEstado[2];

        estado.setReady(codigoAccion.equals("0"));
		estado.setRecibo(recibo);
        estado.setCodigoAccion(codigoAccion);
        estado.setMensaje(mensaje);
        estado.setPagoCorrecto(codigoAccion.equals("0"));

		if (CODIGOS_ERROR.contains(codigoAccion))
			estado.setError(true);

        return estado;
    }

}
