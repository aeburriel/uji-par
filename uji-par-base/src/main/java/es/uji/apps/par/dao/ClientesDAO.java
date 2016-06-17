package es.uji.apps.par.dao;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import es.uji.apps.par.db.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientesDAO extends BaseDAO {
    private QCompraDTO qCompraDTO = QCompraDTO.compraDTO;
    private QSesionAbonoDTO qSesionAbonoDTO = QSesionAbonoDTO.sesionAbonoDTO;
    private QPlantillaDTO qPlantillaDTO = QPlantillaDTO.plantillaDTO;
    private QSesionDTO qSesionDTO = QSesionDTO.sesionDTO;
    private QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
    private QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
    private QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;

    @Transactional
    public List<Tuple> getClientes(String sortParameter, int start, int limit, String userUID) {
        JPAQuery jpaQuery = getQueryClientes(userUID);

        List<Tuple> compras = new ArrayList<Tuple>();
        if (jpaQuery != null) {
            compras = jpaQuery
                    .orderBy(getSort(qCompraDTO, sortParameter))
                    .limit(limit)
                    .offset(start)
                    .list(qCompraDTO.id, qCompraDTO.nombre, qCompraDTO.apellidos, qCompraDTO.direccion, qCompraDTO.poblacion, qCompraDTO.cp, qCompraDTO.provincia, qCompraDTO.telefono, qCompraDTO.email);
        }

        return compras;
    }

    @Transactional
    public int getTotalClientes(String userUID) {
        JPAQuery jpaQuery = getQueryClientes(userUID);

        if (jpaQuery != null) {
            return jpaQuery.list(qCompraDTO.id).size();
        }
        else {
            return 0;
        }
    }

    @Transactional
    private JPAQuery getQueryClientes(String userUID) {
        JPAQuery query = new JPAQuery(entityManager);
        JPAQuery subquery = new JPAQuery(entityManager);

        List<Long> ids = subquery
                .from(qCompraDTO)
                .join(qCompraDTO.parSesion, qSesionDTO)
                .join(qSesionDTO.parSala, qSalaDTO)
                .join(qSalaDTO.parSalasUsuario, qSalasUsuarioDTO)
                .join(qSalasUsuarioDTO.parUsuario, qUsuarioDTO)
                .where(qUsuarioDTO.usuario.eq(userUID).and(qCompraDTO.infoPeriodica.isTrue()))
                .groupBy(qCompraDTO.email)
                .distinct()
                .list(qCompraDTO.id.max());

        if (ids != null && ids.size() > 0) {
            return query.from(qCompraDTO)
                    .where(qCompraDTO.id.in(ids));
        }
        else {
            return null;
        }
    }


    public List<String> getMails(String userUID) {
        JPAQuery queryClientes = getQueryClientes(userUID);
        if (queryClientes != null) {
            return queryClientes.distinct().list(qCompraDTO.email);
        }
        else {
            return null;
        }
    }
}
