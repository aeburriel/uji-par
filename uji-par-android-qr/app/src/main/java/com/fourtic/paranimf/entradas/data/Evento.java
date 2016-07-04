package com.fourtic.paranimf.entradas.data;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable
public class Evento
{
    @DatabaseField(columnName = "id", id = true)
    private int id;

    @SerializedName("tituloEs")
    @DatabaseField(columnName = "titulo")
    private String titulo;
    
    @ForeignCollectionField
    private ForeignCollection<Sesion> sesionesLazy;

    private List<Sesion> sesiones;

    private boolean modificado;

    public Evento()
    {
        sesiones = new ArrayList<Sesion>();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }

    public List<Sesion> getSesiones()
    {
        return sesiones;
    }

    public void setSesiones(List<Sesion> sesiones)
    {
        this.sesiones = sesiones;
    }

    public void setModificado(boolean modificado)
    {
        this.modificado = modificado;
    }

    public boolean getModificado()
    {
        return this.modificado;
    }

	public ForeignCollection<Sesion> getSesionesLazy() {
		return sesionesLazy;
	}

	public void setSesionesLazy(ForeignCollection<Sesion> sesionesLazy) {
		this.sesionesLazy = sesionesLazy;
	}
}
