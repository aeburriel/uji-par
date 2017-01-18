package es.uji.apps.par.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import es.uji.apps.par.db.PreciosSesionDTO;
import es.uji.apps.par.db.SesionDTO;

@XmlRootElement
public class PreciosSesion
{

	private long id;
	private Localizacion localizacion;
	private Sesion sesion;
	private BigDecimal descuento;
	private BigDecimal invitacion;
	private BigDecimal precio;
	private BigDecimal aulaTeatro;
	private Tarifa tarifa;

	public PreciosSesion()
	{

	}

	public PreciosSesion(PreciosPlantilla preciosPlantilla)
	{
		this.id = preciosPlantilla.getId();
		this.localizacion = preciosPlantilla.getLocalizacion();
		this.precio = preciosPlantilla.getPrecio();
		this.tarifa = preciosPlantilla.getTarifa();
	}

	public PreciosSesion(PreciosSesionDTO preciosSesion)
	{
		this.id = preciosSesion.getId();
		this.localizacion = Localizacion.localizacionDTOtoLocalizacion(preciosSesion.getParLocalizacione());
		this.sesion = Sesion.SesionDTOToSesion(preciosSesion.getParSesione());
		this.precio = preciosSesion.getPrecio();
		this.tarifa = Tarifa.tarifaDTOToTarifa(preciosSesion.getParTarifa());
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Localizacion getLocalizacion()
	{
		return localizacion;
	}

	public void setLocalizacion(Localizacion localizacion)
	{
		this.localizacion = localizacion;
	}

	public BigDecimal getDescuento()
	{
		return descuento != null ? descuento.setScale(2, RoundingMode.CEILING) : descuento;
	}

	public void setDescuento(BigDecimal descuento)
	{
		this.descuento = descuento;
	}

	public BigDecimal getInvitacion()
	{
		return invitacion != null ? invitacion.setScale(2, RoundingMode.CEILING) : invitacion;
	}

	public void setInvitacion(BigDecimal invitacion)
	{
		this.invitacion = invitacion;
	}

	public BigDecimal getPrecio()
	{
		return precio != null ? precio.setScale(2, RoundingMode.CEILING) : precio;
	}

	public void setPrecio(BigDecimal precio)
	{
		this.precio = precio;
	}

	public BigDecimal getAulaTeatro()
	{
		return aulaTeatro != null ? aulaTeatro.setScale(2, RoundingMode.CEILING) : aulaTeatro;
	}

	public void setAulaTeatro(BigDecimal aulaTeatro)
	{
		this.aulaTeatro = aulaTeatro;
	}

	public Sesion getSesion()
	{
		return sesion;
	}

	public void setSesion(Sesion sesion)
	{
		this.sesion = sesion;
	}

	public static List<PreciosSesionDTO> listaPreciosSesionToListaPreciosSesionDTO(List<PreciosSesion> listaPreciosSesion)
	{
		List<PreciosSesionDTO> listaPreciosSesionDTO = new ArrayList<PreciosSesionDTO>();
		if (listaPreciosSesion != null)
		{
			for (PreciosSesion preciosSesion : listaPreciosSesion)
				listaPreciosSesionDTO.add(PreciosSesion.precioSesionToPrecioSesionDTO(preciosSesion));
		}

		return listaPreciosSesionDTO;
	}

	public static List<PreciosSesion> listaPreciosSesionDTOToListaPreciosSesion(List<PreciosSesionDTO> listaPreciosSesionDTO)
	{
		List<PreciosSesion> listaPreciosSesion = new ArrayList<PreciosSesion>();
		if (listaPreciosSesionDTO != null)
		{
			for (PreciosSesionDTO preciosSesionDTO : listaPreciosSesionDTO)
				listaPreciosSesion.add(PreciosSesion.precioSesionDTOToPrecioSesion(preciosSesionDTO));
		}

		return listaPreciosSesion;
	}

	public static PreciosSesionDTO precioSesionToPrecioSesionDTO(PreciosSesion preciosSesion)
	{
		PreciosSesionDTO preciosSesionDTO = new PreciosSesionDTO();
		preciosSesionDTO.setPrecio(preciosSesion.getPrecio());
		preciosSesionDTO.setParTarifa(Tarifa.toDTO(preciosSesion.getTarifa()));
		preciosSesionDTO.setParLocalizacione(Localizacion.localizacionToLocalizacionDTO(preciosSesion.getLocalizacion()));

		if (preciosSesion.getSesion() != null)
		{
			SesionDTO sesionDTO = new SesionDTO();
			sesionDTO.setId(preciosSesion.getSesion().getId());
			preciosSesionDTO.setParSesione(sesionDTO);
		}

		return preciosSesionDTO;
	}

	public static PreciosSesion precioSesionDTOToPrecioSesion(PreciosSesionDTO preciosSesionDTO)
	{
		PreciosSesion preciosSesion = new PreciosSesion();
		preciosSesion.setPrecio(preciosSesionDTO.getPrecio());
		preciosSesion.setTarifa(Tarifa.tarifaDTOToTarifa(preciosSesionDTO.getParTarifa()));
		preciosSesion.setLocalizacion(Localizacion.localizacionDTOtoLocalizacion(preciosSesionDTO.getParLocalizacione()));

		if (preciosSesionDTO.getParSesione() != null)
		{
			Sesion sesion = new Sesion();
			sesion.setId(preciosSesionDTO.getId());
			preciosSesion.setSesion(sesion);
		}

		return preciosSesion;
	}

	public Tarifa getTarifa()
	{
		return tarifa;
	}

	public void setTarifa(Tarifa tarifa)
	{
		this.tarifa = tarifa;
	}
}
