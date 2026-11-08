package com.example.regalossweet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Categorias extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias); // nombre real de tu XML

        findViewById(R.id.cardComidaRapida).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Rapido.class)));

        findViewById(R.id.cardCombos).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Rapido.class)));

        findViewById(R.id.cardPostresBebidas).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Rapido.class)));

        findViewById(R.id.cardPasteles).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Personalizado.class)));

        findViewById(R.id.cardDesayunos).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Personalizado.class)));

        findViewById(R.id.cardRegalos).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Personalizado.class)));

        findViewById(R.id.cardRamos).setOnClickListener(v ->
                startActivity(new Intent(Categorias.this, Pedido_Personalizado.class)));
    }
}