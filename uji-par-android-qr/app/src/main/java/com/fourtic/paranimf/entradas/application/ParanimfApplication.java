package com.fourtic.paranimf.entradas.application;

import android.app.Application;
import android.content.res.Configuration;

import java.util.Locale;

public class ParanimfApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        idiomaCa();
    }

    private void idiomaCa()
    {
        Configuration config = getBaseContext().getResources().getConfiguration();

        Locale.setDefault(new Locale("es"));
        getBaseContext().getResources()
                .updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

}
