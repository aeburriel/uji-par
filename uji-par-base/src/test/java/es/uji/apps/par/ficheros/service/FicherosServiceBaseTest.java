package es.uji.apps.par.ficheros.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;

import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.exceptions.*;
import es.uji.apps.par.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import es.uji.apps.par.services.ComprasService;
import es.uji.apps.par.utils.DateUtils;

public class FicherosServiceBaseTest
{
    @Autowired
    private CinesDAO cinesDao;

    @Autowired
    private SalasDAO salasDao;

    @Autowired
    private EventosDAO eventosDAO;

    @Autowired
    private TiposEventosDAO tiposEventoDAO;

    @Autowired
    private SesionesDAO sesionesDAO;

    @Autowired
    private PlantillasDAO plantillasDAO;

    @Autowired
    private LocalizacionesDAO localizacionesDAO;

    @Autowired
    private PreciosPlantillaDAO preciosPlantillaDAO;

	@Autowired
	private TarifasDAO tarifasDAO;

    @Autowired
    private TpvsDAO tpvsDAO;

    @Autowired
    private ComprasService comprasService;

    protected Cine cine;
    protected Localizacion localizacion;
    protected Plantilla plantilla;
    protected PreciosSesion precioSesion;
    protected Sala sala;
    protected TipoEvento tipoEvento;
    protected Evento evento;
	protected Tarifa tarifa;
	protected Tpv tpv;

    protected void setup() throws PrecioRepetidoException {
        tpvsDAO.addTpvDefault();
        cine = creaCine();
        localizacion = creaLocalizacion("Platea");
        sala = creaSala("567", "Sala 1");
        tipoEvento = creaTipoEvento();
        plantilla = creaPlantilla();
		tarifa = creaTarifa();
        PreciosPlantilla precioPlantilla = creaPrecioPlantilla(1.10);
        precioSesion = creaPrecioSesion(precioPlantilla);
        evento = creaEvento(tipoEvento);
    }

	private Tpv creaTpv() {
		tpv = new Tpv();
		tpv.setNombre("TPV Prueba");
		TpvsDTO tpvDefecto = tpvsDAO.getTpvDefault();
		if (tpvDefecto == null)
			tpvsDAO.addTpv(tpv, true);

		TpvsDTO tpvDefectoInsertado = tpvsDAO.getTpvDefault();
		tpv.setId(tpvDefectoInsertado.getId());
		return tpv;
	}

	private Tarifa creaTarifa() {
		Tarifa tarifa = new Tarifa();
		tarifa.setNombre("tarifa1");
		tarifa.setDefecto("true");
		TarifaDTO tarifaDTO = Tarifa.toDTO(tarifa);
		tarifasDAO.add(tarifaDTO);
		tarifa.setId(tarifaDTO.getId());
		return tarifa;
	}

	protected void registraCompra(Sesion sesion1, Butaca... butacas) throws NoHayButacasLibresException,
			ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        ResultadoCompra resultado1 = comprasService.registraCompraTaquilla(sesion1.getId(), Arrays.asList(butacas));
        comprasService.marcaPagada(resultado1.getId());
    }

    protected Sesion creaSesion(Sala sala, Evento evento, String fecha, String hora) throws ParseException
    {
        Sesion sesion = new Sesion();
        sesion.setFechaCelebracionWithDate(DateUtils.spanishStringWithHourstoDate(fecha + " " + hora));
        sesion.setFechaInicioVentaOnline("1/12/2011");
        sesion.setFechaFinVentaOnline("11/12/2012");
        sesion.setEvento(evento);
        sesion.setSala(sala);
        sesion.setPlantillaPrecios(plantilla);
        sesion.setPreciosSesion(Arrays.asList(precioSesion));
        sesion.setVersionLinguistica("1");
        sesion.getEvento().setFormato("3");

        sesionesDAO.addSesion(sesion);
        return sesion;
    }

    protected Sesion creaSesion(Sala sala, Evento evento, String hora) throws ParseException
    {
        return creaSesion(sala, evento, "11/12/2013", hora);
    }

	protected Sesion creaSesion(Sala sala, Evento evento) throws ParseException
	{
		return creaSesion(sala, evento, "11/12/2013", "22:00");
	}

    protected PreciosSesion creaPrecioSesion(PreciosPlantilla precioPlantilla)
    {
        PreciosSesion precioSesion = new PreciosSesion(precioPlantilla);

        sesionesDAO.addPrecioSesion(PreciosSesion.precioSesionToPrecioSesionDTO(precioSesion));

        return precioSesion;
    }

    protected Plantilla creaPlantilla()
    {
        Plantilla plantilla = new Plantilla("test");
		plantilla.setSala(sala);
        plantillasDAO.add(plantilla);

        return plantilla;
    }

    protected Evento creaEvento(TipoEvento tipoEvento)
    {
        return creaEvento(tipoEvento, "1a", "2a", "3a", "4a", "5", "6");
    }

    protected Evento creaEvento(TipoEvento tipoEvento, String expediente, String titulo, String codigoDistribuidora,
            String nombreDistribuidora, String vo, String subtitulos)
    {
		tpv = creaTpv();
        Evento evento = new Evento();
        evento.setTipoEvento(tipoEvento.getId());
        evento.setExpediente(expediente);
        evento.setTituloEs(titulo);
        evento.setCodigoDistribuidora(codigoDistribuidora);
        evento.setNombreDistribuidora(nombreDistribuidora);
        evento.setVo(vo);
        evento.setSubtitulos(subtitulos);
		evento.setParTpv(tpv);

        eventosDAO.addEvento(evento);

        return evento;
    }

    protected TipoEvento creaTipoEvento()
    {
        TipoEvento tipoEvento = new TipoEvento();
        tipoEvento.setNombreEs("Cine");
        tipoEvento.setNombreVa("Cinema");
        tiposEventoDAO.addTipoEvento(tipoEvento);
        return tipoEvento;
    }

    protected Cine creaCine()
    {
        Cine cine = new Cine();
        cine.setCodigo("123");
        cinesDao.addCine(cine);

        return cine;
    }

    protected Sala creaSala(String codigo, String nombre)
    {
        Sala sala = new Sala();
        sala.setCodigo(codigo);
        sala.setNombre(nombre);
        salasDao.addSala(sala);
        return sala;
    }

    protected Localizacion creaLocalizacion(String nombre)
    {
        Localizacion localizacion = new Localizacion(nombre);
        localizacion.setCodigo(nombre);

        localizacionesDAO.add(localizacion);

        return localizacion;
    }

    protected Butaca creaButaca(String fila, String numero)
    {
        Butaca butaca = new Butaca();

        butaca.setLocalizacion("Platea");
        butaca.setFila(fila);
        butaca.setNumero(numero);
        butaca.setTipo(String.valueOf(tarifa.getId()));

        return butaca;
    }

    protected PreciosPlantilla creaPrecioPlantilla(double normal) throws PrecioRepetidoException {
        PreciosPlantilla preciosPlantilla = new PreciosPlantilla(localizacion, plantilla);
        preciosPlantilla.setPrecio(new BigDecimal(normal));
		preciosPlantilla.setTarifa(tarifa);
        preciosPlantillaDAO.add(preciosPlantilla);
        return preciosPlantilla;
    }
}
