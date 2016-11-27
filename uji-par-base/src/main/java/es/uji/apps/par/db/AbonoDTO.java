package es.uji.apps.par.db;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="PAR_ABONOS")
public class AbonoDTO {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="PAR_ABONOS_ID_GENERATOR", sequenceName="par_abonos_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_ABONOS_ID_GENERATOR")
    private long id;

    @Column(name="NOMBRE")
    private String nombre;

    @ManyToOne
    @JoinColumn(name="PLANTILLA_ID")
    private PlantillaDTO parPlantilla;

    @Column(name = "ANULADO")
    private Boolean anulado;

    @OneToMany(mappedBy="parAbono")
    private List<SesionAbonoDTO> parSesiones;

    public AbonoDTO() {
    }

    public AbonoDTO(long id) {
        this.id = id;
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

    public PlantillaDTO getParPlantilla() {
        return parPlantilla;
    }

    public void setParPlantilla(PlantillaDTO parPlantilla) {
        this.parPlantilla = parPlantilla;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }

    public List<SesionAbonoDTO> getParSesiones() {
        return parSesiones;
    }

    public void setParSesiones(List<SesionAbonoDTO> parSesiones) {
        this.parSesiones = parSesiones;
    }
}
