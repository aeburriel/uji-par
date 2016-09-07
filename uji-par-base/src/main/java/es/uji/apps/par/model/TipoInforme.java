package es.uji.apps.par.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TipoInforme {
    private String id;
    private String nombre;
    private String nombreCA;
    private String nombreES;
	private String prefix;
	private String suffix;

    public TipoInforme()
    {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreCA() {
        return nombreCA;
    }

    public void setNombreCA(String nombreCA) {
        this.nombreCA = nombreCA;
    }

    public String getNombreES() {
        return nombreES;
    }

    public void setNombreES(String nombreES) {
        this.nombreES = nombreES;
    }

	public static String getDefaultGenerales() {
		return "[{\"id\":\"excelTaquilla\",\"nombreCA\":\"Excel de taquilla\",\"nombreES\":\"Excel de taquilla\", " +
				"\"prefix\": \"taquilla/\", \"suffix\": \"\"},{\"id\":\"excelEventos\",\"nombreCA\":\"Excel dels events\"," +
				"\"nombreES\":\"Excel de los eventos\", \"prefix\": \"eventos/\", \"suffix\": \"\"},{\"id\":\"pdfTaquilla\"," +
				"\"nombreCA\":\"PDF de taquilla\",\"nombreES\":\"PDF de taquilla\", \"prefix\": \"taquilla/\", " +
				"\"suffix\": \"/pdf\"},{\"id\":\"pdfEfectiu\",\"nombreCA\":\"PDF de taquilla en efectiu\"," +
				"\"nombreES\":\"PDF de taquilla en efectivo\", \"prefix\": \"taquilla/\", \"suffix\": \"/efectivo/pdf\"}," +
				"{\"id\":\"pdfTpv\",\"nombreCA\":\"PDF de TPVs taquilla\",\"nombreES\":\"PDF de TPVs taquilla\", " +
				"\"prefix\": \"taquilla/\", \"suffix\": \"/tpv/pdf\"}, " +
				"{\"id\":\"pdfTpvOnline\",\"nombreCA\":\"PDF de TPVs online\",\"nombreES\":\"PDF de TPVs online\", " +
				"\"prefix\": \"taquilla/\", \"suffix\": \"/tpv/online/pdf\"}, " +
				"{\"id\":\"pdfSGAE\",\"nombreCA\":\"PDF SGAE\",\"nombreES\":\"PDF SGAE\", \"prefix\": \"taquilla/\", \"suffix\": \"/eventos/pdf\"}]";
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}
