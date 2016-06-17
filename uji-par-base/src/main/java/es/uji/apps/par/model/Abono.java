package es.uji.apps.par.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.uji.apps.par.db.AbonoDTO;
import es.uji.apps.par.db.SesionAbonoDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Abono {

    private long id;
    private String nombre;
    private Boolean anulado;
    private Plantilla plantillaPrecios;
    private List<SesionAbono> sesiones;

    public Abono()
    {
    }

    public Abono(String nombre, int plantillaPrecios)
    {
        this.nombre = nombre;
        this.plantillaPrecios = new Plantilla(plantillaPrecios);
    }

    public Abono(AbonoDTO abonoDTO) {
        this.id = abonoDTO.getId();
        this.nombre = abonoDTO.getNombre();
        this.anulado = abonoDTO.getAnulado();
        this.plantillaPrecios =  Plantilla.plantillaPreciosDTOtoPlantillaPrecios(abonoDTO.getParPlantilla());
        this.sesiones = new ArrayList<SesionAbono>();
        for (SesionAbonoDTO sesion : abonoDTO.getParSesiones()) {
            this.sesiones.add(new SesionAbono(sesion));
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }

    public Plantilla getPlantillaPrecios() {
        return plantillaPrecios;
    }

    public void setPlantillaPrecios(Plantilla plantillaPrecios) {
        this.plantillaPrecios = plantillaPrecios;
    }

    public static Abono AbonoDTOToAbono(AbonoDTO parAbono) {
        Abono abono = new Abono();

        abono.setId(parAbono.getId());
        abono.setNombre(parAbono.getNombre());
        abono.setPlantillaPrecios(Plantilla.plantillaPreciosDTOtoPlantillaPrecios(parAbono.getParPlantilla()));

        return abono;
    }

    public List<SesionAbono> getSesiones() {
        return sesiones;
    }

    public void setSesiones(String jsonSesionesAbonos) {
        Gson gson = new Gson();
        List<SesionAbono> sesiones = gson.fromJson(jsonSesionesAbonos, new TypeToken<List<SesionAbono>>(){}.getType());
        this.sesiones = sesiones;
    }
}
