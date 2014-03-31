package es.uji.apps.par.services.rest;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.GeneralPARException;
import es.uji.apps.par.IncidenciaNotFoundException;
import es.uji.apps.par.RegistroSerializaException;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.services.ComunicacionesICAAService;
import es.uji.apps.par.utils.DateUtils;

@Path("comunicacionesicaa")
public class ComunicacionesICAAResource extends BaseResource
{
    @InjectParam
    private ComunicacionesICAAService comunicacionesICAAService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response generaFicheroICAA(@FormParam("ids") List<Integer> ids, 
    		@FormParam("fechaEnvioHabitualAnterior") String fechaEnvioHabitualAnterior, @FormParam("tipoEnvio") String tipoEnvio)
    		throws GeneralPARException, RegistroSerializaException, IncidenciaNotFoundException, 
    		NoSuchProviderException, IOException, InterruptedException
    {
        AuthChecker.canWrite(currentRequest);
        byte[] arr = comunicacionesICAAService.generaFicheroICAA(ids, fechaEnvioHabitualAnterior, tipoEnvio);
        String fileName = Configuration.getCodigoBuzon() + tipoEnvio + DateUtils.getNumeroSemana();
		return Response.ok(arr, 
				MediaType.TEXT_PLAIN).
				header("Content-Disposition","attachment; filename =\"" + fileName + "\"").build();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response marcaEnviosComoEnviados(@FormParam("ids") List<Long> ids)
    {
        AuthChecker.canWrite(currentRequest);
        comunicacionesICAAService.marcaEnviosComoEnviados(ids);
        
		return Response.ok().build();
    }
}
