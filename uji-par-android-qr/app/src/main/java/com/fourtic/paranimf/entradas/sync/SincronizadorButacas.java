package com.fourtic.paranimf.entradas.sync;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.db.ButacaDao;
import com.fourtic.paranimf.entradas.dump.ButacasBackup;
import com.fourtic.paranimf.entradas.exception.EntradaPresentadaException;
import com.fourtic.paranimf.entradas.rest.RestService;
import com.fourtic.paranimf.entradas.rest.RestService.ResultCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.conn.HttpHostConnectException;

@Singleton
public class SincronizadorButacas
{
    @Inject
    private RestService rest;

    @Inject
    private ButacaDao butacaDao;

    @Inject
    private ButacasBackup backup;

    @Inject
    private Context context;

    public interface SyncCallback
    {
        public void onSuccess();

        public void onError(Throwable e, String errorMessage);
    }

    public void sincronizaButacasDesdeRest(final int sesionId, final SyncCallback callback) throws SQLException
    {
        if (butacaDao.hayButacasModificadas(sesionId))
        {
            try
            {
                backup.guardaEntradas(sesionId);
            }
            catch (Exception e)
            {
                callback.onError(e, context.getString(R.string.error_guardando_backup_butacas));
                return;
            }

            subeButacas(sesionId, new SyncCallback()
            {
                @Override
                public void onSuccess()
                {
                    descargaButacas(sesionId, callback);
                }

                @Override
                public void onError(Throwable e, String errorMessage)
                {
                    callback.onError(e, errorMessage);
                }
            });
        }
        else
        {
            descargaButacas(sesionId, callback);
        }
    }

    private void subeButacas(int sesionId, final SyncCallback callback)
    {
        List<Butaca> butacas = null;

        try
        {
            butacas = butacaDao.getButacasModificadas(sesionId);
        }
        catch (SQLException e)
        {
            callback.onError(e, context.getString(R.string.error_consultando_butacas_movil));
        }

        rest.updatePresentadas(sesionId, butacas, new ResultCallback<Void>()
        {
            @Override
            public void onSuccess(Void successData)
            {
                callback.onSuccess();
            }

            @Override
            public void onError(Throwable throwable, String errorMessage)
            {
                callback.onError(throwable, context.getString(R.string.error_enviando_butacas_rest));
            }
        });
    }

    public void subeButacaOnline(int sesionId, Butaca butaca, final SyncCallback callback) {
        rest.updateOnlinePresentada(sesionId, butaca, new ResultCallback<Void>()
        {
            @Override
            public void onSuccess(Void successData)
            {
                callback.onSuccess();
            }

            @Override
            public void onError(Throwable throwable, String errorMessage)
            {
                if (throwable instanceof EntradaPresentadaException) {
                    callback.onError(throwable, context.getString(R.string.ya_presentada));
                }
                else {
                    callback.onSuccess();
                }
            }
        });
    }

    private void descargaButacas(final int sesionId, final SyncCallback callback)
    {
        rest.getButacas(sesionId, new ResultCallback<List<Butaca>>()
        {
            @Override
            public void onSuccess(List<Butaca> butacas)
            {
                try
                {
                    butacaDao.actualizaButacas(sesionId, butacas);

                    callback.onSuccess();
                }
                catch (SQLException e)
                {
                    callback.onError(e, context.getString(R.string.error_insertando_butacas_bd));
                }
            }

            @Override
            public void onError(Throwable e, String errorMessage)
            {
                callback.onError(e, context.getString(R.string.error_actualizando_butacas_rest));
            }
        });
    }

}
