package es.uji.apps.par.report.components;

import es.uji.apps.fopreports.fop.DisplayAlignType;
import es.uji.apps.fopreports.fop.TextAlignType;
import es.uji.apps.fopreports.fop.WhiteSpaceCollapseType;
import es.uji.apps.fopreports.fop.WhiteSpaceTreatmentType;
import es.uji.apps.fopreports.style.ReportStyle;

public class ParanimfBaseReportStyle extends ReportStyle
{
    private String sectionFontSize;
    private String footerContactFontSize;
    private String titleFontSize;
    private String sectionColor;
    private String titleColor;
    private String fontWeight;
    private TextAlignType textAlign;
    private String fontFamily;
    private String cellHeight;
    private String cellVerticalAlign;
    private DisplayAlignType displayAlign;
    private WhiteSpaceTreatmentType whiteSpaceTreatment;
    private WhiteSpaceCollapseType whiteSpaceCollapse;
    private String marginTop;
    private String marginBottom;
    private String tableCellBorderTop;
    private String tableCellBorderBottom;

    public ParanimfBaseReportStyle()
    {
        this.fontWeight = "normal";
        this.textAlign = TextAlignType.LEFT;
        this.fontFamily = "Arial";
        this.setTableCellBorder("0cm");
        this.setTableCellDisplayAlign(DisplayAlignType.CENTER);
        this.setTableCellPadding("0cm");

        this.setSimplePageMasterMarginTop("1.5cm");
        this.setSimplePageMasterMarginBottom("1.5cm");
        this.setSimplePageMasterMarginLeft("1.5cm");
        this.setSimplePageMasterMarginRight("1.5cm");
        this.setSimplePageMasterRegionBodyMarginTop("0cm");
        this.setSimplePageMasterRegionBeforeExtent("0cm");
        
        this.cellVerticalAlign = "top";
        this.displayAlign = DisplayAlignType.AUTO;
        this.whiteSpaceTreatment = WhiteSpaceTreatmentType.PRESERVE;
        this.whiteSpaceCollapse = whiteSpaceCollapse.FALSE;
    }

    public String getCellVerticalAlign()
    {
        return cellVerticalAlign;
    }

    public void setCellVerticalAlign(String cellVerticalAlign)
    {
        this.cellVerticalAlign = cellVerticalAlign;
    }

    public DisplayAlignType getDisplayAlign()
    {
        return displayAlign;
    }

    public void setDisplayAlign(DisplayAlignType displayAlign)
    {
        this.displayAlign = displayAlign;
    }

    public TextAlignType getTextAlign()
    {
        return textAlign;
    }

    public void setTextAlign(TextAlignType textAlign)
    {
        this.textAlign = textAlign;
    }

    public String getFontWeight()
    {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight)
    {
        this.fontWeight = fontWeight;
    }

    public String getSectionFontSize()
    {
        return sectionFontSize;
    }

    public void setSectionFontSize(String sectionFontSize)
    {
        this.sectionFontSize = sectionFontSize;
    }

    public String getFooterContactFontSize()
    {
        return footerContactFontSize;
    }

    public void setFooterContactFontSize(String footerContactSize)
    {
        this.footerContactFontSize = footerContactSize;
    }

    public String getTitleFontSize()
    {
        return titleFontSize;
    }

    public void setTitleFontSize(String titleFontSize)
    {
        this.titleFontSize = titleFontSize;
    }

    public void setSectionColor(String sectionColor)
    {
        this.sectionColor = sectionColor;
    }

    public String getSectionColor()
    {
        return sectionColor;
    }

    public void setTitleColor(String titleColor)
    {
        this.titleColor = titleColor;
    }

    public String getTitleColor()
    {
        return titleColor;
    }

    public String getFontFamily()
    {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily)
    {
        this.fontFamily = fontFamily;
    }

    public String getCellHeight()
    {
        return cellHeight;
    }

    public void setCellHeight(String cellHeight)
    {
        this.cellHeight = cellHeight;
    }

    public WhiteSpaceTreatmentType getWhiteSpaceTreatment()
    {
        return whiteSpaceTreatment;
    }

    public void setWhiteSpaceTreatment(WhiteSpaceTreatmentType whiteSpaceTreatment)
    {
        this.whiteSpaceTreatment = whiteSpaceTreatment;
    }

    public WhiteSpaceCollapseType getWhiteSpaceCollapse()
    {
        return whiteSpaceCollapse;
    }

    public void setWhiteSpaceCollapse(WhiteSpaceCollapseType whiteSpaceCollapse)
    {
        this.whiteSpaceCollapse = whiteSpaceCollapse;
    }

    public String getMarginTop()
    {
        return marginTop;
    }

    public void setMarginTop(String marginTop)
    {
        this.marginTop = marginTop;
    }

    public String getMarginBottom()
    {
        return marginBottom;
    }

    public void setMarginBottom(String marginBottom)
    {
        this.marginBottom = marginBottom;
    }

    public String getTableCellBorderTop()
    {
        return tableCellBorderTop;
    }

    public void setTableCellBorderTop(String tableCellBorderTop)
    {
        this.tableCellBorderTop = tableCellBorderTop;
    }

    public String getTableCellBorderBottom()
    {
        return tableCellBorderBottom;
    }

    public void setTableCellBorderBottom(String tableCellBorderBottom)
    {
        this.tableCellBorderBottom = tableCellBorderBottom;
    }
}
