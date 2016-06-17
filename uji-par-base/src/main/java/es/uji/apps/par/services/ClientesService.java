package es.uji.apps.par.services;

import com.mysema.query.Tuple;
import es.uji.apps.par.dao.ClientesDAO;
import es.uji.apps.par.model.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientesService {
    @Autowired
    private ClientesDAO clientesDAO;

    public List<Cliente> getClientes(String sortParameter, int start, int limit, String userUID) {
        List<Tuple> compras = clientesDAO.getClientes(sortParameter, start, limit, userUID);

        List<Cliente> clientes = new ArrayList<Cliente>();
        for (Tuple compra : compras) {
            Cliente cliente = new Cliente();
            cliente.setId(compra.get(0, Long.class));
            cliente.setNombre(compra.get(1, String.class));
            cliente.setApellidos(compra.get(2, String.class));
            cliente.setDireccion(compra.get(3, String.class));
            cliente.setPoblacion(compra.get(4, String.class));
            cliente.setCp(compra.get(5, String.class));
            cliente.setProvincia(compra.get(6, String.class));
            cliente.setTelefono(compra.get(7, String.class));
            cliente.setEmail(compra.get(8, String.class));
            clientes.add(cliente);
        }

        return clientes;
    }

    public int getTotalClientes(String userUID) {
        return clientesDAO.getTotalClientes(userUID);
    }

    public List<String> getMails(String userUID) {
        return clientesDAO.getMails(userUID);
    }
}
