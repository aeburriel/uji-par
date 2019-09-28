/**
 * Caché de Carritos de la Compra por Sesión HTTP
 * Copyright (c) 2019 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.utils;

import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class UserCarts {
	private static final int LRU_CACHE_SIZE = 16; // Número máximo de compras simultáneas por sesión

	private final Cache<String, Cart> cache = CacheBuilder.newBuilder()
			.maximumSize(LRU_CACHE_SIZE)
			.concurrencyLevel(1)
			.build();

	/**
	 * Elimina de la caché el carrito de la compra indicado
	 * 
	 * @param selector
	 */
	public void clearCart(final String selector) {
		cache.invalidate(selector);
	}

	/**
	 * Recupera el carrito de la compra indicado
	 * 
	 * @param selector
	 * @return el carrito o null si no existe
	 */
	public Cart getCart(final String selector) {
		return cache.getIfPresent(selector);
	}

	/**
	 * Añade el carrito de la compra indicado
	 * 
	 * @param selector
	 * @param cart
	 */
	public void setCart(final String selector, final Cart cart) {
		cache.put(selector, cart);
	}

	/**
	 * Crea y añade a la caché un carrito de la compra nuevo
	 * 
	 * @return su selector
	 */
	public Cart newCart() {
		final String selector = UUID.randomUUID().toString();
		final Cart cart = new Cart(selector);
		cart.setUuid("");
		cache.put(selector, cart);
		return cart;
	}

	/**
	 * Devuelve el UUID de la compra indicada por el selector
	 * 
	 * @param selector
	 * @return UUID o null si el carrito no existe
	 */
	public String getUuid(final String selector) {
		final Cart cart = getCart(selector);
		if (cart == null) {
			return null;
		}

		return cart.getUuid();
	}

	/**
	 * Asigna el UUID de la compra del carrito indicado
	 * 
	 * @param selector
	 * @param uuid
	 * @throws NullPointerException si el selector no es válido
	 */
	public void setUuid(final String selector, final String uuid) throws NullPointerException {
		getCart(selector).setUuid(uuid);
	}

	/**
	 * Devuelve el JSON de las butacas del carrito de la compra indicado
	 * @param selector
	 * @return JSON o null si no existe el carrito
	 */
	public String getButacas(final String selector) {
		final Cart cart = getCart(selector);
		if (cart == null) {
			return null;
		}

		return cart.getButacas();
	}

	/**
	 * Asigna el JSON de las butacas al carrito de la compra indicado
	 * 
	 * @param selector
	 * @param butacas
	 * @throws NullPointerException si el selector no es válido
	 */
	public void setButacas(final String selector, final String butacas) throws NullPointerException {
		getCart(selector).setButacas(butacas);
	}
}
