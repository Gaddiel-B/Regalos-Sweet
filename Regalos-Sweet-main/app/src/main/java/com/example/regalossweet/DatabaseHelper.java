package com.example.regalossweet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "negocio_pedidos.db";
    private static final int DATABASE_VERSION = 3; // Actualizado a la versión 3 para incluir tablas especiales

    // ==================== TABLA PRODUCTOS ====================
    public static final String TABLE_PRODUCTOS = "Productos";
    public static final String COL_PROD_ID = "id_producto";
    public static final String COL_PROD_NOMBRE = "nombre";
    public static final String COL_PROD_CATEGORIA = "categoria";
    public static final String COL_PROD_PRECIO = "precio_estandar";
    public static final String COL_PROD_IMAGEN = "imagen_uri";

    // ==================== TABLA INSUMOS ====================
    public static final String TABLE_INSUMOS = "Insumos";
    public static final String COL_INS_ID = "id_insumo";
    public static final String COL_INS_NOMBRE = "nombre";
    public static final String COL_INS_TIPO = "tipo";
    public static final String COL_INS_UNIDAD = "unidad_medida";
    public static final String COL_INS_STOCK_ACTUAL = "stock_actual";
    public static final String COL_INS_STOCK_MINIMO = "stock_minimo";

    // ==================== TABLA RECETA_INSUMOS ====================
    public static final String TABLE_RECETA_INSUMOS = "Receta_Insumos";
    public static final String COL_REC_ID = "id_receta";
    public static final String COL_REC_PROD_ID = "id_producto";
    public static final String COL_REC_INS_ID = "id_insumo";
    public static final String COL_REC_CANTIDAD = "cantidad_requerida";

    // ==================== TABLA PEDIDOS ====================
    public static final String TABLE_PEDIDOS = "Pedidos";
    public static final String COL_PED_ID = "id_pedido";
    public static final String COL_PED_CLIENTE = "cliente_nombre";
    public static final String COL_PED_TELEFONO = "cliente_telefono";
    public static final String COL_PED_DIRECCION = "direccion_entrega";
    public static final String COL_PED_FECHA = "fecha_entrega";
    public static final String COL_PED_HORA = "hora_entrega";
    public static final String COL_PED_CATEGORIA = "categoria_pedido";
    public static final String COL_PED_SABOR_PAN = "sabor_pan";
    public static final String COL_PED_RELLENO = "relleno";
    public static final String COL_PED_TEMATICA = "tematica";
    public static final String COL_PED_MENSAJE = "mensaje_dedicatoria";
    public static final String COL_PED_IMAGEN_REF = "imagen_referencia_uri";
    public static final String COL_PED_TOTAL = "total";
    public static final String COL_PED_ANTICIPO = "anticipo";
    public static final String COL_PED_SALDO = "saldo_pendiente";
    public static final String COL_PED_METODO_PAGO = "metodo_pago";
    public static final String COL_PED_ESTADO = "estado";

    // ==================== TABLA DETALLE_PEDIDO ====================
    public static final String TABLE_DETALLE_PEDIDO = "Detalle_Pedido";
    public static final String COL_DET_ID = "id_detalle";
    public static final String COL_DET_PED_ID = "id_pedido";
    public static final String COL_DET_PROD_ID = "id_producto";
    public static final String COL_DET_CANTIDAD = "cantidad";
    public static final String COL_DET_PRECIO = "precio_unitario";
    public static final String COL_DET_SUBTOTAL = "subtotal";
    public static final String COL_DET_NOTAS = "notas";

    // ==================== NUEVAS TABLAS DE PEDIDOS ESPECIALES ====================
    public static final String TABLE_PEDIDO_RAMO = "Pedido_Ramo";
    public static final String COL_RAMO_ID = "id_ramo";
    public static final String COL_RAMO_PED_ID = "id_pedido";
    public static final String COL_RAMO_TIPO_FLORES = "tipo_flores";
    public static final String COL_RAMO_PIEZAS = "piezas_flores";
    public static final String COL_RAMO_COLORES = "colores";
    public static final String COL_RAMO_RECIBE = "nombre_recibe";

    public static final String TABLE_PEDIDO_BOCADILLO = "Pedido_Bocadillo";
    public static final String COL_BOC_ID = "id_bocadillo";
    public static final String COL_BOC_PED_ID = "id_pedido";
    public static final String COL_BOC_TIPO = "tipo_bocadillo";
    public static final String COL_BOC_PIEZAS = "piezas";
    public static final String COL_BOC_RECIBE = "nombre_recibe";

    public static final String TABLE_PEDIDO_DESAYUNO = "Pedido_Desayuno";
    public static final String COL_DES_ID = "id_desayuno";
    public static final String COL_DES_PED_ID = "id_pedido";
    public static final String COL_DES_INCLUYE = "incluye_elementos";
    public static final String COL_DES_COLORES = "colores_empaque";
    public static final String COL_DES_MENSAJE = "mensaje_nota";
    public static final String COL_DES_RECIBE = "nombre_recibe";

    public static final String TABLE_PEDIDO_REGALO = "Pedido_Regalo";
    public static final String COL_REG_ID = "id_regalo";
    public static final String COL_REG_PED_ID = "id_pedido";
    public static final String COL_REG_COLORES = "colores";
    public static final String COL_REG_MENSAJE = "mensaje_nota";
    public static final String COL_REG_TAMANO_MODELO = "tamano_modelo";
    public static final String COL_REG_RECIBE = "nombre_recibe";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // PRODUCTOS
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTOS + " ("
                + COL_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROD_NOMBRE + " TEXT NOT NULL, "
                + COL_PROD_CATEGORIA + " TEXT NOT NULL, "
                + COL_PROD_PRECIO + " REAL NOT NULL, "
                + COL_PROD_IMAGEN + " TEXT);");

        // INSUMOS
        db.execSQL("CREATE TABLE " + TABLE_INSUMOS + " ("
                + COL_INS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_INS_NOMBRE + " TEXT NOT NULL, "
                + COL_INS_TIPO + " TEXT NOT NULL, "
                + COL_INS_UNIDAD + " TEXT NOT NULL, "
                + COL_INS_STOCK_ACTUAL + " REAL NOT NULL DEFAULT 0.0, "
                + COL_INS_STOCK_MINIMO + " REAL NOT NULL DEFAULT 0.0);");

        // RECETA_INSUMOS
        db.execSQL("CREATE TABLE " + TABLE_RECETA_INSUMOS + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_PROD_ID + " INTEGER NOT NULL, "
                + COL_REC_INS_ID + " INTEGER NOT NULL, "
                + COL_REC_CANTIDAD + " REAL NOT NULL, "
                + "FOREIGN KEY (" + COL_REC_PROD_ID + ") REFERENCES " + TABLE_PRODUCTOS + "(" + COL_PROD_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COL_REC_INS_ID + ") REFERENCES " + TABLE_INSUMOS + "(" + COL_INS_ID + ") ON DELETE CASCADE);");

        // PEDIDOS
        db.execSQL("CREATE TABLE " + TABLE_PEDIDOS + " ("
                + COL_PED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PED_CLIENTE + " TEXT NOT NULL, "
                + COL_PED_TELEFONO + " TEXT, "
                + COL_PED_DIRECCION + " TEXT, "
                + COL_PED_FECHA + " TEXT NOT NULL, "
                + COL_PED_HORA + " TEXT NOT NULL, "
                + COL_PED_CATEGORIA + " TEXT NOT NULL, "
                + COL_PED_SABOR_PAN + " TEXT, "
                + COL_PED_RELLENO + " TEXT, "
                + COL_PED_TEMATICA + " TEXT, "
                + COL_PED_MENSAJE + " TEXT, "
                + COL_PED_IMAGEN_REF + " TEXT, "
                + COL_PED_TOTAL + " REAL NOT NULL, "
                + COL_PED_ANTICIPO + " REAL NOT NULL DEFAULT 0.0, "
                + COL_PED_SALDO + " REAL NOT NULL, "
                + COL_PED_METODO_PAGO + " TEXT NOT NULL, "
                + COL_PED_ESTADO + " TEXT NOT NULL DEFAULT 'Activo');");

        // DETALLE_PEDIDO
        db.execSQL("CREATE TABLE " + TABLE_DETALLE_PEDIDO + " ("
                + COL_DET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DET_PED_ID + " INTEGER NOT NULL, "
                + COL_DET_PROD_ID + " INTEGER NOT NULL, "
                + COL_DET_CANTIDAD + " INTEGER NOT NULL, "
                + COL_DET_PRECIO + " REAL NOT NULL, "
                + COL_DET_SUBTOTAL + " REAL NOT NULL, "
                + COL_DET_NOTAS + " TEXT, "
                + "FOREIGN KEY (" + COL_DET_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COL_DET_PROD_ID + ") REFERENCES " + TABLE_PRODUCTOS + "(" + COL_PROD_ID + ") ON DELETE CASCADE);");

        // PEDIDO RAMO
        db.execSQL("CREATE TABLE " + TABLE_PEDIDO_RAMO + " ("
                + COL_RAMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RAMO_PED_ID + " INTEGER NOT NULL, "
                + COL_RAMO_TIPO_FLORES + " TEXT NOT NULL, "
                + COL_RAMO_PIEZAS + " TEXT NOT NULL, "
                + COL_RAMO_COLORES + " TEXT NOT NULL, "
                + COL_RAMO_RECIBE + " TEXT, "
                + "FOREIGN KEY (" + COL_RAMO_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

        // PEDIDO BOCADILLO
        db.execSQL("CREATE TABLE " + TABLE_PEDIDO_BOCADILLO + " ("
                + COL_BOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_BOC_PED_ID + " INTEGER NOT NULL, "
                + COL_BOC_TIPO + " TEXT NOT NULL, "
                + COL_BOC_PIEZAS + " TEXT NOT NULL, "
                + COL_BOC_RECIBE + " TEXT, "
                + "FOREIGN KEY (" + COL_BOC_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

        // PEDIDO DESAYUNO
        db.execSQL("CREATE TABLE " + TABLE_PEDIDO_DESAYUNO + " ("
                + COL_DES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DES_PED_ID + " INTEGER NOT NULL, "
                + COL_DES_INCLUYE + " TEXT NOT NULL, "
                + COL_DES_COLORES + " TEXT, "
                + COL_DES_MENSAJE + " TEXT, "
                + COL_DES_RECIBE + " TEXT, "
                + "FOREIGN KEY (" + COL_DES_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

        // PEDIDO REGALO
        db.execSQL("CREATE TABLE " + TABLE_PEDIDO_REGALO + " ("
                + COL_REG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REG_PED_ID + " INTEGER NOT NULL, "
                + COL_REG_COLORES + " TEXT, "
                + COL_REG_MENSAJE + " TEXT, "
                + COL_REG_TAMANO_MODELO + " TEXT, "
                + COL_REG_RECIBE + " TEXT, "
                + "FOREIGN KEY (" + COL_REG_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

        precargarDatosBase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DETALLE_PEDIDO + " ("
                    + COL_DET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_DET_PED_ID + " INTEGER NOT NULL, "
                    + COL_DET_PROD_ID + " INTEGER NOT NULL, "
                    + COL_DET_CANTIDAD + " INTEGER NOT NULL, "
                    + COL_DET_PRECIO + " REAL NOT NULL, "
                    + COL_DET_SUBTOTAL + " REAL NOT NULL, "
                    + COL_DET_NOTAS + " TEXT, "
                    + "FOREIGN KEY (" + COL_DET_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY (" + COL_DET_PROD_ID + ") REFERENCES " + TABLE_PRODUCTOS + "(" + COL_PROD_ID + ") ON DELETE CASCADE);");
        }

        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEDIDO_RAMO + " ("
                    + COL_RAMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RAMO_PED_ID + " INTEGER NOT NULL, "
                    + COL_RAMO_TIPO_FLORES + " TEXT NOT NULL, "
                    + COL_RAMO_PIEZAS + " TEXT NOT NULL, "
                    + COL_RAMO_COLORES + " TEXT NOT NULL, "
                    + COL_RAMO_RECIBE + " TEXT, "
                    + "FOREIGN KEY (" + COL_RAMO_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEDIDO_BOCADILLO + " ("
                    + COL_BOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_BOC_PED_ID + " INTEGER NOT NULL, "
                    + COL_BOC_TIPO + " TEXT NOT NULL, "
                    + COL_BOC_PIEZAS + " TEXT NOT NULL, "
                    + COL_BOC_RECIBE + " TEXT, "
                    + "FOREIGN KEY (" + COL_BOC_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEDIDO_DESAYUNO + " ("
                    + COL_DES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_DES_PED_ID + " INTEGER NOT NULL, "
                    + COL_DES_INCLUYE + " TEXT NOT NULL, "
                    + COL_DES_COLORES + " TEXT, "
                    + COL_DES_MENSAJE + " TEXT, "
                    + COL_DES_RECIBE + " TEXT, "
                    + "FOREIGN KEY (" + COL_DES_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEDIDO_REGALO + " ("
                    + COL_REG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_REG_PED_ID + " INTEGER NOT NULL, "
                    + COL_REG_COLORES + " TEXT, "
                    + COL_REG_MENSAJE + " TEXT, "
                    + COL_REG_TAMANO_MODELO + " TEXT, "
                    + COL_REG_RECIBE + " TEXT, "
                    + "FOREIGN KEY (" + COL_REG_PED_ID + ") REFERENCES " + TABLE_PEDIDOS + "(" + COL_PED_ID + ") ON DELETE CASCADE);");
        }
    }

    // ==================== MÉTODOS DE LECTURA ====================

    public List<Producto> obtenerProductosPorCategoria(String... categorias) {
        List<Producto> lista = new ArrayList<>();
        if (categorias == null || categorias.length == 0) return lista;

        SQLiteDatabase db = getReadableDatabase();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < categorias.length; i++) {
            placeholders.append(i == 0 ? "?" : ", ?");
        }

        String sql = "SELECT * FROM " + TABLE_PRODUCTOS
                + " WHERE " + COL_PROD_CATEGORIA + " IN (" + placeholders + ")"
                + " ORDER BY " + COL_PROD_CATEGORIA + ", " + COL_PROD_NOMBRE;

        Cursor c = db.rawQuery(sql, categorias);
        if (c.moveToFirst()) {
            do {
                lista.add(cursorAProducto(c));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return lista;
    }

    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS + " ORDER BY " + COL_PROD_NOMBRE, null);
        if (c.moveToFirst()) {
            do {
                lista.add(cursorAProducto(c));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return lista;
    }

    private Producto cursorAProducto(Cursor c) {
        return new Producto(
                c.getInt(c.getColumnIndexOrThrow(COL_PROD_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_PROD_NOMBRE)),
                c.getString(c.getColumnIndexOrThrow(COL_PROD_CATEGORIA)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PROD_PRECIO)),
                c.getString(c.getColumnIndexOrThrow(COL_PROD_IMAGEN))
        );
    }

    // ==================== MÉTODOS DE INSERCIÓN ====================

    public long insertarPedido(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_PEDIDOS, null, values);
        db.close();
        return id;
    }

    public long insertarDetallePedido(int idPedido, int idProducto,
                                      int cantidad, double precioUnitario,
                                      double subtotal, String notas) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DET_PED_ID, idPedido);
        values.put(COL_DET_PROD_ID, idProducto);
        values.put(COL_DET_CANTIDAD, cantidad);
        values.put(COL_DET_PRECIO, precioUnitario);
        values.put(COL_DET_SUBTOTAL, subtotal);
        values.put(COL_DET_NOTAS, notas);
        long id = db.insert(TABLE_DETALLE_PEDIDO, null, values);
        db.close();
        return id;
    }

    // ==================== MÉTODOS CRUD PARA EL GESTOR DEL MENÚ ====================

    public boolean insertarProducto(String nombre, String categoria, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROD_NOMBRE, nombre);
        values.put(COL_PROD_CATEGORIA, categoria);
        values.put(COL_PROD_PRECIO, precio);
        values.put(COL_PROD_IMAGEN, "");

        long id = db.insert(TABLE_PRODUCTOS, null, values);
        db.close();
        return id != -1;
    }

    public boolean actualizarProducto(int id, String nombre, String categoria, double precio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROD_NOMBRE, nombre);
        values.put(COL_PROD_CATEGORIA, categoria);
        values.put(COL_PROD_PRECIO, precio);

        int filas = db.update(TABLE_PRODUCTOS, values, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return filas > 0;
    }

    public boolean eliminarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete(TABLE_PRODUCTOS, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return filas > 0;
    }


    // ==================== MÉTODOS DE INSUMOS ====================

    public List<int[]> obtenerInsumosDeProducto(int idProducto) {
        List<int[]> resultado = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_REC_INS_ID + ", " + COL_REC_CANTIDAD
                        + " FROM " + TABLE_RECETA_INSUMOS
                        + " WHERE " + COL_REC_PROD_ID + " = ?",
                new String[]{String.valueOf(idProducto)});
        if (c.moveToFirst()) {
            do {
                int idInsumo = c.getInt(0);
                double cant = c.getDouble(1);
                resultado.add(new int[]{idInsumo, (int) cant});
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return resultado;
    }

    public double obtenerStockInsumo(int idInsumo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_INS_STOCK_ACTUAL + " FROM " + TABLE_INSUMOS
                        + " WHERE " + COL_INS_ID + " = ?",
                new String[]{String.valueOf(idInsumo)});
        double stock = -1;
        if (c.moveToFirst()) stock = c.getDouble(0);
        c.close();
        db.close();
        return stock;
    }

    public void descontarStock(int idInsumo, double cantidad) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_INSUMOS
                        + " SET " + COL_INS_STOCK_ACTUAL + " = " + COL_INS_STOCK_ACTUAL + " - ?"
                        + " WHERE " + COL_INS_ID + " = ?",
                new Object[]{cantidad, idInsumo});
        db.close();
    }

    // ==================== PRECARGA DE DATOS BASE ====================

    private void precargarDatosBase(SQLiteDatabase db) {
        insertarInsumoBase(db, "Harina de Trigo", "Ingrediente", "kg", 10.0, 3.0);
        insertarInsumoBase(db, "Azúcar Glass", "Ingrediente", "kg", 5.0, 1.5);
        insertarInsumoBase(db, "Cacao en Polvo", "Ingrediente", "kg", 2.0, 0.5);
        insertarInsumoBase(db, "Caja de Pastel 30x30", "Empaque", "piezas", 25.0, 5.0);
        insertarInsumoBase(db, "Vaso para Frappé 16oz", "Empaque", "piezas", 50.0, 10.0);

        insertarProductoBase(db, "Hamburguesa Clásica", "Comida rápida", 85.0);
        insertarProductoBase(db, "Frappé de Oreo", "Postres", 55.0);
        insertarProductoBase(db, "Pastel de Chocolate", "Pasteles", 350.0);
        insertarProductoBase(db, "Desayuno Sorpresa Waffles", "Desayunos", 280.0);
        insertarProductoBase(db, "Caja Sorpresa Peluche", "Regalos", 200.0);
        insertarProductoBase(db, "Ramo de 12 Rosas", "Ramos", 320.0);
        insertarProductoBase(db, "Charola de Bocadillos (20 pzas)", "Bocadillos", 250.0);
    }

    private void insertarInsumoBase(SQLiteDatabase db, String nombre, String tipo, String unidad, double actual, double minimo) {
        ContentValues values = new ContentValues();
        values.put(COL_INS_NOMBRE, nombre);
        values.put(COL_INS_TIPO, tipo);
        values.put(COL_INS_UNIDAD, unidad);
        values.put(COL_INS_STOCK_ACTUAL, actual);
        values.put(COL_INS_STOCK_MINIMO, minimo);
        db.insert(TABLE_INSUMOS, null, values);
    }

    private void insertarProductoBase(SQLiteDatabase db, String nombre, String categoria, double precio) {
        ContentValues values = new ContentValues();
        values.put(COL_PROD_NOMBRE, nombre);
        values.put(COL_PROD_CATEGORIA, categoria);
        values.put(COL_PROD_PRECIO, precio);
        values.put(COL_PROD_IMAGEN, "");
        db.insert(TABLE_PRODUCTOS, null, values);
    }
}