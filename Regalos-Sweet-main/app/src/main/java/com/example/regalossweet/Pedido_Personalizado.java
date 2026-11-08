package com.example.regalossweet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedHashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Pedido_Personalizado extends Activity {

    // ============================================================
    // COMPONENTES DE LA INTERFAZ
    // ============================================================

    private EditText etNombreClienteEspecial;
    private EditText etTelefonoClienteEspecial;
    private EditText etDireccionEntregaEspecial;
    private EditText etFechaEntregaEspecial;
    private EditText etHoraEntregaEspecial;

    private Spinner spCategoriaEspecial;

    private EditText etSaborPan;
    private EditText etRelleno;
    private EditText etTematica;
    private EditText etMensajeDedicatoria;

    private ImageView ivFotoReferencia;
    private Button btnCargarFoto;

    private EditText etPrecioTotalEspecial;
    private Spinner spMetodoPagoEspecial;
    private EditText etAnticipoEspecial;

    private TextView tvSaldoRestanteEspecial;

    // Contenedor y campos dinámicos de cada categoría
    private LinearLayout layoutCamposDinamicos;
    private TextInputLayout tilSaborPan;
    private TextInputLayout tilRelleno;
    private TextInputLayout tilTematica;
    private TextInputLayout tilMensajeDedicatoria;
    private final Map<String, EditText> camposDinamicos = new LinkedHashMap<>();

    private Button btnGuardarPedidoEspecial;

    // ============================================================
    // BASE DE DATOS
    // ============================================================

    private DatabaseHelper databaseHelper;

    // URI de la imagen seleccionada
    private String imagenReferenciaUri = "";

    // Código para seleccionar imagen
    private static final int REQUEST_IMAGEN = 100;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pedido_personalizado);

        // Inicializar BD
        databaseHelper = new DatabaseHelper(this);

        // Inicializar componentes
        inicializarVistas();

        // Configurar Spinners
        configurarSpinners();

        // Configurar fecha y hora
        configurarFecha();
        configurarHora();

        // Configurar imagen
        configurarSelectorImagen();

        // Configurar cálculo de saldo
        configurarCalculoSaldo();

        // Configurar comportamiento dinámico
        configurarCategoria();

        // Configurar botón guardar
        configurarBotonGuardar();
    }

    // ============================================================
    // INICIALIZAR VISTAS
    // ============================================================

    private void inicializarVistas() {

        // Cliente y entrega
        etNombreClienteEspecial =
                findViewById(R.id.etNombreClienteEspecial);

        etTelefonoClienteEspecial =
                findViewById(R.id.etTelefonoClienteEspecial);

        etDireccionEntregaEspecial =
                findViewById(R.id.etDireccionEntregaEspecial);

        etFechaEntregaEspecial =
                findViewById(R.id.etFechaEntregaEspecial);

        etHoraEntregaEspecial =
                findViewById(R.id.etHoraEntregaEspecial);

        // Categoría
        spCategoriaEspecial =
                findViewById(R.id.spCategoriaEspecial);

        layoutCamposDinamicos = findViewById(R.id.layoutCamposDinamicos);
        tilSaborPan = findViewById(R.id.tilSaborPan);
        tilRelleno = findViewById(R.id.tilRelleno);
        tilTematica = findViewById(R.id.tilTematica);
        tilMensajeDedicatoria = findViewById(R.id.tilMensajeDedicatoria);

        // Personalización
        etSaborPan =
                findViewById(R.id.etSaborPan);

        etRelleno =
                findViewById(R.id.etRelleno);

        etTematica =
                findViewById(R.id.etTematica);

        etMensajeDedicatoria =
                findViewById(R.id.etMensajeDedicatoria);

        // Imagen
        ivFotoReferencia =
                findViewById(R.id.ivFotoReferencia);

        btnCargarFoto =
                findViewById(R.id.btnCargarFoto);

        // Pago
        etPrecioTotalEspecial =
                findViewById(R.id.etPrecioTotalEspecial);

        spMetodoPagoEspecial =
                findViewById(R.id.spMetodoPagoEspecial);

        etAnticipoEspecial =
                findViewById(R.id.etAnticipoEspecial);

        tvSaldoRestanteEspecial =
                findViewById(R.id.tvSaldoRestanteEspecial);

        // Guardar
        btnGuardarPedidoEspecial =
                findViewById(R.id.btnGuardarPedidoEspecial);
    }

    // ============================================================
    // SPINNERS
    // ============================================================

    private void configurarSpinners() {

        // Categorías disponibles
        String[] categorias = {
                "Pasteles",
                "Desayunos",
                "Regalos",
                "Ramos",
                "Postres",
                "Bocadillos",
                "Comida rápida"
        };

        ArrayAdapter<String> adapterCategorias =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categorias
                );

        adapterCategorias.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategoriaEspecial.setAdapter(
                adapterCategorias
        );

        // Métodos de pago
        String[] metodosPago = {
                "Efectivo",
                "Transferencia",
                "Tarjeta"
        };

        ArrayAdapter<String> adapterPago =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        metodosPago
                );

        adapterPago.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spMetodoPagoEspecial.setAdapter(
                adapterPago
        );
    }

    // ============================================================
    // FECHA
    // ============================================================

    private void configurarFecha() {

        etFechaEntregaEspecial.setOnClickListener(v -> {

            Calendar calendario =
                    Calendar.getInstance();

            int anio =
                    calendario.get(Calendar.YEAR);

            int mes =
                    calendario.get(Calendar.MONTH);

            int dia =
                    calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            Pedido_Personalizado.this,
                            (view, year, month, dayOfMonth) -> {

                                String fecha =
                                        String.format(
                                                Locale.getDefault(),
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                dayOfMonth
                                        );

                                etFechaEntregaEspecial
                                        .setText(fecha);
                            },
                            anio,
                            mes,
                            dia
                    );

            dialog.show();
        });
    }

    // ============================================================
    // HORA
    // ============================================================

    private void configurarHora() {

        etHoraEntregaEspecial.setOnClickListener(v -> {

            Calendar calendario =
                    Calendar.getInstance();

            int hora =
                    calendario.get(Calendar.HOUR_OF_DAY);

            int minuto =
                    calendario.get(Calendar.MINUTE);

            TimePickerDialog dialog =
                    new TimePickerDialog(
                            Pedido_Personalizado.this,
                            (view, hourOfDay, minute) -> {

                                String horaSeleccionada =
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d:%02d",
                                                hourOfDay,
                                                minute
                                        );

                                etHoraEntregaEspecial
                                        .setText(horaSeleccionada);
                            },
                            hora,
                            minuto,
                            true
                    );

            dialog.show();
        });
    }

    // ============================================================
    // SELECTOR DE IMAGEN
    // ============================================================

    private void configurarSelectorImagen() {

        btnCargarFoto.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Intent.ACTION_OPEN_DOCUMENT
                    );

            intent.setType("image/*");

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );

            startActivityForResult(
                    intent,
                    REQUEST_IMAGEN
            );
        });
    }

    // ============================================================
    // RESULTADO DE SELECCIÓN DE IMAGEN
    // ============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == REQUEST_IMAGEN &&
                        resultCode == RESULT_OK &&
                        data != null
        ) {

            Uri uri = data.getData();

            if (uri != null) {

                imagenReferenciaUri =
                        uri.toString();

                // Mostrar imagen
                ivFotoReferencia.setImageURI(uri);

                try {

                    getContentResolver()
                            .takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                } catch (Exception e) {

                    e.printStackTrace();
                }

                Toast.makeText(
                        this,
                        "Imagen seleccionada correctamente",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // ============================================================
    // CÁLCULO DEL SALDO
    // ============================================================

    private void configurarCalculoSaldo() {

        etPrecioTotalEspecial.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        calcularSaldo();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        etAnticipoEspecial.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        calcularSaldo();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    // ============================================================
    // CALCULAR SALDO
    // ============================================================

    private void calcularSaldo() {

        double total = obtenerNumero(
                etPrecioTotalEspecial
        );

        double anticipo = obtenerNumero(
                etAnticipoEspecial
        );

        double saldo =
                total - anticipo;

        if (saldo < 0) {
            saldo = 0;
        }

        tvSaldoRestanteEspecial.setText(
                String.format(
                        Locale.getDefault(),
                        "$%.2f",
                        saldo
                )
        );
    }

    // ============================================================
    // OBTENER NÚMERO DE EDITTEXT
    // ============================================================

    private double obtenerNumero(
            EditText editText
    ) {

        String texto =
                editText.getText()
                        .toString()
                        .trim();

        if (texto.isEmpty()) {
            return 0.0;
        }

        try {

            return Double.parseDouble(texto);

        } catch (NumberFormatException e) {

            return 0.0;
        }
    }

    // ============================================================
    // COMPORTAMIENTO DINÁMICO
    // ============================================================

    private void configurarCategoria() {

        spCategoriaEspecial.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        String categoria =
                                parent
                                        .getItemAtPosition(position)
                                        .toString();

                        actualizarCamposCategoria(
                                categoria
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    // ============================================================
    // ACTUALIZAR CAMPOS SEGÚN CATEGORÍA
    // ============================================================

    private void actualizarCamposCategoria(String categoria) {

        camposDinamicos.clear();
        layoutCamposDinamicos.removeAllViews();

        boolean especial = categoria.equals("Ramos")
                || categoria.equals("Bocadillos")
                || categoria.equals("Desayunos")
                || categoria.equals("Regalos");

        int visibilidad = especial ? View.GONE : View.VISIBLE;
        tilSaborPan.setVisibility(visibilidad);
        tilRelleno.setVisibility(visibilidad);
        tilTematica.setVisibility(visibilidad);
        tilMensajeDedicatoria.setVisibility(visibilidad);
        layoutCamposDinamicos.setVisibility(especial ? View.VISIBLE : View.GONE);

        if (!especial) {
            etSaborPan.setHint("Sabor de Pan / Base");
            etRelleno.setHint("Relleno / Contenido");
            etTematica.setHint("Tema / Tamaño / Características");
            etMensajeDedicatoria.setHint("Mensaje o Dedicatoria");
            return;
        }

        if (categoria.equals("Ramos")) {
            agregarCampo("tipo_flores", "Tipos de flores", "Ej. rosas, girasoles, tulipanes");
            agregarCampo("piezas_flores", "Piezas de cada flor", "Ej. rosas: 12, girasoles: 3");
            agregarCampo("colores", "Colores de flores, envoltura y listón", "Ej. flores rojas, envoltura blanca, listón dorado");
            agregarCampo("nombre_recibe", "Nombre de quien recibe", "Nombre del destinatario");
        } else if (categoria.equals("Bocadillos")) {
            agregarCampo("tipo_bocadillo", "Tipos de bocadillos", "Ej. hamburguesas, cuernitos, sándwiches, hot dogs, pizzas");
            agregarCampo("piezas", "Cantidad de cada tipo", "Ej. 10 hamburguesas, 8 cuernitos");
            agregarCampo("nombre_recibe", "Nombre de quien recibe", "Nombre del destinatario");
        } else if (categoria.equals("Desayunos")) {
            agregarCampo("incluye_elementos", "Componentes del desayuno", "Plato fuerte, postre, bebida, snack y fruta");
            agregarCampo("colores_empaque", "Colores de charola/caja y moños", "Describe los colores");
            agregarCampo("mensaje_nota", "Mensaje o nota (opcional)", "Escribe el mensaje si corresponde");
            agregarCampo("nombre_recibe", "Nombre de quien recibe", "Nombre del destinatario");
        } else if (categoria.equals("Regalos")) {
            agregarCampo("tipo_regalo", "Tipo de regalo", "Ej. caja de regalo, peluche, juguete");
            agregarCampo("colores", "Colores del regalo", "Describe los colores");
            agregarCampo("tamano_modelo", "Tamaño o modelo", "Ej. peluche mediano, caja grande");
            agregarCampo("mensaje_nota", "Mensaje o nota (opcional)", "Escribe el mensaje si corresponde");
            agregarCampo("nombre_recibe", "Nombre de quien recibe", "Nombre del destinatario");
        }
    }

    private void agregarCampo(String clave, String etiqueta, String ejemplo) {
        TextInputLayout contenedor = new TextInputLayout(this);
        contenedor.setHint(etiqueta);
        contenedor.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parametros.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
        contenedor.setLayoutParams(parametros);

        EditText campo = new TextInputEditText(this);
        campo.setHint(ejemplo);
        campo.setSingleLine(false);
        campo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        campo.setMinLines(1);
        contenedor.addView(campo);
        layoutCamposDinamicos.addView(contenedor);
        camposDinamicos.put(clave, campo);
    }

    private String obtenerCampoDinamico(String clave) {
        EditText campo = camposDinamicos.get(clave);
        return campo == null ? "" : campo.getText().toString().trim();
    }

    private boolean validarCamposCategoria(String categoria) {
        String[] obligatorios;

        switch (categoria) {
            case "Ramos":
                obligatorios = new String[]{"tipo_flores", "piezas_flores", "colores", "nombre_recibe"};
                break;
            case "Bocadillos":
                obligatorios = new String[]{"tipo_bocadillo", "piezas", "nombre_recibe"};
                break;
            case "Desayunos":
                obligatorios = new String[]{"incluye_elementos", "nombre_recibe"};
                break;
            case "Regalos":
                obligatorios = new String[]{"tipo_regalo", "nombre_recibe"};
                break;
            default:
                return true;
        }

        for (String clave : obligatorios) {
            if (obtenerCampoDinamico(clave).isEmpty()) {
                EditText campo = camposDinamicos.get(clave);
                if (campo != null) {
                    campo.setError("Este campo es obligatorio");
                    campo.requestFocus();
                }
                Toast.makeText(this, "Completa los detalles obligatorios del pedido", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private boolean guardarDetalleCategoria(SQLiteDatabase db, long idPedido, String categoria) {
        ContentValues detalle = new ContentValues();

        switch (categoria) {
            case "Ramos":
                detalle.put(DatabaseHelper.COL_RAMO_PED_ID, idPedido);
                detalle.put(DatabaseHelper.COL_RAMO_TIPO_FLORES, obtenerCampoDinamico("tipo_flores"));
                detalle.put(DatabaseHelper.COL_RAMO_PIEZAS, obtenerCampoDinamico("piezas_flores"));
                detalle.put(DatabaseHelper.COL_RAMO_COLORES, obtenerCampoDinamico("colores"));
                detalle.put(DatabaseHelper.COL_RAMO_RECIBE, obtenerCampoDinamico("nombre_recibe"));
                return db.insert(DatabaseHelper.TABLE_PEDIDO_RAMO, null, detalle) != -1;

            case "Bocadillos":
                detalle.put(DatabaseHelper.COL_BOC_PED_ID, idPedido);
                detalle.put(DatabaseHelper.COL_BOC_TIPO, obtenerCampoDinamico("tipo_bocadillo"));
                detalle.put(DatabaseHelper.COL_BOC_PIEZAS, obtenerCampoDinamico("piezas"));
                detalle.put(DatabaseHelper.COL_BOC_RECIBE, obtenerCampoDinamico("nombre_recibe"));
                return db.insert(DatabaseHelper.TABLE_PEDIDO_BOCADILLO, null, detalle) != -1;

            case "Desayunos":
                detalle.put(DatabaseHelper.COL_DES_PED_ID, idPedido);
                detalle.put(DatabaseHelper.COL_DES_INCLUYE, obtenerCampoDinamico("incluye_elementos"));
                detalle.put(DatabaseHelper.COL_DES_COLORES, obtenerCampoDinamico("colores_empaque"));
                detalle.put(DatabaseHelper.COL_DES_MENSAJE, obtenerCampoDinamico("mensaje_nota"));
                detalle.put(DatabaseHelper.COL_DES_RECIBE, obtenerCampoDinamico("nombre_recibe"));
                return db.insert(DatabaseHelper.TABLE_PEDIDO_DESAYUNO, null, detalle) != -1;

            case "Regalos":
                detalle.put(DatabaseHelper.COL_REG_PED_ID, idPedido);
                detalle.put(DatabaseHelper.COL_REG_COLORES, obtenerCampoDinamico("colores"));
                detalle.put(DatabaseHelper.COL_REG_MENSAJE, obtenerCampoDinamico("mensaje_nota"));
                // La tabla actual no tiene una columna tipo_regalo; se guarda junto al tamaño/modelo.
                String tipo = obtenerCampoDinamico("tipo_regalo");
                String tamano = obtenerCampoDinamico("tamano_modelo");
                detalle.put(DatabaseHelper.COL_REG_TAMANO_MODELO,
                        "Tipo: " + tipo + (tamano.isEmpty() ? "" : "; Tamaño/modelo: " + tamano));
                detalle.put(DatabaseHelper.COL_REG_RECIBE, obtenerCampoDinamico("nombre_recibe"));
                return db.insert(DatabaseHelper.TABLE_PEDIDO_REGALO, null, detalle) != -1;

            default:
                return true;
        }
    }

    // ============================================================
    // BOTÓN GUARDAR
    // ============================================================

    private void configurarBotonGuardar() {

        btnGuardarPedidoEspecial.setOnClickListener(
                v -> guardarPedido()
        );
    }

    // ============================================================
    // GUARDAR PEDIDO
    // ============================================================

    private void guardarPedido() {

        // --------------------------------------------------------
        // OBTENER DATOS
        // --------------------------------------------------------

        String cliente =
                etNombreClienteEspecial
                        .getText()
                        .toString()
                        .trim();

        String telefono =
                etTelefonoClienteEspecial
                        .getText()
                        .toString()
                        .trim();

        String direccion =
                etDireccionEntregaEspecial
                        .getText()
                        .toString()
                        .trim();

        String fecha =
                etFechaEntregaEspecial
                        .getText()
                        .toString()
                        .trim();

        String hora =
                etHoraEntregaEspecial
                        .getText()
                        .toString()
                        .trim();

        String categoria =
                spCategoriaEspecial
                        .getSelectedItem()
                        .toString();

        String sabor =
                etSaborPan
                        .getText()
                        .toString()
                        .trim();

        String relleno =
                etRelleno
                        .getText()
                        .toString()
                        .trim();

        String tematica =
                etTematica
                        .getText()
                        .toString()
                        .trim();

        String mensaje =
                etMensajeDedicatoria
                        .getText()
                        .toString()
                        .trim();

        String metodoPago =
                spMetodoPagoEspecial
                        .getSelectedItem()
                        .toString();

        double total =
                obtenerNumero(
                        etPrecioTotalEspecial
                );

        double anticipo =
                obtenerNumero(
                        etAnticipoEspecial
                );

        double saldo =
                total - anticipo;

        // --------------------------------------------------------
        // VALIDACIONES
        // --------------------------------------------------------

        if (cliente.isEmpty()) {

            etNombreClienteEspecial.setError(
                    "Ingrese el nombre del cliente"
            );

            etNombreClienteEspecial.requestFocus();

            return;
        }

        if (fecha.isEmpty()) {

            etFechaEntregaEspecial.setError(
                    "Seleccione una fecha"
            );

            etFechaEntregaEspecial.requestFocus();

            return;
        }

        if (hora.isEmpty()) {

            etHoraEntregaEspecial.setError(
                    "Seleccione una hora"
            );

            etHoraEntregaEspecial.requestFocus();

            return;
        }

        if (total <= 0) {

            etPrecioTotalEspecial.setError(
                    "Ingrese un precio válido"
            );

            etPrecioTotalEspecial.requestFocus();

            return;
        }

        if (anticipo < 0) {

            etAnticipoEspecial.setError(
                    "El anticipo no puede ser negativo"
            );

            etAnticipoEspecial.requestFocus();

            return;
        }

        if (anticipo > total) {

            etAnticipoEspecial.setError(
                    "El anticipo no puede superar el total"
            );

            etAnticipoEspecial.requestFocus();

            return;
        }

        if (!validarCamposCategoria(categoria)) {
            return;
        }

        // --------------------------------------------------------
        // INSERTAR PEDIDO Y DETALLE EN UNA TRANSACCIÓN
        // --------------------------------------------------------

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long resultado = -1;
        boolean guardadoCompleto = false;
        db.beginTransaction();
        try {

            ContentValues values =
                    new ContentValues();

            values.put(
                    DatabaseHelper.COL_PED_CLIENTE,
                    cliente
            );

            values.put(
                    DatabaseHelper.COL_PED_TELEFONO,
                    telefono
            );

            values.put(
                    DatabaseHelper.COL_PED_DIRECCION,
                    direccion
            );

            values.put(
                    DatabaseHelper.COL_PED_FECHA,
                    fecha
            );

            values.put(
                    DatabaseHelper.COL_PED_HORA,
                    hora
            );

            values.put(
                    DatabaseHelper.COL_PED_CATEGORIA,
                    categoria
            );

            values.put(
                    DatabaseHelper.COL_PED_SABOR_PAN,
                    sabor
            );

            values.put(
                    DatabaseHelper.COL_PED_RELLENO,
                    relleno
            );

            values.put(
                    DatabaseHelper.COL_PED_TEMATICA,
                    tematica
            );

            values.put(
                    DatabaseHelper.COL_PED_MENSAJE,
                    mensaje
            );

            values.put(
                    DatabaseHelper.COL_PED_IMAGEN_REF,
                    imagenReferenciaUri
            );

            values.put(
                    DatabaseHelper.COL_PED_TOTAL,
                    total
            );

            values.put(
                    DatabaseHelper.COL_PED_ANTICIPO,
                    anticipo
            );

            values.put(
                    DatabaseHelper.COL_PED_SALDO,
                    saldo
            );

            values.put(
                    DatabaseHelper.COL_PED_METODO_PAGO,
                    metodoPago
            );

            values.put(
                    DatabaseHelper.COL_PED_ESTADO,
                    "Activo"
            );

            resultado = db.insert(DatabaseHelper.TABLE_PEDIDOS, null, values);

            if (resultado != -1 && guardarDetalleCategoria(db, resultado, categoria)) {
                db.setTransactionSuccessful();
                guardadoCompleto = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        if (guardadoCompleto) {
            Toast.makeText(this, "Pedido especial guardado correctamente", Toast.LENGTH_LONG).show();
            ProgramadorNotificaciones.programarAlarma(this, cliente, fecha, hora);
            limpiarFormulario();
        } else {
            Toast.makeText(this, "No se pudo guardar el pedido y sus detalles", Toast.LENGTH_LONG).show();
        }
    }

    // ============================================================
    // LIMPIAR FORMULARIO
    // ============================================================

    private void limpiarFormulario() {

        etNombreClienteEspecial.setText("");
        etTelefonoClienteEspecial.setText("");
        etDireccionEntregaEspecial.setText("");
        etFechaEntregaEspecial.setText("");
        etHoraEntregaEspecial.setText("");

        etSaborPan.setText("");
        etRelleno.setText("");
        etTematica.setText("");
        etMensajeDedicatoria.setText("");

        etPrecioTotalEspecial.setText("");
        etAnticipoEspecial.setText("");

        ivFotoReferencia.setImageResource(
                android.R.drawable.ic_menu_camera
        );

        imagenReferenciaUri = "";

        tvSaldoRestanteEspecial.setText(
                "$0.00"
        );

        // Reiniciar los campos dinámicos de la categoría actual.
        String categoriaActual = spCategoriaEspecial.getSelectedItem() == null
                ? "Pasteles" : spCategoriaEspecial.getSelectedItem().toString();
        actualizarCamposCategoria(categoriaActual);
    }
}