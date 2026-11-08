package com.example.regalossweet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Agenda_Entregas extends AppCompatActivity {

    private CalendarView cvAgenda;
    private RecyclerView rvEntregasAgenda;
    private DatabaseHelper dbHelper;
    private EntregaAdapter adapter;
    private List<PedidoItem> listaPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_entregas);

        dbHelper = new DatabaseHelper(this);
        listaPedidos = new ArrayList<>();

        // Vinculación de vistas del layout principal
        cvAgenda = findViewById(R.id.cvAgenda);
        rvEntregasAgenda = findViewById(R.id.rvEntregasAgenda);

        // Configuración del RecyclerView
        rvEntregasAgenda.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntregaAdapter(listaPedidos);
        rvEntregasAgenda.setAdapter(adapter);

        // Consultar los pedidos del día de hoy al abrir
        Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        cargarEntregas(anio, mes, dia);

        // Listener para actualizar al hacer clic en otra fecha
        if (cvAgenda != null) {
            cvAgenda.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                cargarEntregas(year, month, dayOfMonth);
            });
        }
    }

    private void cargarEntregas(int year, int month, int dayOfMonth) {
        listaPedidos.clear();

        // Formato YYYY-MM-DD igual al usado en la base de datos
        String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta filtrada por fecha a la tabla Pedidos
        String query = "SELECT " + DatabaseHelper.COL_PED_CLIENTE + ", "
                + DatabaseHelper.COL_PED_CATEGORIA + ", "
                + DatabaseHelper.COL_PED_HORA + ", "
                + DatabaseHelper.COL_PED_SALDO + ", "
                + DatabaseHelper.COL_PED_DIRECCION
                + " FROM " + DatabaseHelper.TABLE_PEDIDOS
                + " WHERE " + DatabaseHelper.COL_PED_FECHA + " = ?"
                + " ORDER BY " + DatabaseHelper.COL_PED_HORA + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{fechaSeleccionada});

        if (cursor.moveToFirst()) {
            do {
                String cliente = cursor.getString(0);
                String categoria = cursor.getString(1);
                String hora = cursor.getString(2);
                double saldo = cursor.getDouble(3);
                String direccion = cursor.getString(4);

                listaPedidos.add(new PedidoItem(cliente, categoria, hora, saldo, direccion));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
    }

    // Modelo de datos interno para cada renglón
    private static class PedidoItem {
        String cliente, categoria, hora, direccion;
        double saldo;

        PedidoItem(String cliente, String categoria, String hora, double saldo, String direccion) {
            this.cliente = cliente;
            this.categoria = categoria;
            this.hora = hora;
            this.saldo = saldo;
            this.direccion = direccion;
        }
    }

    // Adaptador que usa el layout nativo de Android
    private class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.ViewHolder> {
        private final List<PedidoItem> items;

        EntregaAdapter(List<PedidoItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PedidoItem item = items.get(position);

            // Primera línea de texto: Hora y Cliente
            holder.text1.setText(item.hora + " hrs - " + item.cliente);

            // Segunda línea de texto: Categoria, Ubicación y Saldo Pendiente
            String ubicacion = (item.direccion != null && !item.direccion.isEmpty()) ? item.direccion : "Entrega en local";
            String detalle = item.categoria + " | " + ubicacion + " | Saldo: $" + String.format(Locale.getDefault(), "%.2f", item.saldo);

            holder.text2.setText(detalle);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}