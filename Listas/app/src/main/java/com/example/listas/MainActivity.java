package com.example.listas; // Cambia esto por el nombre de tu paquete

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewUsuarios);

        // ==========================================
        // 3. DEFINIR EL POSICIONAMIENTO (LayoutManager)
        // ==========================================
        // Le indica al RecyclerView cómo organizarse en pantalla:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));

        // Datos para llenar la lista
        List<Usuario> lista = new ArrayList<>();
        lista.add(new Usuario("Ana Pérez", "ana@example.com"));
        lista.add(new Usuario("Carlos Gómez", "carlos@example.com"));
        lista.add(new Usuario("Lucía Torres", "lucia@example.com"));

        // Asignar el adaptador
        UsuarioAdapter adapter = new UsuarioAdapter(lista);
        recyclerView.setAdapter(adapter);
    }
}