package es.uji.apps.par.model;

import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.TarifaDTO;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Tarifa
{
    private long id;
    private String nombre;
    private String isPublico;
    private String defecto;
	private Cine cine;

    public Tarifa()
    {
    }

    public Tarifa(long id)
    {
        this.id = id;
    }

    public Tarifa(TarifaDTO tarifaDTO)
    {
        this.id = tarifaDTO.getId();
        this.nombre = tarifaDTO.getNombre();
        this.isPublico = booleanToString(tarifaDTO.getIsPublica());
        this.defecto = booleanToString(tarifaDTO.getDefecto());
    }
    
    public Tarifa(String nombre, String isPublico)
    {
        this.nombre = nombre;
        this.isPublico = isPublico;
    }

    public Tarifa(Integer id, String nombre, String isPublico) {
		this.id = id;
		this.nombre = nombre;
		this.isPublico = isPublico;
	}

	public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

	public static TarifaDTO toDTO(Tarifa tarifa) {
		TarifaDTO tarifaDTO = new TarifaDTO();
		tarifaDTO.setId(tarifa.getId());
		tarifaDTO.setNombre(tarifa.getNombre());
		tarifaDTO.setIsPublica(stringToBoolean(tarifa.getIsPublico()));
		tarifaDTO.setDefecto(stringToBoolean(tarifa.getDefecto()));

		if (tarifa.getCine() != null)
			tarifaDTO.setParCine(new CineDTO(tarifa.getCine().getId()));

		return tarifaDTO;
	}

	public String getIsPublico() {
		return isPublico;
	}

	public void setIsPublico(String isPublico) {
		this.isPublico = isPublico;
	}
	
	public static String booleanToString(Boolean bool) {
		if (bool != null && bool)
			return "on";
		else
			return "";
	}
	
	public static Boolean stringToBoolean(String str) {
		if (str == null || !str.equals("on"))
			return false;
		else
			return true;
	}

	public static Tarifa tarifaDTOToTarifa(TarifaDTO tarifaDTO) {
		Tarifa tarifa = new Tarifa();
		tarifa.setId(tarifaDTO.getId());
		tarifa.setNombre(tarifaDTO.getNombre());
		tarifa.setIsPublico(booleanToString(tarifaDTO.getIsPublica()));
		tarifa.setDefecto(booleanToString(tarifaDTO.getDefecto()));
		return tarifa;
	}

	public String getDefecto() {
		return defecto;
	}

	public void setDefecto(String defecto) {
		this.defecto = defecto;
	}

	public void setCine(Cine cine)
	{
		this.cine = cine;
	}

	public Cine getCine()
	{
		return cine;
	}
}