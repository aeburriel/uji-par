package es.uji.apps.par.model;

import es.uji.apps.par.db.SalaDTO;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.RegistroSerializaException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Sala
{
    private long id;
    private String codigo;
    private String nombre;
    private int asientos;
    private int asientosDiscapacitados;
    private int asientosNoReservados;
    private String tipo;
    private String formato;
    private String subtitulo;
    private String htmlTemplateName;
    private Cine cine;
    private boolean asientosNumerados;

    public Sala()
    {
    }

    public Sala(String codigo, String nombre, int asientos, int asientosDiscapacitados, int asientosNoReservados,
            String tipo, String formato, String subtitulo, Cine cine)
    {
        this.codigo = codigo;
        this.nombre = nombre;
        this.asientos = asientos;
        this.asientosDiscapacitados = asientosDiscapacitados;
        this.asientosNoReservados = asientosNoReservados;
        this.tipo = tipo;
        this.formato = formato;
        this.subtitulo = subtitulo;
        this.cine = cine;
    }

    public Sala(long id)
    {
        this.id = id;
    }

    public Sala(String nombre)
    {
        this.nombre = nombre;
    }

    public Sala(SalaDTO salaDTO)
    {
        this.setId(salaDTO.getId());
        this.setCodigo(salaDTO.getCodigo());
        this.setNombre(salaDTO.getNombre());
        this.setAsientos(salaDTO.getAsientos());
        this.setAsientosDiscapacitados(salaDTO.getAsientosDiscapacitados());
        this.setAsientosNoReservados(salaDTO.getAsientosNoReservados());
        this.setTipo(salaDTO.getTipo());
        this.setFormato(salaDTO.getFormato());
        this.setSubtitulo(salaDTO.getSubtitulo());
        this.setHtmlTemplateName(salaDTO.getHtmlTemplateName());
        this.setAsientosNumerados(salaDTO.getAsientosNumerados());

        if (salaDTO.getParCine() != null)
            this.setCine(Cine.cineDTOToCine(salaDTO.getParCine()));
    }

    public static Sala salaDTOtoSala(SalaDTO salaDTO)
    {
        return new Sala(salaDTO);
    }

    public static SalaDTO salaToSalaDTO(Sala sala)
    {
        SalaDTO salaDTO = new SalaDTO();

        salaDTO.setId(sala.getId());
        salaDTO.setCodigo(sala.getCodigo());
        salaDTO.setNombre(sala.getNombre());
        salaDTO.setAsientos(sala.getAsientos());
        salaDTO.setAsientosDiscapacitados(sala.getAsientosDiscapacitados());
        salaDTO.setAsientosNoReservados(sala.getAsientosNoReservados());
        salaDTO.setTipo(sala.getTipo());
        salaDTO.setFormato(sala.getFormato());
        salaDTO.setSubtitulo(sala.getSubtitulo());
        salaDTO.setHtmlTemplateName(sala.getHtmlTemplateName());
        salaDTO.setAsientosNumerados(sala.isAsientosNumerados());

        if (sala.getCine() != null)
            salaDTO.setParCine(Cine.cineToCineDTO(sala.getCine()));

        return salaDTO;
    }

    public static List<Sala> salasDTOtoSalas(List<SalaDTO> salasDTO)
    {
        ArrayList<Sala> salas = new ArrayList<Sala>();

        for (SalaDTO salaDTO : salasDTO)
            salas.add(Sala.salaDTOtoSala(salaDTO));

        return salas;
    }

    public long getId()
    {
        return id;
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

    public Cine getCine()
    {
        return cine;
    }

    public void setCine(Cine cine)
    {
        this.cine = cine;
    }

    public String getHtmlTemplateName() {
        return htmlTemplateName;
    }

    public void setHtmlTemplateName(String htmlTemplateName) {
        this.htmlTemplateName = htmlTemplateName;
    }

    public boolean isAsientosNumerados()
    {
        return asientosNumerados;
    }

    public void setAsientosNumerados(boolean asientosNumerados)
    {
        this.asientosNumerados = asientosNumerados;
    }

    public static void checkValidity(String nombre, String codigo) throws RegistroSerializaException {
        if (nombre == null)
            throw new RegistroSerializaException(GeneralPARException.NOMBRE_SALA_NULO_CODE);
        
        Sala.checkValidity(codigo);
    }
    
    public static void checkValidity(String codigo) throws RegistroSerializaException {
    	if (codigo == null)
            throw new RegistroSerializaException(GeneralPARException.SALA_NULA_CODE);
    	
    	 if (codigo.length() > 6)
             throw new RegistroSerializaException(GeneralPARException.CODIGO_SALA_LARGO_CODE);
                     
    }
}