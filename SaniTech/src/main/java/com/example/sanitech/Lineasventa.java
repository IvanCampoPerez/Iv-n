package com.example.sanitech;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;

public class Lineasventa {
    private final IntegerProperty LineaVentaId = new SimpleIntegerProperty();
    private final IntegerProperty VentaId = new SimpleIntegerProperty();
    private final StringProperty CodigoArticulo = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> PrecioVenta = new SimpleObjectProperty<>();
    private final IntegerProperty Cantidad = new SimpleIntegerProperty();

    public Lineasventa() {}

    public Lineasventa(int LineaVentaId, int VentaId, String CodigoArticulo, BigDecimal PrecioVenta, int Cantidad) {
        setLineaVentaId(LineaVentaId);
        setVentaId(VentaId);
        setCodigoArticulo(CodigoArticulo);
        setPrecioVenta(PrecioVenta);
        setCantidad(Cantidad);
    }

    public int getLineaVentaId() {return LineaVentaId.get();}

    public void setLineaVentaId(int lineaVentaId) {this.LineaVentaId.set(lineaVentaId);}

    public int getVentaId() {return VentaId.get();}

    public void setVentaId(int ventaId) {this.VentaId.set(ventaId);}

    public String getCodigoArticulo() {return CodigoArticulo.get();}

    public void setCodigoArticulo(String codigoArticulo) {this.CodigoArticulo.set(codigoArticulo);}

    public BigDecimal getPrecioVenta() {return PrecioVenta.get();}

    public void setPrecioVenta(BigDecimal precioVenta) {this.PrecioVenta.set(precioVenta);}

    public int getCantidad() {return Cantidad.get();}

    public void setCantidad(int cantidad) {this.Cantidad.set(cantidad);}
}
