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

import java.io.File;
import java.io.OutputStream;
import java.util.Locale;

public class EntradaReport extends BenicassimBaseReport implements EntradaReportOnlineInterface
{
    private static final String GRIS_OSCURO = "#666666";
    private static final String FONDO_GRIS = "#EEEEEE";
    private static final String FONDO_BLANCO = "#FFFFFF";
    

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
    private String urlPublicidad;
    private String urlPortada;
    private String barcode;
    private int totalButacas;

	public EntradaReport() throws ReportSerializerInitException {
    	super(reportSerializer, new EntradaReportStyle());
    }

    private EntradaReport(ReportSerializer serializer, ReportStyle style, Locale locale)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.locale = locale;
    }

    public void generaPaginaButaca(EntradaModelReport entrada, String urlPublic)
    {
        this.setFila(entrada.getFila());
        this.setNumero(entrada.getNumero());
        this.setZona(entrada.getZona());
        this.setTotal(entrada.getTotal());
        this.setBarcode(entrada.getBarcode());

        creaSeccionEntrada(urlPublic);
        add(creaHorizontalLine());

        creaSeccionCondiciones(entrada.getTarifaDefecto());
        creaSeccionPublicidad();

        Block pageBreak = withNewBlock();
        pageBreak.setPageBreakAfter(PageBreakAfterType.ALWAYS);
    }

	private void creaSeccionPublicidad()
    {
        Block publicidadBlock = withNewBlock();
        
        publicidadBlock.setMarginTop("0.3cm");

        if (this.urlPublicidad != null && !this.urlPublicidad.contains("example")) {
	        ExternalGraphic externalGraphic = new ExternalGraphic();
	        externalGraphic.setSrc(this.urlPublicidad);
	
	        publicidadBlock.getContent().add(externalGraphic);
        }
    }

    private void creaSeccionCondiciones(Boolean isTarifaDefecto)
    {
    	if (isTarifaDefecto == null)
    		isTarifaDefecto = false;
    	String puntos = "";
    	Block block = new Block();
        Block condicionesBlock = withNewBlock();
        condicionesBlock.setMarginTop("0.3cm");

        block.setFontSize("8pt");
        block.setColor(GRIS_OSCURO);
        block.setFontWeight("bold");
        block.setMarginBottom("0.2em");
        block.getContent().add(ResourceProperties.getProperty(locale, "entrada.condiciones"));
        condicionesBlock.getContent().add(block);

        block = new Block();
        block.setLinefeedTreatment(LinefeedTreatmentType.PRESERVE);
        block.setFontSize("8pt");
        block.setColor(GRIS_OSCURO);
        block.setMarginBottom("0.2em");
        
        if (!isTarifaDefecto) {
        	block.setBackgroundImage("/etc/uji/par/imagenes/entrada_descuento.png");
        	block.setBackgroundRepeat(BackgroundRepeatType.NO_REPEAT);
        	block.setBackgroundPositionVertical("35%");
        }
        
        for (int i = 1; i <= 10; i++)
            puntos += ResourceProperties.getProperty(locale, String.format("entrada.condicion%d", i));
        
        block.getContent().add(puntos);
        condicionesBlock.getContent().add(block);
    }

    private void creaSeccionEntrada(String urlPublic)
    {
        Block entradaBlock = withNewBlock();

        EntradaReportStyle style = new EntradaReportStyle();
        BaseTable entradaTable = new BaseTable(style, 2, "11.8cm", "6.1cm");

        entradaTable.withNewRow();

        TableCell cellIzquierda = entradaTable.withNewCell(createEntradaIzquierda(urlPublic));
        cellIzquierda.setPadding("0.3cm");
        cellIzquierda.setPaddingTop("0.0cm");
        cellIzquierda.setBackgroundColor(FONDO_GRIS);

        TableCell cellDerecha = entradaTable.withNewCell(createEntradaDerecha());
        cellDerecha.setPadding("0.3cm");
        cellDerecha.setPaddingTop("0.3cm");
        cellDerecha.setBackgroundColor(FONDO_GRIS);
        cellDerecha.setBorderLeftWidth("0.03cm");
        cellDerecha.setBorderLeftColor("white");
        cellDerecha.setBorderLeftStyle(BorderStyleType.DOTTED);

        TableRow rowAbajo = entradaTable.withNewRow();
        rowAbajo.setBackgroundColor(FONDO_GRIS);
        entradaTable.withNewCell(ResourceProperties.getProperty(locale, "entrada.entradaValida"), "2");

        entradaBlock.getContent().add(entradaTable);
    }

    private Block createEntradaIzquierda(String urlPublic)
    {
        Block block = new Block();

        block.getContent().add(createEntradaIzquierdaArriba());
        block.getContent().add(createEntradaIzquierdaCentro());
        block.getContent().add(createEntradaIzquierdaAbajo(urlPublic));

        return block;
    }

    private BaseTable createEntradaIzquierdaArriba()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "9cm", "2cm");

        table.withNewRow();
        Block blockTeatro = getBlockWithText(ResourceProperties.getProperty(locale, "entrada.nombreLocalizacion"), "17pt", true, false);
        Block blockEntidad = getBlockWithText(ResourceProperties.getProperty(locale, "entrada.nombreEntidad"), "10pt");
        Block blockDireccion = getBlockWithText(ResourceProperties.getProperty(locale, "entrada.direccion"), "10pt");
        BlockContainer bc = new BlockContainer();
        bc.setWidth("100%");
        bc.getMarkerOrBlockOrBlockContainer().add(blockTeatro);
        bc.getMarkerOrBlockOrBlockContainer().add(blockEntidad);
        bc.getMarkerOrBlockOrBlockContainer().add(blockDireccion);
        table.withNewCell(bc);

        TableCell cell = table.withNewCell(getLogo());
        cell.setTextAlign(TextAlignType.CENTER);
        cell.setDisplayAlign(DisplayAlignType.CENTER);
        cell.setPaddingTop("0px");

        return table;
    }

    private BaseTable createEntradaIzquierdaCentro()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 4, "3cm", "8cm");

        table.setMarginTop("0.2cm");

        table.withNewRow();

        TableCell cellIzquierda = table.withNewCell(createEntradaIzquierdaCentroFoto());
        cellIzquierda.setPadding("0.2cm");
        cellIzquierda.setBackgroundColor(FONDO_BLANCO);

        TableCell cellEnmedio = table.withNewCell(createEntradaIzquierdaCentroDatos());
        cellEnmedio.setPadding("0.2cm");
        cellEnmedio.setBackgroundColor(FONDO_BLANCO);
        return table;
    }

    private Block createEntradaIzquierdaCentroFoto()
    {
        Block block = new Block();

        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(this.urlPortada);
        externalGraphic.setContentWidth("2.5cm");
        externalGraphic.setMaxWidth("2.5cm");

        block.getContent().add(externalGraphic);

        return block;
    }

    private ExternalGraphic getLogo()
    {
    	ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/logo.svg").getAbsolutePath());
        externalGraphic.setContentWidth("2.5cm");
        return externalGraphic;
    }

    private Block createEntradaIzquierdaCentroDatos()
    {
        Block block = new Block();

        BaseTable table = new BaseTable(new EntradaReportStyle(), 3, "2.5cm", "1.8cm", "2.1cm");

        Block titulo = getAdjustedBlock(this.titulo, 350, 10);
        titulo.setMarginBottom("0.2cm");

        table.withNewRow();
        table.withNewCell(titulo, "3");

        table.withNewRow();
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.fecha"));
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.hora"));
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.apertura"));

        table.withNewRow();
        table.withNewCell(this.fecha);
        table.withNewCell(this.hora);
        table.withNewCell(this.horaApertura);

        table.withNewRow();
        TableCell cell = table.withNewCell(ResourceProperties.getProperty(locale, "entrada.zona"), "3");
        cell.setPaddingTop("0.2cm");

        Block zona = new Block();
        zona.getContent().add(this.zona);
        zona.setFontSize("12pt");

        table.withNewRow();
        table.withNewCell(zona, "3");

        if (this.fila != null && this.numero != null)
        {
            table.withNewRow();
            table.withNewCell(ResourceProperties.getProperty(locale, "entrada.butaca", this.fila, this.numero), "3");
        }

        block.getContent().add(table);

        return block;
    }

    private BaseTable createEntradaIzquierdaAbajo(String urlPublic)
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "8.5cm", "2.5cm");

        table.setMarginTop("0.2cm");

        table.withNewRow();
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.cif"));
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.total"));

        table.withNewRow();
        table.withNewCell(this.barcode);
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.importe", this.total));

        table.withNewRow();
        table.withNewCell(createBarcode(urlPublic), "2");

        return table;
    }

    private Block createBarcode(String urlPublic)
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(urlPublic + "/rest/barcode/" + this.barcode + "?size=70");

        Block blockCodebar = new Block();
        blockCodebar.getContent().add(externalGraphic);

        return blockCodebar;
    }

    private Block createEntradaDerecha()
    {
        Block block = new Block();

        block.getContent().add(createEntradaDerechaArriba());
        block.getContent().add(createEntradaDerechaCentro());
        block.getContent().add(createEntradaDerechaAbajo());

        return block;
    }

    private BaseTable createEntradaDerechaArriba()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "3cm", "2.5cm");

        table.withNewRow();
        table.withNewCell("");

        TableCell cellLogo = table.withNewCell(getLogo());
        cellLogo.setTextAlign(TextAlignType.RIGHT);
        cellLogo.setDisplayAlign(DisplayAlignType.CENTER);
        cellLogo.setPaddingTop("1px");

        return table;
    }

    private BaseTable createEntradaDerechaCentro()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "2.5cm", "2.5cm");

        String margin = "0.3cm";
        table.setMarginTop("0.5cm");
        table.setMarginBottom(margin);
        table.setMarginLeft(margin);
        table.setMarginRight(margin);

        table.setBackgroundColor(FONDO_BLANCO);

        Block titulo = getAdjustedBlock(this.titulo, 150, 10);
        titulo.setMarginBottom("0.2cm");

        table.withNewRow();
        TableCell tituloCell = table.withNewCell(titulo, "2");
        tituloCell.setPaddingTop("0.2cm");

        table.withNewRow();
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.fecha"));
        table.withNewCell(ResourceProperties.getProperty(locale, "entrada.hora"));

        table.withNewRow();
        table.withNewCell(this.fecha);
        table.withNewCell(this.hora);

        table.withNewRow();
        TableCell aperturaCell = table.withNewCell(ResourceProperties.getProperty(locale, "entrada.apertura"), "2");
        aperturaCell.setPaddingTop("0.2cm");

        table.withNewRow();
        table.withNewCell(this.horaApertura, "2");

        table.withNewRow();
        TableCell cell = table.withNewCell(ResourceProperties.getProperty(locale, "entrada.zona"), "2");
        cell.setPaddingTop("0.2cm");

        Block zona = new Block();
        zona.getContent().add(this.zona);
        zona.setFontSize("12pt");

        table.withNewRow();
        TableCell zonaCell = table.withNewCell(zona, "2");

        if (this.fila != null && this.numero != null)
        {
            table.withNewRow();
            TableCell butacaCell = table.withNewCell(
                    ResourceProperties.getProperty(locale, "entrada.butaca", this.fila, this.numero), "2");
            butacaCell.setPaddingBottom("0.2cm");
        }
        else
        {
            zonaCell.setPaddingBottom("0.2cm");
        }

        return table;
    }

    private Table createEntradaDerechaAbajo()
    {
        BaseTable table = new BaseTable(new EntradaReportStyle(), 2, "3cm", "2.5cm");

        table.withNewRow();
        table.withNewCell("");
        TableCell cell = table.withNewCell(ResourceProperties.getProperty(locale, "entrada.total"));
        cell.setTextAlign(TextAlignType.RIGHT);
        cell.setPaddingRight("10px");

        table.withNewRow();
        table.withNewCell("");
        TableCell cell1 = table.withNewCell(ResourceProperties.getProperty(locale, "entrada.importe", this.total));
        cell1.setTextAlign(TextAlignType.RIGHT);
        cell1.setPaddingRight("10px");

        return table;
    }

    public Block creaHorizontalLine()
    {
        Leader line = new Leader();
        line.setColor(GRIS_OSCURO);
        line.setBorderAfterStyle(BorderStyleType.DASHED);
        line.setLeaderLengthOptimum("100%");

        Block b = new Block();
        b.getContent().add(line);

        return b;
    }

    private static void initStatics() throws ReportSerializerInitException
    {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
    }

    public EntradaReportOnlineInterface create(Locale locale)
    {
        try
        {
            initStatics();
            EntradaReportStyle estilo = new EntradaReportStyle();
            estilo.setSimplePageMasterMarginBottom("0cm");
            estilo.setSimplePageMasterRegionBodyMarginBottom("0cm");
            return new EntradaReport(reportSerializer, estilo, locale);
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

    public void setZona(String zona)
    {
        this.zona = zona;
    }

    public void setFila(String fila)
    {
        this.fila = fila;
    }

    public void setNumero(String numero)
    {
        this.numero = numero;
    }

    public void setTotal(String total)
    {
        this.total = total;
    }

    public void setUrlPublicidad(String urlPublicidad)
    {
        this.urlPublicidad = urlPublicidad;
    }

    public void setUrlPortada(String urlPortada)
    {
        this.urlPortada = urlPortada;
    }

    public void setBarcode(String barcode)
    {
        this.barcode = barcode;
    }

    @Override
    public boolean esAgrupada() {
        return false;
    }

    @Override
    public void setTotalButacas(int totalButacas) {
        this.totalButacas = totalButacas;
    }
}
