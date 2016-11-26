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
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InformeTaquillaTpvSubtotalesReport extends Report implements InformeInterface
{
    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;
	private Configuration configuration;

	public InformeTaquillaTpvSubtotalesReport() throws ReportSerializerInitException {
		super(reportSerializer, new InformeTaquillaReportStyle());
	}

    private InformeTaquillaTpvSubtotalesReport(ReportSerializer serializer, InformeTaquillaReportStyle style,
            Locale locale, Configuration configuration) throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
        this.configuration = configuration;
    }

    public void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos,
    		String cargoInformeEfectivo, String firmanteInformeEfectivo) throws SinIvaException
    {
        creaCabecera();
        creaTituloYPeriodo(inicio, fin);
        creaIntro();
        creaTabla(compras);
        creaFirma(cargoInformeEfectivo, firmanteInformeEfectivo);
    }

    private void creaCabecera()
    {
        BaseTable table = new BaseTable(style, 3, "10cm", "4.6cm", "3.7cm");

        table.withNewRow();
        table.withNewCell(creaLogo());

        Block block = new Block();
        block.getContent().add(ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.vicerectorat"));
        block.setMarginTop("0.5cm");
        block.setMarginRight("0.5cm");

        TableCell cell = table.withNewCell(block);
        cell.setPadding("0.04cm");
        cell.setBorderRightWidth("0.2cm");
        cell.setBorderRightColor("black");
        cell.setBorderRightStyle(BorderStyleType.SOLID);

        block = new Block();
        block.getContent().add(ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.tituloCabecera"));
        block.setMarginTop("0.5cm");
        block.setMarginLeft("0.5cm");
        TableCell cellDerecha = table.withNewCell(block);
        cellDerecha.setPadding("0.04cm");

        add(table);
    }

    private Block creaLogo()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/" + configuration.getLogoReport()).getAbsolutePath());
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

    private void creaTituloYPeriodo(String inicioTexto, String finTexto)
    {
        Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.titulo"));

        titulo.setMarginTop("1cm");
        titulo.setMarginLeft("6cm");
        add(titulo);

        Block periodo = withNewBlock();
        periodo.setFontWeight("bold");
        periodo.setMarginTop("0.5cm");
        periodo.setMarginLeft("6cm");
        periodo.setWhiteSpace(WhiteSpaceType.PRE);

        periodo.getContent().add(
                ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.periodo", inicioTexto, finTexto));
    }

    private void creaIntro()
    {
        Block intro = withNewBlock();

        intro.setMarginTop("1cm");
        intro.getContent().add(ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.intro"));
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

    private void creaTabla(List<InformeModelReport> compras) throws SinIvaException
    {
        BaseTable table = new BaseTable(style, 7, "3.6cm", "3.6cm", "2.7cm", "3cm", "1.5cm", "1.5cm", "1.5cm");

        table.withNewRow();
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.evento")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.sesion")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.tipo")));

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.numero"));
        numeroBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(numeroBlock);

        Block baseBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.base"));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(baseBlock);

        Block ivaBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.iva"));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(ivaBlock);

        Block totalBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.tabla.total"));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(totalBlock);

        BigDecimal sumaEntradas = BigDecimal.ZERO;
        BigDecimal sumaBase = BigDecimal.ZERO;
        BigDecimal sumaIva = BigDecimal.ZERO;
        BigDecimal sumaTotal = BigDecimal.ZERO;

        BigDecimal subEntradas = BigDecimal.ZERO;
        BigDecimal subBase = BigDecimal.ZERO;
        BigDecimal subIva = BigDecimal.ZERO;
        BigDecimal subTotal = BigDecimal.ZERO;

        String diaAnterior = "";

        for (InformeModelReport dato : compras)
        {
            if (dato.getIva() == null)
                throw new SinIvaException(dato.getEvento());

            if (cambioDia(dato, diaAnterior))
            {
                creaSubtotales(table, diaAnterior, subEntradas, subBase, subIva, subTotal);

                subEntradas = BigDecimal.ZERO;
                subBase = BigDecimal.ZERO;
                subIva = BigDecimal.ZERO;
                subTotal = BigDecimal.ZERO;
            }

            table.withNewRow();
            table.withNewCell(dato.getEvento());
            table.withNewCell(dato.getSesion());
            table.withNewCell(dato.getTipoEntrada());
            table.withNewCell(blockAlignRight(Integer.toString(dato.getNumeroEntradas())));

            BigDecimal base = calculaBase(dato);
            BigDecimal iva = dato.getTotal().subtract(base);

            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(base)));
            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(iva)));
            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(dato.getTotal())));

            subEntradas = subEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            subBase = subBase.add(base);
            subIva = subIva.add(iva);
            subTotal = subTotal.add(dato.getTotal());
            
            sumaEntradas = sumaEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            sumaBase = sumaBase.add(base);
            sumaIva = sumaIva.add(iva);
            sumaTotal = sumaTotal.add(dato.getTotal());
            
            diaAnterior = dato.getFechaCompra();
        }
        
        creaSubtotales(table, diaAnterior, subEntradas, subBase, subIva, subTotal);

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);

        creaTotales(sumaEntradas, sumaBase, sumaIva, sumaTotal);
    }

    private void creaSubtotales(BaseTable table, String diaAnterior, BigDecimal subEntradas, BigDecimal subBase, BigDecimal subIva, BigDecimal subTotal)
    {
        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.subtotal") + diaAnterior));
        setBorders(cell);

        cell = table.withNewCell("");
        setBorders(cell);

        Block entradasBlock = createBoldBlock(subEntradas.toString());
        entradasBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(entradasBlock);
        setBorders(cell);

        Block baseBlock = createBoldBlock(ReportUtils.formatEuros(subBase));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(baseBlock);
        setBorders(cell);

        Block ivaBlock = createBoldBlock(ReportUtils.formatEuros(subIva));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(ivaBlock);
        setBorders(cell);

        Block totalBlock = createBoldBlock(ReportUtils.formatEuros(subTotal));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(totalBlock);
        setBorders(cell);
        
        table.withNewRow();
        
        Block block = new Block();
        block.setMarginTop("0.5cm");
        
        table.withNewCell("");
        table.withNewCell("");
        table.withNewCell("");
        table.withNewCell("");
        table.withNewCell("");
        table.withNewCell(block);
    }

    private boolean cambioDia(InformeModelReport dato, String diaAnterior)
    {
        return !diaAnterior.equals("") && !diaAnterior.equals(dato.getFechaCompra());
    }

    private BigDecimal calculaBase(InformeModelReport dato)
    {
        BigDecimal divisor = new BigDecimal(1).add(dato.getIva().divide(new BigDecimal(100)));

        return dato.getTotal().divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private Block blockAlignRight(String text)
    {
        Block blockEntradas = new Block();
        blockEntradas.getContent().add(text);
        blockEntradas.setTextAlign(TextAlignType.RIGHT);
        return blockEntradas;
    }

    private void creaTotales(BigDecimal sumaEntradas, BigDecimal sumaBase, BigDecimal sumaIva, BigDecimal sumaTotal)
    {
        Block block = withNewBlock();
        BaseTable table = new BaseTable(style, 7, "3.6cm", "3.6cm", "2.7cm", "3cm", "1.5cm", "1.5cm", "1.5cm");

        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeTaquillaTpvSubtotales.totales")));
        setBorders(cell);

        cell = table.withNewCell("");
        setBorders(cell);

        Block entradasBlock = createBoldBlock(sumaEntradas.toString());
        entradasBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(entradasBlock);
        setBorders(cell);

        Block baseBlock = createBoldBlock(ReportUtils.formatEuros(sumaBase));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(baseBlock);
        setBorders(cell);

        Block ivaBlock = createBoldBlock(ReportUtils.formatEuros(sumaIva));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(ivaBlock);
        setBorders(cell);

        Block totalBlock = createBoldBlock(ReportUtils.formatEuros(sumaTotal));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(totalBlock);
        setBorders(cell);

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
        nombreBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.subtotales.firmado",
                		firmanteInformeEfectivo));

        Calendar fecha = Calendar.getInstance();

        Block fechaBlock = withNewBlock();
        fechaBlock.setMarginTop("1cm");
        fechaBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.subtotales.fecha",
                        fecha.get(Calendar.DAY_OF_MONTH), ReportUtils.getMesValenciaConDe(fecha),
                        fecha.get(Calendar.YEAR)));
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

    public InformeInterface create(Locale locale, Configuration configuration)
    {
        try
        {
            initStatics();
			this.configuration = configuration;
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeTaquillaTpvSubtotalesReport(reportSerializer, estilo, locale, configuration);
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
			List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
			BigDecimal totalTaquillaEfectivo, BigDecimal totalOnline) {
		
	}

	public void genera(String inicio, String fin,
			List<InformeModelReport> compras) throws SinIvaException {
		
	}

	public void genera(long sesionId, String userUID) throws SinIvaException {

	}

	public void genera(String fechaInicio, String fechaFin, String userUID) {

	}

	public void genera(String inicio, List<InformeModelReport> compras,
			String cargoInformeEfectivo, String firmanteInformeEfectivo)
			throws SinIvaException {

	}

	public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException {
		
	}
}
