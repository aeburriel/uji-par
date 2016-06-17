package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.SesionAbono;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AbonosDAO extends BaseDAO {

    private QAbonoDTO qAbonoDTO = QAbonoDTO.abonoDTO;
    private QSesionAbonoDTO qSesionAbonoDTO = QSesionAbonoDTO.sesionAbonoDTO;
    private QPlantillaDTO qPlantillaDTO = QPlantillaDTO.plantillaDTO;
    private QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
    private QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
    private QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;

    @Transactional
    public List<Abono> getAbonos(String sortParameter, int start, int limit, String userUID)
    {
        List<Abono> abono = new ArrayList<Abono>();

        JPAQuery query = new JPAQuery(entityManager);

        List<AbonoDTO> abonosDTO = query.from(qAbonoDTO)
                .join(qAbonoDTO.parPlantilla, qPlantillaDTO)
                .join(qPlantillaDTO.sala, qSalaDTO)
                .join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
                .join(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
                .orderBy(getSort(qAbonoDTO, sortParameter))
                .where(qUsuarioDTO.usuario.eq(userUID).and(qAbonoDTO.anulado.eq(false).or(qAbonoDTO.anulado.isNull())))
                .offset(start)
                .limit(limit)
                .distinct()
                .list(qAbonoDTO);

        for (AbonoDTO abonoDB : abonosDTO)
        {
            abono.add(new Abono(abonoDB));
        }

        return abono;
    }

    @Transactional
    public Abono addAbono(Abono abono)
    {
        if (abono.getSesiones() != null && abono.getSesiones().size() > 0)
        {
            AbonoDTO abonoDTO = new AbonoDTO();
            abonoDTO.setNombre(abono.getNombre());
            abonoDTO.setParPlantilla(Plantilla.plantillaPreciosToPlantillaPreciosDTO(abono.getPlantillaPrecios()));

            entityManager.persist(abonoDTO);

            abono.setId(abonoDTO.getId());
            updateSesionesAbono(abonoDTO.getId(), abono.getSesiones());
            return abono;
        }
        else
        {
            return null;
        }
    }

    @Transactional
    private void updateSesionesAbono(long abonoId, List<SesionAbono> sesiones) {
        JPADeleteClause del = new JPADeleteClause(entityManager, qSesionAbonoDTO);
        del.where(qSesionAbonoDTO.parAbono.id.eq(abonoId)).execute();

        if (sesiones != null) {
            for (SesionAbono sesion: sesiones) {
                SesionAbonoDTO sesionAbonoDTO = new SesionAbonoDTO();
                sesionAbonoDTO.setParAbono(new AbonoDTO(abonoId));
                sesionAbonoDTO.setParSesion(new SesionDTO(sesion.getSesion().getId()));
                entityManager.persist(sesionAbonoDTO);
            }
        }
    }

    @Transactional
    public long removeAbono(long id)
    {
        JPAUpdateClause update = new JPAUpdateClause(entityManager, qAbonoDTO);
        update.set(qAbonoDTO.anulado, true).where(qAbonoDTO.id.eq(id)).execute();

        return id;
    }

    @Transactional
    public Abono updateAbono(Abono abono)
    {
        JPAUpdateClause update = new JPAUpdateClause(entityManager, qAbonoDTO);
        update.set(qAbonoDTO.nombre, abono.getNombre())
                .set(qAbonoDTO.parPlantilla, Plantilla.plantillaPreciosToPlantillaPreciosDTO(abono.getPlantillaPrecios()))
                .where(qAbonoDTO.id.eq(abono.getId())).execute();

        updateSesionesAbono(abono.getId(), abono.getSesiones());
        return abono;
    }

    @Transactional
    public Abono updateCamposActualizablesConAbonadosAbono(Abono abono) {
        JPAUpdateClause update = new JPAUpdateClause(entityManager, qAbonoDTO);
        update.set(qAbonoDTO.nombre, abono.getNombre())
                .where(qAbonoDTO.id.eq(abono.getId())).execute();

        return abono;
    }

    @Transactional
    public int getTotalAbonos(String userUID)
    {
        JPAQuery query = new JPAQuery(entityManager);

        return (int) query.from(qAbonoDTO)
                .join(qAbonoDTO.parPlantilla, qPlantillaDTO)
                .join(qPlantillaDTO.sala, qSalaDTO)
                .join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
                .join(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
                .where(qUsuarioDTO.usuario.eq(userUID).and(qAbonoDTO.anulado.eq(false).or(qAbonoDTO.anulado.isNull())))
                .distinct()
                .count();
    }

    @Transactional
    public Abono getAbono(Long abonoId, String userUID) {
        JPAQuery query = new JPAQuery(entityManager);

        AbonoDTO abonosDTO =query.from(qAbonoDTO)
                .leftJoin(qAbonoDTO.parSesiones, qSesionAbonoDTO).fetch()
                .join(qAbonoDTO.parPlantilla, qPlantillaDTO)
                .join(qPlantillaDTO.sala, qSalaDTO)
                .join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
                .join(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
                .where(qUsuarioDTO.usuario.eq(userUID).and(qAbonoDTO.id.eq(abonoId)
                        .and(qAbonoDTO.anulado.eq(false).or(qAbonoDTO.anulado.isNull()))))
                .distinct()
                .uniqueResult(qAbonoDTO);

        if (abonosDTO != null)
            return new Abono(abonosDTO);
        else
            return null;
    }
}
