package es.uji.apps.par.dao;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import com.mysema.query.types.Expression;
import com.mysema.query.types.QTuple;
import com.sun.istack.logging.Logger;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.db.*;
import es.uji.apps.par.exceptions.ButacaOcupadaAlActivarException;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.ficheros.registros.TipoIncidencia;
import es.uji.apps.par.model.Abonado;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.report.InformeModelReport;
import es.uji.apps.par.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class ComprasDAO extends BaseDAO {
	public static Logger log = Logger.getLogger(ComprasDAO.class);
	private static final int ELIMINA_PENDIENTES_MINUTOS = 20;

	@Autowired
	private SesionesDAO sesionDAO;

    @Autowired
    private AbonadosDAO abonadoDAO;

	private DatabaseHelper dbHelper;

	@Autowired
	public ComprasDAO(Configuration configuration) {
		dbHelper = DatabaseHelperFactory.newInstance(configuration);
	}

	@Transactional
	public CompraDTO insertaCompra(Long sesionId, Date fecha, boolean taquilla, BigDecimal importe, String userUID) {
		SesionDTO sesion = sesionDAO.getSesion(sesionId, userUID);
		CompraDTO compraDTO = new CompraDTO(sesion, new Timestamp(
				fecha.getTime()), taquilla, importe, UUID.randomUUID()
				.toString());

		entityManager.persist(compraDTO);

		return compraDTO;
	}

    @Transactional
    public CompraDTO insertaCompra(Long sesionId, Date fecha, boolean taquilla,
                                   BigDecimal importe, String email, String nombre, String apellidos, String userUID) {
        SesionDTO sesion = sesionDAO.getSesion(sesionId, userUID);

        CompraDTO compraDTO = new CompraDTO(sesion, new Timestamp(
                fecha.getTime()), taquilla, importe, UUID.randomUUID()
                .toString(), email, nombre, apellidos);

        entityManager.persist(compraDTO);

        return compraDTO;
    }

    @Transactional
    public CompraDTO insertaCompraAbono(Long sesionId, Date fecha, boolean taquilla,
                                   Abonado abonado, String userUID) {
        SesionDTO sesion = sesionDAO.getSesion(sesionId, userUID);

        CompraDTO compraDTO = new CompraDTO(sesion, new Timestamp(
                fecha.getTime()), taquilla, new BigDecimal(0), UUID.randomUUID()
                .toString(), abonado);

        entityManager.persist(compraDTO);

        return compraDTO;
    }

	@Transactional
	public CompraDTO reserva(Long sesionId, Date fecha, Date desde, Date hasta,
			String observaciones, String userUID) {
		SesionDTO sesion = sesionDAO.getSesion(sesionId, userUID);

		CompraDTO compraDTO = new CompraDTO(sesion, new Timestamp(
				fecha.getTime()), true, BigDecimal.ZERO, UUID.randomUUID()
				.toString());

		compraDTO.setReserva(true);
		compraDTO.setDesde(DateUtils.dateToTimestampSafe(desde));
		compraDTO.setHasta(DateUtils.dateToTimestampSafe(hasta));
		compraDTO.setObservacionesReserva(observaciones);

		entityManager.persist(compraDTO);

		return compraDTO;
	}

	@Transactional
	public CompraDTO guardaCompra(Long compraId, Long sesionId, Date fecha,
			boolean taquilla, BigDecimal importe, String userUID) {
		CompraDTO compraDTO = getCompraById(compraId);

		if (compraDTO == null) {
			compraDTO = insertaCompra(sesionId, fecha, taquilla, importe, userUID);
		} else {
			SesionDTO sesion = sesionDAO.getSesion(sesionId, userUID);
			compraDTO.setParSesion(sesion);
			compraDTO.setFecha(new Timestamp(fecha.getTime()));
			compraDTO.setTaquilla(taquilla);
			compraDTO.setImporte(importe);

			entityManager.persist(compraDTO);
		}

		return compraDTO;
	}

    @Transactional
    public List<Tuple> getComprasYPresentadas(long sesionId) {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qCompraDTO).leftJoin(qCompraDTO.parButacas, qButacaDTO)
                .where(qCompraDTO.parSesion.id.eq(sesionId).and(qCompraDTO.email.isNotNull()).and(qCompraDTO.anulada.isFalse())).groupBy(qCompraDTO.email).list(qCompraDTO.email, qCompraDTO.count(), qButacaDTO.presentada.count());
    }

	@Transactional
	public CompraDTO getCompraById(long id) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

		JPAQuery query = new JPAQuery(entityManager);

		List<CompraDTO> compras = query.from(qCompraDTO, qButacaDTO)
				.where(qCompraDTO.id.eq(id)).distinct().list(qCompraDTO);

		if (compras.size() == 0)
			return null;
		else
            return compras.get(0);
	}

	@Transactional
	public void guardarCodigoPagoTarjeta(long idCompra, String codigo) {
		CompraDTO compra = getCompraById(idCompra);

		compra.setCodigoPagoTarjeta(codigo);

		entityManager.persist(compra);
	}

	@Transactional
	public void marcarPagada(Long idCompra) {
		CompraDTO compra = getCompraById(idCompra);

		compra.setPagada(true);

		entityManager.persist(compra);
	}

	@Transactional
	public void marcarPagadaConReferenciaDePago(Long idCompra, String referenciaPago) {
		CompraDTO compra = getCompraById(idCompra);

		compra.setPagada(true);
		compra.setReferenciaPago(referenciaPago);

		entityManager.persist(compra);
	}

	@Transactional
	public void marcarPagadaConRecibo(Long idCompra, String reciboPinpad) {
		CompraDTO compra = getCompraById(idCompra);

		compra.setPagada(true);
		compra.setReciboPinpad(reciboPinpad);

		entityManager.persist(compra);
	}

    @Transactional
    public void marcarAbonadoPagadaConRecibo(Long idAbonado, String reciboPinpad) {
        AbonadoDTO abonado = abonadoDAO.getAbonado(idAbonado);

        for (CompraDTO compra : abonado.getParCompras()) {
            compra.setPagada(true);
            compra.setReciboPinpad(reciboPinpad);

            entityManager.persist(compra);
        }
    }

	@Transactional
	public void marcarPagadaPasarela(Long idCompra, String codigoPago) {
		CompraDTO compra = getCompraById(idCompra);

		compra.setCodigoPagoPasarela(codigoPago);
		compra.setPagada(true);

		entityManager.persist(compra);
	}

	@Transactional
	public void borrarCompraNoPagada(Long idCompra) {
		CompraDTO compra = getCompraById(idCompra);

		if (compra != null && !compra.getPagada()) {
			entityManager.remove(compra);
		}
	}

    @Transactional
    public void borrarCompraAbonadoNoPagada(Long idAbonado) {
        AbonadoDTO abonado = abonadoDAO.getAbonado(idAbonado);

        if (abonado != null) {
            for (CompraDTO compra : abonado.getParCompras()) {
                if (compra != null && !compra.getPagada()) {
                    entityManager.remove(compra);
                }
            }
        }
    }

	@Transactional
	public void borrarCompraNoPagada(String uuidCompra) {
		CompraDTO compra = getCompraByUuid(uuidCompra);

		if (compra != null && !compra.getPagada()) {
			entityManager.remove(compra);
		}
	}

	@Transactional
	public CompraDTO getCompraByUuid(String uuid) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

		JPAQuery query = new JPAQuery(entityManager);

		List<CompraDTO> compras = query.from(qCompraDTO, qButacaDTO)
				.where(qCompraDTO.uuid.eq(uuid)).distinct().list(qCompraDTO);

		if (compras.size() == 0)
			return null;
		else
			return compras.get(0);
	}

	@Transactional
	public void eliminaComprasPendientes() throws IncidenciaNotFoundException {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -ELIMINA_PENDIENTES_MINUTOS);

		Timestamp limite = new Timestamp(calendar.getTimeInMillis());

		JPAQuery query = new JPAQuery(entityManager);

		List<CompraDTO> comprasACaducar = query
				.from(qCompraDTO)
				.where(qCompraDTO.pagada.eq(false)
						.and(qCompraDTO.anulada.eq(false))
						.and(qCompraDTO.reserva.eq(false))
						.and(qCompraDTO.fecha.lt(limite))).distinct()
				.list(qCompraDTO);

		for (CompraDTO compra : comprasACaducar) {
			compra.setCaducada(true);
			entityManager.persist(compra);

			anularCompraReserva(compra.getId(), false);
		}
	}

	@Transactional
	public void rellenaDatosComprador(String uuidCompra, String nombre,
			String apellidos, String direccion, String poblacion, String cp,
			String provincia, String telefono, String email,
			Object infoPeriodica) {
		log.info("RellenaDatosComprador uuidCompra" + uuidCompra + " nombre "
				+ nombre + " " + apellidos);
		CompraDTO compra = getCompraByUuid(uuidCompra);

		compra.setNombre(nombre);
		compra.setApellidos(apellidos);
		compra.setDireccion(direccion);
		compra.setPoblacion(poblacion);
		compra.setCp(cp);
		compra.setProvincia(provincia);
		compra.setTelefono(telefono);
		compra.setEmail(email);
		compra.setInfoPeriodica(infoPeriodica != null
				&& infoPeriodica.equals("si"));

		entityManager.persist(compra);
	}

	@Transactional
	public List<Tuple> getComprasBySesion(long sesionId, int showAnuladas,
			String sortParameter, int start, int limit, int showOnline,
			String search) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

        QTuple selectGroupByExpression = getSelectGroupByExpression();

        return getQueryComprasBySesion(sesionId, showAnuladas, showOnline,
                search, true, selectGroupByExpression).orderBy(getSort(qCompraDTO, sortParameter))
                .offset(start).limit(limit).list(selectGroupByExpression, qButacaDTO.precio.sum());
	}

    private QTuple getSelectGroupByExpression() {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

        return new QTuple(qCompraDTO.id, qCompraDTO.anulada,
                qCompraDTO.apellidos, qCompraDTO.caducada,
                qCompraDTO.codigoPagoPasarela,
                qCompraDTO.codigoPagoTarjeta, qCompraDTO.cp,
                qCompraDTO.desde, qCompraDTO.direccion,
                qCompraDTO.email, qCompraDTO.fecha,
                qCompraDTO.hasta, qCompraDTO.importe,
                qCompraDTO.infoPeriodica, qCompraDTO.nombre,
                qCompraDTO.observacionesReserva,
                qCompraDTO.pagada, qCompraDTO.parSesion.id,
                qCompraDTO.poblacion, qCompraDTO.provincia,
                qCompraDTO.reciboPinpad,
                qCompraDTO.referenciaPago, qCompraDTO.reserva,
                qCompraDTO.taquilla, qCompraDTO.telefono,
                qCompraDTO.uuid);
    }

	@Transactional
	private JPAQuery getQueryComprasBySesion(long sesionId, int showAnuladas,
			int showOnline, String search, boolean doJoinButacas, QTuple selectGroupByExpression) {
		JPAQuery query = new JPAQuery(entityManager);
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qCompraDTO.parSesion.id.eq(sesionId).and(qCompraDTO.parAbonado.isNull()));

		if (showAnuladas == 0) {
			builder.and(qCompraDTO.anulada.isNull().or(
					qCompraDTO.anulada.eq(false)));
			if (doJoinButacas)
				builder.and(qButacaDTO.anulada.isNull().or(
						qButacaDTO.anulada.eq(false)));
		}

		if (showOnline == 0)
			builder.and(qCompraDTO.taquilla.eq(true));

		if (!"".equals(search)) {
			String isNumeric = "^[\\d]+$";
			if (search.matches(isNumeric)) {
				long numericSearch = Integer.parseInt(search);
				builder.and(qCompraDTO.uuid
						.toUpperCase()
						.like('%' + search.toUpperCase() + '%')
						.or(qCompraDTO.observacionesReserva.toUpperCase()
								.like('%' + search.toUpperCase() + '%')
								.or(qCompraDTO.id.eq(numericSearch))));
			} else {
				search = search.toUpperCase();
				builder.and(qCompraDTO.uuid
						.toUpperCase()
						.like('%' + search + '%')
						.or(qCompraDTO.observacionesReserva.toUpperCase().like(
								'%' + search + '%'))
						.or(qCompraDTO.nombre
								.toUpperCase()
								.like('%' + search + '%')
								.or(qCompraDTO.apellidos.toUpperCase().like(
										'%' + search + '%'))));
			}
		}

		if (doJoinButacas) {
            query.from(qCompraDTO)
                    .leftJoin(qCompraDTO.parButacas, qButacaDTO)
                    .where(builder);

            for (Expression<?> expression : selectGroupByExpression.getArgs())
            {
                query.groupBy(expression);
            }
        }
		else
			query.from(qCompraDTO).where(builder);

		return query;
	}

	@Transactional
	public int getTotalComprasBySesion(Long sesionId, int showAnuladas,
			int showOnline, String search) {
		return (int) getQueryComprasBySesion(sesionId, showAnuladas,
				showOnline, search, false, getSelectGroupByExpression()).count();
	}

	@Transactional
	private SesionDTO getSesion(Long idCompra) {
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qCompraDTO).join(qCompraDTO.parSesion, qSesionDTO).where(qCompraDTO.id.eq(idCompra)).uniqueResult
				(qSesionDTO);
	}

	@Transactional(rollbackForClassName = {"IncidenciaNotFoundException"})
	public void anularCompraReserva(Long idCompraReserva, boolean manual) throws IncidenciaNotFoundException {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		int ANULACION_VENTAS = 9;

		JPAUpdateClause updateCompra = new JPAUpdateClause(entityManager,
				qCompraDTO);
		updateCompra.set(qCompraDTO.anulada, true)
				.where(qCompraDTO.id.eq(idCompraReserva)).execute();

		JPAUpdateClause updateButacas = new JPAUpdateClause(entityManager,
				qButacaDTO);
		updateButacas.set(qButacaDTO.anulada, true)
				.where(qButacaDTO.parCompra.id.eq(idCompraReserva)).execute();

		CompraDTO compra = getCompraById(idCompraReserva);

		if (manual && (compra.getReserva() == null || compra.getReserva() != true)) {
			SesionDTO sesionDTO = getSesion(idCompraReserva);
			sesionDAO.setIncidencia(sesionDTO.getId(), TipoIncidencia.addAnulacionVentasToIncidenciaActual(sesionDTO.getIncidenciaId()));
		}
	}

	@Transactional
	public void desanularCompraReserva(Long idCompraReserva)
			throws ButacaOcupadaAlActivarException {

		try {
			QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
			QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
			JPAUpdateClause updateCompra = new JPAUpdateClause(entityManager,
					qCompraDTO);
			updateCompra.set(qCompraDTO.anulada, false)
					.set(qCompraDTO.caducada, false)
					.set(qCompraDTO.pagada, true)
					.where(qCompraDTO.id.eq(idCompraReserva)).execute();

			JPAUpdateClause updateButacas = new JPAUpdateClause(entityManager,
					qButacaDTO);
			updateButacas.set(qButacaDTO.anulada, false)
					.where(qButacaDTO.parCompra.id.eq(idCompraReserva))
					.execute();
		} catch (Exception e) {
			for (ButacaDTO butaca : getCompraById(idCompraReserva)
					.getParButacas()) {
				if (butacaOcupada(butaca)) {
					String comprador = "";

					if (butaca.getParCompra().getNombre() != null
							|| butaca.getParCompra().getApellidos() != null)
						comprador = butaca.getParCompra().getNombre() + " "
								+ butaca.getParCompra().getApellidos();

					throw new ButacaOcupadaAlActivarException(butaca
							.getParSesion().getId(), butaca
							.getParLocalizacion().getCodigo(),
							butaca.getFila(), butaca.getNumero(), comprador,
							butaca.getParCompra().getTaquilla());
				}
			}
		}
	}

	@Transactional
	private boolean butacaOcupada(ButacaDTO butaca) {
		QButacaDTO b = QButacaDTO.butacaDTO;

		JPAQuery query = new JPAQuery(entityManager);
		query.from(b).where(
				b.parSesion.id
						.eq(butaca.getParSesion().getId())
						.and(b.anulada.eq(false))
						.and(b.parLocalizacion.codigo.eq(butaca
								.getParLocalizacion().getCodigo()))
						.and(b.fila.eq(butaca.getFila()))
						.and(b.numero.eq(butaca.getNumero())));

		return query.count() > 0;
	}

	@Transactional
	public List<CompraDTO> getReservasACaducar(Date date) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

		JPAQuery query = new JPAQuery(entityManager);

		List<CompraDTO> compras = query
				.from(qCompraDTO)
				.where(qCompraDTO.reserva
						.eq(true)
						.and(qCompraDTO.anulada.eq(false))
						.and(qCompraDTO.hasta.lt(DateUtils
								.dateToTimestampSafe(date)))).distinct()
				.list(qCompraDTO);

		return compras;
	}

	private String sqlConditionsToSkipAnuladasIReservas(String fechaInicio, String fechaFin) {
		return "and TO_CHAR(c.fecha, 'YYYY-MM-DD HH24:MI') >= '"	+ fechaInicio + " 00:00' "
				+ "and TO_CHAR(c.fecha, 'YYYY-MM-DD HH24:MI') <= '" + fechaFin + " 23:59' "
				+ "and c.pagada = " + dbHelper.trueString() + " "
				+ "and c.reserva = " + dbHelper.falseString() + " "
				+ "and b.anulada = " + dbHelper.falseString() + " "
				+ "and c.anulada = " + dbHelper.falseString() + " "
				+ "and (s.anulada is null or s.anulada = " + dbHelper.falseString() + ") ";
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasInFechas(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select e.titulo_es, s.fecha_celebracion, b.tipo, l.codigo, count(b.id) as cantidad, sum(b.precio) as total, c.sesion_id, "
				+ "l.nombre_es, f.nombre "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_localizaciones l, par_tarifas f, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id and l.id=b.localizacion_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.trueString() + " "
				+ "and c.codigo_pago_tarjeta is null "
				+ "and f.id = "	+ dbHelper.toInteger("b.tipo") + " "
				+ "group by c.sesion_id, e.titulo_es, b.tipo, s.fecha_celebracion, l.codigo, l.nombre_es, f.nombre "
				+ "order by e.titulo_es";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasPorEventoInFechas(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select e.id, e.titulo_es, b.tipo, count(b.id) as cantidad, sum(b.precio) as total, c.taquilla, "
				+ "f.nombre "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_tarifas f, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and f.id = "	+ dbHelper.toInteger("b.tipo") + " "
				+ "group by e.id, e.titulo_es, b.tipo, c.taquilla, f.nombre "
				+ "order by e.titulo_es";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasEfectivo(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select e.titulo_es, s.fecha_celebracion, b.tipo, count(b.id) as cantidad, sum(b.precio) as total, "
				+ "c.sesion_id, e.porcentaje_iva, "
				+ "f.nombre "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_tarifas f, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.trueString() + " "
				+ getSQLCompraIsEfectivo()
				+ "and f.id = " + dbHelper.toInteger("b.tipo") + " "
				+ "and c.abonado_id is null "
				+ "group by c.sesion_id, e.titulo_es, b.tipo, s.fecha_celebracion, e.porcentaje_iva, f.nombre "
				+ "order by e.titulo_es, s.fecha_celebracion, f.nombre";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getAbonosEfectivo(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select a.nombre, count(a.id)/count(distinct(c.sesion_id)) as abonos, sum(b.precio)/count(distinct(c.sesion_id)) as total "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_tarifas f, par_abonados abdos, par_abonos a, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id and c.abonado_id = abdos.id and abdos.abono_id = a.id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.trueString() + " "
				+ getSQLCompraIsEfectivo()
				+ "and f.id = " + dbHelper.toInteger("b.tipo") + " "
				+ "and c.abonado_id is not null "
				+ "group by a.nombre, b.tipo "
				+ "order by a.nombre";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	private String getSQLCompraIsEfectivo() {
		return "and ((c.codigo_pago_tarjeta IS NULL or c.codigo_pago_tarjeta = '') "
				+ "and (c.referencia_pago IS NULL or c.referencia_pago = '')) ";
	}

	private String getSQLCompraIsTPV(boolean contarOnline) {
		String sql = "and ((c.codigo_pago_tarjeta is not null and " + dbHelper.isEmptyString("c.codigo_pago_tarjeta") + ") "
				+ "or (c.codigo_pago_pasarela is not null and " + dbHelper.isEmptyString("c.codigo_pago_pasarela") + ") "
				+ "or (c.referencia_pago is not null and " + dbHelper.isEmptyString("c.referencia_pago") + ") ";
		sql += (contarOnline) ? "or c.taquilla = " + dbHelper.falseString() + ") " : ") ";
		return sql;
	}

	private String getSQLCompraIsTPVOnline() {
		String sql = "and ((c.codigo_pago_tarjeta is not null "
				+ "or c.codigo_pago_pasarela is not null "
				+ "or c.referencia_pago is not null) "
				+ "and c.taquilla = " + dbHelper.falseString() + ") ";
		return sql;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasTpv(String fechaInicio, String fechaFin, String userUID) {
		return getComprasTpvType(fechaInicio, fechaFin, userUID, false);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasTpvOnline(String fechaInicio, String fechaFin, String userUID) {
		return getComprasTpvType(fechaInicio, fechaFin, userUID, true);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	private List<Object[]> getComprasTpvType(String fechaInicio, String fechaFin, String userUID, boolean onlyOnline) {
		String formato = "DD";
		String sql = "select e.titulo_es, s.fecha_celebracion, b.tipo, count(b.id) as cantidad, sum(b.precio) as total, c.sesion_id, "
				+ "e.porcentaje_iva, "
				+ dbHelper.trunc("c.fecha", formato)
				+ ", f.nombre "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_tarifas f, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and (c.caducada is null or c.caducada = " + dbHelper.falseString() + ") "
				+ (onlyOnline ? getSQLCompraIsTPVOnline() : getSQLCompraIsTPV(false))
				+ "and f.id = " + dbHelper.toInteger("b.tipo") + " "
				+ "group by c.sesion_id, e.titulo_es, b.tipo, s.fecha_celebracion, e.porcentaje_iva, "
				+ dbHelper.trunc("c.fecha", formato) + ", f.nombre "
				+ "order by " + dbHelper.trunc("c.fecha", formato) + ", s.fecha_celebracion, f.nombre";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Object[]> getComprasEventos(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select e.titulo_es, s.fecha_celebracion, b.tipo, count(b.id) as cantidad, sum(b.precio) as total, "
				+ "e.porcentaje_iva, "
				+ dbHelper.caseString("b.tipo", new String[] { "'normal'", "1",
						"'descuento'", "2", "'aulaTeatro'", "3",
						"'invitacion'", "4" })
				+ " as tipoOrden, e.id as eventoId, s.id as sesionId, f.nombre "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, par_tarifas f, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and f.id = "	+ dbHelper.toInteger("b.tipo") + " "
				+ "group by e.id, s.id, e.titulo_es, b.tipo, s.fecha_celebracion, e.porcentaje_iva, f.nombre "
				+ "order by s.fecha_celebracion, tipoOrden";

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@Transactional
	public Object[] getTotalTaquillaTpv(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select sum(b.precio), count(b.precio) "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.trueString() + " "
				+ getSQLCompraIsTPV(false);

        List<Object[]> result = entityManager.createNativeQuery(sql).getResultList();

        if (result.size() > 0)
            return result.get(0);
        else
            return null;
	}

	@Transactional
	public Object[] getTotalTaquillaEfectivo(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select sum(b.precio), count(b.precio) "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.trueString() + " "
				+ getSQLCompraIsEfectivo();

		List<Object[]> result = entityManager.createNativeQuery(sql).getResultList();

        if (result.size() > 0)
            return result.get(0);
        else
            return null;
	}

	@Transactional
	public Object[] getTotalOnline(String fechaInicio, String fechaFin, String userUID) {
		String sql = "select sum(b.precio), count(b.precio) "
				+ "from par_butacas b, par_compras c, par_sesiones s, par_eventos e, "
				+ "par_salas sala, par_salas_usuarios su, par_usuarios u "
				+ "where b.compra_id = c.id and s.id = c.sesion_id and e.id = s.evento_id "
				+ "and sala.id = s.sala_id and u.usuario = '" + userUID + "' and sala.id = su.sala_id and su.usuario_id = u.id "
				+ sqlConditionsToSkipAnuladasIReservas(fechaInicio, fechaFin)
				+ "and c.taquilla = " + dbHelper.falseString() + " ";

        List<Object[]> result = entityManager.createNativeQuery(sql).getResultList();

        if (result.size() > 0)
            return result.get(0);
        else
            return null;
	}

	@Transactional
	public void rellenaCodigoPagoPasarela(long idCompra, String codigoPago) {
		CompraDTO compra = getCompraById(idCompra);
		compra.setCodigoPagoPasarela(codigoPago);

		entityManager.persist(compra);
	}

	@Transactional
	public BigDecimal getRecaudacionSesiones(List<Sesion> sesiones) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

		List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);

		JPAQuery query = new JPAQuery(entityManager);

		BigDecimal total = query
				.from(qCompraDTO)
				.join(qCompraDTO.parSesion, qSesionDTO)
				.join(qCompraDTO.parButacas, qButacaDTO)
				.where(qSesionDTO.id.in(idsSesiones)
                        .and(qCompraDTO.parAbonado.isNull())
						.and(qCompraDTO.reserva.eq(false))
						.and(qCompraDTO.anulada.eq(false))
						.and(qButacaDTO.anulada.eq(false))).distinct()
				.uniqueResult(qButacaDTO.precio.sum());

		if (total == null)
			return BigDecimal.ZERO;
		else
			return total;
	}

	@Transactional
	public int getEspectadores(List<Sesion> sesiones) {
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

		List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);

		JPAQuery query = new JPAQuery(entityManager);

		Long butacas = query
				.from(qCompraDTO)
				.join(qCompraDTO.parSesion, qSesionDTO)
				.join(qCompraDTO.parButacas, qButacaDTO)
				.where(qSesionDTO.id.in(idsSesiones)
						.and(qCompraDTO.reserva.eq(false))
						.and(qCompraDTO.anulada.eq(false))
						.and(qButacaDTO.anulada.eq(false)))
				.uniqueResult(qButacaDTO.count());

		if (butacas == null)
			return 0;
		else
			return butacas.intValue();
	}

	@Transactional
	public List<CompraDTO> getComprasOfEvento(long eventoId) {
		QEventoDTO qEvento = QEventoDTO.eventoDTO;
		QSesionDTO qSesion = QSesionDTO.sesionDTO;
		QCompraDTO qCompra = QCompraDTO.compraDTO;

		JPAQuery query = new JPAQuery(entityManager);
		return query
				.from(qEvento, qSesion, qCompra)
				.where(qEvento.id.eq(eventoId).and(
						qSesion.parEvento.id.eq(qEvento.id).and(
								qCompra.parSesion.id.eq(qSesion.id))))
				.list(qCompra);
	}

    @Transactional
    public List<CompraDTO> getComprasOfSesion(long sesionId) {
        QCompraDTO qCompra = QCompraDTO.compraDTO;

        JPAQuery query = new JPAQuery(entityManager);
        return query
                .from(qCompra)
                .where(qCompra.parSesion.id.eq(sesionId).and(qCompra.anulada.isFalse()).and(qCompra.caducada.isFalse()))
                .list(qCompra);
    }

	public InformeModelReport getResumenSesion(Long sesionId) {
		InformeModelReport r = new InformeModelReport();
		JPAQuery query = new JPAQuery(entityManager);
		QButacaDTO qButaca = QButacaDTO.butacaDTO;
		Long vendidas = query.from(qButaca)
			.where(qButaca.parSesion.id.eq(sesionId).and(qButaca.anulada.eq(false).and(qButaca.parCompra.reserva.eq(false)).and(qButaca.parCompra.parAbonado.isNull()).and
					(qButaca.parSesion.anulada.isNull().or(qButaca.parSesion.anulada.eq(false))))).count();
		r.setNumeroEntradas(vendidas.intValue());

		query = new JPAQuery(entityManager);
		Long canceladas = query
				.from(qButaca)
				.where(qButaca.parSesion.id.eq(sesionId).and(qButaca.parCompra.parAbonado.isNull())
				.and(qButaca.parSesion.anulada.isNull().or(qButaca.parSesion.anulada.eq(false)))
				.and(qButaca.parCompra.reserva.eq(false).and(qButaca.anulada.eq(true)))).count();
		r.setCanceladasTaquilla(canceladas.intValue());

		Sesion sesion = new Sesion(sesionId.intValue());
		r.setTotal(getRecaudacionSesiones(Arrays.asList(sesion)));

		return r;
	}

	@Transactional
	public void passarACompra(Long sesionId, Long idCompraReserva, String recibo) {
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		JPAUpdateClause updateC = new JPAUpdateClause(entityManager, qCompraDTO);
		updateC.set(qCompraDTO.reserva, false)
				.set(qCompraDTO.pagada, true)
				.setNull(qCompraDTO.desde)
				.setNull(qCompraDTO.hasta)
				.set(qCompraDTO.caducada, false)
				.set(qCompraDTO.anulada, false)
				.set(qCompraDTO.referenciaPago, recibo)
				.where(qCompraDTO.id.eq(idCompraReserva).and(
						qCompraDTO.parSesion.id.eq(sesionId).and(
								qCompraDTO.reserva.eq(true)))).execute();
	}

	@Transactional
    public void updateDatosAbonadoCompra(Abonado abonado) {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        JPAUpdateClause updateC = new JPAUpdateClause(entityManager, qCompraDTO);
        updateC.set(qCompraDTO.nombre, abonado.getNombre())
                .set(qCompraDTO.apellidos, abonado.getApellidos())
                .set(qCompraDTO.direccion, abonado.getDireccion())
                .set(qCompraDTO.poblacion, abonado.getPoblacion())
                .set(qCompraDTO.cp, abonado.getCp())
                .set(qCompraDTO.provincia, abonado.getProvincia())
                .set(qCompraDTO.telefono, abonado.getTelefono())
                .set(qCompraDTO.email, abonado.getEmail())
                .set(qCompraDTO.infoPeriodica, abonado.getInfoPeriodica())
                .where(qCompraDTO.parAbonado.id.eq(abonado.getId())).execute();

    }

	@Transactional
	public String getReportClassByCompraUUID(String uuidCompra, String tipo) {
		JPAQuery query = new JPAQuery(entityManager);
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		QReportDTO qReportDTO = QReportDTO.reportDTO;

		return query.from(qCompraDTO).join(qCompraDTO.parSesion, qSesionDTO).join(qSesionDTO.parSala, qSalaDTO).join(qSalaDTO
				.parReports, qReportDTO)
				.where(qCompraDTO.uuid.eq(uuidCompra).and(qReportDTO.tipo.toUpperCase().eq(tipo.toUpperCase())))
				.uniqueResult(qReportDTO.clase);
	}
}
