package com.example.fragmentado; // Tu paquete

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Button btn1, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btnFragment1);
        btn2 = findViewById(R.id.btnFragment2);
        btn3 = findViewById(R.id.btnFragment3);

        // Muestra el Fragmento 1 por defecto al abrir la app
        if (savedInstanceState == null) {
            cargarFragmento(new FragmentUno());
        }

        // Clicks para cambiar de fragmento
        btn1.setOnClickListener(v -> cargarFragmento(new FragmentUno()));
        btn2.setOnClickListener(v -> cargarFragmento(new FragmentDos()));
        btn3.setOnClickListener(v -> cargarFragmento(new FragmentTres()));
    }

    private void cargarFragmento(Fragment fragmento) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragmento);
        transaction.commit();
    }
}