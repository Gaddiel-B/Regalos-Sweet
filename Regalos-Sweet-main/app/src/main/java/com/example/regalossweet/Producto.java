package com.example.regalossweet;

public class Producto {

    private int id;
    private String nombre;
    private String categoria;
    private double precio;
    private String imagenUri;

    public Producto(int id, String nombre, String categoria, double precio, String imagenUri) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUri = imagenUri;
    }

    public Producto(String nombre, String categoria, double precio, String imagenUri) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUri = imagenUri;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagenUri() { return imagenUri; }
    public void setImagenUri(String imagenUri) { this.imagenUri = imagenUri; }

    public boolean esEstandarizado() {
        return "Comida rápida".equals(categoria) || "Bocadillos".equals(categoria);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precio=" + precio +
                '}';
    }
}