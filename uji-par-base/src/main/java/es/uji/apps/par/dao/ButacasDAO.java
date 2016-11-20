package es.uji.apps.par.dao;

import java.util.List;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.exceptions.SesionSinFormatoIdiomaIcaaException;
import es.uji.apps.par.ficheros.registros.TipoIncidencia;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;

import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.exceptions.NoHayButacasLibresException;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.LocalizacionDTO;
import es.uji.apps.par.db.PreciosSesionDTO;
import es.uji.apps.par.db.QButacaDTO;
import es.uji.apps.par.db.QCompraDTO;
import es.uji.apps.par.db.QLocalizacionDTO;
import es.uji.apps.par.db.QSesionDTO;
import es.uji.apps.par.db.QTarifaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Butaca;

@Repository
public class ButacasDAO extends BaseDAO
{
    @Autowired
    private LocalizacionesDAO localizacionesDAO;
    
    @Autowired
    private SesionesDAO sesionesDAO;

	@Autowired
	private ComprasDAO comprasDAO;

	@Autowired
	Configuration configuration;

    private QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

    @Transactional
    public ButacaDTO getButaca(long id)
    {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;

        JPAQuery query = new JPAQuery(entityManager);

        return query
                .from(qButacaDTO)
                .leftJoin(qButacaDTO.parSesion, qSesionDTO).fetch()
                .where(qButacaDTO.id.eq(id))
                .uniqueResult(qButacaDTO);
    }

    @Transactional
    public List<ButacaDTO> getButacasOcupadasNoAnuladasPorLocalizacion(long idSesion, String codigoLocalizacion)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

        JPAQuery query = new JPAQuery(entityManager);

        List<ButacaDTO> list = query
                .from(qButacaDTO)
                .join(qButacaDTO.parSesion, qSesionDTO)
                .join(qButacaDTO.parLocalizacion, qLocalizacionDTO)
                .leftJoin(qButacaDTO.parCompra, qCompraDTO).fetch()
                .where(qSesionDTO.id.eq(idSesion)
                        .and(qLocalizacionDTO.codigo.eq(codigoLocalizacion)).and(qButacaDTO.anulada.eq(false)))
                .list(qButacaDTO);

        return list;
    }

    @Transactional
    public List<ButacaDTO> getButacas(long idSesion, String codigoLocalizacion)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        
        JPAQuery query = new JPAQuery(entityManager);

        List<ButacaDTO> list = query
                .from(qButacaDTO)
                .join(qButacaDTO.parSesion, qSesionDTO)
                .join(qButacaDTO.parLocalizacion, qLocalizacionDTO)
                .leftJoin(qButacaDTO.parCompra, qCompraDTO).fetch()
                .where(qSesionDTO.id.eq(idSesion)
                        .and(qLocalizacionDTO.codigo.eq(codigoLocalizacion)))
                        .list(qButacaDTO);

        return list;
    }
    
    @Transactional
    public List<Tuple> getButacas(long idSesion)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        QTarifaDTO qTarifaDTO = QTarifaDTO.tarifaDTO;
        
        JPAQuery query = new JPAQuery(entityManager);

        List<Tuple> list = query
                .from(qButacaDTO, qTarifaDTO)
                .join(qButacaDTO.parSesion, qSesionDTO)
                .join(qButacaDTO.parLocalizacion, qLocalizacionDTO).fetch()
                .leftJoin(qButacaDTO.parCompra, qCompraDTO).fetch()
                .where(qSesionDTO.id.eq(idSesion).and(qButacaDTO.tipo.castToNum(Long.class).eq(qTarifaDTO.id)).and(qCompraDTO.reserva.eq(false)).and(qCompraDTO.parAbonado.isNull()))
            .list(qButacaDTO, qTarifaDTO.nombre, qTarifaDTO.id);

        return list;
    }

    @Transactional
    public void addButaca(ButacaDTO butacaDTO)
    {
        entityManager.persist(butacaDTO);
    }

    @Transactional
    public boolean estaOcupada(long idSesion, String codigoLocalizacion, String fila, String numero)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;

        JPAQuery query = new JPAQuery(entityManager);

        List<ButacaDTO> list = query
                .from(qButacaDTO, qSesionDTO, qLocalizacionDTO)
                .where(qButacaDTO.parSesion.id.eq(qSesionDTO.id).and(qSesionDTO.id.eq(idSesion))
                        .and(qLocalizacionDTO.codigo.eq(codigoLocalizacion))
                        .and(qButacaDTO.parLocalizacion.id.eq(qLocalizacionDTO.id))
                        .and(qButacaDTO.anulada.eq(false))
                        .and(qButacaDTO.fila.eq(fila))
                        .and(qButacaDTO.numero.eq(numero))).list(qButacaDTO);

        return list.size() > 0;
    }

    @Transactional
    public CompraDTO getCompra(long idSesion, String codigoLocalizacion, String fila, String numero)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

        JPAQuery query = new JPAQuery(entityManager);

        List<CompraDTO> list = query
                .from(qButacaDTO)
                .join(qButacaDTO.parSesion, qSesionDTO)
                .join(qButacaDTO.parLocalizacion, qLocalizacionDTO)
                .join(qButacaDTO.parCompra, qCompraDTO)
                .where(qSesionDTO.id.eq(idSesion)
                        .and(qLocalizacionDTO.codigo.eq(codigoLocalizacion))
                        .and(qButacaDTO.fila.eq(fila))
                        .and(qButacaDTO.numero.eq(numero))
                        .and(qButacaDTO.anulada.isNull().or(qButacaDTO.anulada.eq(false))))
                        .list(qCompraDTO);

        if (list.size() == 0)
            return null;
        else
            return list.get(0);
    }
    
    @Transactional
    private void deleteButacas(CompraDTO compraDTO)
    {
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        
        new JPADeleteClause(entityManager, qButacaDTO).where(qButacaDTO.parCompra.id.eq(compraDTO.getId())).execute();  
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException"})
    public void reservaButacas(Long sesionId, CompraDTO compraDTO, List<Butaca> butacas) throws
            ButacaOcupadaException, NoHayButacasLibresException {
        Butaca butacaActual = null;
        
        try {
            deleteButacas(compraDTO);
            
            SesionDTO sesionDTO = sesionesDAO.getSesion(sesionId);
            List<PreciosSesionDTO> parPreciosSesions = sesionDTO.getParPreciosSesions();
            
            for (Butaca butaca : butacas) {
                butacaActual = butaca;
                LocalizacionDTO localizacionDTO = localizacionesDAO.getLocalizacionByCodigo(butaca.getLocalizacion());
                
                if (butaca.getFila()==null && butaca.getNumero()==null && noHayButacasLibres(sesionDTO, localizacionDTO))
                    throw new NoHayButacasLibresException(sesionId, butaca.getLocalizacion());

                ButacaDTO butacaDTO = Butaca.butacaToButacaDTO(butaca);
                butacaDTO.setParSesion(sesionDTO);
                butacaDTO.setParCompra(compraDTO);
                butacaDTO.setParLocalizacion(localizacionDTO);
                butacaDTO.setTipo(butaca.getTipo());
                butacaDTO.setAnulada(false);
    
                for (PreciosSesionDTO precioSesion : parPreciosSesions)
                {
                    if (precioSesion.getParLocalizacione().getCodigo().equals(butaca.getLocalizacion()) &&
                    		precioSesion.getParTarifa().getId() == Long.valueOf(butaca.getTipo()))
                        butacaDTO.setPrecio(precioSesion.getPrecio());
                }
                
                entityManager.persist(butacaDTO);
                entityManager.flush();
            }
        }
        catch (Exception e)
        {
            if (butacaActual != null && e.getCause() instanceof ConstraintViolationException)
                throw new ButacaOcupadaException(sesionId, butacaActual.getLocalizacion(), butacaActual.getFila(), butacaActual.getNumero());
            else
                throw e;
        }
    }

    @Transactional
    private boolean noHayButacasLibres(SesionDTO sesionDTO, LocalizacionDTO localizacionDTO)
    {
        return getOcupadas(sesionDTO.getId(), localizacionDTO.getCodigo()) >= localizacionDTO.getTotalEntradas().intValue();
    }

    @Transactional
    public int getOcupadas(long sesionId, String codigoLocalizacion)
    {
        QLocalizacionDTO qLocalizacionDTO = QLocalizacionDTO.localizacionDTO;
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
        
        JPAQuery query = new JPAQuery(entityManager);
        
        List<ButacaDTO> list = query.from(qButacaDTO)
                .innerJoin(qButacaDTO.parSesion, qSesionDTO)
                .innerJoin(qButacaDTO.parLocalizacion, qLocalizacionDTO)
                .where(qSesionDTO.id.eq(sesionId)
                        .and(qButacaDTO.anulada.eq(false))
                        .and(qLocalizacionDTO.codigo.eq(codigoLocalizacion))).distinct().list(qButacaDTO);
        
        long ocupadas = list.size();
        
        return (int) ocupadas;
    }

    @Transactional
	public List<Tuple> getButacasCompra(Long idCompra, String sortParameter, int start, int limit) {
    	QTarifaDTO qTarifaDTO = QTarifaDTO.tarifaDTO;
		return getQueryButacasCompra(idCompra).orderBy(getSort(qButacaDTO, sortParameter)).offset(start).limit(limit).list(qButacaDTO, qTarifaDTO);
	}
    
    @Transactional
    private JPAQuery getQueryButacasCompra(Long idCompra) {
    	JPAQuery query = new JPAQuery(entityManager);
    	QCompraDTO qCompraDTO= QCompraDTO.compraDTO;
    	QTarifaDTO qTarifaDTO = QTarifaDTO.tarifaDTO;
    	
    	return query.from(qButacaDTO, qTarifaDTO)
    	        .join(qButacaDTO.parCompra, qCompraDTO).fetch()
    	        .where(qCompraDTO.id.eq(idCompra).and(qTarifaDTO.id.eq(qButacaDTO.tipo.castToNum(Long.class))));
    }

    @Transactional
	public int getTotalButacasCompra(Long idCompra) {
		return (int) getQueryButacasCompra(idCompra).count();
	}

    @Transactional
    public List<ButacaDTO> getButacasNoAnuladas(Long idSesion)
    {
        QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
        
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(qButacaDTO).join(qButacaDTO.parCompra, qCompraDTO).fetch()
                .where(qButacaDTO.parSesion.id.eq(idSesion).and(qButacaDTO.anulada.eq(false)
                		.and(qCompraDTO.reserva.eq(false))))
                .distinct().list(qButacaDTO);
    }

    @Transactional
    public void updatePresentadas(List<Butaca> butacas)
    {
        for (Butaca butaca: butacas)
        {
            updatePresentada(butaca);
        }
    }

    @Transactional
    public long updatePresentada(Butaca butaca)
    {
         return new JPAUpdateClause(entityManager, qButacaDTO)
            .where(qButacaDTO.id.eq(butaca.getId()).and(qButacaDTO.presentada.isNull()))
            .set(qButacaDTO.presentada, butaca.getPresentada()).execute();
    }

	@Transactional
	private SesionDTO getSesion(Long idButaca) {
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qButacaDTO).join(qButacaDTO.parCompra, qCompraDTO).join(qCompraDTO.parSesion,
				qSesionDTO).where(qButacaDTO.id.eq(idButaca)).uniqueResult(qSesionDTO);
	}
    
    @Transactional(rollbackFor=SesionSinFormatoIdiomaIcaaException.class)
	public void anularButaca(Long idButaca) throws IncidenciaNotFoundException {
    	QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
		
		JPAUpdateClause updateButacas = new JPAUpdateClause(entityManager, qButacaDTO);
		updateButacas.set(qButacaDTO.anulada, true).
			where(qButacaDTO.id.eq(idButaca)).execute();

		if (!isButacaFromReserva(idButaca)) {
			SesionDTO sesionDTO = getSesion(idButaca);
			sesionesDAO.setIncidencia(sesionDTO.getId(), TipoIncidencia.addAnulacionVentasToIncidenciaActual(sesionDTO.getIncidenciaId()));
		}
	}

    @Transactional(rollbackFor=SesionSinFormatoIdiomaIcaaException.class)
    public void cambiaFilaNumero(Long idButaca, String fila, String numero) throws IncidenciaNotFoundException {
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;

        JPAUpdateClause updateButacas = new JPAUpdateClause(entityManager, qButacaDTO);
        updateButacas.set(qButacaDTO.fila, fila).set(qButacaDTO.numero, numero).
                where(qButacaDTO.id.eq(idButaca)).execute();
    }

	@Transactional
	protected boolean isButacaFromReserva(Long idButaca) {
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		QCompraDTO qCompraDTO = QCompraDTO.compraDTO;

		JPAQuery query = new JPAQuery(entityManager);
		CompraDTO compra = query.from(qButacaDTO).join(qButacaDTO.parCompra, qCompraDTO).where(qButacaDTO.id.eq(idButaca)).uniqueResult(qCompraDTO);
		if (compra.getReserva() == null || compra.getReserva() == false)
			return false;
		else
			return true;
	}

    @Transactional
    public void asignarIdEntrada(Long idCompra) {
        QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		Integer idEntradaConfiguracion = configuration.getIdEntrada();
	    JPAQuery query = new JPAQuery(entityManager);
        Integer maxIdEntrada = query.from(qButacaDTO).uniqueResult(qButacaDTO.idEntrada.max().coalesce(idEntradaConfiguracion));
		maxIdEntrada = (maxIdEntrada < idEntradaConfiguracion)?idEntradaConfiguracion:maxIdEntrada;

		List<Long> idsButacasCompra = getIdsButacasCompra(idCompra);
		if (idsButacasCompra != null) {
			for (Long idButaca: idsButacasCompra) {
				maxIdEntrada++;
				JPAUpdateClause updateButacas = new JPAUpdateClause(entityManager, qButacaDTO);
				updateButacas.set(qButacaDTO.idEntrada, maxIdEntrada).
						where(qButacaDTO.id.eq(idButaca)).execute();
			}
		}
    }

	@Transactional
	private List<Long> getIdsButacasCompra(Long idCompra) {
		QButacaDTO qButacaDTO = QButacaDTO.butacaDTO;
		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qButacaDTO).where(qButacaDTO.parCompra.id.eq(idCompra)).list(qButacaDTO.id);
	}
}
