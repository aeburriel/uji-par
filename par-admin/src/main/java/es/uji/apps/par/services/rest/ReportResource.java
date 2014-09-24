package es.uji.apps.par.services.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.batik.transcoder.TranscoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.SinIvaException;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.services.ReportService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.utils.DateUtils;

@Path("report")
public class ReportResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @InjectParam
    private ReportService reportService;

    @InjectParam
    private SesionesService sesionesService;

    @POST
    @Path("taquilla/{fechaInicio}/{fechaFin}")
    @Produces("application/vnd.ms-excel")
    public Response generateExcelTaquilla(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException {
        ByteArrayOutputStream ostream = reportService.getExcelTaquilla(fechaInicio, fechaFin, getLocale());
        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeTaquilla " + fechaInicio + "-" + fechaFin + ".xls\"")
                .build();
    }

    @POST
    @Path("eventos/{fechaInicio}/{fechaFin}")
    @Produces("application/vnd.ms-excel")
    public Response generateExcelEventos(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException {
        ByteArrayOutputStream ostream = reportService.getExcelEventos(fechaInicio, fechaFin, getLocale());
        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeEvents " + fechaInicio + "-" + fechaFin + ".xls\"")
                .build();
    }

    @POST
    @Path("taquilla/{fechaInicio}/{fechaFin}/pdf")
    @Produces("application/pdf")
    public Response generatePdfTaquilla(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException, ReportSerializationException, ParseException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        reportService.getPdfTaquilla(fechaInicio, fechaFin, ostream, getLocale());

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeTaquilla " + fechaInicio + "-" + fechaFin + ".pdf\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("sesion/{idSesion}/pdf")
    @Produces("application/pdf")
    public Response generatePdfSesion(@PathParam("idSesion") long idSesion) throws TranscoderException, IOException,
            ReportSerializationException, ParseException, SinIvaException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        Sesion sesion = sesionesService.getSesion(idSesion);
        String fileName = "informeSesion " + DateUtils.dateToSpanishStringWithHour(sesion.getFechaCelebracion()) + ".pdf";

        reportService.getPdfSesion(idSesion, ostream, getLocale());

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"" + fileName + "\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("evento/{idEvento}/pdf")
    @Produces("application/pdf")
    public Response generatePdfEvento(@PathParam("idEvento") long idEvento) throws TranscoderException, IOException,
            ReportSerializationException, ParseException, SinIvaException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        List<Sesion> sesiones = sesionesService.getSesiones(idEvento);
        Collections.sort(sesiones, new Comparator<Sesion>() {
            @Override
            public int compare(Sesion o1, Sesion o2) {
                return o1.getFechaCelebracion().compareTo(o2.getFechaCelebracion());
            }
        });
        String fileName = "informeEvento-" + idEvento + ".pdf";

        reportService.getPdfSesiones(sesiones, ostream, getLocale());

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"" + fileName + "\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("evento/{idEvento}/sesion/{idSesion}/pdf/{tipo}")
    @Produces("application/pdf")
    public Response generatePdf(@PathParam("idEvento") long idEvento, @PathParam("idSesion") long idSesion, @PathParam("tipo") String tipo) throws TranscoderException, IOException,
            ReportSerializationException, ParseException, SinIvaException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        Sesion sesion = sesionesService.getSesion(idSesion);
        String fileName = tipo + "_" + DateUtils.dateToSpanishStringWithHour(sesion.getFechaCelebracion()) + ".pdf";

        reportService.getPdf(idSesion, ostream, tipo, getLocale());

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"" + fileName + "\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("taquilla/{fechaInicio}/{fechaFin}/efectivo/pdf")
    @Produces("application/pdf")
    public Response generatePdfEfectivo(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException, ReportSerializationException, ParseException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        try {
            reportService.getPdfEfectivo(fechaInicio, fechaFin, ostream, getLocale());
        } catch (SinIvaException e) {
            log.error("Error", e);

            String errorMsj = String.format("ERROR: Cal introduir l'IVA de l'event \"%s\"", e.getEvento());
            return Response.serverError().type(MediaType.TEXT_PLAIN).entity(errorMsj).build();
        }

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeTaquilla " + fechaInicio + "-" + fechaFin + ".pdf\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("taquilla/{fechaInicio}/{fechaFin}/tpv/pdf")
    @Produces("application/pdf")
    public Response generatePdfTpv(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException, ReportSerializationException, ParseException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        try {
            reportService.getPdfTpvSubtotales(fechaInicio, fechaFin, ostream, getLocale());
        } catch (SinIvaException e) {
            log.error("Error", e);

            String errorMsj = String.format("ERROR: Cal introduir l'IVA de l'event \"%s\"", e.getEvento());
            return Response.serverError().type(MediaType.TEXT_PLAIN).entity(errorMsj).build();
        }

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeTaquilla " + fechaInicio + "-" + fechaFin + ".pdf\"")
                .type("application/pdf").build();
    }

    @POST
    @Path("taquilla/{fechaInicio}/{fechaFin}/eventos/pdf")
    @Produces("application/pdf")
    public Response generatePdfEventos(@PathParam("fechaInicio") String fechaInicio, @PathParam("fechaFin") String fechaFin) throws TranscoderException, IOException, ReportSerializationException, ParseException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        try {
            reportService.getPdfEventos(fechaInicio, fechaFin, ostream, getLocale());
        } catch (SinIvaException e) {
            log.error("Error", e);

            String errorMsj = String.format("ERROR: Cal introduir l'IVA de l'event \"%s\"", e.getEvento());
            return Response.serverError().type(MediaType.TEXT_PLAIN).entity(errorMsj).build();
        }

        return Response.ok(ostream.toByteArray())
                .header("Content-Disposition", "attachment; filename =\"informeEventos " + fechaInicio + "-" + fechaFin + ".pdf\"")
                .type("application/pdf").build();
    }

    @GET
    @Path("sesiones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response filtraSesiones(@QueryParam("fechaInicio") String fechaInicio, @QueryParam("fechaFin") String fechaFin,
                                   @QueryParam("sort") @DefaultValue("[{\"property\":\"nombre\",\"direction\":\"ASC\"}]") String sort,
                                   @QueryParam("start") int start, @QueryParam("limit") @DefaultValue("1000") int limit) {

        List<Sesion> sesiones = sesionesService.getSesionesCinePorFechas(fechaInicio, fechaFin, sort);
        return Response.ok().entity(new RestResponse(true, sesiones, sesiones.size())).build();
    }
}
