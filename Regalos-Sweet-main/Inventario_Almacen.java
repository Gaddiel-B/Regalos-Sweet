package com.example.regalossweet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestión del inventario (almacén de insumos): CRUD completo.
 *  - Create : botón "Agregar insumo"
 *  - Read   : lista + stock crítico en rojo
 *  - Search : buscador (en vivo) + botón "Buscar"
 *  - Update : botón "Actualizar stock" en cada tarjeta -> ajuste manual de inventario
 *  - Delete : botón "Eliminar" en cada tarjeta (con confirmación)
 *
 * Además expone la validación de stock para los pedidos (hayStockSuficiente).
 */
public class Inventario_Almacen extends AppCompatActivity {

    private static final int DECIMAL = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;

    // Vistas
    private RecyclerView rvInsumos;
    private TextInputEditText etBuscar;
    private MaterialButton btnBuscar, btnAgregar;

    // Datos
    private InsumoAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Insumo> insumosActuales = new ArrayList<>();

    // ====================================================================================== //
    //  CICLO DE VIDA
    // ====================================================================================== //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_almacen);

        dbHelper = new DatabaseHelper(this);

        rvInsumos = findViewById(R.id.rvInsumosAlmacen);
        etBuscar = findViewById(R.id.etBuscarInsumo);
        btnBuscar = findViewById(R.id.btnBuscarInsumo);
        btnAgregar = findViewById(R.id.btnAgregarInsumo);

        // Botones de cada tarjeta: Actualizar stock / Eliminar
        adapter = new InsumoAdapter(new InsumoAdapter.OnInsumoAccion() {
            @Override public void onActualizar(Insumo insumo) { mostrarDialogoAjuste(insumo); }
            @Override public void onEliminar(Insumo insumo) { confirmarEliminar(insumo); }
        });
        rvInsumos.setLayoutManager(new LinearLayoutManager(this));
        rvInsumos.setAdapter(adapter);

        // Buscar: en vivo mientras se escribe...
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { aplicarFiltro(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        // ...y con el botón "Buscar" o la lupa del teclado
        btnBuscar.setOnClickListener(v -> buscarInsumo());
        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscarInsumo();
                return true;
            }
            return false;
        });

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga cada vez que se vuelve a la pantalla (el stock pudo cambiar tras un pedido)
        cargarInsumos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    // ====================================================================================== //
    //  READ / SEARCH
    // ====================================================================================== //
    private void cargarInsumos() {
        insumosActuales = consultarInsumos();
        adapter.setInsumos(insumosActuales);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String texto = etBuscar.getText() != null ? etBuscar.getText().toString() : "";
        adapter.filtrar(texto);
    }

    /** Botón "Buscar": cierra el teclado, aplica el filtro y avisa cuántos resultados hay. */
    private void buscarInsumo() {
        ocultarTeclado();
        aplicarFiltro();

        String texto = etBuscar.getText() != null ? etBuscar.getText().toString().trim() : "";
        if (texto.isEmpty()) return;

        int n = adapter.getItemCount();
        Toast.makeText(this,
                n == 0 ? "No se encontró ningún insumo" : n + " resultado(s)",
                Toast.LENGTH_SHORT).show();
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View foco = getCurrentFocus();
        if (imm != null && foco != null) {
            imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
        }
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

    // ====================================================================================== //
    //  CREATE: Agregar insumo
    // ====================================================================================== //
    private void mostrarDialogoAgregar() {
        LinearLayout form = crearFormulario();

        EditText etNombre = crearCampo("Nombre del insumo",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        Spinner spTipo = crearSpinner(new String[]{"Ingrediente", "Empaque"});
        Spinner spUnidad = crearSpinner(new String[]{"kg", "g", "l", "ml", "piezas"});
        EditText etStock = crearCampo("Stock inicial", DECIMAL);
        EditText etMinimo = crearCampo("Stock mínimo (para la alerta)", DECIMAL);

        form.addView(etNombre);
        form.addView(crearEtiqueta("Tipo"));
        form.addView(spTipo);
        form.addView(crearEtiqueta("Unidad de medida"));
        form.addView(spUnidad);
        form.addView(etStock);
        form.addView(etMinimo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("➕ Agregar insumo")
                .setView(envolverEnScroll(form))
                .setPositiveButton("Guardar", null)   // se define abajo para no cerrar si hay errores
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                etNombre.setError("Escribe el nombre");
                return;
            }
            if (existeNombre(nombre, -1)) {
                etNombre.setError("Ya existe un insumo con ese nombre");
                return;
            }
            Double stock = leerDecimal(etStock);
            if (stock == null) {
                etStock.setError("Escribe un número (0 o mayor)");
                return;
            }
            Double minimo = leerDecimal(etMinimo);
            if (minimo == null) {
                etMinimo.setError("Escribe un número (0 o mayor)");
                return;
            }

            long id = insertarInsumo(nombre,
                    spTipo.getSelectedItem().toString(),
                    spUnidad.getSelectedItem().toString(),
                    stock, minimo);

            if (id == -1) {
                Toast.makeText(this, "No se pudo guardar el insumo", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "Insumo agregado: " + nombre, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            cargarInsumos();
        }));
        dialog.show();
    }

    private long insertarInsumo(String nombre, String tipo, String unidad, double stock, double minimo) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_INS_NOMBRE, nombre);
        cv.put(DatabaseHelper.COL_INS_TIPO, tipo);
        cv.put(DatabaseHelper.COL_INS_UNIDAD, unidad);
        cv.put(DatabaseHelper.COL_INS_STOCK_ACTUAL, stock);
        cv.put(DatabaseHelper.COL_INS_STOCK_MINIMO, minimo);
        return dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_INSUMOS, null, cv);
    }

    /** ¿Ya hay otro insumo con ese nombre? (excluirId = el que se está editando, o -1) */
    private boolean existeNombre(String nombre, int excluirId) {
        for (Insumo i : insumosActuales) {
            if (i.getId() != excluirId && i.getNombre().equalsIgnoreCase(nombre)) return true;
        }
        return false;
    }

    // ====================================================================================== //
    //  UPDATE: Actualizar insumo = ajuste manual de inventario
    // ====================================================================================== //
    private void mostrarDialogoAjuste(Insumo insumo) {
        LinearLayout form = crearFormulario();

        TextView tvInfo = new TextView(this);
        tvInfo.setText(insumo.getNombre() + "\nStock actual: "
                + formatearCantidad(insumo.getStockActual()) + " " + insumo.getUnidad());
        tvInfo.setTextColor(Color.parseColor("#333333"));
        tvInfo.setTextSize(15);
        tvInfo.setTypeface(null, Typeface.BOLD);

        RadioGroup grupo = new RadioGroup(this);
        grupo.setOrientation(RadioGroup.VERTICAL);
        RadioButton rbEntrada = crearOpcion("➕ Entrada (sumar al stock)");
        RadioButton rbSalida = crearOpcion("➖ Salida (restar del stock)");
        RadioButton rbFijar = crearOpcion("✏️ Establecer cantidad exacta");
        grupo.addView(rbEntrada);
        grupo.addView(rbSalida);
        grupo.addView(rbFijar);
        rbEntrada.setChecked(true);

        EditText etCantidad = crearCampo("Cantidad (" + insumo.getUnidad() + ")", DECIMAL);
        EditText etMinimo = crearCampo("Stock mínimo", DECIMAL);
        etMinimo.setText(formatearCantidad(insumo.getStockMinimo()));

        form.addView(tvInfo);
        form.addView(crearEtiqueta("Tipo de ajuste"));
        form.addView(grupo);
        form.addView(etCantidad);
        form.addView(crearEtiqueta("Stock mínimo (puedes modificarlo)"));
        form.addView(etMinimo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ajuste manual de inventario")
                .setView(envolverEnScroll(form))
                .setPositiveButton("Guardar ajuste", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Double cantidad = leerDecimal(etCantidad);
            if (cantidad == null) {
                etCantidad.setError("Escribe un número (0 o mayor)");
                return;
            }
            Double minimo = leerDecimal(etMinimo);
            if (minimo == null) {
                etMinimo.setError("Escribe un número (0 o mayor)");
                return;
            }

            double actual = insumo.getStockActual();
            double nuevo;
            if (rbEntrada.isChecked()) {
                nuevo = actual + cantidad;
            } else if (rbSalida.isChecked()) {
                if (cantidad > actual + 1e-9) {
                    etCantidad.setError("No puedes restar más de lo disponible ("
                            + formatearCantidad(actual) + " " + insumo.getUnidad() + ")");
                    return;
                }
                nuevo = actual - cantidad;
            } else {
                nuevo = cantidad;
            }
            nuevo = Math.round(nuevo * 1000.0) / 1000.0;   // evita 0.30000000000000004

            if (!actualizarStock(insumo.getId(), nuevo, minimo)) {
                Toast.makeText(this, "No se pudo actualizar el insumo", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, insumo.getNombre() + ": "
                    + formatearCantidad(actual) + " → " + formatearCantidad(nuevo)
                    + " " + insumo.getUnidad(), Toast.LENGTH_LONG).show();
            dialog.dismiss();
            cargarInsumos();
        }));
        dialog.show();
    }

    private boolean actualizarStock(int idInsumo, double stockActual, double stockMinimo) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_INS_STOCK_ACTUAL, stockActual);
        cv.put(DatabaseHelper.COL_INS_STOCK_MINIMO, stockMinimo);
        int filas = dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_INSUMOS, cv,
                DatabaseHelper.COL_INS_ID + " = ?", new String[]{String.valueOf(idInsumo)});
        return filas > 0;
    }

    // ====================================================================================== //
    //  DELETE: Eliminar insumo
    // ====================================================================================== //
    private void confirmarEliminar(Insumo insumo) {
        int recetas = contarRecetasConInsumo(insumo.getId());

        StringBuilder msg = new StringBuilder("¿Seguro que quieres eliminar «")
                .append(insumo.getNombre()).append("»?");
        if (recetas > 0) {
            msg.append("\n\nEste insumo se usa en ").append(recetas)
                    .append(recetas == 1 ? " receta." : " recetas.")
                    .append(" También se quitará de ellas y esos productos dejarán de descontarlo del inventario.");
        }
        msg.append("\n\nEsta acción no se puede deshacer.");

        new AlertDialog.Builder(this)
                .setTitle("Eliminar insumo")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(msg.toString())
                .setPositiveButton("Eliminar", (d, w) -> {
                    if (eliminarInsumo(insumo.getId())) {
                        Toast.makeText(this, "Insumo eliminado: " + insumo.getNombre(),
                                Toast.LENGTH_SHORT).show();
                        cargarInsumos();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el insumo", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int contarRecetasConInsumo(int idInsumo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RECETA_INSUMOS
                + " WHERE " + DatabaseHelper.COL_REC_INS_ID + " = ?", new String[]{String.valueOf(idInsumo)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /** Borra el insumo y sus filas en Receta_Insumos dentro de una sola transacción. */
    private boolean eliminarInsumo(int idInsumo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = new String[]{String.valueOf(idInsumo)};
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_RECETA_INSUMOS, DatabaseHelper.COL_REC_INS_ID + " = ?", args);
            int filas = db.delete(DatabaseHelper.TABLE_INSUMOS, DatabaseHelper.COL_INS_ID + " = ?", args);
            db.setTransactionSuccessful();
            return filas > 0;
        } finally {
            db.endTransaction();
        }
    }

    // ====================================================================================== //
    //  UTILIDADES PARA LOS DIÁLOGOS (se crean por código, no requieren archivos XML nuevos)
    // ====================================================================================== //
    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private LinearLayout crearFormulario() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(20), dp(12), dp(20), dp(4));
        return l;
    }

    private ScrollView envolverEnScroll(LinearLayout contenido) {
        ScrollView sv = new ScrollView(this);
        sv.addView(contenido);
        return sv;
    }

    private EditText crearCampo(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setSingleLine(true);
        return e;
    }

    private TextView crearEtiqueta(String texto) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextColor(Color.parseColor("#8B263E"));
        t.setTextSize(13);
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(12), 0, dp(2));
        return t;
    }

    private Spinner crearSpinner(String[] opciones) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones));
        return s;
    }

    private RadioButton crearOpcion(String texto) {
        RadioButton rb = new RadioButton(this);
        rb.setText(texto);
        rb.setId(View.generateViewId());
        return rb;
    }

    /** Lee un número >= 0 (acepta coma o punto). Devuelve null si está vacío o es inválido. */
    private Double leerDecimal(EditText et) {
        String t = et.getText().toString().trim().replace(',', '.');
        if (t.isEmpty()) return null;
        try {
            double v = Double.parseDouble(t);
            if (Double.isNaN(v) || Double.isInfinite(v) || v < 0) return null;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ====================================================================================== //
    //  VALIDACIÓN DE INVENTARIO (se llama desde la pantalla de pedidos)
    //
    //  Uso:  if (!Inventario_Almacen.hayStockSuficiente(this, dbHelper, mapaPedido)) return;
    //  mapaPedido = id_producto -> cantidad pedida
    // ====================================================================================== //

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
                .setIcon(android.R.drawable.ic_dialog_alert)
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

    // ====================================================================================== //
    //  ADAPTADOR: pinta cada insumo con item_insumo_almacen.xml
    // ====================================================================================== //
    static class InsumoAdapter extends RecyclerView.Adapter<InsumoAdapter.ViewHolder> {

        /** Acciones de los botones de cada tarjeta. */
        interface OnInsumoAccion {
            void onActualizar(Insumo insumo);
            void onEliminar(Insumo insumo);
        }

        private final List<Insumo> todos = new ArrayList<>();
        private final List<Insumo> visibles = new ArrayList<>();
        private final OnInsumoAccion listener;

        InsumoAdapter(OnInsumoAccion listener) {
            this.listener = listener;
        }

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

            // Botones de la tarjeta
            h.btnActualizar.setOnClickListener(v -> {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onActualizar(visibles.get(pos));
                }
            });
            h.btnEliminar.setOnClickListener(v -> {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEliminar(visibles.get(pos));
                }
            });
        }

        @Override
        public int getItemCount() {
            return visibles.size();
        }

        private String formatear(double v) {
            return formatearCantidad(v);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvIcono, tvNombre, tvTipo, tvStockMinimo, tvAlerta, tvStockActual, tvUnidad;
            final MaterialButton btnActualizar, btnEliminar;

            ViewHolder(@NonNull View v) {
                super(v);
                tvIcono = v.findViewById(R.id.tvIconoTipo);
                tvNombre = v.findViewById(R.id.tvNombreInsumo);
                tvTipo = v.findViewById(R.id.tvTipoInsumo);
                tvStockMinimo = v.findViewById(R.id.tvStockMinimo);
                tvAlerta = v.findViewById(R.id.tvAlertaStock);
                tvStockActual = v.findViewById(R.id.tvStockActual);
                tvUnidad = v.findViewById(R.id.tvUnidadMedida);
                btnActualizar = v.findViewById(R.id.btnActualizarInsumo);
                btnEliminar = v.findViewById(R.id.btnEliminarInsumo);
            }
        }
    }
}