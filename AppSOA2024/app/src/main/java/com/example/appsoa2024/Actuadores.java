package com.example.appsoa2024;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Actuadores extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actuadores);

        // Configurar el botón Atrás
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Esto cerrará la actividad actual y volverá a la anterior
            }
        });

        // Configurar los botones del Buzzer
        Button btnBuzzerOn = findViewById(R.id.btnBuzzerOn);
        btnBuzzerOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para encender el buzzer
            }
        });

        Button btnBuzzerOff = findViewById(R.id.btnBuzzerOff);
        btnBuzzerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el buzzer
            }
        });

        // Configurar los botones del Relé
        Button btnReleOn = findViewById(R.id.btnReleOn);
        btnReleOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para encender el relé
            }
        });

        Button btnReleOff = findViewById(R.id.btnReleOff);
        btnReleOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para apagar el relé
            }
        });

    }

}
