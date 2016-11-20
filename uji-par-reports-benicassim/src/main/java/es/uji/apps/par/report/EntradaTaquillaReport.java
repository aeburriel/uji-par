package es.uji.apps.par.report;

import es.uji.apps.fopreports.fop.*;
import es.uji.apps.fopreports.serialization.FopPDFSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.fopreports.serialization.ReportSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializerInitException;
import es.uji.apps.fopreports.style.ReportStyle;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.report.components.BaseTable;
import es.uji.apps.par.report.components.EntradaReportStyle;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class EntradaTaquillaReport extends BenicassimBaseReport implements EntradaReportTaquillaInterface
{
    private static final String FONDO_BLANCO = "#FFFFFF";
    private static final String GRIS_OSCURO = "#666666";
    private static final int sizeTxtTituloEvento = 22;
    private static final int sizeTxtZonaFilaButacaInt = 15;
    private static final String sizeTxtZonaFilaButaca = sizeTxtZonaFilaButacaInt + "pt";
	private static final String sixeZonaImpresion = "72.2mm";
	private static final String font = "Verdana";

    private static FopPDFSerializer reportSerializer;
    private static FopFactory fopFactory;

    private Locale locale;

    private String titulo;
    private String fecha;
    private String hora;
    private String horaApertura;
    private String zona;
    private String fila;
    private String numero;
    private String total;
    private String barcode;
    private String tipoEntrada;
    private String iniciales;

    private Configuration configuration;

	public EntradaTaquillaReport() throws ReportSerializerInitException {
    	super(reportSerializer, new EntradaReportStyle());
    }

    private EntradaTaquillaReport(ReportSerializer serializer, ReportStyle style, Locale locale, Configuration configuration)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.locale = locale;
        this.configuration = configuration;
        
        SimplePageMaster reciboPageMaster = withSimplePageMaster();
        reciboPageMaster.setMasterName("reciboPinpad");
        reciboPageMaster.setPageWidth("5cm");
        reciboPageMaster.setPageHeight("8cm");
        
        RegionBody regionBody = new RegionBody();
        reciboPageMaster.setRegionBody(regionBody);
    }

    public void generaPaginaButaca(EntradaModelReport entrada, String urlPublic)
    {
    	this.fila = entrada.getFila();
    	this.numero = entrada.getNumero();
        this.zona = entrada.getZona();
        this.iniciales = entrada.getIniciales();
        this.total = entrada.getTotal();
        this.barcode = entrada.getBarcode();
        this.tipoEntrada = entrada.getTipo();

        creaSeccionEntrada(urlPublic);
        Block pageBreak = withNewBlock();
        pageBreak.setPageBreakAfter(PageBreakAfterType.ALWAYS);
    }
    
    public void generaPaginasReciboPinpad(String reciboPinpad)
    {
        PageSequence pageSequence = withNewPageSequence();
        pageSequence.setMasterReference("reciboPinpad");
        
        Block block = new Block();
        block.setLinefeedTreatment(LinefeedTreatmentType.PRESERVE);
        block.setFontSize("9pt");
        
        block.getContent().add(reciboPinpad);
        
        Block pageBreak = new Block();
        pageBreak.setPageBreakAfter(PageBreakAfterType.ALWAYS);
        
        Flow flow = new Flow();
        flow.setFlowName("xsl-region-body");
        pageSequence.setFlow(flow);
        
        flow.getMarkerOrBlockOrBlockContainer().add(block);
        flow.getMarkerOrBlockOrBlockContainer().add(pageBreak);
    }

    private void creaSeccionEntrada(String urlPublic)
    {
        Block entradaBlock = withNewBlock();

        EntradaReportStyle style = getStyleWithFont();
        style.setSimplePageMasterMarginBottom("0");
        style.setSimplePageMasterRegionBodyMarginBottom("0");
        style.setSimplePageMasterMarginTop("0");
        style.setSimplePageMasterRegionBodyMarginTop("0");

        BaseTable entradaTable = new BaseTable(style, 1, EntradaTaquillaReport.sixeZonaImpresion);
        if (configuration.isTicketLogo())
        {
            entradaTable.withNewRow();
            entradaTable.withNewCell(createLogo());
        }
        entradaTable.withNewRow();
        entradaTable.withNewCell(createEntradaIzquierdaCentro(urlPublic));
        entradaTable.withNewRow();
        entradaTable.withNewCell(createHorizontalLine());
        entradaTable.withNewRow();
        entradaTable.withNewCell(createEntradaDerecha(urlPublic));
        entradaBlock.getContent().add(entradaTable);
    }
    
    private Block createEntradaDerecha(String urlPublic) {
    	Block blockGeneral = new Block();
    	Block blockIzquierda = new Block();
    	
    	BlockContainer bc = new BlockContainer();
    	bc.setWidth("100%");
        final int fontSize = 13;
        final String fontSizeTxt = fontSize + "pt";
        bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(getTextoRecortadoMostrar(this.titulo, 120, Font.PLAIN, fontSize), fontSizeTxt));

    	if (this.fila != null && this.numero != null)
                bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(this.iniciales + " " + this.fila + "-" + this.numero, fontSizeTxt));
        bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(getTextoRecortadoMostrar(this.tipoEntrada + " " + this.total + " €", 120, Font.PLAIN, fontSize), fontSizeTxt));
        bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(this.fecha + "-" + this.hora, fontSizeTxt));
        blockIzquierda.getContent().add(bc);
        
    	BaseTable table = new BaseTable(getStyleWithFont(), 2, "42.2mm", "30mm");
        table.withNewRow();
        table.withNewCell(blockIzquierda);
        TableCell c = table.withNewCell(createBarcode(urlPublic));
        c.setTextAlign(TextAlignType.RIGHT);
        
        blockGeneral.getContent().add(table);
        Block referencia = getBlockWithText(this.barcode, "7pt");
        referencia.setTextAlign(TextAlignType.CENTER);
        blockGeneral.getContent().add(referencia);
        return blockGeneral;
    }
    
    private Block createLogo() {
    	Block b = new Block();
    	b.getContent().add(getLogo());
    	return b;
	}

	private Block createHorizontalLine()
    {
        Leader line = new Leader();
        line.setColor(GRIS_OSCURO);
        line.setBorderAfterStyle(BorderStyleType.DOTTED);
        line.setLeaderLengthOptimum("100%");

        Block b = new Block();
        b.getContent().add(line);

        return b;
    }
    
	private Block createEntradaIzquierdaCentro(String urlPublic)
    {
		Block b = new Block();
		EntradaReportStyle reportStyle = getStyleWithFont();
        BaseTable table = new BaseTable(reportStyle, 1, EntradaTaquillaReport.sixeZonaImpresion);

        table.setMarginTop("0");

        table.withNewRow();
        table.withNewCell(createTitulo());

        table.withNewRow();
        TableCell cellEnmedio = table.withNewCell(createEntradaFechaHoras());
        cellEnmedio.setPadding("0cm");
        cellEnmedio.setBackgroundColor(FONDO_BLANCO);
        
        table.withNewRow();
        table.withNewCell(createZona());
        
        table.withNewRow();
        table.withNewCell(createFilaButacaYUuid());
        b.getContent().add(table);
        return b;
    }
	
	private Block createTipoEntradaPrecio() {
		String txtTipoEntradaPrecio = getTipoEntradaPrecio();
		Block block = getBlockWithText(txtTipoEntradaPrecio, "10pt");
		
		return block;
	}

	private String getTipoEntradaPrecio() {
		return ResourceProperties.getProperty(locale, "entrada.precio").toUpperCase() + this.tipoEntrada + " " + this.total + " €";
	}

	private Block createFilaButacaYUuid() {
	    
	    Block b;
        if (this.fila != null && this.numero != null)
        {
            b = createFilaYButaca();
        }
        else
        {
            b = new Block();
        }
        
        Block blockUuid = getBlockWithText(this.barcode, "7pt");
        blockUuid.setFontWeight("normal");
        blockUuid.setMarginTop("0.1cm");
        blockUuid.setMarginBottom("0.1cm");
        b.getContent().add(blockUuid);
        
        b.getContent().add(createCondicionesYWeb());
		
		return b;
	}

    private Block createFilaYButaca()
    {
		Block filaButaca = new Block();

		filaButaca.setFontSize(EntradaTaquillaReport.sizeTxtZonaFilaButaca);
		filaButaca.setFontWeight("bold");
		filaButaca.setWrapOption(WrapOptionType.WRAP);
		
       	filaButaca.getContent().add(getFilaButaca());
        return filaButaca;
    }
    
    private String getFilaButaca() {
    	String txtFila = ResourceProperties.getProperty(locale, "entrada.fila") + this.fila;
       	String txtButaca = ResourceProperties.getProperty(locale, "entrada.butacaSimple") + this.numero;
       	return txtFila + " | " + txtButaca; 
    }

	private Block createZona() {
		String txtZona = this.zona.toUpperCase();
		Block blkZona = getBlockWithText(getTextoRecortadoMostrar(txtZona, 180, Font.PLAIN, EntradaTaquillaReport.sizeTxtZonaFilaButacaInt), EntradaTaquillaReport.sizeTxtZonaFilaButaca);

		return blkZona;
	}

	private Block createTitulo(){
		Block titulo = getAdjustedBlock(this.titulo, 480, 10);
        titulo.setMarginBottom("0.1cm");

        return titulo;
	}
	
	private ExternalGraphic getLogo()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/logo.svg").getAbsolutePath());
        externalGraphic.setContentWidth(EntradaTaquillaReport.sixeZonaImpresion);

        return externalGraphic;
    }
	
	private Block createEntradaFechaHoras()
    {
        Block block = new Block();
        BaseTable table = new BaseTable(getStyleWithFont(), 1, EntradaTaquillaReport.sixeZonaImpresion);
        table.withNewRow();
        
        String dia = getDiaEvento();
        String hora =  getHoraEvento();
        String horaApertura = getHoraApertura();
        
        table.withNewCell(getBlockWithText(dia + " | " + hora, "12pt"));
        
        table.withNewRow();
        TableCell celdaApertura = table.withNewCell(getBlockWithText(horaApertura , "7pt"));
        celdaApertura.setPaddingTop("0.1cm");
        table.withNewRow();
        
        table.withNewCell(createTipoEntradaPrecio());

        block.getContent().add(table);
        return block;
    }

	private String getHoraEvento() {
		return this.hora + " " + ResourceProperties.getProperty(locale, "entrada.horas");
	}

	private String getDiaEvento() {
		return ResourceProperties.getProperty(locale, "entrada.dia") + this.fecha;
	}
	
	private String getHoraApertura() {
		String horaApertura = ResourceProperties.getProperty(locale, "entrada.apertura") + ": ";
        
        if (this.horaApertura != null)
            horaApertura += this.horaApertura + " " + ResourceProperties.getProperty(locale, "entrada.horas"); 
        return horaApertura;
	}

    private BaseTable createCondicionesYWeb()
    {
        BaseTable table = new BaseTable(getStyleWithFont(), 1, "7.2cm");
        table.withNewRow();
        Block valida = getBlockWithText(ResourceProperties.getProperty(locale, "entrada.entradaValida"), "8pt");
        valida.setTextAlign(TextAlignType.JUSTIFY);
        table.withNewCell(valida);

        table.withNewRow();
        Block web = getBlockWithText(ResourceProperties.getProperty(locale, "entrada.condicionesWeb"), "8pt");
        valida.setTextAlign(TextAlignType.JUSTIFY);
        table.withNewCell(web);
        return table;
    }

    private Block createBarcode(String urlPublic)
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(urlPublic + "/rest/barcode/" + this.barcode);

        Block blockCodebar = new Block();
        blockCodebar.getContent().add(externalGraphic);

        return blockCodebar;
    }
    
    private static void initStatics() throws ReportSerializerInitException, SAXException, IOException
    {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
        
        fopFactory = FopFactory.newInstance();
        fopFactory.setUserConfig(new File("/etc/uji/par/fop.xconf"));
    }

    public EntradaTaquillaReport create(Locale locale, Configuration configuration) throws SAXException, IOException
    {
        try
        {
            initStatics();
            EntradaReportStyle reportStyle = new EntradaReportStyle();
            reportStyle.setFontFamily(EntradaTaquillaReport.font);
            reportStyle.setSimplePageMasterMarginRight("3.7mm");
            reportStyle.setSimplePageMasterMarginLeft("3.85mm");
            reportStyle.setSimplePageMasterMarginBottom("0.3cm");
            reportStyle.setSimplePageMasterMarginTop("0.3cm");
            reportStyle.setSimplePageMasterPageWidth("8cm");
            reportStyle.setSimplePageMasterPageHeight("15cm");
            reportStyle.setSimplePageMasterRegionBeforeExtent("0cm");
            reportStyle.setSimplePageMasterRegionAfterExtent("0cm");
            reportStyle.setSimplePageMasterRegionBodyMarginTop("0cm");
            reportStyle.setSimplePageMasterRegionBodyMarginBottom("0cm");

            return new EntradaTaquillaReport(reportSerializer, reportStyle, locale, configuration);
        }
        catch (ReportSerializerInitException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void serialize(OutputStream output) throws ReportSerializationException
    {
        super.serialize(output, fopFactory);
    }

    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }
    
    public void setFecha(String fecha)
    {
        this.fecha = fecha;
    }
    
    public void setHora(String hora)
    {
        this.hora = hora;
    }
    
    public void setHoraApertura(String horaApertura)
    {
        this.horaApertura = horaApertura;
    }
    
    private EntradaReportStyle getStyleWithFont() {
    	EntradaReportStyle reportStyle = new EntradaReportStyle();
        reportStyle.setFontFamily(EntradaTaquillaReport.font);
        return reportStyle;
    }
    
    public void setUrlPortada(String urlPortada) {
		
	}
}
