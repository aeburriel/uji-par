package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.exceptions.CompraSinButacasException;
import es.uji.apps.par.exceptions.NoHayButacasLibresException;
import es.uji.apps.par.model.ReservaRequest;
import es.uji.apps.par.model.ResultadoCompra;
import es.uji.apps.par.services.ComprasService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("reserva")
public class ReservaResource extends BaseResource
{
    @InjectParam
    private ComprasService comprasService;
    
    @Context
    HttpServletResponse currentResponse;

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reservaEntrada(@PathParam("id") Long sesionId, ReservaRequest datosReserva) throws NoHayButacasLibresException, ButacaOcupadaException
    {
        AuthChecker.canWrite(currentRequest);
        String userUID = AuthChecker.getUserUID(currentRequest);

        try
        {
            ResultadoCompra resultadoCompra =  comprasService.reservaButacas(sesionId, datosReserva.getDesde(), datosReserva.getHasta(), 
            		datosReserva.getButacasSeleccionadas(), datosReserva.getObservaciones(), datosReserva.getHoraInicial(), datosReserva.getHoraFinal(),
            		datosReserva.getMinutoInicial(), datosReserva.getMinutoFinal(), userUID);
            return Response.ok(resultadoCompra).build();
        }
        catch (NoHayButacasLibresException e)
        {   
            return errorResponse("error.noHayButacas", getProperty("localizacion." + e.getLocalizacion()));
        }
        catch (ButacaOcupadaException e)
        {   
            return errorResponse("error.butacaOcupada", getProperty("localizacion." +e.getLocalizacion()),
            		e.getFila(), e.getNumero());
        }
        catch (CompraSinButacasException e)
        {   
            return errorResponse("error.compraSinButacas");
        }        
    }
}
