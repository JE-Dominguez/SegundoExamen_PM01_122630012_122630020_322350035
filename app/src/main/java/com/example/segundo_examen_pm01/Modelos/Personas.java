package com.example.segundo_examen_pm01.Modelos;

public class Personas {

    private int id;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String foto;

    // Constructor vacío
    public Personas() {}

    // Constructor con todos los campos
    public Personas(String nombres, String apellidos, String direccion, String telefono, String foto) {
        this.nombres = nombres != null ? nombres : "";
        this.apellidos = apellidos != null ? apellidos : "";
        this.direccion = direccion != null ? direccion : "";
        this.telefono = telefono != null ? telefono : "";
        this.foto = foto != null ? foto : "";
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getFoto() {
        return foto;
    }

    // Setters con validación básica
    public void setId(int id) {
        this.id = id;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres != null ? nombres : "";
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos != null ? apellidos : "";
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion != null ? direccion : "";
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono != null ? telefono : "";
    }

    public void setFoto(String foto) {
        this.foto = foto != null ? foto : "";
    }
}
