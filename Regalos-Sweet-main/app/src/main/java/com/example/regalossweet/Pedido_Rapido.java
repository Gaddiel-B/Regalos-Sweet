package com.example.regalossweet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Pedido_Rapido extends AppCompatActivity {

    // Vistas
    private TextInputEditText etNombre, etTelefono, etDireccion, etFecha, etHora;
    private RecyclerView rvProductos;
    private TextInputEditText etNotasGenerales;
    private Spinner spMetodoPago;
    private TextInputEditText etAnticipo;
    private TextView tvTotal, tvSaldo;
    private Button btnGuardar;

    // Adapter
    private ProductoAdapter adapter;
    private final List<Producto> listaProductos = new ArrayList<>();

    // BD
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_rapido);

        dbHelper = new DatabaseHelper(this);

        inicializarVistas();
        configurarSpinnerMetodoPago();
        configurarFechaHora();
        cargarProductos();
        configurarListeners();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreCliente);
        etTelefono = findViewById(R.id.etTelefonoCliente);
        etDireccion = findViewById(R.id.etDireccionEntrega);
        etFecha = findViewById(R.id.etFechaEntrega);
        etHora = findViewById(R.id.etHoraEntrega);
        rvProductos = findViewById(R.id.rvProductos);
        etNotasGenerales = findViewById(R.id.etNotasGenerales);
        spMetodoPago = findViewById(R.id.spMetodoPago);
        etAnticipo = findViewById(R.id.etAnticipo);
        tvTotal = findViewById(R.id.tvTotalCalculado);
        tvSaldo = findViewById(R.id.tvSaldoPendiente);
        btnGuardar = findViewById(R.id.btnGuardarPedido);

        rvProductos.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configurarSpinnerMetodoPago() {
        String[] metodos = {"Efectivo", "Transferencia"};
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, metodos);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMetodoPago.setAdapter(ad);
    }

    private void configurarFechaHora() {
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                etFecha.setText(fecha);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        etHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, min) -> {
                String hora = String.format(Locale.getDefault(), "%02d:%02d", h, min);
                etHora.setText(hora);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });
    }

    private void cargarProductos() {
        listaProductos.clear();
        listaProductos.addAll(dbHelper.obtenerProductosPorCategoria("Comida rápida", "Bocadillos"));

        adapter = new ProductoAdapter(listaProductos, this::recalcularTotales);
        rvProductos.setAdapter(adapter);
        recalcularTotales();
    }

    private void configurarListeners() {
        etAnticipo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { recalcularTotales(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGuardar.setOnClickListener(v -> guardarPedido());
    }

    private void recalcularTotales() {
        if (adapter == null) return;
        double total = adapter.getTotal();
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

        double anticipo = parseDouble(etAnticipo.getText() != null ? etAnticipo.getText().toString() : "0");
        double saldo = total - anticipo;
        if (saldo < 0) saldo = 0;
        tvSaldo.setText(String.format(Locale.getDefault(), "$%.2f", saldo));
    }

    private double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
    }

    private void guardarPedido() {
        String nombre = text(etNombre);
        String telefono = text(etTelefono);
        String direccion = text(etDireccion);
        String fecha = text(etFecha);
        String hora = text(etHora);

        if (nombre.isEmpty()) { etNombre.setError("Requerido"); return; }
        if (fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Completa fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
        if (adapter == null || adapter.getTotal() <= 0) {
            Toast.makeText(this, "Selecciona al menos un producto", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = adapter.getTotal();
        double anticipo = parseDouble(text(etAnticipo));
        double saldo = Math.max(total - anticipo, 0);
        String metodo = spMetodoPago.getSelectedItem().toString();
        String notas = text(etNotasGenerales);

        // Cabecera
        ContentValues pedidoValues = new ContentValues();
        pedidoValues.put(DatabaseHelper.COL_PED_CLIENTE, nombre);
        pedidoValues.put(DatabaseHelper.COL_PED_TELEFONO, telefono);
        pedidoValues.put(DatabaseHelper.COL_PED_DIRECCION, direccion);
        pedidoValues.put(DatabaseHelper.COL_PED_FECHA, fecha);
        pedidoValues.put(DatabaseHelper.COL_PED_HORA, hora);
        pedidoValues.put(DatabaseHelper.COL_PED_CATEGORIA, "Rápido");
        pedidoValues.put(DatabaseHelper.COL_PED_TOTAL, total);
        pedidoValues.put(DatabaseHelper.COL_PED_ANTICIPO, anticipo);
        pedidoValues.put(DatabaseHelper.COL_PED_SALDO, saldo);
        pedidoValues.put(DatabaseHelper.COL_PED_METODO_PAGO, metodo);
        pedidoValues.put(DatabaseHelper.COL_PED_ESTADO, "Activo");

        // Transacción
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long idPedido = db.insert(DatabaseHelper.TABLE_PEDIDOS, null, pedidoValues);
            if (idPedido == -1) {
                Toast.makeText(this, "Error al guardar el pedido", Toast.LENGTH_LONG).show();
                return;
            }

            List<Producto> productos = adapter.getProductos();
            int[] cantidades = adapter.getCantidades();
            boolean[] seleccion = adapter.getSeleccionados();

            for (int i = 0; i < productos.size(); i++) {
                if (seleccion[i] && cantidades[i] > 0) {
                    Producto p = productos.get(i);
                    double subtotal = cantidades[i] * p.getPrecio();

                    ContentValues det = new ContentValues();
                    det.put(DatabaseHelper.COL_DET_PED_ID, idPedido);
                    det.put(DatabaseHelper.COL_DET_PROD_ID, p.getId());
                    det.put(DatabaseHelper.COL_DET_CANTIDAD, cantidades[i]);
                    det.put(DatabaseHelper.COL_DET_PRECIO, p.getPrecio());
                    det.put(DatabaseHelper.COL_DET_SUBTOTAL, subtotal);
                    det.put(DatabaseHelper.COL_DET_NOTAS, notas);

                    db.insert(DatabaseHelper.TABLE_DETALLE_PEDIDO, null, det);
                }
            }

            db.setTransactionSuccessful();
            Toast.makeText(this, "Pedido registrado ✅", Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private String text(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}