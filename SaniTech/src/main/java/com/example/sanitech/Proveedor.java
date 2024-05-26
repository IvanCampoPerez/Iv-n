package com.example.sanitech;

import javafx.beans.property.*;

public class Proveedor {
    private final IntegerProperty Codigo_proveedor = new SimpleIntegerProperty();
    private final StringProperty Organizacion = new SimpleStringProperty();
    private final StringProperty Telefono = new SimpleStringProperty();
    private final StringProperty EMail = new SimpleStringProperty();

    public Proveedor() {}

    public Proveedor(int Codigo_proveedor, String Organizacion, String Telefono, String EMail) {
        setCodigoproveedor(Codigo_proveedor);
        setOrganizacion(Organizacion);
        setTelefono(Telefono);
        setEmail(EMail);
    }

    public int getCodigoproveedor() {
        return Codigo_proveedor.get();
    }

    public void setCodigoproveedor(int Codigo_proveedor) {
        this.Codigo_proveedor.set(Codigo_proveedor);
    }

    public IntegerProperty Codigo_proveedorProperty() {
        return Codigo_proveedor;
    }

    public String getOrganizacion() {
        return Organizacion.get();
    }

    public void setOrganizacion(String Organizacion) {
        this.Organizacion.set(Organizacion);
    }

    public StringProperty OrganizacionProperty() {
        return Organizacion;
    }

    public String getTelefono() {
        return Telefono.get();
    }

    public void setTelefono(String Telefono) {
        this.Telefono.set(Telefono);
    }

    public StringProperty TelefonoProperty() {
        return Telefono;
    }

    public String getEmail() {
        return EMail.get();
    }

    public void setEmail(String EMail) {
        this.EMail.set(EMail);
    }

    public StringProperty EMailProperty() {
        return EMail;
    }
}
