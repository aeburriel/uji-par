package com.fourtic.paranimf.entradas.dump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.db.ButacaDao;
import com.google.gson.Gson;
import com.google.inject.Inject;

public class ButacasBackup
{
    public static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");

    @Inject
    Context context;

    @Inject
    ButacaDao butacasDao;

    private Gson gson;

    public ButacasBackup()
    {
        gson = new Gson();
    }

    public void guardaEntradas(int sesionId) throws SQLException, UnsupportedEncodingException, IOException
    {
        File appDir = context.getExternalFilesDir(null);
        
        // El almacenamiento externo no está montado
        if (appDir == null)
        {
            return;
        }
        
        File fich = new File(appDir, getNombreTimestamp());
        FileOutputStream fos = new FileOutputStream(fich);

        List<Butaca> butacas = butacasDao.getButacas(sesionId);

        fos.write(gson.toJson(butacas).getBytes("UTF-8"));
        fos.close();
    }

    synchronized private String getNombreTimestamp()
    {
        return FORMAT.format(new Date()) + ".json";
    }

}
