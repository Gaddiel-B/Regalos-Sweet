package com.example.regalossweet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Vistas del Header
    private TextView tvHeaderTitle, tvHeaderDate;

    // Vistas del Resumen del Día
    private TextView tvResumenPedidos, tvResumenPorCobrar, tvResumenStock;

    // Contenedores/Tarjetas de Módulos de Gestión
    private LinearLayout cardNuevoPedido, cardAgenda, cardInventario, cardRecetario;

    // Botón Ver Más Entregas y Botón Flotante
    private TextView btnVerMasEntregas, btnNuevoPedidoFlotante;

    // Contenedores completos de las tarjetas de entrega (para ocultarlas/mostrarlas)
    private ConstraintLayout cardDelivery1, cardDelivery2;

    // Vistas internas de Próximas Entregas (Item 1 y 2)
    private TextView tvDeliveryTime1, tvDeliveryDetails1, btnEntregado1;
    private TextView tvDeliveryTime2, tvDeliveryDetails2, btnEntregado2;

    // Base de datos
    private DatabaseHelper dbHelper;

    //Menú CRUD
    private TextView btnMenuHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        inicializarVistas();
        configurarNavegacion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarResumenDelDia();
        cargarProximasEntregas();
    }

    private void inicializarVistas() {
        // Header
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderDate = findViewById(R.id.tvHeaderDate);

        // Resumen del Día
        tvResumenPedidos = findViewById(R.id.tvResumenPedidos);
        tvResumenPorCobrar = findViewById(R.id.tvResumenPorCobrar);
        tvResumenStock = findViewById(R.id.tvResumenStock);

        // Módulos de Gestión
        cardNuevoPedido = findViewById(R.id.cardNuevoPedido);
        cardAgenda = findViewById(R.id.cardAgenda);
        cardInventario = findViewById(R.id.cardInventario);

        // Botones de acción general
        btnVerMasEntregas = findViewById(R.id.btnVerMasEntregas);
        btnNuevoPedidoFlotante = findViewById(R.id.btnNuevoPedidoFlotante);

        cardDelivery1 = findViewById(R.id.cardDelivery1);
        cardDelivery2 = findViewById(R.id.cardDelivery2);

        // Vistas de la Entrega 1
        tvDeliveryTime1 = findViewById(R.id.tvDeliveryTime1);
        tvDeliveryDetails1 = findViewById(R.id.tvDeliveryDetails1);
        btnEntregado1 = findViewById(R.id.btnEntregado1);

        // Vistas de la Entrega 2
        tvDeliveryTime2 = findViewById(R.id.tvDeliveryTime2);
        tvDeliveryDetails2 = findViewById(R.id.tvDeliveryDetails2);
        btnEntregado2 = findViewById(R.id.btnEntregado2);

        // Menú CRUD
        btnMenuHeader = findViewById(R.id.btnMenuHeader);
    }

    private void configurarNavegacion() {
        View.OnClickListener abrirNuevoPedidoListener = v -> {
            Intent intent = new Intent(MainActivity.this, Pedido_Rapido.class);
            startActivity(intent);
        };

        View.OnClickListener abrirFormularioListener = v -> {
            Intent intent = new Intent(MainActivity.this, Categorias.class);
            startActivity(intent);
        };

        View.OnClickListener abrirInventario = v -> {
            Intent intent = new Intent(MainActivity.this, Inventario_Almacen.class);
            startActivity(intent);
        };

        View.OnClickListener abrirCatalogoMenu = v -> {
            Intent intent = new Intent(MainActivity.this, Catologo.class);
            startActivity(intent);
        };

        if (btnMenuHeader != null) {
            btnMenuHeader.setOnClickListener(abrirCatalogoMenu);
        }

        if (btnNuevoPedidoFlotante != null) {
            btnNuevoPedidoFlotante.setOnClickListener(abrirNuevoPedidoListener);
        }

        if (cardNuevoPedido != null) {
            cardNuevoPedido.setOnClickListener(abrirFormularioListener);
        }

        if(cardInventario != null){
            cardInventario.setOnClickListener(abrirInventario);
        }

        // Navegación a Agenda de Entregas
        View.OnClickListener abrirAgendaListener = v -> {
            Intent intent = new Intent(MainActivity.this, Agenda_Entregas.class);
            startActivity(intent);
        };

        if (cardAgenda != null) {
            cardAgenda.setOnClickListener(abrirAgendaListener);
        }

        if (btnVerMasEntregas != null) {
            btnVerMasEntregas.setOnClickListener(abrirAgendaListener);
        }

        if (btnNuevoPedidoFlotante != null) {
            btnNuevoPedidoFlotante.setOnClickListener(abrirNuevoPedidoListener);
        }
    }

    private void cargarResumenDelDia() {
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Total de pedidos de hoy
        String queryPedidos = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PEDIDOS +
                " WHERE " + DatabaseHelper.COL_PED_FECHA + " = ? AND " +
                DatabaseHelper.COL_PED_ESTADO + " = 'Activo'";
        Cursor cPedidos = db.rawQuery(queryPedidos, new String[]{fechaHoy});
        int totalPedidos = 0;
        if (cPedidos.moveToFirst()) {
            totalPedidos = cPedidos.getInt(0);
        }
        cPedidos.close();

        // 2. Total por cobrar hoy
        String querySaldo = "SELECT SUM(" + DatabaseHelper.COL_PED_SALDO + ") FROM " +
                DatabaseHelper.TABLE_PEDIDOS + " WHERE " + DatabaseHelper.COL_PED_FECHA +
                " = ? AND " + DatabaseHelper.COL_PED_ESTADO + " = 'Activo'";
        Cursor cSaldo = db.rawQuery(querySaldo, new String[]{fechaHoy});
        double totalSaldo = 0;
        if (cSaldo.moveToFirst()) {
            totalSaldo = cSaldo.getDouble(0);
        }
        cSaldo.close();

        // 3. Faltantes de stock
        String queryStock = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_INSUMOS +
                " WHERE " + DatabaseHelper.COL_INS_STOCK_ACTUAL + " <= " + DatabaseHelper.COL_INS_STOCK_MINIMO;
        Cursor cStock = db.rawQuery(queryStock, null);
        int insumosFaltantes = 0;
        if (cStock.moveToFirst()) {
            insumosFaltantes = cStock.getInt(0);
        }
        cStock.close();

        // Asignación a vistas UI
        if (tvResumenPedidos != null) tvResumenPedidos.setText(String.valueOf(totalPedidos));
        if (tvResumenPorCobrar != null) tvResumenPorCobrar.setText(String.format(Locale.getDefault(), "$%.2f", totalSaldo));
        if (tvResumenStock != null) tvResumenStock.setText(insumosFaltantes + " Faltan");
    }

    private void cargarProximasEntregas() {
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consultamos hasta 2 entregas activas ordenadas por hora para la pantalla principal
        String query = "SELECT " + DatabaseHelper.COL_PED_ID + ", " +
                DatabaseHelper.COL_PED_HORA + ", " +
                DatabaseHelper.COL_PED_CLIENTE + ", " +
                DatabaseHelper.COL_PED_SALDO +
                " FROM " + DatabaseHelper.TABLE_PEDIDOS +
                " WHERE " + DatabaseHelper.COL_PED_FECHA + " = ? AND " +
                DatabaseHelper.COL_PED_ESTADO + " = 'Activo'" +
                " ORDER BY " + DatabaseHelper.COL_PED_HORA + " ASC LIMIT 2";

        Cursor cursor = db.rawQuery(query, new String[]{fechaHoy});

        // Por defecto, ocultamos ambas tarjetas si no hay datos
        if (cardDelivery1 != null) cardDelivery1.setVisibility(View.GONE);
        if (cardDelivery2 != null) cardDelivery2.setVisibility(View.GONE);

        if (cursor.moveToFirst()) {
            // Mostrar y configurar la Tarjeta 1
            if (cardDelivery1 != null) cardDelivery1.setVisibility(View.VISIBLE);

            final long idPedido1 = cursor.getLong(0);
            String hora1 = cursor.getString(1);
            String cliente1 = cursor.getString(2);
            double saldo1 = cursor.getDouble(3);

            if (tvDeliveryTime1 != null) tvDeliveryTime1.setText("🕒 " + hora1 + " hrs");
            if (tvDeliveryDetails1 != null) tvDeliveryDetails1.setText("Cliente: " + cliente1 + "\nSaldo: $" + String.format(Locale.getDefault(), "%.2f", saldo1));
            if (btnEntregado1 != null) {
                btnEntregado1.setOnClickListener(v -> marcarComoEntregado(idPedido1));
            }

            // Si existe un segundo pedido para hoy
            if (cursor.moveToNext()) {
                if (cardDelivery2 != null) cardDelivery2.setVisibility(View.VISIBLE);

                final long idPedido2 = cursor.getLong(0);
                String hora2 = cursor.getString(1);
                String cliente2 = cursor.getString(2);
                double saldo2 = cursor.getDouble(3);

                if (tvDeliveryTime2 != null) tvDeliveryTime2.setText("🕒 " + hora2 + " hrs");
                if (tvDeliveryDetails2 != null) tvDeliveryDetails2.setText("Cliente: " + cliente2 + "\nSaldo: $" + String.format(Locale.getDefault(), "%.2f", saldo2));
                if (btnEntregado2 != null) {
                    btnEntregado2.setOnClickListener(v -> marcarComoEntregado(idPedido2));
                }
            }
        }
        cursor.close();
    }

    private void marcarComoEntregado(long idPedido) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_PEDIDOS +
                " SET " + DatabaseHelper.COL_PED_ESTADO + " = 'Entregado'" +
                " WHERE " + DatabaseHelper.COL_PED_ID + " = ?", new Object[]{idPedido});

        Toast.makeText(this, "Pedido marcado como entregado ✔️", Toast.LENGTH_SHORT).show();
        cargarResumenDelDia();
        cargarProximasEntregas();
    }
}