package com.fourtic.paranimf.entradas.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BaseNormalActivity;
import com.fourtic.paranimf.entradas.constants.Constants;
import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.db.ButacaDao;
import com.fourtic.paranimf.entradas.db.SesionDao;
import com.fourtic.paranimf.entradas.exception.ButacaDeOtraSesionException;
import com.fourtic.paranimf.entradas.exception.ButacaNoEncontradaException;
import com.fourtic.paranimf.entradas.network.EstadoRed;
import com.fourtic.paranimf.entradas.rest.RestService;
import com.fourtic.paranimf.entradas.scan.ResultadoScan;
import com.fourtic.paranimf.entradas.sync.SincronizadorButacas;
import com.fourtic.paranimf.entradas.sync.SincronizadorButacas.SyncCallback;
import com.fourtic.paranimf.utils.Utils;
import com.google.inject.Inject;
import com.google.zxing.integration.android.IntentIntegrator;

import java.sql.SQLException;
import java.util.Date;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class SesionInfoActivity extends BaseNormalActivity
{
    private static final String BARCODE_SCANNER_PACKAGE = "com.google.zxing.client.android";

    private static final int EXTERNAL_SCANNER_REQUEST = 1;
    private static final int BARCODE_SCANNER_REQUEST = 49374;

    @Inject
    private ButacaDao butacaDao;

    @Inject
    private SesionDao sesionDao;

    @Inject
    private SincronizadorButacas sync;

    @InjectView(R.id.eventoTitulo)
    private TextView textEventoTitulo;

    @InjectView(R.id.sesionFecha)
    private TextView textSesionFecha;

    @InjectView(R.id.numeroPresentadas)
    private TextView textNumeroPresentadas;

    @InjectView(R.id.numeroVendidas)
    private TextView textNumeroVendidas;

    @InjectView(R.id.textFaltanSubir)
    private TextView textFaltanSubir;

    @InjectView(R.id.mensaje)
    private TextView textMensaje;

    @InjectView(R.id.escaneaButton)
    private Button escanearBoton;

    @InjectView(R.id.manualButton)
    private Button manualBoton;

    @InjectExtra(value = Constants.EVENTO_TITULO)
    private String eventoTitulo;

    @InjectExtra(value = Constants.SESION_HORA)
    private String sesionHora;

    @InjectExtra(value = Constants.SESION_FECHA)
    private long sesionFechaEpoch;

    @InjectExtra(value = Constants.SESION_ID)
    private int sesionId;

    @Inject
    private EstadoRed red;

    @Inject
    private EstadoRed network;

    @Inject
    private RestService rest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sesion_info_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        iniciaBotones();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        actualizaInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.sesion_info, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_sync:

            if (red.estaActiva())
            {
                sincroniza();
            }
            else
            {
                muestraError(getString(R.string.conexion_red_no_disponible));
            }

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void sincroniza()
    {
        try
        {
            if (red.estaActiva())
                sincronizaButacas();
            else
                muestraError(getString(R.string.conexion_red_no_disponible));
        }
        catch (SQLException e)
        {
            gestionaError(getString(R.string.error_sincronizando_entradas), e);
        }
    }

    private void iniciaBotones()
    {

        escanearBoton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                abreActividadEscanear();
            }
        });

        manualBoton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                abreActividadManual();
            }
        });
    }

    private void sincronizaButacas() throws SQLException
    {
        muestraProgreso();

        sync.sincronizaButacasDesdeRest(sesionId, new SyncCallback()
        {
            @Override
            public void onSuccess()
            {
                actualizaInfo();
                muestraMensaje(getString(R.string.sincronizado));
                ocultaProgreso();
            }

            @Override
            public void onError(Throwable e, String errorMessage)
            {
                gestionaError(errorMessage, e);
                ocultaProgreso();
            }
        });
    }

    protected void abreActividadEscanear()
    {
        if (this.rest.hasExtScan())
        {
            Intent intent = new Intent(this, LectorExternoActivity.class);
            startActivityForResult(intent, EXTERNAL_SCANNER_REQUEST);
        }
        else
        {
            if (aplicacionInstalada(BARCODE_SCANNER_PACKAGE))
            {
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
            } else
            {
                Toast.makeText(this, R.string.instalar_barcode_scanner, Toast.LENGTH_LONG).show();
                abrirGooglePlay(BARCODE_SCANNER_PACKAGE);
            }
        }
    }

    private void abrirGooglePlay(String appPackage)
    {
        try
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackage)));
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="
                    + appPackage)));
        }
    }

    protected void abreActividadManual()
    {
        Intent intent = new Intent(this, EntradaManualActivity.class);
        intent.putExtra(Constants.SESION_ID, sesionId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == EXTERNAL_SCANNER_REQUEST || requestCode == BARCODE_SCANNER_REQUEST) && resultCode == Activity.RESULT_OK)
        {
        	try
            {
                String scanningResult;
                if (requestCode == BARCODE_SCANNER_REQUEST)
                {
                    scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data).getContents();
                }
                else
                {
                    scanningResult = data.getAction();
                }

            	if (scanningResult != null) {
            		procesCodigoBarras(scanningResult);
            	}
            	else{
            	    Toast toast = Toast.makeText(getApplicationContext(), 
            	        "No scan data received!", Toast.LENGTH_SHORT);
            	    toast.show();
            	}
            }
            catch (Exception e)
            {
                gestionaError(getString(R.string.error_escaneando_entrada), e);
            }
        }
    }

    private void procesCodigoBarras(String uuid) throws SQLException
    {
        abreActividadEscanear();

        try
        {
            final Butaca butaca = butacaDao.getButacaPorUuid(sesionId, uuid);

            if (butaca.getFechaPresentada() == null)
            {
                butaca.setFechaPresentada(new Date());
                butaca.setFechaPresentadaEpoch(butaca.getFechaPresentada().getTime());
                if (network.estaActiva())
                {
                    sync.subeButacaOnline(sesionId, butaca, new SyncCallback()
                    {
                        @Override
                        public void onSuccess()
                        {
                            presentaEntrada(butaca);
                        }

                        @Override
                        public void onError(Throwable e, String errorMessage)
                        {
                            muestraDialogoResultadoScan(errorMessage, ResultadoScan.ERROR);
                        }
                    });
                }
                else {
                    presentaEntrada(butaca);
                }
            }
            else
            {
                muestraDialogoResultadoScan(getString(R.string.ya_presentada) + Utils.formatDateWithTime(butaca.getFechaPresentada()), ResultadoScan.ERROR);
            }
        }
        catch (ButacaNoEncontradaException e)
        {
            muestraDialogoResultadoScan(getString(R.string.entrada_no_sesion), ResultadoScan.ERROR);
        }
        catch (ButacaDeOtraSesionException e)
        {
            muestraDialogoResultadoScan(getString(R.string.entrada_otra_sesion), ResultadoScan.ERROR);
        }
    }

    private void presentaEntrada(Butaca butaca)
    {
        try
        {
            butacaDao.actualizaFechaPresentada(butaca.getUuid(), butaca.getFechaPresentada());

            if (butaca.getTipo().equals("descuento"))
                muestraDialogoResultadoScan(getString(R.string.entrada_descuento), ResultadoScan.DESCUENTO);
            else
                muestraDialogoResultadoScan(getString(R.string.entrada_ok), ResultadoScan.OK);
        }
        catch (SQLException e) {
            muestraDialogoResultadoScan(getString(R.string.error_marcando_presentada), ResultadoScan.ERROR);
        }
    }

    private void muestraDialogoResultadoScan(String message, ResultadoScan resultado)
    {
        Intent intent = new Intent(this, ResultadoScanActivity.class);
        intent.putExtra(Constants.DIALOG_MESSAGE, message);
        intent.putExtra(Constants.SCAN_RESULT, resultado.ordinal());

        startActivity(intent);
    }

    private void actualizaInfo()
    {
        try
        {
            textEventoTitulo.setText(eventoTitulo);
            textSesionFecha.setText(Utils.formatDate(new Date(sesionFechaEpoch)) + " " + sesionHora);

            textNumeroVendidas.setText(Long.toString(butacaDao.getNumeroButacas(sesionId)));
            textNumeroPresentadas.setText(Long.toString(butacaDao.getNumeroButacasPresentadas(sesionId)));

            long modificadas = butacaDao.getNumeroButacasModificadas(sesionId);

            textFaltanSubir.setVisibility(modificadas == 0 ? View.INVISIBLE : View.VISIBLE);

            Date lastSync = sesionDao.getFechaSincronizacion(sesionId);

            if (lastSync == null)
            {
                textMensaje.setText(R.string.no_sincronizada);
            }
            else
            {
                textMensaje.setText(getString(R.string.ultima_sinc) + Utils.formatDateWithTime(lastSync));
            }
        }
        catch (Exception e)
        {
            gestionaError(getString(R.string.error_recuperando_datos_sesion), e);
        }
    }

    private boolean aplicacionInstalada(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }
}
