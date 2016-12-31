package com.fourtic.paranimf.entradas.activity;

import java.sql.SQLException;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BaseNormalActivity;
import com.fourtic.paranimf.entradas.adapter.EventosListAdapter;
import com.fourtic.paranimf.entradas.constants.Constants;
import com.fourtic.paranimf.entradas.data.Evento;
import com.fourtic.paranimf.entradas.db.EventoDao;
import com.fourtic.paranimf.entradas.network.EstadoRed;
import com.fourtic.paranimf.entradas.sync.SincronizadorEventos;
import com.fourtic.paranimf.entradas.sync.SincronizadorEventos.SyncCallback;
import com.google.inject.Inject;

public class EventosActivity extends BaseNormalActivity
{
    @InjectView(R.id.eventos)
    private ListView eventosList;

    @InjectView(R.id.sinEventos)
    private ViewGroup sinEventos;
    
    @InjectView(R.id.eventos_empty_text)
    private TextView eventosEmptyText;
    
    @InjectView(R.id.filter_text)
    private TextView filterText;
    
    @InjectView(R.id.settings_empty_text)
    private TextView settingsEmptyText;

    @Inject
    private EventoDao eventoDao;

    @Inject
    private SincronizadorEventos sync;

    @Inject
    private EstadoRed network;

    private EventosListAdapter adapter;
    
    boolean isChecked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.eventos_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        setTitle(R.string.title_eventos);

        iniciaList();
    }

    private void iniciaList()
    {
        adapter = new EventosListAdapter(this);
        eventosList.setAdapter(adapter);

        eventosList.setEmptyView(sinEventos);

        eventosList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position < eventosList.getAdapter().getCount())
                {
                    Evento evento = (Evento) eventosList.getAdapter().getItem(position);

                    abreActivitySesiones(evento);
                }
            }
        });
        eventosList.setFastScrollEnabled(true);
    }

    protected void abreActivitySesiones(Evento evento)
    {
        Intent intent = new Intent(this, SesionesActivity.class);
        intent.putExtra(Constants.EVENTO_ID, evento.getId());
        intent.putExtra(Constants.EVENTO_TITULO, evento.getTitulo());

        startActivity(intent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        
        cargaEventosDesdeBd();
    }
    
    @Override
    protected void onResume() 
    {
    	super.onResume();
    	
        if (isEmptyURLOrAPIKey())
        {
        	filterText.setVisibility(View.GONE);
        	eventosEmptyText.setVisibility(View.GONE);
        	settingsEmptyText.setVisibility(View.VISIBLE);
        }
        else
        {
        	filterText.setVisibility(View.VISIBLE);
        	eventosEmptyText.setVisibility(View.VISIBLE);
        	settingsEmptyText.setVisibility(View.GONE);
        }
    }

    private void cargaEventosDesdeBd()
    {
        try
        {
            adapter.update(eventoDao.getEventos());
            if (isChecked)
            	adapter.getFilter().filter("");
        }
        catch (SQLException e)
        {
            Log.e(Constants.TAG, getString(R.string.error_recuperando_eventos_bd), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
    	getSupportMenuInflater().inflate(R.menu.settings, menu);
    	getSupportMenuInflater().inflate(R.menu.eventos, menu);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.action_filter);
        checkable.setChecked(isChecked);
        filterText.setText(R.string.filter_on);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_sync:
            
            if (network.estaActiva())
            {
            	if (isEmptyURLOrAPIKey())
            		muestraError(getString(R.string.no_url_apikey));
            	else
            		sincroniza();
            }
            else
                muestraError(getString(R.string.conexion_red_no_disponible));

            return true;
        case R.id.action_settings:
            
        	Intent settingsIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsIntent);

            return true;
        case R.id.action_filter:
        	isChecked = !item.isChecked();
            item.setChecked(isChecked);
            if (isChecked)
            	filterText.setText(R.string.filter_on);
            else
            	filterText.setText(R.string.filter_off);
        	cargaEventosDesdeBd();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private boolean isEmptyURLOrAPIKey()
    {
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	String host = sharedPref.getString(SettingsActivity.PREF_HOST, "");
    	String apiKey = sharedPref.getString(SettingsActivity.PREF_APIKEY, "");
    	
    	return host == null || host.length() == 0 || apiKey == null || apiKey.length() == 0;
    }

    private void sincroniza()
    {
        muestraProgreso();

        sync.actualizaEventosDesdeRest(new SyncCallback()
        {
            @Override
            public void onSuccess()
            {
                muestraMensaje(getString(R.string.actualizado));
                cargaEventosDesdeBd();

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

}
