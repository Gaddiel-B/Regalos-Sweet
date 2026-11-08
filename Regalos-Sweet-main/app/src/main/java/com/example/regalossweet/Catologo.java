package com.example.regalossweet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class Catologo extends AppCompatActivity {

    private TabLayout tabLayoutCategorias;
    private RecyclerView rvProductosCatalogo;

    private DatabaseHelper dbHelper;
    private GestorProductoAdapter adapter;
    private List<Producto> listaProductos;

    // Las 7 categorías exactas del TabLayout
    private final String[] categoriasNombres = {
            "Comida rápida",
            "Postres",
            "Pasteles",
            "Desayunos",
            "Regalos",
            "Ramos",
            "Bocadillos"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catologo);

        dbHelper = new DatabaseHelper(this);
        listaProductos = new ArrayList<>();

        // Vinculación con los IDs reales de tu XML
        tabLayoutCategorias = findViewById(R.id.tabLayoutCategorias);
        rvProductosCatalogo = findViewById(R.id.rvProductosCatalogo);

        rvProductosCatalogo.setLayoutManager(new LinearLayoutManager(this));

        // Cargar los productos de la primera pestaña por defecto ("Comida rápida")
        cargarProductosPorCategoria(categoriasNombres[0]);

        // Listener para cambiar de categoría al hacer clic en las pestañas
        tabLayoutCategorias.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < categoriasNombres.length) {
                    cargarProductosPorCategoria(categoriasNombres[position]);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cargarProductosPorCategoria(String categoria) {
        listaProductos.clear();
        listaProductos.addAll(dbHelper.obtenerProductosPorCategoria(categoria));

        adapter = new GestorProductoAdapter(listaProductos, new GestorProductoAdapter.OnProductoListener() {
            @Override
            public void onEditar(Producto producto) {
                mostrarDialogoFormulario(producto); // Editar o eliminar producto existente
            }

            @Override
            public void onEliminar(Producto producto) {
                dbHelper.eliminarProducto(producto.getId());
                Toast.makeText(Catologo.this, "Producto dado de baja", Toast.LENGTH_SHORT).show();
                recargarCategoriaActual();
            }
        });

        rvProductosCatalogo.setAdapter(adapter);
    }

    private void recargarCategoriaActual() {
        int tabIndex = tabLayoutCategorias.getSelectedTabPosition();
        if (tabIndex >= 0 && tabIndex < categoriasNombres.length) {
            cargarProductosPorCategoria(categoriasNombres[tabIndex]);
        }
    }

    private void mostrarDialogoFormulario(Producto productoExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre del producto");

        EditText etPrecio = new EditText(this);
        etPrecio.setHint("Precio estándar");
        etPrecio.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        Spinner spCat = new Spinner(this);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasNombres);
        spCat.setAdapter(catAdapter);

        if (productoExistente != null) {
            etNombre.setText(productoExistente.getNombre());
            etPrecio.setText(String.valueOf(productoExistente.getPrecio()));
            for (int i = 0; i < categoriasNombres.length; i++) {
                if (categoriasNombres[i].equals(productoExistente.getCategoria())) {
                    spCat.setSelection(i);
                    break;
                }
            }
        } else {
            // Seleccionar en el diálogo la categoría de la pestaña activa actual
            int tabIndex = tabLayoutCategorias.getSelectedTabPosition();
            spCat.setSelection(tabIndex);
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);
        layout.addView(etNombre);
        layout.addView(etPrecio);
        layout.addView(spCat);

        builder.setView(layout);
        builder.setTitle(productoExistente == null ? "Alta de Producto" : "Editar Producto");

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String precioStr = etPrecio.getText().toString().trim();
            String categoria = spCat.getSelectedItem().toString();

            if (nombre.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio = Double.parseDouble(precioStr);

            if (productoExistente == null) {
                dbHelper.insertarProducto(nombre, categoria, precio);
                Toast.makeText(this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.actualizarProducto(productoExistente.getId(), nombre, categoria, precio);
                Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
            }
            recargarCategoriaActual();
        });

        if (productoExistente != null) {
            builder.setNeutralButton("Dar de baja", (dialog, which) -> {
                dbHelper.eliminarProducto(productoExistente.getId());
                Toast.makeText(this, "Producto dado de baja", Toast.LENGTH_SHORT).show();
                recargarCategoriaActual();
            });
        }

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}