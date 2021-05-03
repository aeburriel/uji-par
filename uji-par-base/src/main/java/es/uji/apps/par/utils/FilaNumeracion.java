package es.uji.apps.par.utils;

import es.uji.apps.par.model.Butaca;

public class FilaNumeracion {
	private final String localizacion;
	private final int fila;
	private final int primera;
	private final int ultima;
	private final int paso;

	public FilaNumeracion(final String localizacion, final int fila, final int primera, final int ultima, final int paso) {
		this.localizacion = localizacion;
		this.fila = fila;
		this.primera = primera;
		this.ultima = ultima;
		this.paso = paso;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}

		final FilaNumeracion fila = (FilaNumeracion) object;
		return this.fila == fila.fila
				&& this.primera == fila.primera && this.ultima == fila.ultima
				&& this.paso == fila.paso && this.localizacion.equals(fila.localizacion);
	}

	@Override
	public String toString() {
		return String.format("FilaNumeracion(%s_%d_[%d..%d]|%d)", localizacion, fila, primera, ultima, paso);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getLocalizacion() {
		return localizacion;
	}

	public int getFila() {
		return fila;
	}

	public int getPrimera() {
		return primera;
	}

	public int getUltima() {
		return ultima;
	}

	public int getPaso() {
		return paso;
	}

	/**
	 * Comprueba si la butaca indicada pertenece a esta fila
	 *
	 * @param butaca
	 * @return true si pertenece
	 */
	public boolean pertenece(final Butaca butaca) {
		final int numero = Integer.parseInt(butaca.getNumero());

		return pertenece(numero) && fila == Integer.parseInt(butaca.getFila())
				&& localizacion.equals(butaca.getLocalizacion());
	}

	/**
	 * Comprueba si el número de butaca indicado pertenece a esta fila
	 *
	 * @param numero de butaca
	 * @return true si pertenece
	 */
	public boolean pertenece(final int numero) {
		// X_i = primera + paso * (i - 1)
		// (X_i - primera) / paso = (i - 1)
		return ((numero - primera) % paso == 0) && ultima >= numero && primera <= numero;
	}

	/**
	 * Devuelve el número de butacas de esta fila
	 *
	 * @return Número de butacas
	 */
	public int getCantidadButacas() {
		return getCantidadButacas(primera, ultima);
	}

	/**
	 * Calcula el número de butacas entre dos extremos, ambos incluídos
	 *
	 * @param n0 número de butaca inferior
	 * @param n1 número de butaca superior
	 * @return el número de butacas, incluyendo ambos extremos
	 */
	public int getCantidadButacas(final int n0, final int n1) {
		if (n0 > n1 || !pertenece(n0) || !pertenece(n1)) {
			throw new ArrayIndexOutOfBoundsException();
		}

		return ((n1 - n0) / paso) + 1;
	}

	/**
	 * Devuelve el índice para el número de butaca indicado
	 *
	 * @param numero de butaca
	 * @return el íncide
	 */
	public int getIndice(final int numero) {
		if (!pertenece(numero)) {
			throw new IndexOutOfBoundsException();
		}
		return (numero - primera) / paso;
	}

	/**
	 * Devuelve el número de butaca según su posición
	 *
	 * @param indice Posición de la butaca, empezando por 0
	 * @return El número de la butaca indicada
	 */
	public int getNumeroButaca(final int indice) {
		final int numero = primera + paso * indice;
		if (numero > ultima || numero < primera) {
			throw new IndexOutOfBoundsException();
		}

		return numero;
	}

	/**
	 * Devuelve true si el número de fila es par
	 * @return
	 */
	public boolean isFilaPar() {
		return fila % 2 == 0;
	}
}

