package es.uji.apps.par.report;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.uji.apps.fopreports.Report;
import es.uji.apps.fopreports.fop.Block;
import es.uji.apps.fopreports.fop.BorderStyleType;
import es.uji.apps.fopreports.fop.ExternalGraphic;
import es.uji.apps.fopreports.fop.TableCell;
import es.uji.apps.fopreports.fop.TextAlignType;
import es.uji.apps.fopreports.fop.WhiteSpaceType;
import es.uji.apps.fopreports.serialization.FopPDFSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.fopreports.serialization.ReportSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializerInitException;
import es.uji.apps.par.SinIvaException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.Informe;
import es.uji.apps.par.report.components.BaseTable;
import es.uji.apps.par.report.components.InformeTaquillaReportStyle;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;

public class InformeEfectivoReport extends Report
{
    private static final String FONT_SIZE = "9pt";

    private static final String NEGRO = "#000000";

    private static FopPDFSerializer reportSerializer;

    private Locale locale;
    private final InformeTaquillaReportStyle style;

    private InformeEfectivoReport(ReportSerializer serializer, InformeTaquillaReportStyle style, Locale locale)
            throws ReportSerializerInitException
    {
        super(serializer, style);

        this.style = style;
        this.locale = locale;
    }

    public void genera(Date inicio, Date fin, List<Informe> compras) throws SinIvaException
    {
        creaLogo();
        creaCabecera(inicio, fin);
        creaIntro();
        creaTabla(compras);
        creaFirma();
    }

    private void creaLogo()
    {
        ExternalGraphic externalGraphic = new ExternalGraphic();
        externalGraphic.setSrc(new File("/etc/uji/par/imagenes/uji_logo_color.png").getAbsolutePath());
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

    private void creaCabecera(Date inicio, Date fin)
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

        String inicioTexto = DateUtils.dateToSpanishString(inicio);
        String finTexto = DateUtils.dateToSpanishString(fin);

        periodo.getContent().add(
                ResourceProperties.getProperty(locale, "informeEfectivo.periodo", inicioTexto, finTexto));
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

    private void creaTabla(List<Informe> compras) throws SinIvaException
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

        for (Informe dato : compras)
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

            table.withNewCell(blockAlignRight(Utils.formatEuros(base)));
            table.withNewCell(blockAlignRight(Utils.formatEuros(iva)));
            table.withNewCell(blockAlignRight(Utils.formatEuros(dato.getTotal())));

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

    private BigDecimal calculaBase(Informe dato)
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

        Block baseBlock = createBoldBlock(Utils.formatEuros(sumaBase));
        baseBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(baseBlock);
        setBorders(cell);

        Block ivaBlock = createBoldBlock(Utils.formatEuros(sumaIva));
        ivaBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(ivaBlock);
        setBorders(cell);

        Block totalBlock = createBoldBlock(Utils.formatEuros(sumaTotal));
        totalBlock.setTextAlign(TextAlignType.RIGHT);
        cell = table.withNewCell(totalBlock);
        setBorders(cell);

        block.getContent().add(table);
    }

    private void creaFirma()
    {
        Block cargoBlock = withNewBlock();
        cargoBlock.setMarginTop("1cm");
        String cargo = Configuration.getCargoInformeEfectivo();
        cargoBlock.getContent().add(cargo);

        Block nombreBlock = withNewBlock();
        nombreBlock.setMarginTop("2cm");
        nombreBlock.getContent().add(ResourceProperties.getProperty(locale, "informeEfectivo.subtotales.firmado", Configuration.getFirmanteInformeEfectivo()));

        Calendar fecha = Calendar.getInstance();

        Block fechaBlock = withNewBlock();
        fechaBlock.setMarginTop("1cm");
        fechaBlock.getContent().add(
                ResourceProperties.getProperty(locale, "informeEfectivo.subtotales.fecha",
                        fecha.get(Calendar.DAY_OF_MONTH), DateUtils.getMesValenciaConDe(fecha), fecha.get(Calendar.YEAR)));
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

    public static InformeEfectivoReport create(Locale locale)
    {
        try
        {
            initStatics();
            InformeTaquillaReportStyle estilo = new InformeTaquillaReportStyle();

            return new InformeEfectivoReport(reportSerializer, estilo, locale);
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
}
