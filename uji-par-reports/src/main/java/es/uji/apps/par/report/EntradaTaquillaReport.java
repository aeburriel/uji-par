package es.uji.apps.par.report;

import es.uji.apps.fopreports.Report;
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
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class EntradaTaquillaReport extends Report implements EntradaReportTaquillaInterface
{
    private static final String FONDO_BLANCO = "#FFFFFF";
    private static final String GRIS_OSCURO = "#666666";
    private static final String sizeTxtParanimf = "22pt";
    private static final String sizeTxtDireccionParanimf = "6pt";
    private static final String sizeTxtTituloEvento = "22pt";
    private static final String sizeTxtZonaFilaButaca = "15pt";
    private static final int sizeTxtZonaFilaButacaInt = 15;

    private static FopPDFSerializer reportSerializer;

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
	private String cif;
	private String promotor;
	private String nifPromotor;
	private String nombreEntidad;
	private String direccion;

    public EntradaTaquillaReport() throws ReportSerializerInitException {
    	super(reportSerializer, new EntradaReportStyle());
    }

    private EntradaTaquillaReport(ReportSerializer serializer, ReportStyle style, Locale locale)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.locale = locale;
        
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
        this.total = entrada.getTotal();
        this.barcode = entrada.getBarcode();
        this.tipoEntrada = entrada.getTipo();

        creaSeccionEntrada(urlPublic);
        //this.getSimplePageMaster().setPageHeight("auto");

        Block pageBreak = withNewBlock();
        pageBreak.setPageBreakAfter(PageBreakAfterType.ALWAYS);
    }
    
    public void generaPaginasReciboPinpad(String reciboPinpad)
    {
        PageSequence pageSequence = withNewPageSequence();
        pageSequence.setMasterReference("reciboPinpad");
        
        Block block = new Block();
        //block.setReferenceOrientation("90");
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

        EntradaReportStyle style = new EntradaReportStyle();
        style.setFontFamily("Arial");
        style.setSimplePageMasterMarginBottom("0");
        style.setSimplePageMasterRegionBodyMarginBottom("0");
        style.setSimplePageMasterMarginTop("0");
        style.setSimplePageMasterRegionBodyMarginTop("0");

        BaseTable entradaTable = new BaseTable(style, 1, "10.4cm");

        entradaTable.withNewRow();

        TableCell cellIzquierda = entradaTable.withNewCell(createEntradaIzquierda(urlPublic));
        cellIzquierda.setPadding("0.1cm");
        cellIzquierda.setPaddingTop("0.0cm");
        cellIzquierda.setBackgroundColor(FONDO_BLANCO);

        entradaBlock.getContent().add(entradaTable);
    }

    private Block createEntradaIzquierda(String urlPublic)
    {
        Block block = new Block();

        block.getContent().add(createEntradaIzquierdaArriba());
        block.getContent().add(createEntradaIzquierdaCentro(urlPublic));

        return block;
    }

    private BaseTable createEntradaIzquierdaArriba()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 1, "10.4cm");

        table.withNewRow();
        table.withNewCell(createParanimfDireccionLogo());

        table.withNewRow();
        table.withNewCell(createHorizontalLine());
        return table;
    }
    
    private Block createParanimfDireccionLogo() {
    	String textUJICIF = ResourceProperties.getProperty(locale, "entrada.nombreEntidad") + ". " + ResourceProperties.getProperty(locale, "entrada.cifSimple");
    	String textDireccion = ResourceProperties.getProperty(locale, "entrada.direccion");
    	Block b = new Block();
    	
    	BlockContainer bc = new BlockContainer();
    	bc.getMarkerOrBlockOrBlockContainer().add(createTextParanimf(EntradaTaquillaReport.sizeTxtParanimf));
    	bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(textUJICIF, EntradaTaquillaReport.sizeTxtDireccionParanimf));
    	bc.getMarkerOrBlockOrBlockContainer().add(getBlockWithText(textDireccion, EntradaTaquillaReport.sizeTxtDireccionParanimf));
    	Block bloqueUJIDireccionCIF = new Block();
    	bloqueUJIDireccionCIF.getContent().add(bc);
    	
    	BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "7.5cm", "2.5cm");
    	table.withNewRow();
    	table.withNewCell(bloqueUJIDireccionCIF);
        
        TableCell cell = table.withNewCell(logoUji());
        cell.setTextAlign(TextAlignType.CENTER);
        cell.setDisplayAlign(DisplayAlignType.AFTER);
        
    	b.getContent().add(table);
    	return b;
	}

	private Block createHorizontalLine()
    {
        Leader line = new Leader();
        line.setColor(GRIS_OSCURO);
        line.setBorderAfterStyle(BorderStyleType.SOLID);
        line.setLeaderLengthOptimum("100%");

        Block b = new Block();
        b.getContent().add(line);

        return b;
    }
    
    private Block getBlockWithText(String property, String size) {
    	return getBlockWithText(property, size, false, false);
    }
    
    private Block getBlockWithText(String property, String size, boolean italic, boolean bold) {
    	Block text = new Block();
        text.getContent().add(property);
        text.setFontSize(size);
        
        if (italic)
        	text.setFontStyle(FontStyleType.ITALIC);
        
        if (bold)
            text.setFontWeight("bold");
        
        return text;
	}

	private BaseTable createEntradaIzquierdaCentro(String urlPublic)
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "7.0cm", "3.2cm");

        table.setMarginTop("0");

        table.withNewRow();
        table.withNewCell(createTitulo(), "2");

        table.withNewRow();
        TableCell cellEnmedio = table.withNewCell(createEntradaFechaHoras(), "2");
        cellEnmedio.setPadding("0cm");
        cellEnmedio.setBackgroundColor(FONDO_BLANCO);
        
        table.withNewRow();
        table.withNewCell(createZona(), "2");
        
        table.withNewRow();
        //this.fila = "10";this.numero = "10";
        
        table.withNewCell(createFilaButacaYUuid());
        	
        TableCell cellBarCode = table.withNewCell(createBarcode(urlPublic));
        cellBarCode.setTextAlign(TextAlignType.RIGHT);
        cellBarCode.setPaddingLeft("0.2cm");
        

        return table;
    }
	
	private Block createTipoEntradaPrecio() {
		String txtTipoEntradaPrecio = ResourceProperties.getProperty(locale, "entrada.precio").toUpperCase() + this.tipoEntrada + " " + this.total + " €";
		Block block = getBlockWithText(txtTipoEntradaPrecio, "10pt");
		
		block.setMarginRight("0.3cm");
		
		return block;
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
        
        Block blockUuid = getBlockWithText(this.barcode, "8pt", false, true);
        blockUuid.setMarginTop("0.1cm");
        blockUuid.setMarginBottom("0.05cm");
        b.getContent().add(blockUuid);
        
        b.getContent().add(createCondicionesYWeb());
		
		return b;
	}

    private Block createFilaYButaca()
    {
        Block b = new Block();
		BlockContainer bc = new BlockContainer();
		Block filaButaca = new Block();
		
		filaButaca.setPaddingLeft("0.1cm");
		filaButaca.setPaddingRight("0.1cm");
		filaButaca.setPaddingTop("0.1cm");
		filaButaca.setMarginTop("0.1cm");
		filaButaca.setBorder("1px solid");
		filaButaca.setFontSize(EntradaTaquillaReport.sizeTxtZonaFilaButaca);
		filaButaca.setFontWeight("bold");
		filaButaca.setWrapOption(WrapOptionType.NO_WRAP);
		
       	String txtFila = ResourceProperties.getProperty(locale, "entrada.fila") + this.fila;
       	String txtButaca = ResourceProperties.getProperty(locale, "entrada.butacaSimple") + this.numero;
       	String txtFilaButaca = txtFila + " | " + txtButaca;
       	
       	filaButaca.getContent().add(txtFilaButaca);
       	bc.getMarkerOrBlockOrBlockContainer().add(filaButaca);
       	
       	int width = getWidthTexto(txtFilaButaca, Font.BOLD, EntradaTaquillaReport.sizeTxtZonaFilaButacaInt) - 8;
       	bc.setWidth(width + "px");
       	bc.setWrapOption(WrapOptionType.NO_WRAP);
		b.getContent().add(bc);
        return b;
    }

	private Block createZona() {
		String txtZona = this.zona.toUpperCase();
		Block b = new Block();
		BlockContainer bc = new BlockContainer();
		Block blkZona = new Block();

		blkZona.setPaddingLeft("0.1cm");
		blkZona.setPaddingRight("0.1cm");
		blkZona.setPaddingTop("0.1cm");
		blkZona.setMarginTop("0");
		blkZona.getContent().add(txtZona);
		blkZona.setBorder("1px solid");
		blkZona.setFontSize(EntradaTaquillaReport.sizeTxtZonaFilaButaca);
		blkZona.setWrapOption(WrapOptionType.NO_WRAP);
		bc.getMarkerOrBlockOrBlockContainer().add(blkZona);
		
		int width = getWidthTexto(txtZona, Font.PLAIN, EntradaTaquillaReport.sizeTxtZonaFilaButacaInt) + 8;
		bc.setWidth(width + "px");
		bc.setWrapOption(WrapOptionType.NO_WRAP);
		
		b.getContent().add(bc);
		return b;
	}

	private Block createTitulo() {
		Block titulo = new Block();
        titulo.setFontSize(EntradaTaquillaReport.sizeTxtTituloEvento);
        String textoAMostrar = getTextoAMostrar();
        titulo.setFontStyle(FontStyleType.ITALIC);
        titulo.getContent().add(textoAMostrar);
        titulo.setMarginBottom("0.1cm");

        return titulo;
	}
	
	private int getWidthTexto(String texto, int tipo, int size) {
		Font font = new Font("Arial", tipo, size);
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(font);
        return fm.stringWidth(texto);
	}

    private String getTextoAMostrar() {
        boolean textoIncorrecto = true;
        String texto = this.titulo;
        
        while (textoIncorrecto) {
        	int width = getWidthTexto(texto, Font.ITALIC, 22);
        	if (width > 308)
        		texto = texto.substring(0, texto.lastIndexOf(" ")) + "...";
        	else
        		textoIncorrecto = false;
        }
        return texto;
	}

	private ExternalGraphic logoUji()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/uji_logo.png").getAbsolutePath());
        externalGraphic.setContentWidth("3cm");

        return externalGraphic;
    }

    private Block createEntradaFechaHoras()
    {
        Block block = new Block();
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "3.3cm", "7.4cm");

        table.withNewRow();
        
        String dia = ResourceProperties.getProperty(locale, "entrada.dia") + this.fecha;
        String hora =  this.hora + " " + ResourceProperties.getProperty(locale, "entrada.horas");
        String horaApertura = ResourceProperties.getProperty(locale, "entrada.apertura") + 
        		": ";
        
        if (this.horaApertura != null)
        {
            horaApertura += this.horaApertura + " " + ResourceProperties.getProperty(locale, "entrada.horas"); 
        }
        
        TableCell cell = table.withNewCell(getBlockWithText(dia, "12pt"));
        cell.setBorderRight("1px solid");
        TableCell cellHora = table.withNewCell(getBlockWithText(hora, "12pt"));
        cellHora.setPaddingLeft("0.4cm");
        
        table.withNewRow();
        TableCell celdaApertura = table.withNewCell(getBlockWithText(horaApertura , "7pt"));
        celdaApertura.setPaddingTop("0.1cm");
        
        TableCell celdaTipoEntrada = table.withNewCell(createTipoEntradaPrecio(), "1");
        celdaTipoEntrada.setTextAlign(TextAlignType.RIGHT);

        block.getContent().add(table);
        return block;
    }

    private BaseTable createCondicionesYWeb()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 1, "7.2cm");
        table.withNewRow();
        table.withNewCell(getBlockWithText(ResourceProperties.getProperty(locale, "entrada.entradaValida"), "8pt"));

        table.withNewRow();
        table.withNewCell(getBlockWithText(ResourceProperties.getProperty(locale, "entrada.condicionesWeb"), "8pt"));
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
    
    private Block createTextParanimf(String fontSize)
    {
        Block textParanimf = new Block();
        textParanimf.getContent().add(ResourceProperties.getProperty(locale, "entrada.nombreLocalizacion"));
        textParanimf.setFontSize(fontSize);
        textParanimf.setFontStyle(FontStyleType.ITALIC);
        return textParanimf;
    }

    private static void initStatics() throws ReportSerializerInitException
    {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
    }

    public EntradaTaquillaReport create(Locale locale, Configuration configuration) throws SAXException, IOException
    {
        try
        {
            initStatics();
            EntradaReportStyle reportStyle = new EntradaReportStyle();
            reportStyle.setFontFamily("Arial");
            reportStyle.setSimplePageMasterMarginRight("0.3cm");
            reportStyle.setSimplePageMasterMarginLeft("0.3cm");
            reportStyle.setSimplePageMasterMarginBottom("0.3cm");
            reportStyle.setSimplePageMasterMarginTop("0.3cm");
            reportStyle.setSimplePageMasterPageWidth("11cm");
            reportStyle.setSimplePageMasterPageHeight("8cm");
            reportStyle.setSimplePageMasterRegionBeforeExtent("0cm");
            reportStyle.setSimplePageMasterRegionAfterExtent("0cm");
            reportStyle.setSimplePageMasterRegionBodyMarginTop("0cm");
            reportStyle.setSimplePageMasterRegionBodyMarginBottom("0cm");

            return new EntradaTaquillaReport(reportSerializer, reportStyle, locale);
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

	public void setPromotor(String promotor)
	{
		this.promotor = promotor;
	}

	public void setCif(String cif)
	{
		this.cif = cif;
	}

	public void setNifPromotor(String nifPromotor)
	{
		this.nifPromotor = nifPromotor;
	}

	public void setUrlPortada(String urlPortada) {
		// TODO Auto-generated method stub
		
	}

	public void setNombreEntidad(String nombreEntidad)
	{
		this.nombreEntidad = nombreEntidad;
	}

	public void setDireccion(String direccion)
	{
		this.direccion = direccion;
	}
}
