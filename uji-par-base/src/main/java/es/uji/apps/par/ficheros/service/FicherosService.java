package es.uji.apps.par.ficheros.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.IncidenciaNotFoundException;
import es.uji.apps.par.SesionSinFormatoIdiomaIcaaException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.ficheros.registros.FicheroRegistros;
import es.uji.apps.par.ficheros.registros.RegistroBuzon;
import es.uji.apps.par.ficheros.registros.RegistroPelicula;
import es.uji.apps.par.ficheros.registros.RegistroSala;
import es.uji.apps.par.ficheros.registros.RegistroSesion;
import es.uji.apps.par.ficheros.registros.RegistroSesionPelicula;
import es.uji.apps.par.ficheros.registros.RegistroSesionProgramada;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;

@Service
public class FicherosService
{
    @Autowired
    private CinesDAO cinesDao;

    @Autowired
    private SalasDAO salasDAO;

    @Autowired
    private ComprasDAO comprasDAO;

    @Autowired
    private SesionesDAO sesionesDAO;

    public FicheroRegistros generaFicheroRegistros(Date fechaEnvioAnterior, String tipo, List<Sesion> sesiones) 
    		throws IncidenciaNotFoundException, UnsupportedEncodingException, SesionSinFormatoIdiomaIcaaException
    {
        FicheroRegistros ficheroRegistros = new FicheroRegistros();

        ficheroRegistros.setRegistroBuzon(generaRegistroBuzon(fechaEnvioAnterior, tipo, sesiones));
        ficheroRegistros.setRegistrosSalas(generaRegistrosSala(sesiones));
        ficheroRegistros.setRegistrosSesiones(generaRegistrosSesion(sesiones));
        ficheroRegistros.setRegistrosSesionesPeliculas(generaRegistrosSesionPelicula(sesiones));
        ficheroRegistros.setRegistrosPeliculas(generaRegistrosPelicula(sesiones));
        ficheroRegistros.setRegistrosSesionesProgramadas(generaRegistrosSesionesProgramadas(sesiones));

        ficheroRegistros.getRegistroBuzon().setLineas(
                1 + ficheroRegistros.getRegistrosSalas().size() + ficheroRegistros.getRegistrosSesiones().size()
                        + ficheroRegistros.getRegistrosSesionesPeliculas().size()
                        + ficheroRegistros.getRegistrosPeliculas().size()
                        + ficheroRegistros.getRegistrosSesionesProgramadas().size());

        return ficheroRegistros;
    }

    private RegistroBuzon generaRegistroBuzon(Date fechaEnvioAnterior, String tipo, List<Sesion> sesiones)
    {
        List<CineDTO> cines = cinesDao.getCines();
        CineDTO cine = cines.get(0);

        RegistroBuzon registroBuzon = new RegistroBuzon();

        registroBuzon.setCodigo(cine.getCodigo());
        registroBuzon.setFechaEnvioHabitualAnterior(fechaEnvioAnterior);
        Calendar cal = Calendar.getInstance();
        registroBuzon.setFechaEnvio(cal.getTime());
        registroBuzon.setTipo(tipo);
        registroBuzon.setSesiones(sesiones.size());
        registroBuzon.setRecaudacion(comprasDAO.getRecaudacionSesiones(sesiones));
        registroBuzon.setEspectadores(comprasDAO.getEspectadores(sesiones));

        // int lineasRegistroSala = salasDAO.getSalas(sesiones).size(); 

        return registroBuzon;
    }

    private List<RegistroSala> generaRegistrosSala(List<Sesion> sesiones) throws UnsupportedEncodingException
    {
        List<RegistroSala> registrosSala = new ArrayList<RegistroSala>();

        List<Sala> salas = salasDAO.getSalas(sesiones);

        for (Sala sala : salas)
        {
            RegistroSala registroSala = new RegistroSala();

            registroSala.setCodigo(sala.getCodigo());
            registroSala.setNombre(new String(Utils.stripAccents(sala.getNombre()).getBytes("US-ASCII")));

            registrosSala.add(registroSala);
        }

        return registrosSala;
    }

    private List<RegistroSesion> generaRegistrosSesion(List<Sesion> sesiones) throws IncidenciaNotFoundException
    {
        return sesionesDAO.getRegistrosSesiones(sesiones);
    }

    private List<RegistroSesionPelicula> generaRegistrosSesionPelicula(List<Sesion> sesiones) throws SesionSinFormatoIdiomaIcaaException
    {
        return sesionesDAO.getRegistrosSesionesPeliculas(sesiones);
    }

    private List<RegistroPelicula> generaRegistrosPelicula(List<Sesion> sesiones)
    {
        return sesionesDAO.getRegistrosPeliculas(sesiones);
    }

    private List<RegistroSesionProgramada> generaRegistrosSesionesProgramadas(List<Sesion> sesiones)
    {
        List<RegistroSesionProgramada> registros = new ArrayList<RegistroSesionProgramada>();

        List<SesionDTO> sesionesDTO = sesionesDAO.getSesionesOrdenadas(sesiones);

        String codigoSala = "";
        String ddmmaa = "";
        RegistroSesionProgramada registro = null;

        for (SesionDTO sesionDTO : sesionesDTO)
        {
            if (!codigoSala.equals(sesionDTO.getParSala().getCodigo())
                    || !DateUtils.formatDdmmyy(sesionDTO.getFechaCelebracion()).equals(ddmmaa))
            {
                if (registro != null)
                    registros.add(registro);

                registro = new RegistroSesionProgramada();
                registro.setCodigoSala(sesionDTO.getParSala().getCodigo());
                registro.setFechaSesion(DateUtils.formatDdmmyy(sesionDTO.getFechaCelebracion()));
                registro.setNumeroSesiones(0);

                codigoSala = sesionDTO.getParSala().getCodigo();
                ddmmaa = DateUtils.formatDdmmyy(sesionDTO.getFechaCelebracion());
            }

            registro.setNumeroSesiones(registro.getNumeroSesiones() + 1);
        }

        registros.add(registro);

        return registros;
    }
    
    /*public static byte[] encryptData(byte[] toEncrypt) throws IOException, PGPException, NoSuchProviderException {
        PGPPublicKey key = buildEncryptionKey(publicKey);
        byte[] encrypted = encrypt(toEncrypt, key);
        return encrypted;
    }
    
    
    private static PGPPublicKeyRing getKeyring(InputStream keyBlockStream) throws IOException {
        // PGPUtil.getDecoderStream() will detect ASCII-armor automatically and decode it,
        // the PGPObject factory then knows how to read all the data in the encoded stream
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyBlockStream));
     
        // these files should really just have one object in them,
        // and that object should be a PGPPublicKeyRing.
        Object o = factory.nextObject();
        if (o instanceof PGPPublicKeyRing) {
            return (PGPPublicKeyRing)o;
        }
        throw new IllegalArgumentException("Input text does not contain a PGP Public Key");
    }
 
    @SuppressWarnings("rawtypes")
	private static PGPPublicKey buildEncryptionKey(String publicKey) throws IOException, PGPException {
        PGPPublicKeyRing keyRing = getKeyring(new FileInputStream(new File(publicKey)));
        PGPPublicKey ret = null;

        Iterator kIt = keyRing.getPublicKeys();
 
        while (kIt.hasNext()) {
            ret = (PGPPublicKey) kIt.next();
 
            if (ret.isEncryptionKey()) {
                break;
            }
        }
  
        return ret;
    }
     
    private static byte[] encrypt(byte[] clearData, PGPPublicKey encKey)  throws IOException, PGPException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
         
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        //OutputStream out = new ByteArrayOutputStream();// = new ArmoredOutputStream(encOut);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
        OutputStream cos = comData.open(bOut); // open it with the final destination
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
 
        // we want to generate compressed data. This might be a user option later,
        // in which case we would pass in bOut.
        OutputStream pOut = lData.open(cos, // the compressed output stream
                PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, // "filename" to store
                clearData.length, // length of clear data
                new Date() // current time
                );
        pOut.write(clearData);
 
        lData.close();
        comData.close();
 
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
                PGPEncryptedData.CAST5, true, new SecureRandom(),
                "BC");
 
        cPk.addMethod(encKey);
 
        byte[] bytes = bOut.toByteArray();
 
        OutputStream cOut = cPk.open(encOut, bytes.length);
        cOut.write(bytes); // obtain the actual bytes from the compressed stream
        cOut.close();
        encOut.close();
 
        return encOut.toByteArray();
        //return bytes;
    }*/
    
    public byte[] encryptData(byte[] contenido) throws IOException, InterruptedException {
    	Calendar cal = Calendar.getInstance();
    	String filePath = Configuration.getTmpFolder() + "/" + DateUtils.dateToStringForFileNames(cal.getTime()); 
 
	    FileOutputStream fileOuputStream =  new FileOutputStream(filePath); 
	    fileOuputStream.write(contenido);
	    fileOuputStream.close();
	    
	    String fileEncryptedPath = doPGP(filePath);
	    FileInputStream fileInputStream = null;
        File file = new File(fileEncryptedPath);
 
        byte[] bFile = new byte[(int) file.length()];
	    fileInputStream = new FileInputStream(file);
	    fileInputStream.read(bFile);
	    fileInputStream.close();
        return bFile;
	}
    
    private String doPGP(String filePath) throws IOException, InterruptedException {
    	String pathFicheroSalida = filePath + "_cifrado";
    	String comando = "gpg --compress-algo 1 --cipher-algo cast5 --no-armor --output " 
    		+ pathFicheroSalida + " --symmetric --passphrase " + Configuration.getPassphrase() + " " + filePath;
    	Process process = Runtime.getRuntime().exec(comando);
    	process.waitFor();
    	return pathFicheroSalida;
	}
}
