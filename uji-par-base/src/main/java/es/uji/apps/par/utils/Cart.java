/**
 * Carrito de la Compra
 * Copyright (c) 2019 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.utils;

public class Cart {
	private String uuid;		// UUID de la compra
	private String butacas;		// JSON con la selección de butacas
	private String selector;	// Selector de la compra

	public Cart(final String selector) {
		this.selector = selector;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getButacas() {
		if (butacas == null || butacas.isEmpty()) {
			return "[]";
		} else {
			return butacas;
		}
	}

	public void setButacas(final String butacas) {
		this.butacas = butacas;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(final String selector) {
		this.selector = selector;
	}
}
