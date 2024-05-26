package com.example.sanitech;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Inventario {
    private final IntegerProperty InventarioId = new SimpleIntegerProperty();
    private final StringProperty CodigoArticulo = new SimpleStringProperty();
    private final IntegerProperty CantidadDisponible = new SimpleIntegerProperty();

    public Inventario() {}

    public Inventario(int InventarioId, String CodigoArticulo, int CantidadDisponible) {
        setInventarioId(InventarioId);
        setCodigoArticulo(CodigoArticulo);
        setCantidadDisponible(CantidadDisponible);
    }

    public int getInventarioId() {return InventarioId.get();}

    public void setInventarioId(int inventarioId) {this.InventarioId.set(inventarioId);}

    public IntegerProperty InventarioIdProperty() {return InventarioId;}

    public String getCodigoArticulo() {return CodigoArticulo.get();}

    public void setCodigoArticulo(String codigoArticulo) {this.CodigoArticulo.set(codigoArticulo);}

    public StringProperty CodigoArticuloProperty() {return CodigoArticulo;}

    public int getCantidadDisponible() {return CantidadDisponible.get();}

    public void setCantidadDisponible(int cantidadDisponible) {this.CantidadDisponible.set(cantidadDisponible);}

    public IntegerProperty CantidadDisponibleProperty() {return CantidadDisponible;}
}
