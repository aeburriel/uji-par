/**
 * Gestión de las compras de butacas con distanciamiento social en sesiones numeradas.
 * Copyright (c) 2020 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Butaca;

@Service
public class ButacasDistanciamientoSocialService {
	@Autowired
	Configuration configuration;

	@Autowired
	ButacasService butacasService;

	@Autowired
	private SesionesDAO sesionesDAO;

	private static final String ADMIN_UID = "admin";

	private static final int BUTACAS_GUARDA = 2;
	private static final String LOCALIZACION_TEATRO_ANFITEATRO_CENTRO = "anfiteatro_central";
	private static final String LOCALIZACION_TEATRO_ANFITEATRO_IMPAR = "anfiteatro_lateral_senar";
	private static final String LOCALIZACION_TEATRO_ANFITEATRO_PAR = "anfiteatro_lateral_par";
	private static final String LOCALIZACION_TEATRO_GENERAL = "general";

	class FilaNumeracion {
		private final String localizacion;
		private final int fila;
		private final int primera;
		private final int ultima;
		private final int paso;

		FilaNumeracion(final String localizacion, final int fila, final int primera, final int ultima, final int paso) {
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
	}

	/**
	 * Devuelve los datos de numeración de la fila correspondiente a la butaca
	 * indicada
	 *
	 * @param butaca
	 * @return FilaNumeracion
	 */
	private FilaNumeracion getFilaNumeracion(final Butaca butaca) {
		final String localizacion = butaca.getLocalizacion();
		final int fila = Integer.parseInt(butaca.getFila());
		final boolean par = Integer.parseInt(butaca.getNumero()) % 2 == 0;

		int primera;
		int ultima;
		int paso;

		switch (localizacion) {
		case LOCALIZACION_TEATRO_ANFITEATRO_CENTRO:
			primera = 1;
			ultima = 7;
			paso = 1;
			break;
		case LOCALIZACION_TEATRO_ANFITEATRO_IMPAR:
			primera = 1;
			if (fila <= 2) {
				ultima = 13;
			} else if (fila <= 4) {
				ultima = 15;
			} else {
				ultima = 17;
			}
			paso = 2;
			break;
		case LOCALIZACION_TEATRO_ANFITEATRO_PAR:
			primera = 2;
			ultima = 14;
			paso = 2;
			break;
		case LOCALIZACION_TEATRO_GENERAL:
			if (fila == 1) {
				paso = 2;
				if (par) {
					// Butacas pares
					primera = 2;
					ultima = 18;
				} else {
					// Butacas impares
					primera = 1;
					ultima = 17;
				}
			} else if (par) {
				// Resto de butacas pares
				primera = 2;
				ultima = 20;
				paso = 2;
			} else {
				// Resto de butacas impares
				primera = 1;
				paso = 2;
				if (fila == 2) {
					ultima = 17;
				} else if (fila <= 4) {
					ultima = 19;
				} else if (fila <= 6) {
					ultima = 21;
				} else if (fila <= 8) {
					ultima = 23;
				} else {
					ultima = 25;
				}
			}
			break;
		default:
			return null;
		}

		return new FilaNumeracion(localizacion, fila, primera, ultima, paso);
	}

	/**
	 * Clasifica por filas un conjunto de butacas
	 *
	 * @param butacas a clasificar
	 * @return Multimap
	 */
	private SortedSetMultimap<FilaNumeracion, Butaca> butacas2Filas(final List<Butaca> butacas) {
		// Al clasificar por localización y fila solo hay que comparar el número de la butaca
		class SortbyNumeracion implements Comparator<Butaca> {
			@Override
			public int compare(final Butaca b0, final Butaca b1) {
				if (b0 == null && b1 == null) {
					return 0;
				}
				if (b0 == null) {
					return -1;
				}
				if (b1 == null) {
					return 1;
				}

				final int n0 = Integer.parseInt(b0.getNumero());
				final int n1 = Integer.parseInt(b1.getNumero());
				return n0 - n1;
			}

		}
		final SortedSetMultimap<FilaNumeracion, Butaca> filas = MultimapBuilder.hashKeys()
				.treeSetValues(new SortbyNumeracion()).build();

		for (final Butaca butaca : butacas) {
			final FilaNumeracion fila = getFilaNumeracion(butaca);
			filas.put(fila, butaca);
		}

		return filas;
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param fila FilaNumeracion
	 * @param bi0  índice inferior del grupo de butacas
	 * @param bi1  índice superior del grupo de butacas
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final FilaNumeracion fila, final int bi0, final int bi1) {
		int fi0 = fila.getIndice(fila.getPrimera());
		int fin = fila.getIndice(fila.getUltima());

		final List<Integer> indices = new ArrayList<Integer>();
		if (bi0 - fi0 > BUTACAS_GUARDA) {
			fi0 = bi0 - BUTACAS_GUARDA;
		}
		if (fin - bi1 > BUTACAS_GUARDA) {
			fin = bi1 + BUTACAS_GUARDA;
		}
		for (int i = fi0; i < bi0; i++) {
			indices.add(i);
		}
		for (int i = bi1 + 1; i <= fin; i++) {
			indices.add(i);
		}

		final Set<Butaca> entorno = Sets.newHashSet();
		for (final Integer i : indices) {
			final Butaca butaca = new Butaca();
			butaca.setLocalizacion(fila.getLocalizacion());
			butaca.setFila(String.valueOf(fila.getFila()));
			butaca.setNumero(String.valueOf(fila.getNumeroButaca(i)));

			entorno.add(butaca);
		}
		return entorno;
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param butaca0 Butaca
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final Butaca butaca0) {
		final FilaNumeracion fila = getFilaNumeracion(butaca0);
		final int bi = fila.getIndice(Integer.parseInt(butaca0.getNumero()));
		return getButacasEntorno(fila, bi, bi);
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param fila          FilaNumeracion
	 * @param seleccionadas Butacas seleccionadas
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final FilaNumeracion fila, final SortedSet<Butaca> seleccionadas) {
		final int bi0 = fila.getIndice(Integer.parseInt(seleccionadas.first().getNumero()));
		final int bi1 = fila.getIndice(Integer.parseInt(seleccionadas.last().getNumero()));

		return getButacasEntorno(fila, bi0, bi1);
	}

	/**
	 * Determina si la butaca indicada puede estar disponible, atendiendo al
	 * criterio de distanciamiento social. Este método se tiene que llamar al
	 * generar la lista de butacas ocupadas.
	 *
	 * @param sesion de la compra
	 * @param butaca a comprobar
	 * @return true si la butaca puede estar disponible para su venta
	 */
	public boolean isButacaLibrePermitida(final long sesionId, final Butaca butaca) {
		if (!configuration.isAforoDistanciamientoSocial()) {
			return true;
		}

		final Set<Butaca> entorno = getButacasEntorno(butaca);

		for (final Butaca candidata : entorno) {
			if (butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), butaca.getFila(),
					candidata.getNumero())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Determina si la butaca indicada puede estar disponible, atendiendo al
	 * criterio de distanciamiento social. Este método se tiene que llamar al
	 * generar la lista de butacas ocupadas.
	 *
	 * @param sesion    de la compra
	 * @param butacaDTO a comprobar
	 * @return true si la butaca puede estar disponible para su venta
	 */
	public boolean isButacaLibrePermitida(final long sesionId, final ButacaDTO butacaDTO) {
		final Butaca butaca = new Butaca();
		butaca.setLocalizacion(butacaDTO.getParLocalizacion().getCodigo());
		butaca.setFila(butacaDTO.getFila());
		butaca.setNumero(butacaDTO.getNumero());

		return isButacaLibrePermitida(sesionId, butaca);
	}

	private boolean isButacaOcupada(final long sesionId, final Butaca butaca) {
		return butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero());
	}

	/**
	 * Verifica que las butacas seleccionadas de una misma fila respetan la
	 * distancia de seguridad
	 * Se comprueba que, o bien están alineadas con uno de los extremos de la fila,
	 * o bien se respeta la distancia de guarda con la siguiente butaca ocupada.
	 * No se permite una distancia de guarda mayor que la mínima, con el fin de
	 * maximizar el aforo.
	 *
	 * @param fila    FilaNumeracion
	 * @param butacas asociadas a la fila
	 * @return true si no hay huecos innecesarios por alguno de los extremos
	 */
	private boolean isButacasFilaGuarda(final long sesionId, final FilaNumeracion fila,
			final SortedSet<Butaca> butacas) {
		final int i0 = fila.getIndice(Integer.parseInt(butacas.first().getNumero()));
		final int i1 = fila.getIndice(Integer.parseInt(butacas.last().getNumero()));
		final String localizacion = fila.getLocalizacion();

		// 1. Comprobamos si alguna de las butacas está en el extremo de la fila
		if (i0 == fila.getIndice(fila.getPrimera()) || i1 == fila.getIndice(fila.getUltima())) {
			return true;
		}

		// 2. Comprobamos si alguno de los extremos está exactamente a la distancia de
		// seguridad de la siguiente butaca ocupada
		final Butaca candidata = new Butaca();
		candidata.setLocalizacion(localizacion);
		candidata.setFila(String.valueOf(fila.getFila()));

		int distancia = 0;
		int i = i0;
		while (i-- > 0) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesionId, candidata)) {
				break;
			}
			distancia++;
		}
		if (distancia == BUTACAS_GUARDA) {
			return true;
		}

		distancia = 0;
		i = i1;
		while (++i < fila.getCantidadButacas()) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesionId, candidata)) {
				break;
			}
			distancia++;
		}
		if (distancia == BUTACAS_GUARDA) {
			return true;
		}

		return false;
	}

	/**
	 * Comprueba si entre las butacas seleccionadas de una misma fila hay algún
	 * hueco
	 *
	 * @param fila    FilaNumeracion
	 * @param butacas asociadas a la fila
	 * @return true si no hay ningún hueco entre la primera y última butaca
	 *         seleccionada
	 */
	private boolean isButacasFilaCompacta(final FilaNumeracion fila, final SortedSet<Butaca> butacas) {
		final int n0 = Integer.parseInt(butacas.first().getNumero());
		final int n1 = Integer.parseInt(butacas.last().getNumero());
		return fila.getCantidadButacas(n0, n1) == butacas.size();
	}

	/**
	 * Verifica si la combinación de butacas elegida esté permitida
	 * Se tiene que llamar cada vez que se comprueban las butacas en una venta
	 * en proceso. En particular se comprueba que:
	 *   1. Las restricciones de distanciamiento social estén activas.
	 *   2. Sea una sesión numerada.
	 *   3. En cada fila, las butacas elegidas sean contiguas.
	 *   4. En cada fila, se respete el espacio de guarda en ambos extremos o
	 *   exactamente la distancia de seguridad con la butaca ocupada más cercana.
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butacas  Lista con las butacas seleccionadas
	 * @return true si las butacas elegidas están permitidas
	 */
	public boolean validaButacas(final Long sesionId, final List<Butaca> butacas) {
		// 1. Estado distanciamiento social
		if (!configuration.isAforoDistanciamientoSocial()) {
			return true;
		}

		// 2. Sesión numerada
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Boolean numerado = sesion.getParEvento().getAsientosNumerados();
		if (numerado == null || !numerado) {
			return true;
		}

		final SortedSetMultimap<FilaNumeracion, Butaca> b2f = butacas2Filas(butacas);
		for (final FilaNumeracion fila : b2f.keySet()) {
			final SortedSet<Butaca> butacasFila = b2f.get(fila);

			// 3. Butacas contiguas
			if (!isButacasFilaCompacta(fila, butacasFila)) {
				return false;
			}

			// 4. Distancia de seguridad con la butaca ocupada más próxima
			if (!isButacasFilaGuarda(sesionId, fila, butacasFila)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Verifica que el cambio de butaca solicitado sea posible Se tiene que llamar
	 * antes de hacer un cambio de butaca.
	 *
	 * @param butaca Butaca sobre la que estamos haciendo la comprobación
	 * @param fila   Nueva fila, en la misma localización que la butaca inicial
	 * @param numero Nuevo número, en la misma localización que la butaca inicial
	 * @return true si la butaca destino es aceptable
	 */
	public boolean cambiaFilaNumero(final ButacaDTO butaca, final String fila, final String numero) {
		if (butaca == null || fila == null || numero == null) {
			return false;
		}

		if (!configuration.isAforoDistanciamientoSocial()) {
			return true;
		}

		// No permitimos cambios de butacas
		return false;
	}

}
