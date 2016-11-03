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

public class InformeEfectivoReport extends Report implements InformeInterface
{
    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;

	Configuration configuration;
    String logoReport;
    
    public InformeEfectivoReport() throws ReportSerializerInitException {
		super(reportSerializer, new InformeTaquillaReportStyle());
	}

    private InformeEfectivoReport(ReportSerializer serializer, InformeTaquillaReportStyle style, Locale locale, Configuration configuration, String logoReport)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
		this.configuration = configuration;
        this.logoReport = logoReport;
    }

    public void genera(String titulo, String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
            String firmanteInformeEfectivo) throws SinIvaException
    {
        creaLogo();
        creaCabecera(inicio, fin);
        creaIntro();
        creaTabla(compras);
        if (abonos != null && abonos.size() > 0) {
            creaSubtabla(abonos);
        }
        creaFirma(cargoInformeEfectivo, firmanteInformeEfectivo);
    }

    public void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
                       String firmanteInformeEfectivo) throws SinIvaException
    {
        genera(null, inicio, fin, compras, abonos, cargoInformeEfectivo, firmanteInformeEfectivo);
    }

    private void creaLogo()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/" + logoReport).getAbsolutePath());
        externalGraphic.setMaxWidth("2cm");

        Block block = withNewBlock();
        block.setMarginTop("0cm");
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

    private void creaCabecera(String inicio, String fin)
    {
        Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.titulo"));

        titulo.setMarginTop("1cm");
        titulo.setMarginLeft("6cm");
        add(titulo);

        Block periodo = withNewBlock();
        periodo.setFontWeight("bold");
        periodo.setMarginTop("0.5cm");
        periodo.setMarginLeft("6cm");
        periodo.setWhiteSpace(WhiteSpaceType.PRE);


        periodo.getContent().add(
                ResourceProperties.getProperty(locale, "informeEfectivo.periodo", inicio, fin));
    }

    private void creaIntro()
    {
        Block intro = withNewBlock();

        intro.setMarginTop("1cm");
        intro.getContent().add(ResourceProperties.getProperty(locale, "informeEfectivo.intro"));
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
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.evento")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.sesion")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.tipo")));

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.numero"));
        numeroBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(numeroBlock);

        Block baseBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.base"));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(baseBlock);

        Block ivaBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.iva"));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(ivaBlock);

        Block totalBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.total"));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(totalBlock);

        BigDecimal sumaEntradas = BigDecimal.ZERO;
        BigDecimal sumaBase = BigDecimal.ZERO;
        BigDecimal sumaIva = BigDecimal.ZERO;
        BigDecimal sumaTotal = BigDecimal.ZERO;

        for (InformeModelReport dato : compras)
        {
            if (dato.getIva() == null)
                throw new SinIvaException(dato.getEvento());
            
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

            sumaEntradas = sumaEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            sumaBase = sumaBase.add(base);
            sumaIva = sumaIva.add(iva);
            sumaTotal = sumaTotal.add(dato.getTotal());
        }

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);

        creaTotales(sumaEntradas, sumaBase, sumaIva, sumaTotal);
    }

    private void creaSubtabla(List<InformeAbonoReport> abonos) throws SinIvaException
    {
        BaseTable table = new BaseTable(style, 3, "11.4cm", "3cm", "3cm");

        table.withNewRow();
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.abono")));

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.abonados"));
        numeroBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(numeroBlock);

        Block totalBlock = createBoldBlock(ResourceProperties.getProperty(locale, "informeEfectivo.tabla.total"));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(totalBlock);

        BigDecimal sumaAbonos = BigDecimal.ZERO;
        BigDecimal sumaTotal = BigDecimal.ZERO;
        for (InformeAbonoReport dato : abonos)
        {
            table.withNewRow();
            table.withNewCell(dato.getNombre());
            table.withNewCell(blockAlignRight(Integer.toString(dato.getAbonados())));
            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(dato.getTotal())));

            sumaAbonos = sumaAbonos.add(new BigDecimal(dato.getAbonados()));
            sumaTotal = sumaTotal.add(dato.getTotal());
        }

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);

        creaTotalesAbonos(sumaAbonos, sumaTotal);
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
        block.setMarginTop("0.5cm");
        //block.setMarginLeft("10cm");
        //block.setWhiteSpace(WhiteSpaceType.PRE);

        BaseTable table = new BaseTable(style, 7, "3.6cm", "3.6cm", "2.7cm", "3cm", "1.5cm", "1.5cm", "1.5cm");

        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEfectivo.totales")));
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

    private void creaTotalesAbonos(BigDecimal sumaAbonos, BigDecimal sumaTotal)
    {
        Block block = withNewBlock();
        block.setMarginTop("0.5cm");

        BaseTable table = new BaseTable(style, 5, "3.6cm", "6.8cm", "1cm", "3cm", "3cm");

        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEfectivo.totales")));
        setBorders(cell);

        cell = table.withNewCell("");
        setBorders(cell);

        Block entradasBlock = createBoldBlock(sumaAbonos.toString());
        entradasBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(entradasBlock);
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

            return new InformeEfectivoReport(reportSerializer, estilo, locale, configuration, logoReport);
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

	public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException {
		// TODO Auto-generated method stub
		
	}
}
