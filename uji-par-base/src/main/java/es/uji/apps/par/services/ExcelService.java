package es.uji.apps.par.services;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelService {
	private HSSFWorkbook workbook;
	private CellStyle estilNegreta;
	private HSSFSheet fulla;

	public ExcelService() {
		this.workbook = new HSSFWorkbook();
		generaEstilNegreta();
	}

	public void addFulla(String nomFulla) {
		this.fulla = this.workbook.createSheet(nomFulla);
	}

	private void generaEstilNegreta() {
		CellStyle cs = this.workbook.createCellStyle();
		Font f = workbook.createFont();
		f.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cs.setFont(f);
		this.estilNegreta = cs;
	}

	public void generaCeldes(CellStyle estil, int rownum, String... textos) {
		Row row = getNewRow(rownum);
		int cellnum = 0;
		for (String text : textos) {
			addCell(cellnum++, text, estil, row);
		}
	}

	public CellStyle getEstilNegreta() {
		return estilNegreta;
	}

	public Row getNewRow(int rownum) {
		return this.fulla.createRow(rownum);
	}

	public void setEstilNegreta(CellStyle estilNegreta) {
		this.estilNegreta = estilNegreta;
	}

	public void addCell(int cellNumber, float valor, Row row) {
		Cell cell = row.createCell(cellNumber);
		CellStyle cellStyle = this.workbook.createCellStyle();
		cellStyle = addFloatStyle(cellStyle);
		cell.setCellValue(valor);
		cell.setCellStyle(cellStyle);
	}

	private CellStyle addFloatStyle(CellStyle style) {
		DataFormat format = this.workbook.createDataFormat();
		style.setDataFormat(format.getFormat("0.00"));
		return style;
	}

	public void addCell(int cellNumber, String text, CellStyle estil, Row row) {
		Cell cell = row.createCell(cellNumber);
		cell.setCellValue(text);

		if (estil != null)
			cell.setCellStyle(estil);
	}

	public void addCell(int cellNumber, int number, CellStyle estil, Row row) {
		Cell cell = row.createCell(cellNumber);
		cell.setCellValue((Integer) number);

		if (estil != null)
			cell.setCellStyle(estil);
	}

	public ByteArrayOutputStream getExcel() throws IOException {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		this.workbook.write(outstream);
		outstream.flush();
		outstream.close();

		return outstream;
	}

	public void addCellWithFormula(int cellnum, String formula, CellStyle estil, Row row) {
		Cell cell = row.createCell(cellnum);
		cell.setCellStyle(estil);
		cell.setCellFormula(formula);
	}

	public void addFloatCellWithFormula(int cellnum, String formula, CellStyle estil, Row row) {
		Cell cell = row.createCell(cellnum);
		estil = addFloatStyle(estil);
		cell.setCellStyle(estil);
		cell.setCellFormula(formula);
	}

	public int getTotalFilas() {
		return this.fulla.getPhysicalNumberOfRows();
	}

	public Object getCellValue(String referencia) {
		CellReference cellReference = new CellReference(referencia);
		Row row = this.fulla.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());
		Object value = null;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BOOLEAN:
				value = cell.getBooleanCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				value = cell.getNumericCellValue();
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
		}
		return value;
	}
}
