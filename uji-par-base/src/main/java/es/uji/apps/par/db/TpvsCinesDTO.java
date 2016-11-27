package es.uji.apps.par.db;

import javax.persistence.*;

@Entity
@Table(name="PAR_TPVS_CINES")
public class TpvsCinesDTO
{
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="PAR_TPV_CINE_ID_GENERATOR", sequenceName="par_tpvs_cines_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_TPV_CINE_ID_GENERATOR")
    private long id;

    @ManyToOne
    @JoinColumn(name="TPV_ID")
    private TpvsDTO tpv;

    @ManyToOne
    @JoinColumn(name="CINE_ID")
    private CineDTO cine;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public TpvsDTO getTpv()
    {
        return tpv;
    }

    public void setTpv(TpvsDTO tpv)
    {
        this.tpv = tpv;
    }

    public CineDTO getCine()
    {
        return cine;
    }

    public void setCine(CineDTO cine)
    {
        this.cine = cine;
    }
}
