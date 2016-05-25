package es.uji.apps.par.report.components;

import es.uji.apps.fopreports.fop.Block;
import es.uji.apps.fopreports.fop.BlockContainer;
import es.uji.apps.fopreports.fop.ExternalGraphic;
import es.uji.apps.fopreports.fop.Table;
import es.uji.apps.fopreports.fop.TableBody;
import es.uji.apps.fopreports.fop.TableCell;
import es.uji.apps.fopreports.fop.TableColumn;
import es.uji.apps.fopreports.fop.TableRow;

public class BaseTable extends Table
{
    private static final int WIDTH_MILLIMETERS = 180;
    private static final String SIN_COLSPAN = "";

    private int columns;
    protected TableBody body;

    private final ParanimfBaseReportStyle style;

    public BaseTable(ParanimfBaseReportStyle style, int columns, String... columnsWidths)
    {
        super();

        this.style = style;
        this.columns = columns;
        this.body = withNewTableBody();

        createColumns(columnsWidths);
    }
    
    public TableRow getLastRow()
    {
        if (hasRows())
        	return body.getTableRow().get(body.getTableRow().size() - 1);
        else
        	return null;
    }

	private boolean hasRows() {
		if (body.getTableRow().size() == 0)
            return false;
		else
			return true;
	}

    public TableRow getPrevLastRow()
    {
        if (hasRows())
        	return body.getTableRow().get(body.getTableRow().size() - 2);
        else
        	return null;
    }

    protected boolean mustCreateNewRow()
    {
        TableRow lastRow = getLastRow();

        if (lastRow == null)
            return true;

        if (lastRow.getTableCell().size() == columns)
            return true;

        return false;
    }

    protected void createColumns(String[] columnsWidths)
    {
        getTableColumn().clear();

        for (int i = 0; i < columns; i++)
        {
            TableColumn tableColumn = new TableColumn();

            if (i < columnsWidths.length)
                tableColumn.getColumnWidth().add(columnsWidths[i]);
            else
                tableColumn.getColumnWidth().add(WIDTH_MILLIMETERS / columns + "mm");

            getTableColumn().add(tableColumn);
        }
    }

    public TableCell withNewCell(String text)
    {
    	return withNewCell(text, SIN_COLSPAN);
    }
    
    public TableCell withNewCell(String text, String colspan) {
    	return withNewCell(text, style, colspan);
    }
    
    public TableCell withNewCell(Block block, String colspan) {
        return withNewCell(block, style, colspan);
    }
    
    public TableCell withNewCell(String text, ParanimfBaseReportStyle reportStyle, String colspan) {
    	Block block = addToBlock(text);
        block.setFontWeight("normal");

        return getNewTableCell(block, reportStyle, colspan);
    }
    
    public TableCell withNewCell(Block block, ParanimfBaseReportStyle reportStyle, String colspan) {
        return getNewTableCell(block, reportStyle, colspan);
    }
    
    public TableCell withNewCell(String text, EntradaReportStyle reportStyle)
    {
        return getNewTableCell(text, reportStyle);
    }
    
    public TableCell withNewCell(Block bloque)
    {
        return getNewTableCell(bloque);
    }
    
    public TableCell withNewCell(BlockContainer bc)
    {
        return getNewTableCell(bc);
    }
    
    public TableCell withNewCell(Block bloque, EntradaReportStyle estilo)
    {
        return getNewTableCell(bloque, estilo);
    }
    
    public TableCell withNewCell(ExternalGraphic imagen)
    {
        Block block = addToBlock(imagen);

        return getNewTableCell(block);
    }

	private Block addToBlock(Object object) {
		Block block = new Block();
        block.getContent().add(object);
        block.setColor(style.getTableCellColor());
        block.setFontSize(style.getTableCellFontSize());
		return block;
	}

	private TableCell getNewTableCell(Block block) {
		return getNewTableCell(block, style, SIN_COLSPAN);
	}
	
	private TableCell getNewTableCell(BlockContainer bc) {
		return getNewTableCell(bc, style, SIN_COLSPAN);
	}
	
	private TableCell getNewTableCell(Block block, EntradaReportStyle estilo) {
		return getNewTableCell(block, estilo, SIN_COLSPAN);
	}
	
	public TableCell getNewTableCell(Block block, ParanimfBaseReportStyle estilo, String colspan) {
		TableCell tableCell = createNewTableCell(estilo);
		tableCell.getMarkerOrBlockOrBlockContainer().add(block);
		
		if (!colspan.equals(SIN_COLSPAN))
			tableCell.setNumberColumnsSpanned(colspan);
		
		if (estilo.getTableCellBorder() != null && !estilo.getTableCellBorder().equals(""))
			tableCell.setBorder(estilo.getTableCellBorder());
		
		if (estilo.getTableCellBorderBottom() != null && !estilo.getTableCellBorderBottom().equals(""))
			tableCell.setBorderBottom(estilo.getTableCellBorderBottom());
		
		if (estilo.getTableCellBorderTop() != null && !estilo.getTableCellBorderTop().equals(""))
			tableCell.setBorderTop(estilo.getTableCellBorderTop());
		
		return tableCell;
	}
	
	public TableCell getNewTableCell(BlockContainer bc, ParanimfBaseReportStyle estilo, String colspan) {
		TableCell tableCell = createNewTableCell(estilo);
		tableCell.getMarkerOrBlockOrBlockContainer().add(bc);
		
		if (!colspan.equals(SIN_COLSPAN))
			tableCell.setNumberColumnsSpanned(colspan);
		
		if (estilo.getTableCellBorder() != null && !estilo.getTableCellBorder().equals(""))
			tableCell.setBorder(estilo.getTableCellBorder());
		
		if (estilo.getTableCellBorderBottom() != null && !estilo.getTableCellBorderBottom().equals(""))
			tableCell.setBorderBottom(estilo.getTableCellBorderBottom());
		
		if (estilo.getTableCellBorderTop() != null && !estilo.getTableCellBorderTop().equals(""))
			tableCell.setBorderTop(estilo.getTableCellBorderTop());
		
		return tableCell;
	}
	
	private TableCell getNewTableCell(String text, EntradaReportStyle estilo) {
		Block b = new Block();
		b.getContent().add(text);
		
		return getNewTableCell(b, estilo, SIN_COLSPAN);
	}

	private TableCell createNewTableCell(ParanimfBaseReportStyle estilo) {
		TableCell tableCell = new TableCell();
        tableCell.setBorder(estilo.getTableCellBorder());
        tableCell.setDisplayAlign(estilo.getTableCellDisplayAlign());
        tableCell.setPadding(estilo.getTableCellPadding());
        tableCell.getBorderColor().add(estilo.getTableCellBorderColor());
        
        tableCell.setColor(estilo.getTableCellColor());
        tableCell.setFontSize(estilo.getTableCellFontSize());
        tableCell.setFontWeight(estilo.getFontWeight());
        tableCell.setTextAlign(estilo.getTextAlign());
        
        if (!estilo.getFontFamily().equals(""))
        	tableCell.setFontFamily(estilo.getFontFamily());
        
        if (estilo.getCellHeight() != null && !estilo.getCellHeight().equals(""))
        	tableCell.setHeight(estilo.getCellHeight());
        
        tableCell.setVerticalAlign(estilo.getCellVerticalAlign());
        tableCell.setDisplayAlign(estilo.getDisplayAlign());
        
        getLastRow().getTableCell().add(tableCell);

        return tableCell;
	}

    public TableRow withNewRow()
    {
        TableRow tableRow = new TableRow();
        
        body.getTableRow().add(tableRow);
        
        return tableRow;
    }
}
