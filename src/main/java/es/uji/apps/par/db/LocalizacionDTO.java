package es.uji.apps.par.db;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The persistent class for the PAR_LOCALIZACIONES database table.
 * 
 */
@Entity
@Table(name = "PAR_LOCALIZACIONES")
public class LocalizacionDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "PAR_LOCALIZACIONES_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_LOCALIZACIONES_ID_GENERATOR")
    private long id;

    @Column(name = "NOMBRE_ES")
    private String nombreEs;

    @Column(name = "NOMBRE_VA")
    private String nombreVa;

    @Column(name = "TOTAL_ENTRADAS")
    private BigDecimal totalEntradas;

    public LocalizacionDTO()
    {
    }

    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getNombreEs()
    {
        return this.nombreEs;
    }

    public void setNombreEs(String nombreEs)
    {
        this.nombreEs = nombreEs;
    }

    public String getNombreVa()
    {
        return this.nombreVa;
    }

    public void setNombreVa(String nombreVa)
    {
        this.nombreVa = nombreVa;
    }

    public BigDecimal getTotalEntradas()
    {
        return this.totalEntradas;
    }

    public void setTotalEntradas(BigDecimal totalEntradas)
    {
        this.totalEntradas = totalEntradas;
    }

}