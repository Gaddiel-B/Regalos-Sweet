package com.example.regalossweet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    public interface OnProductoCambiaListener {
        void onCambio();
    }

    private final List<Producto> productos;
    private final int[] cantidades;
    private final boolean[] seleccionados;
    private final OnProductoCambiaListener listener;

    public ProductoAdapter(List<Producto> productos, OnProductoCambiaListener listener) {
        this.productos = productos;
        this.listener = listener;
        this.cantidades = new int[productos.size()];
        this.seleccionados = new boolean[productos.size()];
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_rapido, parent, false);
        return new ProductoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder h, int position) {
        Producto p = productos.get(position);
        h.tvNombre.setText(p.getNombre());
        h.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", p.getPrecio()));
        h.tvCantidad.setText(String.valueOf(cantidades[position]));
        h.cbProducto.setChecked(seleccionados[position]);

        boolean activo = seleccionados[position];
        h.btnMas.setEnabled(activo);
        h.btnMenos.setEnabled(activo && cantidades[position] > 0);

        double subtotal = cantidades[position] * p.getPrecio();
        h.tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));

        h.cbProducto.setOnCheckedChangeListener((view, checked) -> {
            seleccionados[position] = checked;
            if (checked && cantidades[position] == 0) {
                cantidades[position] = 1;
            }
            if (!checked) {
                cantidades[position] = 0;
            }
            notifyItemChanged(position);
            if (listener != null) listener.onCambio();
        });

        h.btnMas.setOnClickListener(v -> {
            cantidades[position]++;
            notifyItemChanged(position);
            if (listener != null) listener.onCambio();
        });

        h.btnMenos.setOnClickListener(v -> {
            if (cantidades[position] > 1) {
                cantidades[position]--;
            } else {
                cantidades[position] = 0;
                seleccionados[position] = false;
            }
            notifyItemChanged(position);
            if (listener != null) listener.onCambio();
        });
    }

    @Override
    public int getItemCount() { return productos.size(); }

    public double getTotal() {
        double total = 0;
        for (int i = 0; i < productos.size(); i++) {
            total += cantidades[i] * productos.get(i).getPrecio();
        }
        return total;
    }

    public List<Producto> getProductos() { return productos; }
    public int[] getCantidades() { return cantidades; }
    public boolean[] getSeleccionados() { return seleccionados; }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbProducto;
        TextView tvNombre, tvPrecio, tvCantidad, tvSubtotal;
        Button btnMas, btnMenos;

        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            cbProducto = itemView.findViewById(R.id.cbProducto);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvPrecio = itemView.findViewById(R.id.tvPrecioUnitario);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnMas = itemView.findViewById(R.id.btnMas);
            btnMenos = itemView.findViewById(R.id.btnMenos);
        }
    }
}