package com.fourtic.paranimf.entradas.activity;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BaseNormalActivity;
import com.fourtic.paranimf.entradas.adapter.ButacasListAdapter;
import com.fourtic.paranimf.entradas.constants.Constants;
import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.db.ButacaDao;
import com.google.inject.Inject;

public class EntradaManualActivity extends BaseNormalActivity
{
    @InjectView(R.id.manualButacas)
    private ListView butacasList;

    @InjectView(R.id.manualBuscar)
    private TextView buscar;

    @InjectExtra(value = Constants.SESION_ID)
    private int sesionId;

    @Inject
    private ButacaDao butacaDao;

    private ButacasListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrada_manual_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        setTitle(R.string.title_entrada_manual);

        iniciaList();
        iniciaBuscar();
    }

    private void iniciaList()
    {
        adapter = new ButacasListAdapter(this);
        butacasList.setAdapter(adapter);

        butacasList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position < butacasList.getAdapter().getCount())
                {
                    Butaca butaca = (Butaca) butacasList.getAdapter().getItem(position);

                    muestraDialogoMarcar(butaca);
                }
            }
        });
    }

    private void iniciaBuscar()
    {
        buscar.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                cargaButacasDesdeBd();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
    }

    protected void muestraDialogoMarcar(final Butaca butaca)
    {
        String mensaje = getString(R.string.marcar_como_presentada, butaca.getUltimoBloqueUuid());

        showConfirmDialog(null, mensaje, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int arg1)
            {
                try
                {
                    marcaComoPresentada(butaca);
                    cargaButacasDesdeBd();
                }
                catch (SQLException e)
                {
                    gestionaError(getString(R.string.error_marcando_presentada), e);
                }
            }
        });
    }

    protected void marcaComoPresentada(Butaca butaca) throws SQLException
    {
        butacaDao.actualizaFechaPresentada(butaca.getUuid(), new Date());
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        cargaButacasDesdeBd();
    }

    private void cargaButacasDesdeBd()
    {
        try
        {
            List<Butaca> butacas = butacaDao.getButacasNoPresentadasPorUuid(sesionId, buscar.getText().toString());

            adapter.update(butacas);
        }
        catch (SQLException e)
        {
            Log.e(Constants.TAG, getString(R.string.error_recuperando_eventos_bd), e);
        }
    }

}
