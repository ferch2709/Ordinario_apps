package com.example.verthorizon;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CicloDeVida";

    private EditText etDato;
    private Button btnMostrar;
    private TextView tvResultado;

    private void mostrarMensaje(String metodo) {
        Log.d(TAG, metodo);
        Toast.makeText(this, metodo, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mostrarMensaje("onCreate()");

        etDato = findViewById(R.id.etDato);
        btnMostrar = findViewById(R.id.btnMostrar);
        tvResultado = findViewById(R.id.tvResultado);

        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String texto = etDato.getText().toString();
                tvResultado.setText("Dato capturado: " + texto);
            }
        });

        // NOTA: Aquí quitamos la restauración para que ocurra el error
    }

    // NOTA: Aquí quitamos onSaveInstanceState para que NO se guarde nada

    @Override
    protected void onStart() {
        super.onStart();
        mostrarMensaje("onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarMensaje("onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mostrarMensaje("onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mostrarMensaje("onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mostrarMensaje("onDestroy()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mostrarMensaje("onRestart()");
    }
}