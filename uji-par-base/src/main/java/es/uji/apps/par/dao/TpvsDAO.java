package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPAQuery;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Tpv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class TpvsDAO extends BaseDAO {
	@Autowired
	Configuration configuration;

    private QTpvsDTO qTpvsDTO = QTpvsDTO.tpvsDTO;
	private QTpvsCinesDTO qTpvsCinesDTO = QTpvsCinesDTO.tpvsCinesDTO;
	private QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
	private QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
	private QCineDTO qCineDTO = QCineDTO.cineDTO;
	private QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;

    private DatabaseHelper databaseHelper;

	@Autowired
    public TpvsDAO(Configuration configuration) {
		if (this.configuration == null)
			this.configuration = configuration;
        databaseHelper = DatabaseHelperFactory.newInstance(configuration);
    }

	@Transactional
	public TpvsDTO getTpvDefault(Long cineId) {
		JPAQuery query = new JPAQuery(entityManager);

		return query.from(qTpvsDTO)
				.leftJoin(qTpvsDTO.parTpvsCines, qTpvsCinesDTO)
				.leftJoin(qTpvsCinesDTO.cine, qCineDTO)
				.where(qCineDTO.id.eq(cineId))
				.orderBy(qTpvsDTO.id.asc())
				.limit(1)
				.uniqueResult(qTpvsDTO);
	}

    @Transactional
    public TpvsDTO getTpvDefault(String userUID) {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qTpvsDTO)
				.leftJoin(qTpvsDTO.parTpvsCines, qTpvsCinesDTO)
				.leftJoin(qTpvsCinesDTO.cine, qCineDTO)
				.leftJoin(qCineDTO.parSalas, qSalaDTO)
				.leftJoin(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
				.leftJoin(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
				.where(qUsuarioDTO.usuario.eq(userUID))
				.orderBy(qTpvsDTO.id.asc())
				.limit(1)
				.uniqueResult(qTpvsDTO);
    }

    @Transactional
    public List<TpvsDTO> getTpvs(String userUID, String sortParameter, int start, int limit) {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qTpvsDTO)
				.leftJoin(qTpvsDTO.parTpvsCines, qTpvsCinesDTO)
				.leftJoin(qTpvsCinesDTO.cine, qCineDTO)
				.leftJoin(qCineDTO.parSalas, qSalaDTO)
				.leftJoin(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
				.leftJoin(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
				.where(qUsuarioDTO.usuario.eq(userUID))
				.orderBy(getSort(qTpvsDTO, sortParameter))
				.offset(start)
				.limit(limit)
				.list(qTpvsDTO);
    }

	@Transactional
	public TpvsDTO addTpv(Tpv tpv, Long cineId) {
		TpvsDTO tpvsDTO = Tpv.tpvToTpvDTO(tpv);
		entityManager.persist(tpvsDTO);

		TpvsCinesDTO tpvsCinesDTO = new TpvsCinesDTO();
		tpvsCinesDTO.setTpv(tpvsDTO);
		tpvsCinesDTO.setCine(new CineDTO(cineId));
		entityManager.persist(tpvsCinesDTO);

		return tpvsDTO;
	}
}
