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
	private static final String cortes = " -,;:._/|\\~";

	public BenicassimBaseReport(ReportSerializer serializer, ReportStyle style) throws ReportSerializerInitException {
		super(serializer, style);
	}

	public BenicassimBaseReport(FopPDFSerializer reportSerializer, EntradaReportStyle style) throws ReportSerializerInitException {
		super(reportSerializer, new EntradaReportStyle());
	}

    protected String getTextoRecortadoMostrar(String texto, int widthMaximo, int fontType, int fontSize) {
        boolean caracter = false;

        while (true) {
            int width = getWidthTexto(texto, fontType, fontSize);
            if (width > widthMaximo) {
                int i = texto.length();
                while (i > 0) {
                    if (caracter || cortes.contains(texto.substring(i - 1, i))) {
                        break;
                    }
                    i -= 1;
                }

                // No hay ningún punto de corte predefinido válido, lo haremos caracter a caracter
                if (i <= 0 && !caracter) {
                    caracter = true;
                    continue;
                }

                // En caracter a caracter, omitimos los puntos suspensivos
                if (caracter && texto.substring(texto.length() - 1).equals("\u2026")) {
                    i -= 1;
                }
                texto = texto.substring(0, i - 1) + "\u2026";
            }
            else {
                break;
            }
        }
        return texto;
    }

    protected int getFontTitulo(String texto, int widthMaximo) {
        boolean textoIncorrecto = true;

        int fontSize = 15;
        while (textoIncorrecto) {
            int width = getWidthTexto(texto, Font.PLAIN, fontSize);
            if (width > widthMaximo)
                fontSize -= 1;
            else
                textoIncorrecto = false;
        }
        return fontSize;
    }

    private int getWidthTexto(String texto, int tipo, int size) {
        Font fuente = new Font(BenicassimBaseReport.font, tipo, size);
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(fuente);
        return fm.stringWidth(texto);
	}

    protected Block getAdjustedBlock(String texto, int maxWidth, int minFont) {
        boolean textoIncorrecto = true;

        int fontSize = 22;
        while (textoIncorrecto) {
            int width = getWidthTexto(texto, Font.ITALIC, fontSize);
            if (width > maxWidth) {
                if (fontSize > minFont) {
                    fontSize -= 1;
                }
                else
                {
                    return getBlockWithText(getTextoRecortadoMostrar(texto, maxWidth, Font.ITALIC, fontSize), fontSize + "pt");
                }
            }
            else
                textoIncorrecto = false;
        }

        return getBlockWithText(texto, fontSize + "pt");
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
