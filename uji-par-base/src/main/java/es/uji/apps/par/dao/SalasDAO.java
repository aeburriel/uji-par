package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPAQuery;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.PlantaSala;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class SalasDAO extends BaseDAO
{
    private QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
    private QSalaDTO qSalaDTO = QSalaDTO.salaDTO;

    @Transactional
    public List<Sala> getSalas(String userUID)
    {
        JPAQuery query = new JPAQuery(entityManager);

        List<SalaDTO> list = query.from(qSalaDTO)
                .join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
                .leftJoin(qSalaDTO.parCine).fetch()
                .where(qSalasUsuarioDTO.parUsuario.usuario.eq(userUID))
                .list(qSalaDTO);

        return Sala.salasDTOtoSalas(list);
    }

    @Transactional
    public Sala addSala(Sala sala)
    {
        SalaDTO salaDTO = Sala.salaToSalaDTO(sala);

        entityManager.persist(salaDTO);

        sala.setId(salaDTO.getId());
        return sala;
    }

    @Transactional
    public List<PlantaSala> getPlantas(long idSala)
    {
        QPlantaSalaDTO qPlantaSalaDTO = QPlantaSalaDTO.plantaSalaDTO;

        JPAQuery query = new JPAQuery(entityManager);

        List<PlantaSalaDTO> list = query.from(qPlantaSalaDTO).where(qPlantaSalaDTO.parSala.id.eq(idSala)).list(qPlantaSalaDTO);

        return PlantaSala.plantasSalasDTOToPlantasSalas(list);
    }

    @Transactional
    public PlantaSala addPlanta(PlantaSala plantaSala)
    {
        PlantaSalaDTO plantaDTO = PlantaSala.plantaSalaToPlantaSalaDTO(plantaSala);

        entityManager.persist(plantaDTO);

        plantaSala.setId(plantaDTO.getId());
        
        return plantaSala;
    }

    @Transactional
    public List<Sala> getSalas(List<Sesion> sesiones)
    {
        QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
        
        JPAQuery query = new JPAQuery(entityManager);

        List<SalaDTO> salasDTO = query.from(qSalaDTO).join(qSalaDTO.parSesiones, qSesionDTO).
        		where(qSesionDTO.id.in(Sesion.getIdsSesiones(sesiones))).distinct().list(qSalaDTO);
        
        return Sala.salasDTOtoSalas(salasDTO);
    }

    @Transactional
    public void persistSala(SalaDTO salaDTO) {
        entityManager.persist(salaDTO);
    }

	@Transactional
	public void addReport(ReportDTO reportDTO) {
		entityManager.persist(reportDTO);
	}
}
