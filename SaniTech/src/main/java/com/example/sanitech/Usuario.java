package com.example.sanitech;

import javafx.beans.property.*;

public class Usuario {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty rol = new SimpleStringProperty();

    public Usuario() {}

    public Usuario(int id, String nombre, String rol) {
        setId(id);
        setNombre(nombre);
        setRol(rol);
    }

    // Métodos getter y setter para las propiedades id, nombre y rol
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getRol() {
        return rol.get();
    }

    public void setRol(String rol) {
        this.rol.set(rol);
    }

    public StringProperty rolProperty() {
        return rol;
    }
}

