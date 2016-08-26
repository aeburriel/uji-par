package es.uji.apps.par.report;

import es.uji.apps.fopreports.Report;
import es.uji.apps.fopreports.fop.*;
import es.uji.apps.fopreports.serialization.FopPDFSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.fopreports.serialization.ReportSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializerInitException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.exceptions.SinIvaException;
import es.uji.apps.par.ficheros.registros.TipoIncidencia;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.*;
import es.uji.apps.par.report.components.BaseTable;
import es.uji.apps.par.report.components.InformeTaquillaReportStyle;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.ReportUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Service
public class InformeIncidenciasReport extends Report implements InformeInterface, ApplicationContextAware {
	private static ApplicationContext appContext;
    SesionesDAO sesionesDAO;
    CinesDAO cinesDAO;

    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;
	Configuration configuration;
	String logoReport;

    public InformeIncidenciasReport() throws ReportSerializerInitException {
        super(reportSerializer, new InformeTaquillaReportStyle());
    }

    private InformeIncidenciasReport(ReportSerializer serializer, InformeTaquillaReportStyle style, Locale locale, Configuration configuration, String logoReport)
            throws ReportSerializerInitException {
        super(serializer, style);

        this.cinesDAO = appContext.getBean(CinesDAO.class);
        this.sesionesDAO = appContext.getBean(SesionesDAO.class);

        this.style = style;
        this.locale = locale;
		this.configuration = configuration;
		this.logoReport = logoReport;
    }

    private List<InformeSesion> getInformeSesiones(String fechaInicio, String fechaFin, String userUID) throws ParseException {
        List<SesionDTO> sesionesDTO = sesionesDAO.getSesionesCinePorFechas(DateUtils.databaseWithSecondsToDate(fechaInicio + " " +
				"00:00:00"),
				DateUtils.databaseWithSecondsToDate(fechaFin + " 23:59:59"), "fechaCelebracion", userUID);
		List<InformeSesion> listaInformesSesion = new ArrayList<InformeSesion>();

		for (SesionDTO sesionDTO: sesionesDTO) {
			Sesion sesion = Sesion.SesionDTOToSesion(sesionDTO);
			Sala sala = Sala.salaDTOtoSala(sesionDTO.getParSala());
			Evento evento = Evento.eventoDTOtoEvento(sesionDTO.getParEvento());

			InformeSesion informeSesion = new InformeSesion();
			informeSesion.setSala(sala);
			informeSesion.setSesion(sesion);
			informeSesion.setEvento(evento);
			informeSesion.setTipoIncidenciaId(sesion.getIncidenciaId());
			listaInformesSesion.add(informeSesion);
		}
        return listaInformesSesion;
    }

	private void creaCabecera(String userUID)
	{
		BaseTable table = new BaseTable(style, 3, "10cm", "4.6cm", "3.7cm");

		table.withNewRow();
		table.withNewCell(creaLogo());

		Block block = new Block();
		block.getContent().add(ResourceProperties.getProperty(locale, "informeIncidencias.vicerectorat"));
		block.setMarginTop("0.5cm");
		block.setMarginRight("0.5cm");

		TableCell cell = table.withNewCell(block);
		cell.setPadding("0.04cm");
		cell.setBorderRightWidth("0.2cm");
		cell.setBorderRightColor("black");
		cell.setBorderRightStyle(BorderStyleType.SOLID);

		Cine cine = Cine.cineDTOToCine(cinesDAO.getCines(userUID).get(0));
		block = new Block();
		block.getContent().add(getDatosCine(cine));
		block.setMarginTop("0.5cm");
		block.setMarginLeft("0.5cm");
		TableCell cellDerecha = table.withNewCell(block);
		cellDerecha.setPadding("0.04cm");

		add(table);
	}

	private Block creaLogo()
	{
		ExternalGraphic externalGraphic = new ExternalGraphic();
		externalGraphic.setSrc(new File("/etc/uji/par/imagenes/" + logoReport).getAbsolutePath());
		externalGraphic.setMaxWidth("2cm");

		Block block = new Block();
		block.setMarginTop("0cm");
		block.setMarginLeft("0.6cm");
		block.getContent().add(externalGraphic);

		return block;
	}

	private void creaTituloYPeriodo(String inicioTexto, String finTexto)
	{
		Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeIncidencias.titulo"));

		titulo.setMarginTop("1cm");
		titulo.setMarginLeft("6cm");
		add(titulo);

		Block periodo = withNewBlock();
		periodo.setFontWeight("bold");
		periodo.setMarginTop("0.5cm");
		periodo.setMarginLeft("6cm");
		periodo.setMarginBottom("0.5cm");
		periodo.setWhiteSpace(WhiteSpaceType.PRE);

		periodo.getContent().add(
				ResourceProperties.getProperty(locale, "informeIncidencias.periodo", inicioTexto, finTexto));
	}

    public void genera(String fechaInicio, String fechaFin, String userUID) throws ParseException {
		creaCabecera(userUID);
		creaTituloYPeriodo(fechaInicio, fechaFin);
		creaTabla(fechaInicio, fechaFin, userUID);
		creaFirma(configuration.getCargoInformeEfectivo(), configuration.getFirmanteInformeEfectivo());
    }

    private BlockContainer getDatosCine(Cine cine) {
        BlockContainer bc = new BlockContainer();
        bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getNombre()));
        bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getCodigo()));
        bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getDireccion()));
        bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getNombreMunicipio() + " - " + cine.getCp()));
        bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getEmpresa() + " - " + cine.getCif()));
        return bc;
    }

    private Block getCineBlock(String txt) {
        Block b = new Block(style);
        b.setTextAlign(TextAlignType.RIGHT);
        b.setFontWeight("bold");
		b.setFontSize("9pt");
        b.getContent().add(txt);
        return b;
    }

    public Block withNewBlock() {
        Block block = super.withNewBlock();
        block.setFontSize(FONT_SIZE);
        block.setColor(NEGRO);

        return block;
    }

    private Block createBoldBlock(String text) {
        Block block = new Block();

        block.setFontSize(FONT_SIZE);
        block.setFontWeight("bold");
        block.setFontFamily("Arial");
        block.getContent().add(text);

        return block;
    }

    private void creaTabla(String fechaInicio, String fechaFin, String userUID) throws SinIvaException, ParseException {
		BaseTable tableDetalle = new BaseTable(style, 3, "7.2cm", "3.4cm", "6.8cm");
		tableDetalle.withNewRow();
		tableDetalle.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
				"informeIncidencias.eventoTitulo").toUpperCase()));
		tableDetalle.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
				"informeIncidencias.sesionTitulo").toUpperCase()));
		tableDetalle.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
				"informeIncidencias.incidenciaTitulo").toUpperCase()));

		for (InformeSesion informeSesion : getInformeSesiones(fechaInicio, fechaFin, userUID)) {
			tableDetalle.withNewRow();
			tableDetalle.withNewCell(informeSesion.getEvento().getTituloEs());
			tableDetalle.withNewCell(DateUtils.dateToSpanishString(informeSesion.getSesion().getFechaCelebracion()));
			tableDetalle.withNewCell(TipoIncidencia.getNombreTraducido(informeSesion.getTipoIncidenciaId(), locale));
		}
		add(tableDetalle);
    }

    private void creaFirma(String cargoInformeEfectivo, String firmanteInformeEfectivo) {
        Block cargoBlock = withNewBlock();
        cargoBlock.setMarginTop("1cm");
        String cargo = cargoInformeEfectivo;
        cargoBlock.getContent().add(cargo);

        Block nombreBlock = withNewBlock();
        nombreBlock.setMarginTop("2cm");
        nombreBlock.getContent().add(ResourceProperties.getProperty(locale, "informeIncidencias.firmado",
				firmanteInformeEfectivo));

        Calendar fecha = Calendar.getInstance();

        Block fechaBlock = withNewBlock();
        fechaBlock.setMarginTop("1cm");
        fechaBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeIncidencias.fecha",
                        fecha.get(Calendar.DAY_OF_MONTH), ReportUtils.getMesValenciaConDe(fecha), fecha.get(Calendar.YEAR)));
    }

    private static void initStatics() throws ReportSerializerInitException {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
    }

    public InformeInterface create(Locale locale, Configuration configuration, String logoReport) {
        try {
            initStatics();
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeIncidenciasReport(reportSerializer, estilo, locale, configuration, logoReport);
        } catch (ReportSerializerInitException e) {
            throw new RuntimeException(e);
        }
    }

    public void serialize(OutputStream output) throws ReportSerializationException {
        super.serialize(output);
    }

    public void genera(String inicio, String fin, List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
                       BigDecimal totalTaquillaEfectivo, BigDecimal totalOnline) {
        // TODO Auto-generated method stub

    }

    public void genera(String inicio, String fin,
                       List<InformeModelReport> compras) throws SinIvaException {
        // TODO Auto-generated method stub

    }

    public void genera(String inicio, String fin,
                       List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
                       String firmanteInformeEfectivo) throws SinIvaException {
        // TODO Auto-generated method stub

    }

    public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException {
        // TODO Auto-generated method stub
    }

	public void genera(long sesionId, String userUID) throws SinIvaException {

	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
	}
}
