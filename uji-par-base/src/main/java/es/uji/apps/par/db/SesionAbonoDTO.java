package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="PAR_SESIONES_ABONOS")
public class SesionAbonoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="PAR_SESIONES_ABONOS_ID_GENERATOR", sequenceName="par_sesiones_abonos_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_SESIONES_ABONOS_ID_GENERATOR")
    private long id;

    @ManyToOne
    @JoinColumn(name="SESION_ID")
    private SesionDTO parSesion;

    @ManyToOne
    @JoinColumn(name="ABONO_ID")
    private AbonoDTO parAbono;

    public SesionAbonoDTO() {
    }

    public SesionAbonoDTO(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SesionDTO getParSesion() {
        return parSesion;
    }

    public void setParSesion(SesionDTO parSesion) {
        this.parSesion = parSesion;
    }

    public AbonoDTO getParAbono() {
        return parAbono;
    }

    public void setParAbono(AbonoDTO parAbono) {
        this.parAbono = parAbono;
    }
}
