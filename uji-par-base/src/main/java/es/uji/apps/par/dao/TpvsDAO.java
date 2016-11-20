package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPAQuery;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.db.QTpvsDTO;
import es.uji.apps.par.db.TpvsDTO;
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

    private DatabaseHelper databaseHelper;

	@Autowired
    public TpvsDAO(Configuration configuration) {
		if (this.configuration == null)
			this.configuration = configuration;
        databaseHelper = DatabaseHelperFactory.newInstance(configuration);
    }

    @Transactional
    public void addTpvDefault() {
        TpvsDTO tpvsDTO = new TpvsDTO();
        tpvsDTO.setNombre(configuration.getTpvNombre());
        tpvsDTO.setCode(configuration.getTpvCode());
        tpvsDTO.setCurrency(configuration.getTpvCurrency());
        tpvsDTO.setLangCaCode(configuration.getTpvLangCaCode());
        tpvsDTO.setLangEsCode(configuration.getTpvLangEsCode());
        tpvsDTO.setOrderPrefix(configuration.getTpvOrderPrefixCodeCajamar());
        tpvsDTO.setTerminal(configuration.getTpvTerminal());
        tpvsDTO.setTransactionCode(configuration.getTpvTransaction());
        tpvsDTO.setDefaultTpv(true);

        entityManager.persist(tpvsDTO);
    }

    @Transactional
    public TpvsDTO getTpvDefault() {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qTpvsDTO).where(qTpvsDTO.defaultTpv.isTrue()).uniqueResult(qTpvsDTO);
    }

    @Transactional
    public List<TpvsDTO> getTpvs(String sortParameter, int start, int limit) {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qTpvsDTO).orderBy(getSort(qTpvsDTO, sortParameter)).
                offset(start).limit(limit).list(qTpvsDTO);
    }

	@Transactional
	public void addTpv(Tpv tpv, boolean isDefault) {
		TpvsDTO tpvsDTO = Tpv.tpvToTpvDTO(tpv);
		tpvsDTO.setDefaultTpv(isDefault);
		entityManager.persist(tpvsDTO);
	}
}
