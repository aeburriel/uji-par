package es.uji.apps.par.services;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.EventoMultisesion;
import es.uji.apps.par.report.EntradaModelReport;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.report.EntradaReportOnlineInterface;
import es.uji.apps.par.report.EntradaReportTaquillaInterface;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

@Service
public class EntradasService {
    public static final String BUTACAS_COMPRA = "butacasCompra";
    public static final String UUID_COMPRA = "uuidCompra";
    public static final String ID_SESION = "idSesion";
    public static final String BEAN_REPORT_SUFFIX = "Report";
    public static final String BEAN_REPORT_PREFIX = "Entrada";
    private static final Logger log = LoggerFactory.getLogger(EntradasService.class);

    @Autowired
    private EventosService eventosService;

    @Autowired
    private ComprasDAO comprasDAO;

    @Autowired
    private TarifasDAO tarifasDAO;

	@Autowired
	Configuration configuration;

    private static EntradaReportTaquillaInterface entradaTaquillaReport;
    private static EntradaReportOnlineInterface entradaOnlineReport;

    public void generaEntrada(String uuidCompra, OutputStream outputStream, String userUID, String urlPublicSinHTTPS, String urlPieEntrada) throws ReportSerializationException {
		try {
			EntradaReportOnlineInterface entrada = generaEntradaOnlineYRellena(uuidCompra, userUID, urlPublicSinHTTPS, urlPieEntrada);
			entrada.serialize(outputStream);
		} catch (NullPointerException e) {
			log.error("La compra con uuid " + uuidCompra + " no existe");
		}
    }

	public EntradaReportOnlineInterface generaEntradaOnlineYRellena(String uuidCompra, String userUID, String urlPublicSinHTTPS, String urlPieEntrada) throws
			ReportSerializationException {
		CompraDTO compra = comprasDAO.getCompraByUuid(uuidCompra);
		if (compra == null)
			throw new NullPointerException();

		EventoDTO evento = compra.getParSesion().getParEvento();
		try {
			String className = "es.uji.apps.par.report." + BEAN_REPORT_PREFIX + evento.getParTiposEvento().getNombreEs() +
					BEAN_REPORT_SUFFIX;
			entradaOnlineReport = EntradaReportFactory.newInstanceByClassName(className);
		} catch(Exception e) {
			String reportClass = comprasDAO.getReportClassByCompraUUID(uuidCompra, EntradaReportFactory.TIPO_ENTRADA_ONLINE);
			entradaOnlineReport = EntradaReportFactory.newInstanceOnline(reportClass);
		}

		EntradaReportOnlineInterface entrada = entradaOnlineReport.create(new Locale(configuration.getIdiomaPorDefecto()), configuration);
		rellenaEntrada(compra, entrada, userUID, urlPublicSinHTTPS, urlPieEntrada);
		return entrada;
	}

	public void generaEntradaTaquilla(String uuidCompra, OutputStream outputStream, String userUID, String urlPublicSinHTTPS) throws ReportSerializationException, SAXException, IOException {
		EntradaReportTaquillaInterface entrada = generaEntradaTaquillaYRellena(uuidCompra, userUID, urlPublicSinHTTPS);
        entrada.serialize(outputStream);
    }

	public EntradaReportTaquillaInterface generaEntradaTaquillaYRellena(String uuidCompra, String userUID, String urlPublicSinHTTPS) throws SAXException, IOException {
		String reportClass = comprasDAO.getReportClassByCompraUUID(uuidCompra, EntradaReportFactory.TIPO_ENTRADA_TAQUILLA);
		if (entradaTaquillaReport == null)
			entradaTaquillaReport = EntradaReportFactory.newInstanceTaquilla(reportClass);

		EntradaReportTaquillaInterface entrada = entradaTaquillaReport.create(new Locale(configuration.getIdiomaPorDefecto()), configuration);

		rellenaEntradaTaquilla(uuidCompra, entrada, userUID, urlPublicSinHTTPS);
		return entrada;
	}

	private void rellenaEntradaTaquilla(String uuidCompra, EntradaReportTaquillaInterface entrada, String userUID, String urlPublicSinHTTPS) {
        CompraDTO compra = comprasDAO.getCompraByUuid(uuidCompra);
        List<TarifaDTO> tarifas = tarifasDAO.getAll("", 0, 100, userUID);

		String titulo;
		List<EventoMultisesion> peliculas = eventosService.getPeliculas(compra.getParSesion().getParEvento().getId());
		Locale locale = new Locale(configuration.getIdiomaPorDefecto());
		if (locale.getLanguage().equals("ca"))
		{
			titulo = compra.getParSesion().getParEvento().getTituloVa();
			if (peliculas.size() > 0)
			{
				titulo += ": ";
				for (EventoMultisesion pelicula : peliculas)
				{
					titulo += pelicula.getTituloVa() + ", ";
				}
				titulo = titulo.substring(0, titulo.length() - 2);
			}
		}
		else {
			titulo = compra.getParSesion().getParEvento().getTituloEs();
			if (peliculas.size() > 0)
			{
				titulo += ": ";
				for (EventoMultisesion pelicula : peliculas)
				{
					titulo += pelicula.getTituloEs() + ", ";
				}
				titulo = titulo.substring(0, titulo.length() - 2);
			}
		}
        String fecha = DateUtils.dateToSpanishString(compra.getParSesion().getFechaCelebracion());
        String hora = DateUtils.dateToHourString(compra.getParSesion().getFechaCelebracion());
        String horaApertura = compra.getParSesion().getHoraApertura();

        entrada.setTitulo(titulo);
        entrada.setFecha(fecha);
        entrada.setHora(hora);
        entrada.setHoraApertura(horaApertura);
        entrada.setUrlPortada(urlPublicSinHTTPS + "/rest/evento/"
                + compra.getParSesion().getParEvento().getId() + "/imagenEntrada");
		entrada.setNombreEntidad(compra.getParSesion().getParEvento().getParCine().getNombre());
		entrada.setDireccion(String.format("%s %s %s", compra.getParSesion().getParEvento().getParCine().getDireccion(), compra.getParSesion().getParEvento().getParCine().getCp(), compra.getParSesion().getParEvento().getParCine().getNombreMunicipio()));
		entrada.setCif(compra.getParSesion().getParEvento().getParTpv().getCif());
		entrada.setPromotor(compra.getParSesion().getParEvento().getPromotor());
		entrada.setNifPromotor(compra.getParSesion().getParEvento().getNifPromotor());

        for (ButacaDTO butaca : compra.getParButacas()) {
            if (butaca.getAnulada() == null || butaca.getAnulada() == false) {
                EntradaModelReport entradaModelReport = new EntradaModelReport();
                entradaModelReport.setFila(butaca.getFila());
                entradaModelReport.setNumero(butaca.getNumero());
				if (locale.getLanguage().equals("ca"))
				{
					entradaModelReport.setZona(butaca.getParLocalizacion().getNombreVa());
				}
				else
				{
					entradaModelReport.setZona(butaca.getParLocalizacion().getNombreEs());
				}
                entradaModelReport.setTotal(ReportUtils.formatEuros(butaca.getPrecio()));
                entradaModelReport.setNombreEmpresa(butaca.getParSesion().getParEvento().getParTpv().getNombre());
                if (configuration.isIdEntrada()) {
                    entradaModelReport.setBarcode(compra.getUuid() + "-" + butaca.getIdEntrada());
                } else {
                    entradaModelReport.setBarcode(compra.getUuid() + "-" + butaca.getId());
                }
                entradaModelReport.setTipo(butaca.getTipo());
                entradaModelReport.setIniciales(butaca.getParLocalizacion().getIniciales());
				entradaModelReport.setSala(butaca.getParSesion().getParSala().getNombre());

                for (TarifaDTO tarifa : tarifas) {
                    if (tarifa.getId() == Long.valueOf(butaca.getTipo())) {
                        entradaModelReport.setTipo(tarifa.getNombre());
                        break;
                    }
                }

                entrada.generaPaginaButaca(entradaModelReport, urlPublicSinHTTPS);
            }
        }

        if (compra.getReciboPinpad() != null) {
            for (int i = 0; i < 2; i++) {
                entrada.generaPaginasReciboPinpad(compra.getReciboPinpad());
            }
        }
    }

    private void rellenaEntrada(CompraDTO compra, EntradaReportOnlineInterface entrada, String userUID, String urlPublicSinHTTPS, String urlPieEntrada) throws NullPointerException {
		String titulo;
		List<EventoMultisesion> peliculas = eventosService.getPeliculas(compra.getParSesion().getParEvento().getId());
		Locale locale = new Locale(configuration.getIdiomaPorDefecto());
		if (locale.getLanguage().equals("ca"))
		{
			titulo = compra.getParSesion().getParEvento().getTituloVa();
			if (peliculas.size() > 0)
			{
				titulo += ": ";
				for (EventoMultisesion pelicula : peliculas)
				{
					titulo += pelicula.getTituloVa() + ", ";
				}
				titulo = titulo.substring(0, titulo.length() - 2);
			}
		}
		else
		{
			titulo = compra.getParSesion().getParEvento().getTituloEs();
			if (peliculas.size() > 0)
			{
				titulo += ": ";
				for (EventoMultisesion pelicula : peliculas)
				{
					titulo += pelicula.getTituloEs() + ", ";
				}
				titulo = titulo.substring(0, titulo.length() - 2);
			}
		}
        String fecha = DateUtils.dateToSpanishString(compra.getParSesion().getFechaCelebracion());
        String hora = DateUtils.dateToHourString(compra.getParSesion().getFechaCelebracion());
        String horaApertura = compra.getParSesion().getHoraApertura();

        entrada.setTitulo(titulo);
        entrada.setFecha(fecha);
        entrada.setHora(hora);
        entrada.setHoraApertura(horaApertura);
        entrada.setUrlPortada(urlPublicSinHTTPS + "/rest/evento/"
                + compra.getParSesion().getParEvento().getId() + "/imagenEntrada");

		if (compra.getParSesion().getParEvento().getImagenPubliSrc() != null)
		{
			entrada.setUrlPublicidad(urlPublicSinHTTPS + "/rest/evento/"
					+ compra.getParSesion().getParEvento().getId() + "/imagenPubliEntrada");
		}
		else
		{
			entrada.setUrlPublicidad(urlPieEntrada);
		}

		entrada.setNombreEntidad(compra.getParSesion().getParEvento().getParCine().getNombre());
		entrada.setDireccion(String.format("%s %s %s", compra.getParSesion().getParEvento().getParCine().getDireccion(), compra.getParSesion().getParEvento().getParCine().getCp(), compra.getParSesion().getParEvento().getParCine().getNombreMunicipio()));
		entrada.setCif(compra.getParSesion().getParEvento().getParTpv().getCif());
		entrada.setPromotor(compra.getParSesion().getParEvento().getPromotor());
		entrada.setNifPromotor(compra.getParSesion().getParEvento().getNifPromotor());
        int totalButacas = 0;

        for (ButacaDTO butaca : compra.getParButacas()) {
            if (butaca.getAnulada() == null || butaca.getAnulada() == false) {
                if (entrada.esAgrupada()) {
                    totalButacas++;
                }
                else {
                    EntradaModelReport entradaModelReport = new EntradaModelReport();
                    rellenaButaca(entradaModelReport, compra, entrada, butaca, userUID, urlPublicSinHTTPS);
                }
            }
        }
        if (entrada.esAgrupada()) {
            EntradaModelReport entradaModelReport = new EntradaModelReport();
            entrada.setTotalButacas(totalButacas);
            
            rellenaButaca(entradaModelReport, compra, entrada, compra.getParButacas().get(0), userUID, urlPublicSinHTTPS);
        }
    }

    private void rellenaButaca(EntradaModelReport entradaModelReport, CompraDTO compra, EntradaReportOnlineInterface entrada, ButacaDTO butaca, String userUID, String urlPublicSinHTTPS) {
        TarifaDTO tarifaCompra = tarifasDAO.get(Integer.valueOf(butaca.getTipo()), userUID);
		Locale locale = new Locale(configuration.getIdiomaPorDefecto());
		if (locale.getLanguage().equals("ca"))
		{
			entradaModelReport.setZona(butaca.getParLocalizacion().getNombreVa());
		}
		else
		{
			entradaModelReport.setZona(butaca.getParLocalizacion().getNombreEs());
		}
        entradaModelReport.setFila(butaca.getFila());
        entradaModelReport.setNumero(butaca.getNumero());
        entradaModelReport.setTotal(ReportUtils.formatEuros(butaca.getPrecio()));
        entradaModelReport.setCifEmpresa(butaca.getParSesion().getParEvento().getParTpv().getCif());
        entradaModelReport.setNombreEmpresa(butaca.getParSesion().getParEvento().getParTpv().getNombre());
        if (configuration.isIdEntrada()) {
            entradaModelReport.setBarcode(compra.getUuid() + "-" + butaca.getIdEntrada());
        } else {
            entradaModelReport.setBarcode(compra.getUuid() + "-" + butaca.getId());
        }
        entradaModelReport.setTipo(tarifaCompra.getNombre());
        entradaModelReport.setTarifaDefecto(tarifaCompra.getDefecto());
		entradaModelReport.setSala(butaca.getParSesion().getParSala().getNombre());
        entrada.generaPaginaButaca(entradaModelReport, urlPublicSinHTTPS);
    }

    public static void main(String[] args) throws FileNotFoundException, ReportSerializationException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext-db.xml");

        EntradasService service = ctx.getBean(EntradasService.class);

        //service.generaEntradaTaquilla("e3a762c9-9107-47b7-b13d-175e308aa24f", new FileOutputStream("/tmp/entrada.pdf"));
        service.generaEntrada("e3a762c9-9107-47b7-b13d-175e308aa24f", new FileOutputStream("/tmp/entrada.pdf"), "", "https", "urlPieEntrada");
    }
}
