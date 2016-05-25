package es.uji.apps.par.report;

import es.uji.apps.fopreports.Report;
import es.uji.apps.fopreports.fop.Block;
import es.uji.apps.fopreports.fop.FontStyleType;
import es.uji.apps.fopreports.serialization.FopPDFSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializer;
import es.uji.apps.fopreports.serialization.ReportSerializerInitException;
import es.uji.apps.fopreports.style.ReportStyle;
import es.uji.apps.par.report.components.EntradaReportStyle;

import java.awt.*;

public class BenicassimBaseReport extends Report {
	private static final String font = "Verdana";

	public BenicassimBaseReport(ReportSerializer serializer, ReportStyle style) throws ReportSerializerInitException {
		super(serializer, style);
	}

	public BenicassimBaseReport(FopPDFSerializer reportSerializer, EntradaReportStyle style) throws ReportSerializerInitException {
		super(reportSerializer, new EntradaReportStyle());
	}

	protected String getTituloPequenyoAMostrar(String texto, int widthMaximo) {
        boolean textoIncorrecto = true;

        while (textoIncorrecto) {
        	int width = getWidthTexto(texto, Font.PLAIN, 13);
        	if (width > widthMaximo)
        		texto = texto.substring(0, texto.lastIndexOf(" ")) + "\u2026";
        	else
        		textoIncorrecto = false;
        }
        return texto;
	}

    protected String getTextoRecortadoMostrar(String texto, int widthMaximo, int font) {
        boolean textoIncorrecto = true;

        while (textoIncorrecto) {
            int width = getWidthTexto(texto, Font.PLAIN, font);
            if (width > widthMaximo)
                texto = texto.substring(0, texto.lastIndexOf(" ")) + "\u2026";
            else
                textoIncorrecto = false;
        }
        return texto;
    }

    protected int getFontTitulo(String texto, int widthMaximo) {
        boolean textoIncorrecto = true;

        int font = 15;
        while (textoIncorrecto) {
            int width = getWidthTexto(texto, Font.PLAIN, font);
            if (width > widthMaximo)
                font -= 1;
            else
                textoIncorrecto = false;
        }
        return font;
    }

    private int getWidthTexto(String texto, int tipo, int size) {
		Font font = new Font(BenicassimBaseReport.font, tipo, size);
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(font);
        return fm.stringWidth(texto);
	}

    protected Block getAdjustedBlock(String texto, int maxWidth, int minFont) {
        boolean textoIncorrecto = true;

        int font = 22;
        while (textoIncorrecto) {
            int width = getWidthTexto(texto, Font.ITALIC, font);
            if (width > maxWidth) {
                if (font > minFont) {
                    font -= 1;
                }
                else
                {
                    return getBlockWithText(getTextoRecortadoMostrar(texto, maxWidth, font), font + "pt");
                }
            }
            else
                textoIncorrecto = false;
        }

        return getBlockWithText(texto, font + "pt");
    }
    
    protected Block getBlockWithText(String property, String size) {
    	return getBlockWithText(property, size, false, false);
    }
    
    protected Block getBlockWithText(String property, String size, boolean italic, boolean bold) {
    	Block text = new Block();
        text.getContent().add(property);
        text.setFontSize(size);
        
        if (italic)
        	text.setFontStyle(FontStyleType.ITALIC);
        
        if (bold)
            text.setFontWeight("bold");
        
        return text;
	}
}
