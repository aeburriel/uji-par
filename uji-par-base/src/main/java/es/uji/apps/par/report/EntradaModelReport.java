package es.uji.apps.par.report;

public class EntradaModelReport {

	private String fila;
	private String numero;
	private String total;
	private String barcode;
	private String zona;
	private String tipo;
	private Boolean tarifaDefecto;
	private String iniciales;
	
	public EntradaModelReport() {
		
	}
	
	public String getFila() {
		return fila;
	}
	public void setFila(String fila) {
		this.fila = fila;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getZona() {
		return zona;
	}
	public void setZona(String zona) {
		this.zona = zona;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Boolean getTarifaDefecto() {
		return tarifaDefecto;
	}

	public void setTarifaDefecto(Boolean tarifaDefecto) {
		this.tarifaDefecto = tarifaDefecto;
	}

	public String getIniciales() {
		return iniciales;
	}

	public void setIniciales(String iniciales) {
		this.iniciales = iniciales;
	}
	
}
