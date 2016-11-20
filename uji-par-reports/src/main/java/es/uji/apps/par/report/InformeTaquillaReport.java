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
import es.uji.apps.par.model.InformeSesion;
import es.uji.apps.par.report.components.BaseTable;
import es.uji.apps.par.report.components.InformeTaquillaReportStyle;
import es.uji.apps.par.utils.ReportUtils;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class InformeTaquillaReport extends Report implements InformeInterface
{
    private static final String FONT_SIZE = "10pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;
	Configuration configuration;
    
    public InformeTaquillaReport() throws ReportSerializerInitException {
		super(reportSerializer, new InformeTaquillaReportStyle());
	}

    private InformeTaquillaReport(ReportSerializer serializer, InformeTaquillaReportStyle style, Locale locale, Configuration configuration)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
		this.configuration = configuration;
    }

    public void genera(String inicio, String fin, List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
            BigDecimal totalTaquillaEfectivo, BigDecimal totalOnline)
    {
        creaLogo();
        creaCabecera(inicio, fin);
        creaTabla(compras);
        creaTotales(compras);
        creaSubtotales(totalTaquillaTPV, totalTaquillaEfectivo, totalOnline);
    }

    private void creaLogo()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/" + configuration.getLogoReport()).getAbsolutePath());
        externalGraphic.setMaxWidth("2cm");

        Block block = withNewBlock();
        block.setMarginTop("2.2cm");
        block.setMarginLeft("0.6cm");
        block.getContent().add(externalGraphic);
    }

    public Block withNewBlock()
    {
        Block block = super.withNewBlock();
        block.setFontSize(FONT_SIZE);
        block.setColor(NEGRO);

        return block;
    }

    private void creaCabecera(String inicioTexto, String finTexto)
    {
        Block titulo = withNewBlock();

        titulo.setMarginTop("2.5cm");
        titulo.setMarginLeft("6cm");
        //titulo.setTextAlign(TextAlignType.CENTER);
        titulo.getContent().add(ResourceProperties.getProperty(locale, "informeTaquilla.titulo"));

        Block periodo = withNewBlock();
        periodo.setMarginTop("0.5cm");
        periodo.setMarginLeft("6cm");
        periodo.setWhiteSpace(WhiteSpaceType.PRE);

        periodo.getContent().add(
                ResourceProperties.getProperty(locale, "informeTaquilla.periodo", inicioTexto, finTexto));
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

    private void creaTabla(List<InformeModelReport> compras)
    {
        BaseTable table = new BaseTable(style, 5, "6cm", "4cm", "3cm", "3cm", "2.5cm");

        table.withNewRow();
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeTaquilla.tabla.evento")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeTaquilla.tabla.tipo")));
        table.withNewCell(createBoldBlock(ResourceProperties
                .getProperty(locale, "informeTaquilla.tabla.onlineTaquilla")));

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeTaquilla.tabla.numero"));
        numeroBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(numeroBlock);

        Block totalBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeTaquilla.tabla.total"));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(totalBlock);

        for (InformeModelReport dato : compras)
        {
            table.withNewRow();
            table.withNewCell(dato.getEvento());
            table.withNewCell(dato.getTipoEntrada());
            table.withNewCell(dato.getTipoCompra());
            table.withNewCell(blockAlignRight(Integer.toString(dato.getNumeroEntradas())));
            table.withNewCell(blockAlignRight(String.format("%.2f", dato.getTotal())));
        }

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);

        /*
         informeTaquilla.tabla.evento=Event
        informeTaquilla.tabla.tipo=Tipus d'entrada
        informeTaquilla.tabla.onlineTaquilla= Online o taquilla
        informeTaquilla.tabla.numero=Nombre d'entrades
        informeTaquilla.tabla.total=Total
         */
    }

    private Block blockAlignRight(String text)
    {
        Block blockEntradas = new Block();
        blockEntradas.getContent().add(text);
        blockEntradas.setTextAlign(TextAlignType.RIGHT);
        return blockEntradas;
    }

    private void creaTotales(List<InformeModelReport> compras)
    {
        Block block = withNewBlock();
        block.setMarginTop("1cm");
        //block.setMarginLeft("10cm");
        //block.setWhiteSpace(WhiteSpaceType.PRE);

        BaseTable table = new BaseTable(style, 4, "10cm", "3cm", "3cm", "2.5cm");

        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquilla.totales")));
        setBorders(cell);

        Block entradasBlock = createBoldBlock(Integer.toString(sumaEntradas(compras)));
        entradasBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(entradasBlock);
        setBorders(cell);

        Block eurosBlock = createBoldBlock(ReportUtils.formatEuros(sumaTotalEuros(compras)));
        eurosBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(eurosBlock);
        setBorders(cell);

        block.getContent().add(table);
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

    private int sumaEntradas(List<InformeModelReport> compras)
    {
        int entradas = 0;

        for (InformeModelReport compra : compras)
        {
            entradas += compra.getNumeroEntradas();
        }

        return entradas;
    }

    private BigDecimal sumaTotalEuros(List<InformeModelReport> compras)
    {
        BigDecimal total = new BigDecimal(0);

        for (InformeModelReport compra : compras)
        {
            total = total.add(compra.getTotal());
        }

        return total;
    }

    private void creaSubtotales(BigDecimal totalTaquillaTPV, BigDecimal totalTaquillaEfectivo, BigDecimal totalOnline)
    {
        Block block = withNewBlock();
        block.setMarginTop("1cm");

        BaseTable table = new BaseTable(style, 3, "6cm", "12.1cm", "0.5cm");

        table.withNewRow();

        table.withNewCell("");
        table.withNewCell(ResourceProperties.getProperty(locale, "informeTaquilla.subtotales.tpv"));
        Block blockTaquillaTpv = createBoldBlock(ReportUtils.formatEuros(totalTaquillaTPV));
        blockTaquillaTpv.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(blockTaquillaTpv);

        table.withNewRow();

        table.withNewCell("");
        table.withNewCell(ResourceProperties.getProperty(locale, "informeTaquilla.subtotales.efectivo"));
        Block blockTaquillaEfectivo = createBoldBlock(ReportUtils.formatEuros(totalTaquillaEfectivo));
        blockTaquillaEfectivo.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(blockTaquillaEfectivo);
        

        table.withNewRow();

        table.withNewCell("");
        table.withNewCell(ResourceProperties.getProperty(locale, "informeTaquilla.subtotales.online"));
        Block blockOnline = createBoldBlock(ReportUtils.formatEuros(totalOnline));
        blockOnline.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(blockOnline);

        block.getContent().add(table);
    }

    private static void initStatics() throws ReportSerializerInitException
    {
        if (reportSerializer == null)
            reportSerializer = new FopPDFSerializer();
    }

    public InformeInterface create(Locale locale, Configuration configuration)
    {
        try
        {
            initStatics();
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeTaquillaReport(reportSerializer, estilo, locale, configuration);
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
	public void genera(String inicio, String fin,
			List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
			String firmanteInformeEfectivo) throws SinIvaException {
		// TODO Auto-generated method stub
		
	}

	public void genera(String inicio, String fin,
			List<InformeModelReport> compras) throws SinIvaException {
		// TODO Auto-generated method stub
		
	}

    public void genera(long sesionId) throws SinIvaException {
        // TODO Auto-generated method stub

    }

	public void genera(String fechaInicio, String fechaFin) {

	}

	public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException {
		// TODO Auto-generated method stub
		
	}
}
