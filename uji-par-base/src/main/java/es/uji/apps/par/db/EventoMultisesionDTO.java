package es.uji.apps.par.db;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="PAR_EVENTOS_MULTISESION", uniqueConstraints={@UniqueConstraint(columnNames={"EVENTO_ID", "EVENTO_HIJO_ID"})})
public class EventoMultisesionDTO {
    @Id
    @SequenceGenerator(name="PAR_EVENTOS_MULTISESION_ID_GENERATOR", sequenceName="par_eventos_multisesion_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_EVENTOS_MULTISESION_ID_GENERATOR")
    private long id;

    @ManyToOne
    @JoinColumn(name="EVENTO_ID")
    private EventoDTO parEvento;

	@ManyToOne
	@JoinColumn(name="EVENTO_HIJO_ID")
	private EventoDTO parEventoHijo;

    @Column(name="VER_LING")
    private String versionLinguistica;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EventoDTO getParEvento() {
        return parEvento;
    }

    public void setParEvento(EventoDTO parEvento) {
        this.parEvento = parEvento;
    }

	public EventoDTO getParEventoHijo() {
		return parEventoHijo;
	}

	public void setParEventoHijo(EventoDTO parEventoHijo) {
		this.parEventoHijo = parEventoHijo;
	}

    public String getVersionLinguistica() {
        return versionLinguistica;
    }

    public void setVersionLinguistica(String versionLinguistica) {
        this.versionLinguistica = versionLinguistica;
    }
}
