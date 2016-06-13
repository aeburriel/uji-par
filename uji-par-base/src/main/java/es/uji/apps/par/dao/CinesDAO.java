package es.uji.apps.par.dao;

import java.util.List;

import es.uji.apps.par.db.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;

import es.uji.apps.par.model.Cine;

@Repository
public class CinesDAO extends BaseDAO
{
    private QCineDTO qCineDTO = QCineDTO.cineDTO;

    @Transactional
    public List<CineDTO> getCines()
    {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qCineDTO).list(qCineDTO);
    }

	@Transactional
	public List<CineDTO> getCines(String userUID)
	{
		JPAQuery query = new JPAQuery(entityManager);
		QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;

		return query.from(qCineDTO)
				.leftJoin(qCineDTO.parSalas, qSalaDTO)
				.leftJoin(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
				.leftJoin(qSalasUsuarioDTO.parUsuario, qUsuarioDTO).on(qUsuarioDTO.usuario.eq(userUID))
				.list(qCineDTO);
	}

    @Transactional
    public Cine addCine(Cine cine)
    {
        CineDTO cineDTO = Cine.cineToCineDTO(cine);

        entityManager.persist(cineDTO);
        
        cine.setId(cineDTO.getId());
        
        return cine;
    }
}
