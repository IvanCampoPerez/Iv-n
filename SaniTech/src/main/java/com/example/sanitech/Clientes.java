package com.example.sanitech;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Clientes {
    private final IntegerProperty ClienteId = new SimpleIntegerProperty();
    private final StringProperty Nombre = new SimpleStringProperty();
    private final StringProperty Apellidos = new SimpleStringProperty();
    private final StringProperty Compania = new SimpleStringProperty();
    private final StringProperty Direccion = new SimpleStringProperty();
    private final StringProperty Ciudad = new SimpleStringProperty();
    private final StringProperty Comunidad = new SimpleStringProperty();
    private final StringProperty Pais = new SimpleStringProperty();
    private final StringProperty CodigoPostal = new SimpleStringProperty();
    private final StringProperty Telefono = new SimpleStringProperty();
    private final IntegerProperty EmpleadoId = new SimpleIntegerProperty();

    public Clientes() {}

    public Clientes(int ClienteId, String Nombre, String Apellidos, String Compania, String Direccion, String Ciudad, String Comunidad, String Pais, String CodigoPostal, String Telefono, int EmpleadoId) {
        setClienteId(ClienteId);
        setNombre(Nombre);
        setApellidos(Apellidos);
        setCompania(Compania);
        setDireccion(Direccion);
        setCiudad(Ciudad);
        setComunidad(Comunidad);
        setPais(Pais);
        setCodigoPostal(CodigoPostal);
        setTelefono(Telefono);
        setEmpleadoId(EmpleadoId);
    }

    public int getClienteId() {return ClienteId.get();}

    public void setClienteId(int clienteId) {this.ClienteId.set(clienteId);}

    public String getNombre() {return Nombre.get();}

    public void setNombre(String nombre) {this.Nombre.set(nombre);}

    public String getApellidos() {return Apellidos.get();}

    public void setApellidos(String apellidos) {this.Apellidos.set(apellidos);}

    public String getCompania() {return Compania.get();}

    public void setCompania(String compania) {this.Compania.set(compania);}

    public String getDireccion() {return Direccion.get();}

    public void setDireccion(String direccion) {this.Direccion.set(direccion);}

    public String getCiudad() {return Ciudad.get();}

    public void setCiudad(String ciudad) {this.Ciudad.set(ciudad);}

    public String getComunidad() {return Comunidad.get();}

    public void setComunidad(String comunidad) {this.Comunidad.set(comunidad);}

    public String getPais() {return Pais.get();}

    public void setPais(String pais) {this.Pais.set(pais);}

    public String getCodigoPostal() {return CodigoPostal.get();}

    public void setCodigoPostal(String codigoPostal) {this.CodigoPostal.set(codigoPostal);}

    public String getTelefono() {return Telefono.get();}

    public void setTelefono(String telefono) {this.Telefono.set(telefono);}

    public int getEmpleadoId() {return EmpleadoId.get();}

    public void setEmpleadoId(int empleadoId) {this.EmpleadoId.set(empleadoId);}
}