package es.uji.apps.par.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The persistent class for the PAR_SALAS database table.
 * 
 */
@Entity
@Table(name = "PAR_SALAS")
public class SalaDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "PAR_SALAS_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_SALAS_ID_GENERATOR")
    private long id;

    @Column(name = "CODIGO")
    private String codigo;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "ASIENTOS")
    private int asientos;

    @Column(name = "ASIENTO_DISC")
    private int asientosDiscapacitados;

    @Column(name = "ASIENTO_NORES")
    private int asientosNoReservados;

    @Column(name = "TIPO")
    private String tipo;

    @Column(name = "FORMATO")
    private String formato;

    @Column(name = "SUBTITULO")
    private String subtitulo;

    @Column(name = "HTML_TEMPLATE_NAME")
    private String htmlTemplateName;

    @Column(name="ASIENTOS_NUMERADOS")
    private Boolean asientosNumerados;

    @ManyToOne
    @JoinColumn(name = "CINE_ID")
    private CineDTO parCine;

    @OneToMany(mappedBy = "parSala", fetch = FetchType.LAZY)
    private List<PlantaSalaDTO> parPlantas;

    @OneToMany(mappedBy = "parSala", fetch = FetchType.LAZY)
    private List<SesionDTO> parSesiones;
    
    @OneToMany(mappedBy = "sala", fetch = FetchType.LAZY)
    private List<LocalizacionDTO> parLocalizaciones;
    
    @OneToMany(mappedBy = "sala", fetch = FetchType.LAZY)
    private List<PlantillaDTO> parPlantillas;

	@OneToMany(mappedBy = "sala", fetch = FetchType.LAZY)
	private List<ReportDTO> parReports;

	@OneToMany(mappedBy = "parSala", fetch = FetchType.LAZY)
	private List<SalasUsuarioDTO> parSalasUsuario;

    public SalaDTO()
    {
    }

    public SalaDTO(CineDTO cineDTO, String codigo, String nombre, int asientos, int asientosDiscapacitados,
            int asientosNoReservados, String tipo, String formato, String subtitulo)
    {
        this.codigo = codigo;
        this.nombre = nombre;
        this.asientos = asientos;
        this.asientosDiscapacitados = asientosDiscapacitados;
        this.asientosNoReservados = asientosNoReservados;
        this.tipo = tipo;
        this.formato = formato;
        this.subtitulo = subtitulo;
        this.parCine = cineDTO;
        this.parPlantas = new ArrayList<PlantaSalaDTO>();
    }

    public SalaDTO(long id) {
		setId(id);
	}

	public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public int getAsientos()
    {
        return asientos;
    }

    public void setAsientos(int asientos)
    {
        this.asientos = asientos;
    }

    public int getAsientosDiscapacitados()
    {
        return asientosDiscapacitados;
    }

    public void setAsientosDiscapacitados(int asientosDiscapacitados)
    {
        this.asientosDiscapacitados = asientosDiscapacitados;
    }

    public int getAsientosNoReservados()
    {
        return asientosNoReservados;
    }

    public void setAsientosNoReservados(int asientosNoReservados)
    {
        this.asientosNoReservados = asientosNoReservados;
    }

    public String getTipo()
    {
        return tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    public String getFormato()
    {
        return formato;
    }

    public void setFormato(String formato)
    {
        this.formato = formato;
    }

    public String getSubtitulo()
    {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo)
    {
        this.subtitulo = subtitulo;
    }

    public CineDTO getParCine()
    {
        return parCine;
    }

    public void setParCine(CineDTO parCine)
    {
        this.parCine = parCine;
    }

    public List<PlantaSalaDTO> getParPlantas()
    {
        return parPlantas;
    }

    public void setParPlantas(List<PlantaSalaDTO> parPlantas)
    {
        this.parPlantas = parPlantas;
    }

    public List<SesionDTO> getParSesiones()
    {
        return parSesiones;
    }

    public void setParSesiones(List<SesionDTO> parSesiones)
    {
        this.parSesiones = parSesiones;
    }

	public List<LocalizacionDTO> getParLocalizaciones() {
		return parLocalizaciones;
	}

	public void setParLocalizaciones(List<LocalizacionDTO> parLocalizaciones) {
		this.parLocalizaciones = parLocalizaciones;
	}

	public List<PlantillaDTO> getParPlantillas() {
		return parPlantillas;
	}

	public void setParPlantillas(List<PlantillaDTO> parPlantillas) {
		this.parPlantillas = parPlantillas;
	}

    public String getHtmlTemplateName() {
        return htmlTemplateName;
    }

    public void setHtmlTemplateName(String htmlTemplateName) {
        this.htmlTemplateName = htmlTemplateName;
    }

	public List<ReportDTO> getParReports() {
		return parReports;
	}

	public void setParReports(List<ReportDTO> parReports) {
		this.parReports = parReports;
	}

	public List<SalasUsuarioDTO> getParSalasUsuario() {
		return parSalasUsuario;
	}

	public void setParSalasUsuario(List<SalasUsuarioDTO> parSalasUsuario) {
		this.parSalasUsuario = parSalasUsuario;
	}

    public Boolean getAsientosNumerados()
    {
        return asientosNumerados;
    }

    public void setAsientosNumerados(Boolean asientosNumerados)
    {
        this.asientosNumerados = asientosNumerados;
    }
}