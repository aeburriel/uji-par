package es.uji.apps.par.dao;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import com.mysema.query.types.expr.BooleanExpression;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.db.*;
import es.uji.apps.par.exceptions.EdicionSesionAnuladaException;
import es.uji.apps.par.exceptions.EventoConCompras;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.exceptions.SesionSinFormatoIdiomaIcaaException;
import es.uji.apps.par.ficheros.registros.RegistroPelicula;
import es.uji.apps.par.ficheros.registros.RegistroSesion;
import es.uji.apps.par.ficheros.registros.RegistroSesionPelicula;
import es.uji.apps.par.ficheros.registros.TipoIncidencia;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Pair;
import es.uji.apps.par.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class SesionesDAO extends BaseDAO {
    private static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HHmm");

    private QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
    private QPreciosSesionDTO qPreciosSesionDTO = QPreciosSesionDTO.preciosSesionDTO;

	@Autowired
	Configuration configuration;

    @Autowired
    private EventosDAO eventosDAO;

    @Transactional
    public List<SesionDTO> getSesiones(long eventoId, String sortParameter, int start, int limit) {
        return getSesiones(eventoId, false, sortParameter, start, limit);
    }

    @Transactional
    public List<SesionDTO> getSesionesActivas(long eventoId, String sortParameter, int start, int limit) {
        return getSesiones(eventoId, true, sortParameter, start, limit);
    }

    @Transactional
    public List<SesionDTO> getSesiones(long eventoId, boolean activos, String sortParameter, int start, int limit) {
        List<SesionDTO> sesion = new ArrayList<SesionDTO>();

        if (activos)
            sesion = getQuerySesionesActivas(eventoId).orderBy(getSort(qSesionDTO, sortParameter)).offset(start)
                    .limit(limit).list(qSesionDTO);
        else
            sesion = getQuerySesiones(eventoId).orderBy(getSort(qSesionDTO, sortParameter)).offset(start).limit(limit)
                    .list(qSesionDTO);

        return sesion;
    }

    @Transactional
    public List<SesionDTO> getSesionesPorRssId(String rssId) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qSesionDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch().
                where(qSesionDTO.parEvento.rssId.eq(rssId)).list(qSesionDTO);
    }

    @Transactional
    public List<Tuple> getSesionesConButacasVendidas(long eventoId, boolean activas, String sortParameter,
                                                     int start, int limit) {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QAbonadoDTO qAbonadoDTO = QAbonadoDTO.abonadoDTO;

        JPAQuery query;

        if (activas)
            query = getQuerySesionesActivas(eventoId);
        else
            query = getQuerySesiones(eventoId);

        JPASubQuery queryVendidas = new JPASubQuery();
        queryVendidas.from(qButacaDTO, qCompraDTO);
        queryVendidas.where(qSesionDTO.id.eq(qButacaDTO.parSesion.id).and(qButacaDTO.anulada.eq(false).or(qButacaDTO.anulada.isNull())).
                and(qButacaDTO.parCompra.id.eq(qCompraDTO.id).and(qCompraDTO.reserva.eq(false)))).leftJoin(qButacaDTO.parCompra.parAbonado, qAbonadoDTO).where(qAbonadoDTO.isNull());

        JPASubQuery queryReservadas = new JPASubQuery();
        queryReservadas.from(qButacaDTO, qCompraDTO);
        queryReservadas.where(qSesionDTO.id.eq(qButacaDTO.parSesion.id).and(qButacaDTO.anulada.eq(false).or(qButacaDTO.anulada.isNull())).
                and(qButacaDTO.parCompra.id.eq(qCompraDTO.id).and(qCompraDTO.reserva.eq(true))));

        List<Tuple> sesiones = query.orderBy(getSort(qSesionDTO, sortParameter))/*.offset(start).limit(limit)*/
                .list(qSesionDTO, queryVendidas.list(qButacaDTO).count(), queryReservadas.list(qButacaDTO).count());

        return sesiones;
    }

    @Transactional
    private JPAQuery getQuerySesionesActivas(long eventoId) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;

        JPAQuery query = new JPAQuery(entityManager);

        Timestamp now = new Timestamp(configuration.dateConMargenTrasVenta().getTime());

        return query.from(qSesionDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch()
                .where(qSesionDTO.parEvento.id.eq(eventoId).and(qSesionDTO.fechaCelebracion.after(now)).and(qSesionDTO.anulada
                        .isNull().or(qSesionDTO.anulada.eq(false))));
    }

    @Transactional
    private JPAQuery getQuerySesiones(long eventoId) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qSesionDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch().
                where(qSesionDTO.parEvento.id.eq(eventoId));
    }

    @Transactional
    public List<SesionDTO> getSesionesPorSala(long eventoId, long salaId, String sortParameter) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        List<SesionDTO> sesiones = query.from(qSesionDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch().
                where(qSesionDTO.parEvento.id.eq(eventoId).and(qSalaDTO.id.eq(salaId))).orderBy(getSort(qSesionDTO, sortParameter)).list(qSesionDTO);

        return sesiones;
    }

    @Transactional
    public long removeSesion(long id) {
        JPADeleteClause delete = new JPADeleteClause(entityManager, qSesionDTO);
        return delete.where(qSesionDTO.id.eq(id)).execute();
    }

    @Transactional
    public SesionDTO persistSesion(SesionDTO sesionDTO) {
        entityManager.persist(sesionDTO);
        return sesionDTO;
    }

    @Transactional
    public Sesion addSesion(Sesion sesion) {
        SesionDTO sesionDTO = Sesion.SesionToSesionDTO(sesion);
        persistSesion(sesionDTO);
        setIncidenciaSesion(sesionDTO.getFechaCelebracion(), sesionDTO.getParSala().getId());
        anulaSesionesConLaMismaHoraYSala(sesionDTO.getFechaCelebracion(), sesionDTO.getParSala().getId(), sesionDTO.getId());
        sesion.setId(sesionDTO.getId());
        //TODO -> Esta sesion.getVersionLingustica sera erronea cuando sea multisesion, mejor hacer un insert en la tabla hija
        // por cada pelicula de la multisesion (evento.getFormato, evento.getId, evento.getPeliculasMultisesion()
        // .getversionLingusitca())
        ////addSesionFormatoIdiomaIfNeeded(sesion.getEvento().getId(), sesion.getEvento().getFormato(),	sesion.getVersionLinguistica());
        return sesion;
    }

    @Transactional
    private void anulaSesionesConLaMismaHoraYSala(Timestamp fechaCelebracion, long salaId, long sesionId) {
        List<SesionDTO> sesionesMismaHoraYSala = getSesionesMismaFechaYLocalizacion(fechaCelebracion, salaId);
        for (SesionDTO sesionMismaHoraYSala : sesionesMismaHoraYSala) {
            if (sonDistintaSesion(sesionMismaHoraYSala.getId(), sesionId) && !isSesionAnulada(sesionMismaHoraYSala)) {
                sesionMismaHoraYSala.setCanalInternet(false);
                sesionMismaHoraYSala.setAnulada(true);
                entityManager.merge(sesionMismaHoraYSala);
            }
        }
    }

    private boolean isSesionAnulada(SesionDTO sesionDTO) {
        if (sesionDTO.getCanalInternet() != null && !sesionDTO.getCanalInternet() &&
                sesionDTO.getAnulada() != null && sesionDTO.getAnulada())
            return true;
        return false;
    }

    private boolean sonDistintaSesion(long sesionId1, long sesionId2) {
        return (sesionId1 != sesionId2);
    }
    
    /*@Transactional
    //TODO ajustar a que le puedan entrar n versiones linguisticas
    private void addSesionFormatoIdiomaIfNeeded(long eventoId, String formato, String versionLinguistica) {
		EventoDTO eventoDTO = eventosDAO.getEventoById(eventoId);
        if (eventoDTO.getParTiposEvento().getExportarICAA()) {
        	if (formato != null && versionLinguistica != null) {
		        List<SesionFormatoIdiomaICAADTO> sesionesFormatIdiomaICAA = 
		        		getSesionFormatoIdiomaIcaa(formato, versionLinguistica, eventoId);
		        
		        if (sesionesFormatIdiomaICAA.size() == 0)
		        	addSesionFormatoIdiomaICAA(formato, versionLinguistica, eventoId);
        	}
        }
	}*/

    //TODO - aparentemente esta mal, ya que deberia ser con sesion y no con evento
    @Transactional
    private void addSesionFormatoIdiomaICAA(String formato, String versionLinguistica, long eventoId) {
        SesionFormatoIdiomaICAADTO sesionFormatoIdiomaICAA = new SesionFormatoIdiomaICAADTO();
        sesionFormatoIdiomaICAA.setFormato(formato);
        sesionFormatoIdiomaICAA.setVersionLinguistica(versionLinguistica);
        sesionFormatoIdiomaICAA.setParEvento(new EventoDTO(eventoId));
        entityManager.persist(sesionFormatoIdiomaICAA);
    }

    @Transactional(rollbackForClassName = {"EdicionSesionAnuladaException"})
    public void updateSesion(Sesion sesion, boolean hasCompras) {
        SesionDTO sesionDTO = getSesion(sesion.getId());

        if (isSesionAnulada(sesionDTO))
            throw new EdicionSesionAnuladaException();

        Timestamp fechaCelebracion = DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion.getFechaCelebracion
                (), sesion.getHoraCelebracion()));

        if (!sesionDTO.getFechaCelebracion().equals(fechaCelebracion) && sesionDTO.getFechaCelebracion().before(new Date()) && hasCompras)
            throw new EventoConCompras(sesion.getEvento().getId());

        if (sesion.getFechaInicioVentaOnline() != null)
            sesionDTO.setFechaInicioVentaOnline(DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion
                    .getFechaInicioVentaOnline(), sesion.getHoraInicioVentaOnline())));
        else
            sesionDTO.setFechaInicioVentaOnline(null);

        if (sesion.getFechaFinVentaOnline() != null)
            sesionDTO.setFechaFinVentaOnline(DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion
                    .getFechaFinVentaOnline(), sesion.getHoraFinVentaOnline())));
        else
            sesionDTO.setFechaFinVentaOnline(null);

        sesionDTO.setFechaCelebracion(fechaCelebracion);
        sesionDTO.setCanalInternet(sesion.getCanalInternet());
        sesionDTO.setCanalTaquilla(sesion.getCanalTaquilla());
        sesionDTO.setHoraApertura(sesion.getHoraApertura());
        sesionDTO.setParEvento(Evento.eventoToEventoDTO(sesion.getEvento()));
        sesionDTO.setParPlantilla( Plantilla.plantillaPreciosToPlantillaPreciosDTO(sesion.getPlantillaPrecios()));
        sesionDTO.setVersionLinguistica(sesion.getVersionLinguistica());
        sesionDTO.setRssId(sesion.getRssId());
        sesionDTO.setParSala(Sala.salaToSalaDTO(sesion.getSala()));
        entityManager.merge(sesionDTO);

        //TODO -> Esta sesion.getVersionLingustica sera erronea cuando sea multisesion, mejor hacer un insert en la tabla hija
        // por cada pelicula de la multisesion (evento.getFormato, evento.getId, evento.getPeliculasMultisesion()
        // .getversionLingusitca())
        ////addSesionFormatoIdiomaIfNeeded(sesion.getEvento().getId(), sesion.getEvento().getFormato(),	sesion.getVersionLinguistica());
        setIncidenciaSesion(fechaCelebracion, sesion.getSala().getId());
        anulaSesionesConLaMismaHoraYSala(fechaCelebracion, sesion.getSala().getId(), sesion.getId());
    }

    @Transactional
    private void setIncidenciaSesion(Timestamp fechaCelebracion, Long salaId) {
        List<SesionDTO> sesionesMismaHoraYSala = getSesionesMismaFechaYLocalizacion(fechaCelebracion, salaId);

        for (SesionDTO sesionMismaHoraYSalaIncluidaLaPropia : sesionesMismaHoraYSala) {
            long totalAnuladas = getButacasAnuladasTotal(sesionMismaHoraYSalaIncluidaLaPropia.getId());
            boolean hasVentasDegradadas = hasVentasDegradadas(sesionMismaHoraYSalaIncluidaLaPropia.getId(),
                    fechaCelebracion);
            boolean isSesionReprogramada = isSesionReprogramada(fechaCelebracion, sesionMismaHoraYSalaIncluidaLaPropia.getParSala().getId(),
                    sesionMismaHoraYSalaIncluidaLaPropia.getId());
            int tipoIncidenciaId = getTipoIncidenciaSesion(totalAnuladas, hasVentasDegradadas, isSesionReprogramada);
            setIncidencia(sesionMismaHoraYSalaIncluidaLaPropia.getId(), tipoIncidenciaId);
        }
    }

    private boolean hasVentasDegradadas(long sesionId, Timestamp fechaCelebracion) {
        Timestamp fechaTopeParaSaberSiEsDegradada = configuration.getDataTopePerASaberSiEsDegradada(fechaCelebracion);
        JPAQuery query = new JPAQuery(entityManager);
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

        query.from(qCompraDTO).where(qCompraDTO.parSesion.id.eq(sesionId).and(qCompraDTO.fecha.gt
                (fechaTopeParaSaberSiEsDegradada)));
        return (query.count() > 0L);
    }

    @Transactional
    public void addPrecioSesion(PreciosSesionDTO precioSesionDTO) {
        entityManager.persist(precioSesionDTO);
    }

    @Transactional
    public void deleteExistingPreciosSesion(long sesionId) {
        JPADeleteClause delete = new JPADeleteClause(entityManager, qPreciosSesionDTO);
        delete.where(qPreciosSesionDTO.parSesione.id.eq(sesionId)).execute();
    }

    @Transactional
    public List<PreciosSesionDTO> getPreciosSesion(long sesionId, String sortParameter, int start, int limit) {
        return getQueryPreciosSesion(sesionId).orderBy(getSort(qPreciosSesionDTO, sortParameter)).offset(start)
                .limit(limit).list(qPreciosSesionDTO);
    }

    @Transactional
    private JPAQuery getQueryPreciosSesion(long sesionId) {
        QTarifaDTO qTarifa = QTarifaDTO.tarifaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qPreciosSesionDTO, qTarifa).where(qPreciosSesionDTO.parSesione.id.eq(sesionId).
                and(qTarifa.id.eq(qPreciosSesionDTO.parTarifa.id)));
    }

    @Transactional
    public PreciosSesionDTO persistPreciosSesion(PreciosSesionDTO precioSesionDTO) {
        entityManager.persist(precioSesionDTO);
        return precioSesionDTO;
    }

    @Transactional
    public SesionDTO getSesion(long sesionId) {
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qSesionDTO).
                leftJoin(qSesionDTO.parPreciosSesions, qPreciosSesionDTO).fetch()
                .where(qSesionDTO.id.eq(sesionId)).uniqueResult(qSesionDTO);
    }

    @Transactional
    public int getTotalSesionesActivas(Long eventoId) {
        return (int) getQuerySesionesActivas(eventoId).count();
    }

    @Transactional
    public int getTotalSesiones(Long eventoId) {
        return (int) getQuerySesiones(eventoId).count();
    }

    @Transactional
    public int getTotalPreciosSesion(Long sesionId) {
        return (int) getQueryPreciosSesion(sesionId).count();
    }

    @Transactional(rollbackForClassName = {"IncidenciaNotFoundException"})
    public int getNumeroSesionesValidasParaFicheroICAA(List<Sesion> sesionesAValidar) throws IncidenciaNotFoundException {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;

        List<Long> idsSesionesAValidar = Sesion.getIdsSesiones(sesionesAValidar);
        JPAQuery query = new JPAQuery(entityManager);

        long numeroSesiones = query
                .from(qSesionDTO)
                .where(qSesionDTO.id.in(idsSesionesAValidar).and(qSesionDTO.anulada.isFalse()))
                .distinct()
                .count();

        return (int)numeroSesiones;
    }

    @Transactional(rollbackForClassName = {"IncidenciaNotFoundException"})
    public List<RegistroSesion> getRegistrosSesiones(List<Sesion> sesiones) throws IncidenciaNotFoundException {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        QEventoMultisesionDTO eventoMultisesionDTO = new QEventoMultisesionDTO("eventoMultisesionDTO");

        List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);
        JPAQuery query = new JPAQuery(entityManager);

        List<Tuple> resultado = query
                .from(qSesionDTO)
                .join(qSesionDTO.parSala, qSalaDTO)
                .leftJoin(qSesionDTO.parCompras, qCompraDTO).on(qCompraDTO.anulada.isNull().or(qCompraDTO.anulada.eq(false)))
                .leftJoin(qCompraDTO.parButacas, qButacaDTO).on(qButacaDTO.anulada.isNull().or(qButacaDTO.anulada.eq(false)))
                .where(qSesionDTO.id.in(idsSesiones))
                .distinct()
                .groupBy(qSesionDTO.id, qSalaDTO.codigo, qSesionDTO.fechaCelebracion, qSesionDTO.incidenciaId)
                .list(qSesionDTO.id, qSalaDTO.codigo, qSesionDTO.fechaCelebracion, qButacaDTO.precio.sum(), qSesionDTO.incidenciaId,
                        new JPASubQuery().from(eventoMultisesionDTO).where(eventoMultisesionDTO.parEvento.id.eq(qSesionDTO.parEvento
                                .id)).count());

        List<RegistroSesion> registros = new ArrayList<RegistroSesion>();

        for (Tuple row : resultado) {
            int idIncidencia = Utils.safeObjectToInt(row.get(4, BigDecimal.class));

            long idSesion = row.get(0, Long.class);
            String codigoSala = (String) row.get(1, String.class);
            Date fechaCelebracion = row.get(2, Date.class);
            BigDecimal recaudacion = row.get(3, BigDecimal.class);
            Long peliculasMultisesion = row.get(5, Long.class);
            Long espectadores = getEspectadores(idSesion);

            RegistroSesion registro = new RegistroSesion();

            registro.setCodigoSala(codigoSala);
            registro.setEspectadores(espectadores.intValue());
            registro.setPeliculas((peliculasMultisesion == 0L) ? 1 : peliculasMultisesion.intValue());
            registro.setFecha(fechaCelebracion);
            registro.setHora(HOUR_FORMAT.format(fechaCelebracion));
            registro.setIncidencia(TipoIncidencia.intToTipoIncidencia(idIncidencia));

            if (recaudacion == null)
                registro.setRecaudacion(BigDecimal.ZERO);
            else
                registro.setRecaudacion(recaudacion);

            registros.add(registro);
        }

        return registros;
    }

    private Long getEspectadores(long sesionId) {
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qButacaDTO).
                where(qButacaDTO.parSesion.id.eq(sesionId).and(
                                qButacaDTO.anulada.eq(false).or(qButacaDTO.anulada.isNull()
                                ))
                ).count();
    }

    @Transactional(rollbackForClassName = {"SesionSinFormatoIdiomaIcaaException", "IncidenciaNotFoundException"})
    public List<RegistroSesionPelicula> getRegistrosSesionesPeliculas(List<Sesion> sesiones) throws SesionSinFormatoIdiomaIcaaException, IncidenciaNotFoundException {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;
        QEventoMultisesionDTO qEventoMultisesionDTO = new QEventoMultisesionDTO("qEventoMultisesionDTO");

        List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);

        JPAQuery query = new JPAQuery(entityManager);

        List<SesionDTO> resultado = query
                .from(qSesionDTO)
                .join(qSesionDTO.parEvento, qEventoDTO)
                .join(qSesionDTO.parSala).fetch()
                .where(qSesionDTO.id.in(idsSesiones))
                .distinct()
                .list(qSesionDTO);

        List<RegistroSesionPelicula> registros = new ArrayList<RegistroSesionPelicula>();

        for (SesionDTO sesionDTO : resultado) {
            if (sesionDTO.getParEvento().getFormato() == null)
                throw new SesionSinFormatoIdiomaIcaaException(sesionDTO.getParEvento().getTituloVa());

            List<Tuple> peliculasMultisesion = eventosDAO.getPeliculasMultisesion(sesionDTO.getParEvento().getId());

            if (peliculasMultisesion.size() > 0) {
                for (Tuple peliculaMultisesion : peliculasMultisesion) {
                    EventoDTO eventoDTO = peliculaMultisesion.get(0, EventoDTO.class);
                    String versionLinguistica = peliculaMultisesion.get(1, String.class);

                    try {
                        Sesion.checkSesionValoresIcaa(eventoDTO.getFormato(), eventoDTO.getId(), versionLinguistica);
                    } catch (Exception e) {
                        throw new SesionSinFormatoIdiomaIcaaException(sesionDTO.getParEvento().getId(),
                                sesionDTO.getParEvento().getFormato(), versionLinguistica);
                    }

                    RegistroSesionPelicula registro = new RegistroSesionPelicula();
                    registro.setCodigoSala(sesionDTO.getParSala().getCodigo());
                    registro.setCodigoPelicula((int) eventoDTO.getId());
                    registro.setFecha(sesionDTO.getFechaCelebracion());
                    registro.setHora(HOUR_FORMAT.format(sesionDTO.getFechaCelebracion()));
                    registros.add(registro);
                }
            } else {
                try {
                    Sesion.checkSesionValoresIcaa(sesionDTO.getParEvento().getFormato(), sesionDTO.getParEvento().getId(), sesionDTO.getVersionLinguistica());
                } catch (Exception e) {
                    throw new SesionSinFormatoIdiomaIcaaException(sesionDTO.getParEvento().getId(),
                            sesionDTO.getParEvento().getFormato(), sesionDTO.getVersionLinguistica());
                }

                RegistroSesionPelicula registro = new RegistroSesionPelicula();
                registro.setCodigoSala(sesionDTO.getParSala().getCodigo());
                registro.setCodigoPelicula((int) sesionDTO.getParEvento().getId());
                registro.setFecha(sesionDTO.getFechaCelebracion());
                registro.setHora(HOUR_FORMAT.format(sesionDTO.getFechaCelebracion()));
                registros.add(registro);
            }
        }

        return registros;
    }

    @Transactional(rollbackForClassName = {"IncidenciaNotFoundException"})
    public List<RegistroPelicula> getRegistrosPeliculas(List<Sesion> sesiones) throws IncidenciaNotFoundException {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;

        List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);

        JPAQuery query = new JPAQuery(entityManager);

        List<Tuple> resultado = query
                .from(qSesionDTO)
                .join(qSesionDTO.parEvento, qEventoDTO)
                .join(qSesionDTO.parSala).fetch()
                .where(qSesionDTO.id.in(idsSesiones))
                .distinct()
                .list(qSesionDTO, qEventoDTO.id);

        List<RegistroPelicula> registros = new ArrayList<RegistroPelicula>();

        for (Tuple row : resultado) {
            SesionDTO sesion = row.get(0, SesionDTO.class);
            Long idEvento = row.get(1, Long.class);

            List<Tuple> peliculasMultisesion = eventosDAO.getPeliculasMultisesion(idEvento);
            if (peliculasMultisesion.size() > 0) {
                for (Tuple peliculaMultisesion : peliculasMultisesion) {
                    EventoDTO eventoDTO = peliculaMultisesion.get(0, EventoDTO.class);
                    String versionLinguistica = peliculaMultisesion.get(1, String.class);

                    RegistroPelicula registro = new RegistroPelicula();
                    registro.setCodigoSala(sesion.getParSala().getCodigo());
                    registro.setCodigoPelicula((int) eventoDTO.getId());
                    registro.setCodigoExpediente(eventoDTO.getExpediente());
                    registro.setTitulo(Utils.stripAccents(eventoDTO.getTituloEs()));
                    registro.setCodigoDistribuidora(eventoDTO.getCodigoDistribuidora());
                    registro.setNombreDistribuidora(Utils.stripAccents(eventoDTO.getNombreDistribuidora()));
                    registro.setVersionOriginal(eventoDTO.getVo());
                    registro.setVersionLinguistica(versionLinguistica);
                    registro.setIdiomaSubtitulos(eventoDTO.getSubtitulos());
                    registro.setFormatoProyeccion(eventoDTO.getFormato());
                    registros.add(registro);
                }
            } else {
                RegistroPelicula registro = new RegistroPelicula();
                registro.setCodigoSala(sesion.getParSala().getCodigo());
                registro.setCodigoPelicula(idEvento.intValue());
                registro.setCodigoExpediente(sesion.getParEvento().getExpediente());
                registro.setTitulo(Utils.stripAccents(sesion.getParEvento().getTituloEs()));
                registro.setCodigoDistribuidora(sesion.getParEvento().getCodigoDistribuidora());
                registro.setNombreDistribuidora(Utils.stripAccents(sesion.getParEvento().getNombreDistribuidora()));
                registro.setVersionOriginal(sesion.getParEvento().getVo());
                registro.setVersionLinguistica(sesion.getVersionLinguistica());
                registro.setIdiomaSubtitulos(sesion.getParEvento().getSubtitulos());
                registro.setFormatoProyeccion(sesion.getParEvento().getFormato());
                registros.add(registro);
            }
        }

        return registros;
    }

    @Transactional
    public List<SesionDTO> getSesionesOrdenadas(List<Sesion> sesiones) {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;

        List<Long> idsSesiones = Sesion.getIdsSesiones(sesiones);

        JPAQuery query = new JPAQuery(entityManager);

        List<SesionDTO> resultado = query
                .from(qSesionDTO)
                .join(qSesionDTO.parSala, qSalaDTO).fetch()
                .where(qSesionDTO.id.in(idsSesiones))
                .orderBy(qSalaDTO.id.asc(), qSesionDTO.fechaCelebracion.asc())
                .list(qSesionDTO);

        return resultado;
    }

    @Transactional
    public Sesion getSesionByRssId(String rssId) {
        JPAQuery query = new JPAQuery(entityManager);
        SesionDTO uniqueResult = query.from(QSesionDTO.sesionDTO)
                .where(QSesionDTO.sesionDTO.rssId.eq(rssId)).uniqueResult(QSesionDTO.sesionDTO);

        if (uniqueResult == null)
            return null;
        else
            return Sesion.SesionDTOToSesion(uniqueResult);
    }

    @Transactional
    public List<SesionDTO> getSesionesCinePorFechas(Date dtInicio, Date dtFin, String sort, String userUID) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;
		QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;

        JPAQuery query = new JPAQuery(entityManager);
        query.from(qSesionDTO).join(qSesionDTO.parEvento, qEventoDTO).join(qSesionDTO.parSala, qSalaDTO).fetch().join(qSalaDTO
				.parSalasUsuario, qSalasUsuarioDTO).join(qSalasUsuarioDTO.parUsuario, qUsuarioDTO);

        if (dtInicio != null || dtFin != null) {
            BooleanBuilder condicion = new BooleanBuilder();

            if (dtInicio != null)
                condicion = condicion.and(qSesionDTO.fechaCelebracion.goe(new Timestamp(dtInicio.getTime())));

            if (dtFin != null)
                condicion = condicion.and(qSesionDTO.fechaCelebracion.loe(new Timestamp(dtFin.getTime())));

            query.where(condicion.and(qUsuarioDTO.usuario.eq(userUID)));
        } else
			query.where(qUsuarioDTO.usuario.eq(userUID));

        return query.orderBy(getSort(qSesionDTO, sort)).distinct().list(qSesionDTO);
    }

    @Transactional
    public List<SesionDTO> getSesiones(List<Long> ids) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;

        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qSesionDTO).join(qSesionDTO.parEvento, qEventoDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch()
                .where(qSesionDTO.id.in(ids)).list(qSesionDTO);
    }

    @Transactional
    public List<SesionDTO> getSesionesICAAPorFechas(Date dtInicio, Date dtFin, String sort) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;
        QEnviosSesionDTO qEnviosSesion = QEnviosSesionDTO.enviosSesionDTO;
        QEnvioDTO qEnvioDTO = QEnvioDTO.envioDTO;

        JPAQuery query = new JPAQuery(entityManager);
        query.from(qSesionDTO).join(qSesionDTO.parEvento, qEventoDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch().
                leftJoin(qSesionDTO.parEnviosSesion, qEnviosSesion).fetch().leftJoin(qEnviosSesion.parEnvio, qEnvioDTO).fetch();
        BooleanExpression condicion = qEventoDTO.parTiposEvento.exportarICAA.eq(true);

        if (dtInicio != null)
            condicion = condicion.and(qSesionDTO.fechaCelebracion.goe(new Timestamp(dtInicio.getTime())));

        if (dtFin != null)
            condicion = condicion.and(qSesionDTO.fechaCelebracion.loe(new Timestamp(dtFin.getTime())));

        query.where(condicion.and(qSesionDTO.anulada.isFalse()));

        return query.orderBy(getSort(qSesionDTO, sort)).distinct().list(qSesionDTO);
    }

    @Transactional
    public List<SesionDTO> getSesionesPorFechas(Date dtInicio, Date dtFin, String sort) {
        QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
        QEventoDTO qEventoDTO = QEventoDTO.eventoDTO;

        JPAQuery query = new JPAQuery(entityManager);
        query.from(qSesionDTO).join(qSesionDTO.parEvento, qEventoDTO).leftJoin(qSesionDTO.parSala, qSalaDTO).fetch();
        BooleanBuilder condicion = new BooleanBuilder();

        if (dtInicio != null)
            condicion = condicion.and(qSesionDTO.fechaCelebracion.goe(new Timestamp(dtInicio.getTime())));

        if (dtFin != null)
            condicion = condicion.and(qSesionDTO.fechaCelebracion.loe(new Timestamp(dtFin.getTime())));

        query.where(condicion);

        return query.orderBy(getSort(qSesionDTO, sort)).list(qSesionDTO);
    }

    @Transactional
    public List<TarifaDTO> getTarifasPreciosSesion(long sesionId) {
        QTarifaDTO qTarifa = QTarifaDTO.tarifaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qPreciosSesionDTO, qTarifa).where(qPreciosSesionDTO.parSesione.id.eq(sesionId).
                and(qTarifa.id.eq(qPreciosSesionDTO.parTarifa.id))).distinct().list(qTarifa);
    }

    @Transactional
    public List<TarifaDTO> getTarifasPreciosPlantilla(long sesionId) {
        QTarifaDTO qTarifa = QTarifaDTO.tarifaDTO;
        QPreciosPlantillaDTO qPreciosPlantilla = QPreciosPlantillaDTO.preciosPlantillaDTO;

        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qPreciosPlantilla, qTarifa).where(qPreciosPlantilla.parPlantilla.id.eq(
                new JPASubQuery().from(qSesionDTO).where(qSesionDTO.id.eq(sesionId)).unique(qSesionDTO.parPlantilla.id)).
                and(qTarifa.id.eq(qPreciosPlantilla.parTarifa.id))).distinct().list(qTarifa);
    }

    @Transactional
    public void setIncidencia(long sesionId, int incidenciaId) {
        JPAUpdateClause jpaUpdate = new JPAUpdateClause(entityManager, qSesionDTO);
        jpaUpdate.set(qSesionDTO.incidenciaId, incidenciaId).where(qSesionDTO.id.eq(sesionId)).execute();
    }

    @Transactional
    public void removeIncidencia(long sesionId) {
        JPAUpdateClause jpaUpdate = new JPAUpdateClause(entityManager, qSesionDTO);
        jpaUpdate.set(qSesionDTO.incidenciaId, 0).where(qSesionDTO.id.eq(sesionId)).execute();
    }

    @Transactional
    public long getButacasAnuladasTotal(long idSesion) {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qSesionDTO).join(qSesionDTO.parCompras, qCompraDTO).join(qCompraDTO.parButacas,
                qButacaDTO).on(qButacaDTO.anulada.eq(true)).where(qSesionDTO.id.eq(idSesion)).count();
    }

    @Transactional(rollbackFor = IncidenciaNotFoundException.class)
    public int getTipoIncidenciaSesion(long totalAnuladas, boolean isVentaDegradada,
                                       boolean isSesionReprogramada) throws IncidenciaNotFoundException {
        int tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.SIN_INCIDENCIAS);

        if (totalAnuladas > 0) {
            if (!isVentaDegradada && !isSesionReprogramada)
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.ANULACIO_VENDES);
            else if (isVentaDegradada && !isSesionReprogramada)
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.VENDA_MANUAL_I_ANULACIO_VENTES);
            else if (isVentaDegradada && isSesionReprogramada)
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.VENDA_MANUAL_I_ANULACIO_VENTES_I_REPROGRAMACIO);
            else if (!isVentaDegradada && isSesionReprogramada)
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.ANULACIO_VENTES_I_REPROGRAMACIO);
        } else if (isVentaDegradada) {
            if (isSesionReprogramada)
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.VENDA_MANUAL_I_REPROGRAMACIO);
            else
                tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.VENDA_MANUAL_DEGRADADA);
        } else if (isSesionReprogramada)
            tipoIncidenciaId = TipoIncidencia.tipoIncidenciaToInt(TipoIncidencia.REPROGRAMACIO);

        return tipoIncidenciaId;
    }

    @Transactional
    public Pair getCantidadSesionesMismaFechaYLocalizacion(Timestamp fechaCelebracion, long salaId,
                                                           Long sesionId) {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        JPAQuery query = new JPAQuery(entityManager);
        JPAQuery queryCompras = new JPAQuery(entityManager);

        Pair result;
        if (sesionId != null) {
            result = new Pair(query.from(qSesionDTO).where(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                    .id.eq(salaId)).and(qSesionDTO.id.ne(sesionId).and(qSesionDTO.anulada.ne(true)))).count(),
                    queryCompras.from(qSesionDTO).join(qSesionDTO.parCompras, qCompraDTO).join(qCompraDTO.parButacas,
                            qButacaDTO).on(qButacaDTO.anulada.ne(true)).where(qSesionDTO.id.eq(sesionId).and(qSesionDTO.anulada.ne(true).and(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                            .id.eq(salaId)).and(qSesionDTO.id.ne(sesionId))))).count());
        } else {
            result = new Pair(query.from(qSesionDTO).where(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                    .id.eq(salaId).and(qSesionDTO.anulada.ne(true)))).count(),
                    queryCompras.from(qSesionDTO).join(qSesionDTO.parCompras, qCompraDTO).join(qCompraDTO.parButacas,
                            qButacaDTO).on(qButacaDTO.anulada.ne(true)).where(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                            .id.eq(salaId).and(qSesionDTO.anulada.ne(true)))).count());
        }

        return result;
    }

    @Transactional
    public boolean isSesionReprogramada(Timestamp fechaCelebracion, long salaId,
                                                           Long sesionId) {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qSesionDTO).where(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                .id.eq(salaId)).and(qSesionDTO.id.ne(sesionId))).count() > 0;
    }

    @Transactional
    public List<SesionDTO> getSesionesMismaFechaYLocalizacion(Timestamp fechaCelebracion, long salaId) {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qSesionDTO).where(qSesionDTO.fechaCelebracion.eq(fechaCelebracion).and(qSesionDTO.parSala
                .id.eq(salaId))).list(qSesionDTO);
    }

    @Transactional
    public void removeAnulacionVentasFromSesion(long sesionId) {
        SesionDTO sesionDTO = getSesion(sesionId);
        sesionDTO.setIncidenciaId(TipoIncidencia.removeAnulacionVentasFromIncidenciaActual(sesionDTO.getIncidenciaId()));
        entityManager.merge(sesionDTO);
    }
}
