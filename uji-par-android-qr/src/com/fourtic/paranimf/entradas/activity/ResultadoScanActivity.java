package com.fourtic.paranimf.entradas.activity;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Window;
import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BaseNormalActivity;
import com.fourtic.paranimf.entradas.constants.Constants;

public class ResultadoScanActivity extends BaseNormalActivity
{
    private static final long RETARDO_OK = 2000;
    private static final long RETARDO_ERROR = 4000;

    @InjectView(R.id.dialogRoot)
    private RelativeLayout rootLayout;

    @InjectView(R.id.texto)
    private TextView textView;

    @InjectExtra(value = Constants.DIALOG_MESSAGE)
    private String message;

    @InjectExtra(value = Constants.DIALOG_ERROR)
    private boolean error;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultado_scan_activity);

        handler = new Handler();

        textView.setText(message);
        cambiaColorFondo();

        programaCierreActivity();
    }

    private void programaCierreActivity()
    {
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, error ? RETARDO_ERROR : RETARDO_OK);
    }

    private void cambiaColorFondo()
    {
        if (error)
        {
            rootLayout.setBackgroundColor(getResources().getColor(R.color.dialog_scan_error));
        }
        else
        {
            rootLayout.setBackgroundColor(getResources().getColor(R.color.dialog_scan_ok));
        }
    }
}
