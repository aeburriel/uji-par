package com.fourtic.paranimf.entradas.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BaseNormalActivity;

import roboguice.inject.InjectView;

public class LectorExternoActivity extends BaseNormalActivity
{
    @InjectView(R.id.inicieLectura)
    private TextView inicieLecturaTexto;

    @InjectView(R.id.input)
    private TextView input;

    String inputResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lector_externo_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        setTitle(R.string.title_lector_externo);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        inicieLecturaTexto.startAnimation(anim);

        iniciaBuscar();
    }

    private void iniciaBuscar()
    {
        input.requestFocus();
        input.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if (!b)
                {
                    input.requestFocus();
                }
            }
        });

        input.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                inputResult = s.toString();
                boolean end = inputResult.endsWith("\n");
                if (end)
                {
                    inputResult = inputResult.substring(0, Math.max(inputResult.length() - 1, 0));
                    Intent result = new Intent(inputResult, null);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }
        });
    }
}
