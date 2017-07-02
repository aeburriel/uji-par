package es.uji.apps.par.report;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.uji.apps.fopreports.Report;
import es.uji.apps.fopreports.fop.Block;
import es.uji.apps.fopreports.fop.BorderStyleType;
import es.uji.apps.fopreports.fop.ExternalGraphic;
import es.uji.apps.fopreports.fop.PageBreakAfterType;
import es.uji.apps.fopreports.fop.TableCell;
import es.uji.apps.fopreports.fop.TextAlignType;
import es.uji.apps.fopreports.fop.WhiteSpaceType;
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

public class InformeEventosReport extends Report implements InformeInterface
{
    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private InformeTaquillaReportStyle style;
	Configuration configuration;
    String logoReport;
    
    public InformeEventosReport() throws ReportSerializerInitException {
		super(reportSerializer, new InformeTaquillaReportStyle());
	}

    private InformeEventosReport(ReportSerializer serializer, InformeTaquillaReportStyle style,
			Locale locale, Configuration configuration, String logoReport) throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
		this.configuration = configuration;
        this.logoReport = logoReport;
    }

    public void genera(String inicio, String fin, List<InformeModelReport> compras) throws SinIvaException
    {
        if (compras.size() == 0)
            generaPdfSinCompras();
        else
            generaPdfCompras(inicio, fin, compras);
    }

    private void generaPdfSinCompras()
    {
        Block block = withNewBlock();
        block.getContent().add(ResourceProperties.getProperty(locale, "informeEventos.sinCompras"));
    }

    private void generaPdfCompras(String inicio, String fin, List<InformeModelReport> compras) throws SinIvaException
    {
        List<List<InformeModelReport>> separadosPorSesion = separaPorSesion(compras);

        for (List<InformeModelReport> datosSesion:separadosPorSesion)
        {
            creaInicioPagina(inicio, fin);
            creaTabla(datosSesion);

            createPageBreak();
        }
    }

    private List<List<InformeModelReport>> separaPorSesion(List<InformeModelReport> datos)
    {
        List<List<InformeModelReport>> result = new ArrayList<List<InformeModelReport>>();
        List<InformeModelReport> sesionActual = new ArrayList<InformeModelReport>();

        long sesionAnterior = -1;

        for (InformeModelReport dato:datos)
        {
            if (sesionAnterior!=-1 && dato.getSesionId()!=sesionAnterior)
            {
                result.add(sesionActual);
                sesionActual = new ArrayList<InformeModelReport>();
            }

            sesionActual.add(dato);
            sesionAnterior = dato.getSesionId();
        }

        if (sesionActual.size() > 0)
            result.add(sesionActual);

        return result;
    }

    private void createPageBreak()
    {
        Block pageBreak = withNewBlock();
        pageBreak.setPageBreakAfter(PageBreakAfterType.ALWAYS);
    }

    private void creaInicioPagina(String inicio, String fin)
    {
        creaCabecera();
        creaTituloYPeriodo(inicio, fin);
        creaIntro();
    }

    private void creaCabecera()
    {
        BaseTable table = new BaseTable(style, 3, "10cm", "4.6cm", "3.7cm");

        table.withNewRow();
        table.withNewCell(creaLogo());

        Block block = new Block();
        block.getContent().add(ResourceProperties.getProperty(locale, "informeEventos.vicerectorat"));
        block.setMarginTop("0.5cm");
        block.setMarginRight("0.5cm");

        TableCell cell = table.withNewCell(block);
        cell.setPadding("0.04cm");
        cell.setBorderRightWidth("0.2cm");
        cell.setBorderRightColor("black");
        cell.setBorderRightStyle(BorderStyleType.SOLID);

        block = new Block();
        block.getContent().add(ResourceProperties.getProperty(locale, "informeEventos.tituloCabecera"));
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

    public Block withNewBlock()
    {
        Block block = super.withNewBlock();
        block.setFontSize(FONT_SIZE);
        block.setColor(NEGRO);

        return block;
    }

    private void creaTituloYPeriodo(String inicioTexto, String finTexto)
    {
        Block titulo = createBoldBlock(ResourceProperties.getProperty(locale, "informeEventos.titulo"));

        titulo.setMarginTop("1cm");
        titulo.setMarginLeft("6cm");
        add(titulo);

        Block periodo = withNewBlock();
        periodo.setFontWeight("bold");
        periodo.setMarginTop("0.5cm");
        periodo.setMarginLeft("6cm");
        periodo.setWhiteSpace(WhiteSpaceType.PRE);

        periodo.getContent().add(
                ResourceProperties.getProperty(locale, "informeEventos.periodo", inicioTexto, finTexto));
    }

    private void creaIntro()
    {
        Block intro = withNewBlock();

        intro.setMarginTop("1cm");
        intro.getContent().add(ResourceProperties.getProperty(locale, "informeEventos.intro"));
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
        BaseTable table = new BaseTable(style, 7, "3.6cm", "3.6cm", "2.7cm", "2cm", "1.5cm", "2.5cm", "1.5cm");

        table.withNewRow();
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.evento")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.sesion")));
        table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.tipo")));

        Block numeroBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.numero"));
        numeroBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(numeroBlock);

        Block baseBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.base"));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(baseBlock);

        Block ivaBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.iva"));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(ivaBlock);

        Block totalBlock = createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.tabla.total"));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        table.withNewCell(totalBlock);

        BigDecimal sumaSesionEntradas = BigDecimal.ZERO;
        BigDecimal sumaSesionBase = BigDecimal.ZERO;
        BigDecimal sumaSesionIva = BigDecimal.ZERO;
        BigDecimal sumaSesionTotal = BigDecimal.ZERO;

        BigDecimal sumaEventoEntradas = BigDecimal.ZERO;
        BigDecimal sumaEventoBase = BigDecimal.ZERO;
        BigDecimal sumaEventoIva = BigDecimal.ZERO;
        BigDecimal sumaEventoTotal = BigDecimal.ZERO;

        BigDecimal sumaTotalEntradas = BigDecimal.ZERO;
        BigDecimal sumaTotalBase = BigDecimal.ZERO;
        BigDecimal sumaTotalIva = BigDecimal.ZERO;
        BigDecimal sumaTotalTotal = BigDecimal.ZERO;

        long sesionAnterior = -1;
        long eventoAnterior = -1;

        for (InformeModelReport dato : compras)
        {
            if (dato.getIva() == null)
                throw new SinIvaException(dato.getEvento());

            if (cambioSesion(dato, sesionAnterior))
            {
                creaSubtotalesSesion(table, sumaSesionEntradas, sumaSesionBase, sumaSesionIva, sumaSesionTotal);

                sumaSesionEntradas = BigDecimal.ZERO;
                sumaSesionBase = BigDecimal.ZERO;
                sumaSesionIva = BigDecimal.ZERO;
                sumaSesionTotal = BigDecimal.ZERO;
            }

            if (cambioEvento(dato, eventoAnterior))
            {
                creaSubtotalesEvento(table, sumaEventoEntradas, sumaEventoBase, sumaEventoIva, sumaEventoTotal);

                sumaEventoEntradas = BigDecimal.ZERO;
                sumaEventoBase = BigDecimal.ZERO;
                sumaEventoIva = BigDecimal.ZERO;
                sumaEventoTotal = BigDecimal.ZERO;
            }

            table.withNewRow();
            table.withNewCell(dato.getEvento());
            table.withNewCell(dato.getSesion());
            table.withNewCell(dato.getTipoEntrada());
            table.withNewCell(blockAlignRight(Integer.toString(dato.getNumeroEntradas())));

            BigDecimal base = calculaBase(dato);
            BigDecimal iva = dato.getTotal().subtract(base);

            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(base)));
            table.withNewCell(blockAlignRight(String.format("(%s%%) %s", dato.getIva(), ReportUtils.formatEuros(iva))));
            table.withNewCell(blockAlignRight(ReportUtils.formatEuros(dato.getTotal())));

            sumaSesionEntradas = sumaSesionEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            sumaSesionBase = sumaSesionBase.add(base);
            sumaSesionIva = sumaSesionIva.add(iva);
            sumaSesionTotal = sumaSesionTotal.add(dato.getTotal());

            sumaEventoEntradas = sumaEventoEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            sumaEventoBase = sumaEventoBase.add(base);
            sumaEventoIva = sumaEventoIva.add(iva);
            sumaEventoTotal = sumaEventoTotal.add(dato.getTotal());

            sumaTotalEntradas = sumaTotalEntradas.add(new BigDecimal(dato.getNumeroEntradas()));
            sumaTotalBase = sumaTotalBase.add(base);
            sumaTotalIva = sumaTotalIva.add(iva);
            sumaTotalTotal = sumaTotalTotal.add(dato.getTotal());

            sesionAnterior = dato.getSesionId();
            eventoAnterior = dato.getEventoId();
        }

        creaSubtotalesSesion(table, sumaSesionEntradas, sumaSesionBase, sumaSesionIva, sumaSesionTotal);

        //creaSubtotalesEvento(table, sumaEventoEntradas, sumaEventoBase, sumaEventoIva, sumaEventoTotal);

        Block block = withNewBlock();
        block.setMarginTop("1cm");
        block.getContent().add(table);

        //creaTotales(sumaTotalEntradas, sumaTotalBase, sumaTotalIva, sumaTotalTotal);
    }

    private void creaSubtotalesSesion(BaseTable table, BigDecimal subEntradas, BigDecimal subBase, BigDecimal subIva, BigDecimal subTotal)
    {
        String titulo = ResourceProperties.getProperty(locale, "informeEventos.subtotalSesion");

        creaLineaTotales(table, subEntradas, subBase, subIva, subTotal, titulo);
    }

    private void creaSubtotalesEvento(BaseTable table, BigDecimal subEntradas, BigDecimal subBase, BigDecimal subIva, BigDecimal subTotal)
    {
        String titulo = ResourceProperties.getProperty(locale, "informeEventos.subtotalEvento");

        creaLineaTotales(table, subEntradas, subBase, subIva, subTotal, titulo);
    }

    private void creaLineaTotales(BaseTable table, BigDecimal subEntradas, BigDecimal subBase, BigDecimal subIva,
            BigDecimal subTotal, String titulo)
    {
        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(titulo));
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

    private boolean cambioSesion(InformeModelReport dato, long sesionAnterior)
    {
        return sesionAnterior!=-1 && dato.getSesionId()!=sesionAnterior;
    }

    private boolean cambioEvento(InformeModelReport dato, long eventoAnterior)
    {
        return eventoAnterior!=-1 && dato.getEventoId()!=eventoAnterior;
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

    @SuppressWarnings("unused")
	private void creaTotales(BigDecimal sumaEntradas, BigDecimal sumaBase, BigDecimal sumaIva, BigDecimal sumaTotal)
    {
        Block block = withNewBlock();
        //block.setMarginLeft("10cm");
        //block.setWhiteSpace(WhiteSpaceType.PRE);

        BaseTable table = new BaseTable(style, 7, "3.6cm", "3.6cm", "2.7cm", "3cm", "1.5cm", "1.5cm", "1.5cm");

        table.withNewRow();

        table.withNewCell("");

        TableCell cell = table.withNewCell(createBoldBlock(ResourceProperties.getProperty(locale,
                "informeEventos.totales")));
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

    @SuppressWarnings("unused")
	private void creaFirma()
    {
        Block cargoBlock = withNewBlock();
        cargoBlock.setMarginTop("1cm");
        String cargo = configuration.getCargoInformeEfectivo();
        cargoBlock.getContent().add(cargo);

        Block nombreBlock = withNewBlock();
        nombreBlock.setMarginTop("2cm");
        nombreBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeEventos.subtotales.firmado",
                        configuration.getFirmanteInformeEfectivo()));

        Calendar fecha = Calendar.getInstance();

        Block fechaBlock = withNewBlock();
        fechaBlock.setMarginTop("1cm");
        fechaBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeEventos.subtotales.fecha",
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

    public InformeInterface create(Locale locale, Configuration configuration, String logoReport, boolean showIVA, String location)
    {
        try
        {
            initStatics();
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeEventosReport(reportSerializer, estilo, locale, configuration, logoReport);
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
			BigDecimal totalTaquillaEfectivo, BigDecimal totalTaquillaTransferencia, BigDecimal totalOnline) {
		// TODO Auto-generated method stub

	}

    public void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo, String firmanteInformeEfectivo) throws SinIvaException {
        genera(null, inicio, fin, compras, abonos, cargoInformeEfectivo, firmanteInformeEfectivo);
    }

    public void genera(String titulo, String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo, String firmanteInformeEfectivo) throws SinIvaException {

    }

	public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException {
		// TODO Auto-generated method stub

	}

    public void genera(long sesionId, String userUID) throws SinIvaException {
        // TODO Auto-generated method stub

    }

	public void genera(String fechaInicio, String fechaFin, String userUID) {

	}
}
