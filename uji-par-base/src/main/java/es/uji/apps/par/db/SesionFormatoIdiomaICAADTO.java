package es.uji.apps.par.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * The persistent class for the PAR_SESIONES_FORMATO_IDIOMA_ICAA database table.
 * 
 */
@Entity
@Table(name="PAR_SESIONES_FORMATO_IDI_ICAA")
public class SesionFormatoIdiomaICAADTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_SESIONES_FORMATO_IDI_ICAA", sequenceName="par_sesiones_formato_idioma_icaa_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_SESIONES_FORMATO_IDI_ICAA")
	private long id;
    
    @Column(name="FORMATO")
    private String formato;

    @Column(name="VER_LING")
    private String versionLinguistica;
    
	//bi-directional many-to-one association to EventoDTO
    @ManyToOne
	@JoinColumn(name="EVENTO_ID")
	private EventoDTO parEvento;

	public SesionFormatoIdiomaICAADTO() {
	}
	
	public SesionFormatoIdiomaICAADTO(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public EventoDTO getParEvento() {
		return this.parEvento;
	}

	public void setParEvento(EventoDTO parEvento) {
		this.parEvento = parEvento;
	}

	public String getFormato()
    {
        return formato;
    }

    public void setFormato(String formato)
    {
        this.formato = formato;
    }

    public String getVersionLinguistica()
    {
        return versionLinguistica;
    }

    public void setVersionLinguistica(String versionLinguistica)
    {
        this.versionLinguistica = versionLinguistica;
    }
}