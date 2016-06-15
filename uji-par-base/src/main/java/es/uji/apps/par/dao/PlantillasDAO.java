package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Plantilla;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlantillasDAO extends BaseDAO {

	private QPlantillaDTO qPlantillaDTO = QPlantillaDTO.plantillaDTO;
	
	@Transactional
	public List<PlantillaDTO> get(boolean filtrarEditables, String sortParameter, int start, int limit, String userUID) {
        List<PlantillaDTO> plantillaPrecios = new ArrayList<PlantillaDTO>();
        List<PlantillaDTO> listaPlantillaPreciosDTO = new ArrayList<PlantillaDTO>();
        
        if (filtrarEditables)
        	listaPlantillaPreciosDTO = getQueryPlantillasEditables(userUID).orderBy(getSort(qPlantillaDTO, sortParameter)).
        		offset(start).limit(limit).list(qPlantillaDTO);
        else
        	listaPlantillaPreciosDTO = getQueryPlantillas(userUID).orderBy(getSort(qPlantillaDTO, sortParameter)).
        		offset(start).limit(limit).list(qPlantillaDTO);

        for (PlantillaDTO plantillaPreciosDB : listaPlantillaPreciosDTO)
        {
            plantillaPrecios.add(plantillaPreciosDB);
        }

        return plantillaPrecios;
	}

	@Transactional
	private JPAQuery getQueryPlantillasEditables(String userUID) {
		QSalaDTO qSalaDTO = new QSalaDTO("qSalaDTO");
		QSalasUsuarioDTO qSalasUsuarioDTO = new QSalasUsuarioDTO("qSalasUsuarioDTO");

		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qPlantillaDTO)
				.leftJoin(qPlantillaDTO.sala, qSalaDTO).fetch()
				.join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
				.where(qPlantillaDTO.id.ne(Long.valueOf("-1")).and(qSalasUsuarioDTO.parUsuario.usuario.eq(userUID)));
	}
	
	@Transactional
	private JPAQuery getQueryPlantillas(String userUID) {
		QSalaDTO qSalaDTO = new QSalaDTO("qSalaDTO");
		QSalasUsuarioDTO qSalasUsuarioDTO = new QSalasUsuarioDTO("qSalasUsuarioDTO");

		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qPlantillaDTO)
				.leftJoin(qPlantillaDTO.sala, qSalaDTO).fetch()
				.join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
				.where(qSalasUsuarioDTO.parUsuario.usuario.eq(userUID));
	}
	
	@Transactional
	public long remove(long id) {
		JPADeleteClause delete = new JPADeleteClause(entityManager, qPlantillaDTO);
        return delete.where(qPlantillaDTO.id.eq(id)).execute();
	}
		
	@Transactional
	public Plantilla add(Plantilla plantillaPrecios) {
		PlantillaDTO plantillaDTO = new PlantillaDTO();
		plantillaDTO.setNombre(plantillaPrecios.getNombre());
		plantillaDTO.setSala(new SalaDTO(plantillaPrecios.getSala().getId()));
		
		entityManager.persist(plantillaDTO);
		plantillaPrecios.setId(plantillaDTO.getId());
		
		return plantillaPrecios;
	}
	
	@Transactional
	public Plantilla update(Plantilla plantillaPrecios) {
        JPAUpdateClause update = new JPAUpdateClause(entityManager, qPlantillaDTO);
        update.set(qPlantillaDTO.nombre, plantillaPrecios.getNombre())
        	.set(qPlantillaDTO.sala, new SalaDTO(plantillaPrecios.getSala().getId()))
            .where(qPlantillaDTO.id.eq(plantillaPrecios.getId())).execute();

        return plantillaPrecios;
	}

	@Transactional
	public int getTotalPlantillaPrecios(String userUID) {
		return (int) getQueryPlantillas(userUID).count();
	}

	@Transactional
	public int getTotalPlantillasEditables(String userUID) {
		return (int) getQueryPlantillasEditables(userUID).count();
	}
}
