package es.uji.apps.par.model;

import es.uji.apps.par.db.TipoEventoDTO;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TipoEvento
{
    private long id;
    private String nombreEs;
    private String nombreVa;
    private Boolean exportarICAA;
	private Cine cine;

    public TipoEvento()
    {
    }

    public TipoEvento(long id)
    {
        this.id = id;
    }

    public TipoEvento(String nombreEs, String nombreVa, Boolean exportarICAA) {
        this.nombreEs = nombreEs;
        this.nombreVa = nombreVa;
        this.exportarICAA = exportarICAA;
    }

	public TipoEvento(String nombreEs, String nombreVa, Boolean exportarICAA, Cine cine) {
		this.nombreEs = nombreEs;
		this.nombreVa = nombreVa;
		this.exportarICAA = exportarICAA;
		this.cine = cine;
	}

    public TipoEvento(TipoEventoDTO tipoEventoDTO)
    {
        this.id = tipoEventoDTO.getId();
        this.nombreEs = tipoEventoDTO.getNombreEs();
        this.nombreVa = tipoEventoDTO.getNombreVa();
        this.exportarICAA = tipoEventoDTO.getExportarICAA();
    }

    public TipoEvento(String nombreEs)
    {
        this.nombreEs = nombreEs;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getNombreEs()
    {
        return nombreEs;
    }

    public void setNombreEs(String nombreEs)
    {
        this.nombreEs = nombreEs;
    }

    public String getNombreVa()
    {
        return nombreVa;
    }

    public void setNombreVa(String nombreVa)
    {
        this.nombreVa = nombreVa;
    }

	public static TipoEventoDTO tipoEventoToTipoEventoDTO(TipoEvento tipoEvento) {
		TipoEventoDTO tipoEventoDTO = new TipoEventoDTO();
    	tipoEventoDTO.setId(tipoEvento.getId());
    	tipoEventoDTO.setNombreEs(tipoEvento.getNombreEs());
    	tipoEventoDTO.setNombreVa(tipoEvento.getNombreVa());

    	return tipoEventoDTO;
	}

	public static TipoEvento tipoEventoDTOToTipoEvento(TipoEventoDTO eventoDTO) {
		TipoEvento tipoEvento = new TipoEvento();
    	tipoEvento.setId(eventoDTO.getId());
    	tipoEvento.setNombreEs(eventoDTO.getNombreEs());
    	tipoEvento.setNombreVa(eventoDTO.getNombreVa());
    	
    	return tipoEvento;
	}

	public Boolean getExportarICAA() {
		return exportarICAA;
	}

	public void setExportarICAA(Boolean exportarICAA) {
		this.exportarICAA = exportarICAA;
	}

	public Cine getCine()
	{
		return cine;
	}

	public void setCine(Cine cine)
	{
		this.cine = cine;
	}
}