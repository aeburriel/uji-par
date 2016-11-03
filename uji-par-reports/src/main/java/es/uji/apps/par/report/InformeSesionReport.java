package es.uji.apps.par.report;

import es.uji.apps.fopreports.Report;
import es.uji.apps.fopreports.fop.*;
import es.uji.apps.fopreports.serialization.FopPDFSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.fopreports.serialization.ReportSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializerInitException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.exceptions.SinIvaException;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.InformeSesion;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.report.components.BaseTable;
import es.uji.apps.par.report.components.InformeTaquillaReportStyle;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.ReportUtils;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

public class InformeSesionReport extends Report implements InformeInterface
{
    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;
	Configuration configuration;
	String logoReport;
    
    public InformeSesionReport() throws ReportSerializerInitException {
		super(reportSerializer, new InformeTaquillaReportStyle());
	}

    private InformeSesionReport(ReportSerializer serializer, InformeTaquillaReportStyle style, Locale locale, Configuration configuration, String logoReport)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
		this.configuration = configuration;
		this.logoReport = logoReport;
    }

    public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) 
			throws SinIvaException
    {
    	BaseTable table = new BaseTable(style, 2, "6cm", "11cm");
        table.withNewRow();
        table.withNewCell(getLogo());
        table.withNewCell(getDatosCine(cine));
        add(table);

        if (informesSesion.size() > 0)
        {
        	InformeSesion informePrimeraSesion = informesSesion.get(0);
        	if (printSesion)
        	{
        		createCabeceraSesionTitulo();
        	}
        	else
        	{
        		createCabeceraEventoTitulo();
        	}
        	
        	int i = 0;
	        for (InformeSesion informeSesion: informesSesion)
	        {
	        	if (i == 0)
	        	{
	        		creaCabecera(DateUtils.dateToSpanishStringWithHour(informePrimeraSesion.getSesion().getFechaCelebracion()), informePrimeraSesion.getEvento(), informePrimeraSesion.getSala());
		        	if (printSesion)
		        	{
		        		creaIntro();
		        	}
	        	}
	        	else
	        	{
	        		creaCabeceraSesion(DateUtils.dateToSpanishStringWithHour(informeSesion.getSesion().getFechaCelebracion()), informeSesion.getEvento(), informeSesion.getSala());
	        	}
		        creaTablaDetalleTipoEntrada(informeSesion.getCompras());
		        creaTablaDetalleLocalizacion(informeSesion.getCompras());
		        creaTabla(informeSesion.getVendidas(), informeSesion.getAnuladas(), informeSesion.getTotal());
		        i++;
	        }
        }
        creaFirma(cargo, firmante);
    }

    private BlockContainer getDatosCine(Cine cine) {
		BlockContainer bc = new BlockContainer();
		bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getNombre()));
		bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getDireccion()));
		bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getNombreMunicipio() + " - " + cine.getCp()));
		bc.getMarkerOrBlockOrBlockContainer().add(getCineBlock(cine.getEmpresa() + " - " + cine.getCif()));
		return bc;
	}

	private Block getCineBlock(String txt) {
		Block b = new Block(style);
		b.setTextAlign(TextAlignType.RIGHT);
		b.setFontWeight("bold");
		b.getContent().add(txt);
		return b;
	}

	private Block getLogo()
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

    public Block withNewBlock()
    {
        Block block = super.withNewBlock();
        block.setFontSize(FONT_SIZE);
        block.setColor(NEGRO);

        return block;
    }
    
    private Block addBlockWithDatos(String key, Object... values) {
		Block block = withNewBlock();
        block.setFontWeight("bold");
        block.setMarginLeft("6cm");
        block.getContent().add(ResourceProperties.getProperty(locale, key, values));
        return block;
	}

    private void creaCabecera(String inicio, Evento evento, Sala sala)
    {
    	creaCabeceraEvento(inicio, evento, sala);
    	creaCabeceraSesion(inicio, evento, sala);
    }
    
    private void creaCabeceraEvento(String inicio, Evento evento, Sala sala)
    {
        Block bEvento = addBlockWithDatos("informeSesion.evento", evento.getTituloVa());
        bEvento.setMarginTop("0.5cm");
        
        Block bEmpresa = addBlockWithDatos("informeSesion.empresaDistribuidora", evento.getNombreDistribuidora());
        bEmpresa.setWhiteSpace(WhiteSpaceType.PRE);
    }

	private void createCabeceraSesionTitulo() {
		Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.titulo"));

        titulo.setMarginTop("1cm");
        titulo.setMarginLeft("6cm");
        add(titulo);
	}
	
	private void createCabeceraEventoTitulo() {
		Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeEvento.titulo"));

        titulo.setMarginTop("1cm");
        titulo.setMarginLeft("6cm");
        add(titulo);
	}
    
    private void creaCabeceraSesion(String inicio, Evento evento, Sala sala)
    {
        Block periodoSesion = addBlockWithDatos("informeSesion.fechaSesion", inicio);
        periodoSesion.setWhiteSpace(WhiteSpaceType.PRE);
        
        Block bSala = addBlockWithDatos("informeSesion.sala", sala.getNombre(), sala.getCodigo());
        bSala.setWhiteSpace(WhiteSpaceType.PRE);
    }

    private void creaIntro()
    {
        Block intro = withNewBlock();

        intro.setMarginTop("1cm");
        intro.getContent().add(ResourceProperties.getProperty(locale, "informeSesion.intro"));
    }

    private Block createBoldBlock(String text)
    {
        Block block = new Block();

        block.setFontSize(FONT_SIZE);
        block.setFontWeight("bold");
        block.setFontFamily("Arial");
        block.getContent().add(text);

        return block;
    }
    
    private void creaTablaDetalleLocalizacion(List<InformeModelReport> compras) {
    	BaseTable table = new BaseTable(style, 4, "7.2cm", "3.4cm", "3.4cm", "3.4cm");
    	Map<String, List<InformeModelReport>> mapOrdenadoLocalizacion = new HashMap<String, List<InformeModelReport>>();
    	
    	for (InformeModelReport informeModel: compras) {
    		List<InformeModelReport> listaAux = new ArrayList<InformeModelReport>();
    		if (mapOrdenadoLocalizacion.containsKey(informeModel.getLocalizacion()))
    			listaAux = mapOrdenadoLocalizacion.get(informeModel.getLocalizacion());
    		listaAux.add(informeModel);
    		mapOrdenadoLocalizacion.put(informeModel.getLocalizacion(), listaAux);
    	}
    	createCabeceraTabla(table, "informeSesion.localizacion.titulo");
        
        for(String localizacion: mapOrdenadoLocalizacion.keySet()) {
        	Integer vendidas = 0;
        	Integer anuladas = 0;
        	BigDecimal total = new BigDecimal(0);
        	List<InformeModelReport> butacasTarifa = mapOrdenadoLocalizacion.get(localizacion);
        	for (InformeModelReport butaca: butacasTarifa) {
        		if (butaca.getAnulada())
        			anuladas++;
        		else {
        			vendidas++;
        			total = total.add(butaca.getTotal());
        		}
        	}
        	
        	addRowToTablaDetalle(table, localizacion, vendidas, anuladas, total);
        }
        
        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);
    }
    
    private void creaTablaDetalleTipoEntrada(List<InformeModelReport> compras) {
    	BaseTable table = new BaseTable(style, 4, "7.2cm", "3.4cm", "3.4cm", "3.4cm");
    	
    	Map<String, List<InformeModelReport>> mapOrdenadoTarifa = new HashMap<String, List<InformeModelReport>>();
    	
    	for (InformeModelReport informeModel: compras) {
    		List<InformeModelReport> listaAux = new ArrayList<InformeModelReport>();
    		if (mapOrdenadoTarifa.containsKey(informeModel.getTipoEntrada()))
    			listaAux = mapOrdenadoTarifa.get(informeModel.getTipoEntrada());
    		listaAux.add(informeModel);
    		mapOrdenadoTarifa.put(informeModel.getTipoEntrada(), listaAux);
    	}
        createCabeceraTabla(table, "informeSesion.tarifa.titulo");
        
        for(String tarifa: mapOrdenadoTarifa.keySet()) {
        	Integer vendidas = 0;
        	Integer anuladas = 0;
        	BigDecimal total = new BigDecimal(0);
        	List<InformeModelReport> butacasTarifa = mapOrdenadoTarifa.get(tarifa);
        	for (InformeModelReport butaca: butacasTarifa) {
        		if (butaca.getAnulada())
        			anuladas++;
        		else {
        			vendidas++;
        			total = total.add(butaca.getTotal());
        		}
        	}
        	
        	addRowToTablaDetalle(table, tarifa, vendidas, anuladas, total);
        }
        
        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);
	}

	private void addRowToTablaDetalle(BaseTable table, String label, Integer vendidas,
			Integer anuladas, BigDecimal total) {
		table.withNewRow();
		Block labelBlock = createBoldBlock(label);
		TableCell cell = table.withNewCell(labelBlock);
		setBorders(cell);
		//table.withNewCell(labelBlock);

		Block vendidasTarifaBlock = createBoldBlock(vendidas.toString());
		vendidasTarifaBlock.setTextAlign(TextAlignType.CENTER);
		cell = table.withNewCell(vendidasTarifaBlock);
		setBorders(cell);

		Block anuladasTarifaBlock = createBoldBlock(anuladas.toString());
		anuladasTarifaBlock.setTextAlign(TextAlignType.CENTER);
		cell = table.withNewCell(anuladasTarifaBlock);
		setBorders(cell);

		Block totalTarifaBlock = createBoldBlock(ReportUtils.formatEuros(total));
		totalTarifaBlock.setTextAlign(TextAlignType.CENTER);
		cell = table.withNewCell(totalTarifaBlock);
		setBorders(cell);
	}

	private void createCabeceraTabla(BaseTable table, String tituloTabla) {
		table.withNewRow();
		Block tituloBlock = createBoldBlock(ResourceProperties.getProperty(locale, tituloTabla));
        table.withNewCell(tituloBlock);
        Block vendidasLabelBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.vendidas"));
        vendidasLabelBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(vendidasLabelBlock);

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.anuladas"));
        numeroBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(numeroBlock);

        Block baseBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.total"));
        baseBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(baseBlock);
	}

    private void creaTabla(Integer vendidas, Integer anuladas, BigDecimal total) throws SinIvaException
    {
        BaseTable table = new BaseTable(style, 4, "7.2cm", "3.4cm", "3.4cm", "3.4cm");

        table.withNewRow();
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.total.titulo")));
        
        
        Block vendidasLabelBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.vendidas"));
        vendidasLabelBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(vendidasLabelBlock);

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.anuladas"));
        numeroBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(numeroBlock);

        Block baseBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeSesion.tabla.total"));
        baseBlock.setTextAlign(TextAlignType.CENTER);
        table.withNewCell(baseBlock);
        
        table.withNewRow();
        table.withNewCell("");

        Block vendidasBlock = createBoldBlock(vendidas.toString());
        vendidasBlock.setTextAlign(TextAlignType.CENTER);
        TableCell cell = table.withNewCell(vendidasBlock);
        setBorders(cell);

        Block anuladasBlock = createBoldBlock(anuladas.toString());
        anuladasBlock.setTextAlign(TextAlignType.CENTER);
        cell = table.withNewCell(anuladasBlock);
        setBorders(cell);

        Block totalBlock = createBoldBlock(ReportUtils.formatEuros(total));
        totalBlock.setTextAlign(TextAlignType.CENTER);
        cell = table.withNewCell(totalBlock);
        setBorders(cell);

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.setMarginBottom("1cm");
        block.getContent().add(table);

    }

    private void creaFirma(String cargoInformeEfectivo, String firmanteInformeEfectivo)
    {
        Block cargoBlock = withNewBlock();
        cargoBlock.setMarginTop("1cm");
        String cargo = cargoInformeEfectivo;
        cargoBlock.getContent().add(cargo);

        Block nombreBlock = withNewBlock();
        nombreBlock.setMarginTop("2cm");
        nombreBlock.getContent().add(ResourceProperties.getProperty(locale, "informeEfectivo.subtotales.firmado", firmanteInformeEfectivo));

        Calendar fecha = Calendar.getInstance();

        Block fechaBlock = withNewBlock();
        fechaBlock.setMarginTop("1cm");
        fechaBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeEfectivo.subtotales.fecha",
                        fecha.get(Calendar.DAY_OF_MONTH), ReportUtils.getMesValenciaConDe(fecha), fecha.get(Calendar.YEAR)));
    }

    private void setBorders(TableCell cell)
    {
        cell.setBorderTopWidth("0.05cm");
        cell.setBorderTopColor("black");
        cell.setBorderTopStyle(BorderStyleType.SOLID);

        cell.setBorderBottomWidth("0.05cm");
        cell.setBorderBottomColor("black");
        cell.setBorderBottomStyle(BorderStyleType.SOLID);
    }

    private static void initStatics() throws ReportSerializerInitException
    {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
    }

    public InformeInterface create(Locale locale, Configuration configuration, String logoReport)
    {
        try
        {
            initStatics();
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeSesionReport(reportSerializer, estilo, locale, configuration, logoReport);
        }
        catch (ReportSerializerInitException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void serialize(OutputStream output) throws ReportSerializationException
    {
        super.serialize(output);
    }

	public void genera(String inicio, String fin, List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
			BigDecimal totalTaquillaEfectivo, BigDecimal totalTaquillaTransferencia, BigDecimal totalOnline) {
		// TODO Auto-generated method stub
		
	}

	public void genera(String inicio, String fin,
			List<InformeModelReport> compras) throws SinIvaException {
		// TODO Auto-generated method stub
		
	}

    public void genera(long sesionId, String userUID) throws SinIvaException {
        // TODO Auto-generated method stub

    }

	public void genera(String fechaInicio, String fechaFin, String userUID) {

	}

	public void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo, String firmanteInformeEfectivo) throws SinIvaException {
		genera(null, inicio, fin, compras, abonos, cargoInformeEfectivo, firmanteInformeEfectivo);
	}

	public void genera(String titulo, String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo, String firmanteInformeEfectivo) throws SinIvaException {

	}
}
