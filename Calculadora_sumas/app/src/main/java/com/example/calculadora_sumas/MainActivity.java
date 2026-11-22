package com.example.calculadora_sumas;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText num1;
    EditText num2;
    Button suma;
    TextView resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        num1 = findViewById(R.id.editTextNumber);
        num2 = findViewById(R.id.editTextNumber2);
        suma = findViewById(R.id.button);
        resultado = findViewById(R.id.resultado);

        suma.setOnClickListener(e -> {
            String texto1 = num1.getText().toString();
            String texto2 = num2.getText().toString();

            if (!texto1.isEmpty() && !texto2.isEmpty()) {
                double n1 = Double.parseDouble(texto1);
                double n2 = Double.parseDouble(texto2);
                double res = n1 + n2;

                resultado.setText("Resultado: " + res);
            } else {
                resultado.setText("Ingresa ambos números");
            }
        });
    }
}