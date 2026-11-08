package com.example.regalossweet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class GestorProductoAdapter extends RecyclerView.Adapter<GestorProductoAdapter.ViewHolder> {

    public interface OnProductoListener {
        void onEditar(Producto producto);
        void onEliminar(Producto producto);
    }

    private final List<Producto> lista;
    private final OnProductoListener listener;

    public GestorProductoAdapter(List<Producto> lista, OnProductoListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos un layout simple de Android para listar el nombre, precio y categoría de forma segura
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto p = lista.get(position);
        holder.text1.setText(p.getNombre() + " — $" + String.format(Locale.getDefault(), "%.2f", p.getPrecio()));
        holder.text2.setText("Categoría: " + p.getCategoria());

        // Al hacer clic en un producto de la lista, se ejecuta la acción de editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}