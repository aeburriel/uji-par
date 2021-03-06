package es.uji.apps.par;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import es.uji.apps.par.exceptions.GeneralPARException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.uji.apps.par.model.ResultatOperacio;

@Provider
public class CommonExceptionMapper implements ExceptionMapper<Exception>
{
	private static final Logger log = LoggerFactory.getLogger(CommonExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception)
    {
		log.error("CommonExceptionMapper", exception);
    	String message = "";
		if (exception instanceof GeneralPARException) {
			message = ((exception.getMessage() != null && !exception.getMessage().equals("")) ? exception.getMessage()
					: GeneralPARException.ERROR_GENERAL);

			message = message.replace("java.lang.Throwable: ", "");

		} else
			message = GeneralPARException.ERROR_GENERAL;

		Integer codiExcepcio = GeneralPARException.getCodeFromMessage(message);

		if (codiExcepcio != null && codiExcepcio == 404)
			return Response.status(404).build();
		else
			return Response.serverError().header("Content-type", "application/json;charset=UTF-8")
					.entity(new ResultatOperacio(false, codiExcepcio, message)).build();
    }
}
