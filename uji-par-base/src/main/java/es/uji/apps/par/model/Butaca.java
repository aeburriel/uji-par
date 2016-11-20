package es.uji.apps.par.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.db.ButacaDTO;
import org.springframework.beans.factory.annotation.Autowired;

@XmlRootElement
public class Butaca
{
    private long id;
    private String fila;
    private String numero;
    private BigDecimal precio;
    private String localizacion;
    private String localizacionNombre;
    private String x;
    private String y;
    private String tipo;
    private Date presentada;
    private String uuid;
    private String texto;
    private boolean anulada;

    public static ButacaDTO butacaToButacaDTO(Butaca butaca)
    {
        ButacaDTO butacaDTO = new ButacaDTO();

        butacaDTO.setId(butaca.getId());
        butacaDTO.setFila(butaca.getFila());
        butacaDTO.setNumero(butaca.getNumero());
        butacaDTO.setPrecio(butaca.getPrecio());

        return butacaDTO;
    }

    public static List<Butaca> butacasDTOToButacas(List<ButacaDTO> butacasDTO, boolean isIdEntrada)
    {
        List<Butaca> butacas = new ArrayList<Butaca>();
         
         for (ButacaDTO butacaDTO: butacasDTO)
         {
             butacas.add(new Butaca(butacaDTO, isIdEntrada));
         }
         
         return butacas;
    }
    
    public Butaca()
    {
    }

    public Butaca(String localizacion, String tipo)
    {
        this.localizacion = localizacion;
        this.tipo = tipo;
    }

    public Butaca(ButacaDTO butacaDTO, boolean isIdEntrada)
    {
        id = butacaDTO.getId();
        fila = butacaDTO.getFila();
        numero = butacaDTO.getNumero();
        precio = butacaDTO.getPrecio();
        localizacion = butacaDTO.getParLocalizacion().getCodigo();
        localizacionNombre = butacaDTO.getParLocalizacion().getNombreVa();
        tipo = butacaDTO.getTipo();
        presentada = butacaDTO.getPresentada();
        
        if (butacaDTO.getAnulada() != null)
        	anulada = butacaDTO.getAnulada();
        
        if (butacaDTO.getParCompra() != null) {
            if (isIdEntrada) {
                uuid = butacaDTO.getParCompra().getUuid() + "-" + butacaDTO.getIdEntrada();
            } else {
                uuid = butacaDTO.getParCompra().getUuid() + "-" + butacaDTO.getId();
            }
        }
    }

    public static List<Butaca> parseaJSON(String jsonButacas)
    {
        Gson gson = new Gson();
        return gson.fromJson(jsonButacas, new TypeToken<List<Butaca>>()
        {
        }.getType());
    }
    
    public static String toJSON(List<Butaca> butacas)
    {
        Gson gson = new Gson();
        return gson.toJson(butacas, new TypeToken<List<Butaca>>()
        {
        }.getType());
    }

    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getFila()
    {
        return fila;
    }

    public void setFila(String fila)
    {
        this.fila = fila;
    }

    public String getNumero()
    {
        return numero;
    }

    public void setNumero(String numero)
    {
        this.numero = numero;
    }

    public BigDecimal getPrecio()
    {
        return precio;
    }

    public void setPrecio(BigDecimal precio)
    {
        this.precio = precio;
    }

    public String getLocalizacion()
    {
        return localizacion;
    }

    public void setLocalizacion(String localizacion)
    {
        this.localizacion = localizacion;
    }

    public String getX()
    {
        return x;
    }

    public void setX(String x)
    {
        this.x = x;
    }

    public String getY()
    {
        return y;
    }

    public void setY(String y)
    {
        this.y = y;
    }

    public String getTipo()
    {
        return tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }
    
    @Override
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append("<id=");
        buff.append(id);
        buff.append(", localizacion=");
        buff.append(localizacion);
        buff.append(", fila=");
        buff.append(fila);
        buff.append(", numero=");
        buff.append(numero);
        buff.append(", tipo=");
        buff.append(tipo);        
        
        return buff.toString();
    }

    public Date getPresentada()
    {
        return presentada;
    }

    public void setPresentada(Date presentada)
    {
        this.presentada = presentada;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public boolean isAnulada() {
		return anulada;
	}

	public void setAnulada(boolean anulada) {
		this.anulada = anulada;
	}

	public String getLocalizacionNombre() {
		return localizacionNombre;
	}

	public void setLocalizacionNombre(String localizacionNombre) {
		this.localizacionNombre = localizacionNombre;
	}
    
}