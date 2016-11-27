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
 * The persistent class for the PAR_PLANTAS_SALA database table.
 * 
 */
@Entity
@Table(name = "PAR_PLANTAS_SALA")
public class PlantaSalaDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "PAR_PLANTAS_SALA_ID_GENERATOR", sequenceName = "par_plantas_sala_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_PLANTAS_SALA_ID_GENERATOR")
    private long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "SALA_ID")
    private SalaDTO parSala;

    public PlantaSalaDTO()
    {
    }

    public PlantaSalaDTO(SalaDTO salaDTO, String nombre)
    {
        this.nombre = nombre;
    }

    public long getId()
    {
        return this.id;
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

    public SalaDTO getParSala()
    {
        return parSala;
    }

    public void setParSala(SalaDTO parSala)
    {
        this.parSala = parSala;
    }
}