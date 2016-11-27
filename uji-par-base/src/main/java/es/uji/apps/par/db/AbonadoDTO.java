package es.uji.apps.par.db;

import es.uji.apps.par.model.Abonado;
import es.uji.apps.par.model.Abono;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="PAR_ABONADOS")
public class AbonadoDTO {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="PAR_ABONADOS_ID_GENERATOR", sequenceName="par_abonados_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_ABONADOS_ID_GENERATOR")
    private long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "APELLIDOS")
    private String apellidos;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "POBLACION")
    private String poblacion;

    @Column(name = "CP")
    private String cp;

    @Column(name = "PROVINCIA")
    private String provincia;

    @Column(name = "TFNO")
    private String telefono;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "INFO_PERIODICA")
    private Boolean infoPeriodica;

    @Column(name = "FECHA")
    private Timestamp fecha;

    @Column(name = "IMPORTE")
    private BigDecimal importe;

    @Column(name = "ANULADO")
    private Boolean anulado;

    @ManyToOne
    @JoinColumn(name="ABONO_ID")
    private AbonoDTO parAbono;

    // bi-directional many-to-one association to ButacaDTO
    @OneToMany(mappedBy = "parAbonado", fetch=FetchType.LAZY)
    private List<CompraDTO> parCompras;

    public AbonadoDTO() {
    }

    public AbonadoDTO(Abonado abonado) {
        this.nombre = abonado.getNombre();
        this.apellidos = abonado.getApellidos();
        this.direccion = abonado.getDireccion();
        this.poblacion = abonado.getPoblacion();
        this.cp = abonado.getCp();
        this.provincia = abonado.getProvincia();
        this.telefono = abonado.getTelefono();
        this.email = abonado.getEmail();
        this.infoPeriodica = abonado.getInfoPeriodica();
        this.fecha = new Timestamp(new Date().getTime());
        this.importe = abonado.getImporte();
        this.anulado = abonado.getAnulado();
        this.parAbono = new AbonoDTO(abonado.getAbono().getId());
    }

    public AbonadoDTO(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getInfoPeriodica() {
        return infoPeriodica;
    }

    public void setInfoPeriodica(Boolean infoPeriodica) {
        this.infoPeriodica = infoPeriodica;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public AbonoDTO getParAbono() {
        return parAbono;
    }

    public void setParAbono(AbonoDTO parAbono) {
        this.parAbono = parAbono;
    }

    public List<CompraDTO> getParCompras() {
        return parCompras;
    }

    public void setParCompras(List<CompraDTO> parCompras) {
        this.parCompras = parCompras;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }
}
