package es.uji.apps.par.db;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="PAR_TPVS")
public class TpvsDTO {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="PAR_TPV_ID_GENERATOR", sequenceName="HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_TPV_ID_GENERATOR")
    private long id;

    @Column(name="NOMBRE")
    private String nombre;

    @Column(name="CODE")
    private String code;

    @Column(name="CURRENCY")
    private String currency;

    @Column(name="TERMINAL")
    private String terminal;

    @Column(name="TRANSACTION_CODE")
    private String transactionCode;

    @Column(name="ORDER_PREFIX")
    private String orderPrefix;

    @Column(name="LANG_CA_CODE")
    private String langCaCode;

    @Column(name="LANG_ES_CODE")
    private String langEsCode;

    @Column(name="SECRET")
    private String secret;

    @Column(name="URL")
    private String url;

    @Column(name="WSDL_URL")
    private String wsdlUrl;

    @Column(name="CIF")
    private String cif;

    @Column(name="SIGNATURE_METHOD")
    private String signatureMethod;

    //bi-directional many-to-one association to SesionDTO
    @OneToMany(mappedBy="parTpv")
    private List<EventoDTO> parEventos;

    @OneToMany(mappedBy="tpv")
    private List<TpvsCinesDTO> parTpvsCines;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getOrderPrefix() {
        return orderPrefix;
    }

    public void setOrderPrefix(String orderPrefix) {
        this.orderPrefix = orderPrefix;
    }

    public String getLangCaCode() {
        return langCaCode;
    }

    public void setLangCaCode(String langCaCode) {
        this.langCaCode = langCaCode;
    }

    public String getLangEsCode() {
        return langEsCode;
    }

    public void setLangEsCode(String langEsCode) {
        this.langEsCode = langEsCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public List<EventoDTO> getParEventos() {
        return parEventos;
    }

    public void setParEventos(List<EventoDTO> parEventos) {
        this.parEventos = parEventos;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public List<TpvsCinesDTO> getParTpvsCines()
    {
        return parTpvsCines;
    }

    public void setParTpvsCines(List<TpvsCinesDTO> parTpvsCines)
    {
        this.parTpvsCines = parTpvsCines;
    }
}
