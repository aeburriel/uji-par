package com.fourtic.paranimf.entradas.activity.base;

import cz.msebera.android.httpclient.conn.HttpHostConnectException;

import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.crashlytics.android.Crashlytics;
import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.EventosActivity;
import com.fourtic.paranimf.entradas.constants.Constants;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseNormalActivity extends RoboSherlockFragmentActivity implements BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setSupportProgressBarIndeterminateVisibility(false);

        if (getSupportActionBar() != null)
            getSupportActionBar().setHomeButtonEnabled(true);

        //        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy()
    {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    public void ocultaProgreso()
    {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void muestraProgreso()
    {
        setSupportProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void showError(Throwable exception)
    {
        BaseActivityHelper.showError(this, exception);
    }

    public void muestraError(String errorMessage)
    {
        BaseActivityHelper.showError(this, errorMessage);
    }

    private void goToHome()
    {
        Intent intent = new Intent(this, EventosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    protected void gestionaError(String message, Throwable e)
    {
        Crashlytics.logException(e);
        
        Log.e(Constants.TAG, message, e);

        if (e instanceof HttpHostConnectException)
        {
            message += "\n(" + getString(R.string.error_de_conexion) + ")";
        }

        muestraError(message);
    }

    protected void muestraMensaje(String message)
    {
        BaseActivityHelper.showMessage(this, message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            goToHome();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void showConfirmDialog(String titulo, String mensaje, OnClickListener okListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
    
        builder.setTitle(titulo).setMessage(mensaje).setPositiveButton(android.R.string.yes, okListener)
                .setNegativeButton(android.R.string.cancel, null);
    
        builder.create().show();
    }

}
