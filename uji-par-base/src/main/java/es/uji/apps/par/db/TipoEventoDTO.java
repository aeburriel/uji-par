package es.uji.apps.par.db;

import es.uji.apps.par.model.TipoEvento;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;


/**
 * The persistent class for the PAR_TIPOS_EVENTO database table.
 * 
 */
@Entity
@Table(name="PAR_TIPOS_EVENTO")
public class TipoEventoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_TIPOS_EVENTO_ID_GENERATOR", sequenceName="HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_TIPOS_EVENTO_ID_GENERATOR")
	private long id;

	@Column(name="NOMBRE_ES")
	private String nombreEs;

	@Column(name="NOMBRE_VA")
	private String nombreVa;

	//bi-directional many-to-one association to EventoDTO
	@OneToMany(mappedBy="parTiposEvento")
	private List<EventoDTO> parEventos;
	
	@Column(name="EXPORTAR_ICAA")
	private Boolean exportarICAA;

	@ManyToOne
	@JoinColumn(name="CINE_ID")
	private CineDTO parCine;

	public TipoEventoDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombreEs() {
		return this.nombreEs;
	}

	public void setNombreEs(String nombreEs) {
		this.nombreEs = nombreEs;
	}

	public String getNombreVa() {
		return this.nombreVa;
	}

	public void setNombreVa(String nombreVa) {
		this.nombreVa = nombreVa;
	}

	public List<EventoDTO> getParEventos() {
		return this.parEventos;
	}

	public void setParEventos(List<EventoDTO> parEventos) {
		this.parEventos = parEventos;
	}

	public Boolean getExportarICAA() {
		return exportarICAA;
	}

	public void setExportarICAA(Boolean exportarICAA) {
		this.exportarICAA = exportarICAA;
	}

	public CineDTO getParCine()
	{
		return parCine;
	}

	public void setParCine(CineDTO parCine)
	{
		this.parCine = parCine;
	}

	public static TipoEventoDTO fromTipoEvento(TipoEvento tipoEvento) {
        TipoEventoDTO tipoEventoDTO = new TipoEventoDTO();
        tipoEventoDTO.setId(tipoEvento.getId());
        tipoEventoDTO.setExportarICAA(tipoEvento.getExportarICAA());
        tipoEventoDTO.setNombreEs(tipoEvento.getNombreEs());
        tipoEventoDTO.setNombreVa(tipoEvento.getNombreVa());

        return tipoEventoDTO;
    }
}