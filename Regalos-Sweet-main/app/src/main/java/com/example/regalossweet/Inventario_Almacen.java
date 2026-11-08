package com.example.regalossweet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Inventario_Almacen extends AppCompatActivity {
    private RecyclerView rvInsumos;
    private TextInputEditText etBuscar;

    private InsumoAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_almacen);

        dbHelper = new DatabaseHelper(this);

        rvInsumos = findViewById(R.id.rvInsumosAlmacen);
        etBuscar = findViewById(R.id.etBuscarInsumo);

        adapter = new InsumoAdapter();
        rvInsumos.setLayoutManager(new LinearLayoutManager(this));
        rvInsumos.setAdapter(adapter);

        // Buscador rápido: filtra mientras se escribe
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga cada vez que se vuelve a la pantalla (el stock pudo cambiar tras un pedido)
        adapter.setInsumos(consultarInsumos());
        String filtro = etBuscar.getText() != null ? etBuscar.getText().toString() : "";
        adapter.filtrar(filtro);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    /** Lee la tabla Insumos; los de stock crítico (actual <= mínimo) aparecen primero. */
    private List<Insumo> consultarInsumos() {
        List<Insumo> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT " + DatabaseHelper.COL_INS_ID + ", "
                + DatabaseHelper.COL_INS_NOMBRE + ", "
                + DatabaseHelper.COL_INS_TIPO + ", "
                + DatabaseHelper.COL_INS_UNIDAD + ", "
                + DatabaseHelper.COL_INS_STOCK_ACTUAL + ", "
                + DatabaseHelper.COL_INS_STOCK_MINIMO
                + " FROM " + DatabaseHelper.TABLE_INSUMOS
                + " ORDER BY (" + DatabaseHelper.COL_INS_STOCK_ACTUAL + " <= "
                + DatabaseHelper.COL_INS_STOCK_MINIMO + ") DESC, "
                + DatabaseHelper.COL_INS_NOMBRE;
        Cursor c = db.rawQuery(sql, null);
        try {
            while (c.moveToNext()) {
                lista.add(new Insumo(c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getDouble(4), c.getDouble(5)));
            }
        } finally {
            c.close();
        }
        return lista;
    }


    /** Insumo que necesita el pedido: cuánto requiere vs. cuánto hay en el almacén. */
    static class Requerimiento {
        final String nombre;
        final String unidad;
        final double disponible;
        double requerido;

        Requerimiento(String nombre, String unidad, double disponible) {
            this.nombre = nombre;
            this.unidad = unidad;
            this.disponible = disponible;
        }

        double getFaltante() { return requerido - disponible; }
    }

    /**
     * Consulta Receta_Insumos (unida con Insumos) y suma lo que necesita TODO el pedido
     * por insumo. Si dos productos usan la misma harina, se acumula.
     */
    private static Map<Integer, Requerimiento> calcularRequerimientos(
            SQLiteDatabase db, Map<Integer, Integer> pedido) {

        Map<Integer, Requerimiento> requeridos = new LinkedHashMap<>();
        if (pedido == null || pedido.isEmpty()) return requeridos;

        // IN (?, ?, ?) con un "?" por cada producto del pedido
        StringBuilder marcas = new StringBuilder();
        String[] args = new String[pedido.size()];
        int n = 0;
        for (Integer idProducto : pedido.keySet()) {
            marcas.append(n == 0 ? "?" : ", ?");
            args[n++] = String.valueOf(idProducto);
        }

        String sql = "SELECT r." + DatabaseHelper.COL_REC_PROD_ID
                + ", r." + DatabaseHelper.COL_REC_INS_ID
                + ", r." + DatabaseHelper.COL_REC_CANTIDAD
                + ", i." + DatabaseHelper.COL_INS_NOMBRE
                + ", i." + DatabaseHelper.COL_INS_UNIDAD
                + ", i." + DatabaseHelper.COL_INS_STOCK_ACTUAL
                + " FROM " + DatabaseHelper.TABLE_RECETA_INSUMOS + " r"
                + " INNER JOIN " + DatabaseHelper.TABLE_INSUMOS + " i"
                + " ON i." + DatabaseHelper.COL_INS_ID + " = r." + DatabaseHelper.COL_REC_INS_ID
                + " WHERE r." + DatabaseHelper.COL_REC_PROD_ID + " IN (" + marcas + ")";

        Cursor c = db.rawQuery(sql, args);
        try {
            while (c.moveToNext()) {
                int idProducto = c.getInt(0);
                int idInsumo = c.getInt(1);
                double porUnidad = c.getDouble(2);      // cantidad de insumo por 1 producto
                Integer cantidadPedida = pedido.get(idProducto);
                if (cantidadPedida == null) continue;

                Requerimiento req = requeridos.get(idInsumo);
                if (req == null) {
                    req = new Requerimiento(c.getString(3), c.getString(4), c.getDouble(5));
                    requeridos.put(idInsumo, req);
                }
                req.requerido += porUnidad * cantidadPedida;
            }
        } finally {
            c.close();
        }
        return requeridos;
    }

    /**
     * Valida el pedido contra el inventario.
     *
     * @param activity   Activity que está guardando el pedido (necesaria para el diálogo)
     * @param pedido     mapa id_producto -> cantidad pedida
     * @return true si hay stock suficiente (se puede guardar); false si falta algo.
     *         En ese caso ya mostró el diálogo y el pedido debe detenerse.
     */
    public static boolean hayStockSuficiente(Context activity, DatabaseHelper dbHelper,
                                             Map<Integer, Integer> pedido) {
        List<Requerimiento> faltantes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        for (Requerimiento r : calcularRequerimientos(db, pedido).values()) {
            if (r.requerido > r.disponible + 1e-9) faltantes.add(r);
        }

        if (faltantes.isEmpty()) return true;

        mostrarDialogoStockInsuficiente(activity, faltantes);
        return false;
    }

    private static void mostrarDialogoStockInsuficiente(Context activity, List<Requerimiento> faltantes) {
        StringBuilder sb = new StringBuilder("No se puede registrar el pedido. Faltan insumos:\n");
        for (Requerimiento f : faltantes) {
            sb.append("\n• ").append(f.nombre)
                    .append("\n   Necesario: ").append(formatearCantidad(f.requerido)).append(" ").append(f.unidad)
                    .append("\n   Disponible: ").append(formatearCantidad(f.disponible)).append(" ").append(f.unidad)
                    .append("\n   Faltan: ").append(formatearCantidad(f.getFaltante())).append(" ").append(f.unidad)
                    .append("\n");
        }
        new AlertDialog.Builder(activity)
                .setTitle("Stock insuficiente")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setMessage(sb.toString())
                .setPositiveButton("Entendido", null)
                .show();
    }

    /**
     * (Opcional) Descuenta del inventario lo que consume el pedido. Llamar DENTRO de la
     * transacción de guardado, con la misma "db" abierta con getWritableDatabase().
     */
    public static void descontarStock(SQLiteDatabase db, Map<Integer, Integer> pedido) {
        for (Map.Entry<Integer, Requerimiento> e : calcularRequerimientos(db, pedido).entrySet()) {
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_INSUMOS
                            + " SET " + DatabaseHelper.COL_INS_STOCK_ACTUAL
                            + " = " + DatabaseHelper.COL_INS_STOCK_ACTUAL + " - ?"
                            + " WHERE " + DatabaseHelper.COL_INS_ID + " = ?",
                    new Object[]{e.getValue().requerido, e.getKey()});
        }
    }

    private static String formatearCantidad(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) {
            return String.format(Locale.getDefault(), "%.0f", v);
        }
        return String.format(Locale.getDefault(), "%.2f", v);
    }

    // ====================================================================================== //
    //  MODELO: un insumo del almacén
    // ====================================================================================== //
    static class Insumo {
        private final int id;
        private final String nombre;
        private final String tipo;
        private final String unidad;
        private final double stockActual;
        private final double stockMinimo;

        Insumo(int id, String nombre, String tipo, String unidad,
               double stockActual, double stockMinimo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
            this.unidad = unidad;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
        }

        int getId() { return id; }
        String getNombre() { return nombre; }
        String getTipo() { return tipo; }
        String getUnidad() { return unidad; }
        double getStockActual() { return stockActual; }
        double getStockMinimo() { return stockMinimo; }

        /** Stock crítico: la existencia es igual o menor al mínimo. */
        boolean esCritico() { return stockActual <= stockMinimo; }
    }

    static class InsumoAdapter extends RecyclerView.Adapter<InsumoAdapter.ViewHolder> {

        private final List<Insumo> todos = new ArrayList<>();
        private final List<Insumo> visibles = new ArrayList<>();

        /** Reemplaza la lista completa. Después llama a filtrar(...) para refrescar la vista. */
        void setInsumos(List<Insumo> nuevos) {
            todos.clear();
            todos.addAll(nuevos);
        }

        /** Filtra por nombre o tipo, sin distinguir mayúsculas. */
        void filtrar(String texto) {
            String q = texto == null ? "" : texto.trim().toLowerCase(Locale.getDefault());
            visibles.clear();
            for (Insumo i : todos) {
                if (q.isEmpty()
                        || i.getNombre().toLowerCase(Locale.getDefault()).contains(q)
                        || i.getTipo().toLowerCase(Locale.getDefault()).contains(q)) {
                    visibles.add(i);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_insumo_almacen, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            Insumo i = visibles.get(position);

            h.tvIcono.setText("Empaque".equalsIgnoreCase(i.getTipo()) ? "📦" : "🥚");
            h.tvNombre.setText(i.getNombre());
            h.tvTipo.setText(i.getTipo());
            h.tvStockMinimo.setText(formatear(i.getStockMinimo()) + " " + i.getUnidad());
            h.tvStockActual.setText(formatear(i.getStockActual()));
            h.tvUnidad.setText(i.getUnidad());

            // Alerta roja solo si el stock es crítico
            boolean critico = i.esCritico();
            h.tvAlerta.setVisibility(critico ? View.VISIBLE : View.GONE);
            h.tvStockActual.setTextColor(Color.parseColor(critico ? "#D32F2F" : "#2E7D32"));
        }

        @Override
        public int getItemCount() {
            return visibles.size();
        }

        private String formatear(double v) {
            if (Math.abs(v - Math.rint(v)) < 1e-9) {
                return String.format(Locale.getDefault(), "%.0f", v);
            }
            return String.format(Locale.getDefault(), "%.2f", v);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvIcono, tvNombre, tvTipo, tvStockMinimo, tvAlerta, tvStockActual, tvUnidad;

            ViewHolder(@NonNull View v) {
                super(v);
                tvIcono = v.findViewById(R.id.tvIconoTipo);
                tvNombre = v.findViewById(R.id.tvNombreInsumo);
                tvTipo = v.findViewById(R.id.tvTipoInsumo);
                tvStockMinimo = v.findViewById(R.id.tvStockMinimo);
                tvAlerta = v.findViewById(R.id.tvAlertaStock);
                tvStockActual = v.findViewById(R.id.tvStockActual);
                tvUnidad = v.findViewById(R.id.tvUnidadMedida);
            }
        }
    }
}