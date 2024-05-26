package com.example.sanitech;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Compras {
    private final IntegerProperty LineaCompraId = new SimpleIntegerProperty();
    private final StringProperty CodigoArticulo = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> PrecioCompra = new SimpleObjectProperty<>();
    private final IntegerProperty Cantidad = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> TotalCompra = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> FechaCompra = new SimpleObjectProperty<>();

    public Compras() {}

    public Compras(int LineaCompraId, String CodigoArticulo, BigDecimal PrecioCompra, int Cantidad, BigDecimal TotalCompra, LocalDate FechaCompra) {
        setLineaCompraId(LineaCompraId);
        setCodigoArticulo(CodigoArticulo);
        setPrecioCompra(PrecioCompra);
        setCantidad(Cantidad);
        setTotalCompra(TotalCompra);
        setFechaCompra(FechaCompra);
    }

    public int getLineaCompraId() {return LineaCompraId.get();}

    public void setLineaCompraId(int lineaCompraId) {this.LineaCompraId.set(lineaCompraId);}

    public IntegerProperty LineaCompraIdProperty() {return LineaCompraId;}

    public String getCodigoArticulo() {return CodigoArticulo.get();}

    public void setCodigoArticulo(String codigoArticulo) {this.CodigoArticulo.set(codigoArticulo);}

    public StringProperty CodigoArticuloProperty() {return CodigoArticulo;}

    public BigDecimal getPrecioCompra() {return PrecioCompra.get();}

    public void setPrecioCompra(BigDecimal precioCompra) {this.PrecioCompra.set(precioCompra);}

    public ObjectProperty<BigDecimal> PrecioCompraProperty() {return PrecioCompra;}

    public int getCantidad() {return Cantidad.get();}

    public void setCantidad(int cantidad) {this.Cantidad.set(cantidad);}

    public IntegerProperty CantidadProperty() {return Cantidad;}

    public BigDecimal getTotalCompra() {return TotalCompra.get();}

    public void setTotalCompra(BigDecimal totalCompra) {this.TotalCompra.set(totalCompra);}

    public ObjectProperty<BigDecimal> TotalCompraProperty() {return TotalCompra;}

    public LocalDate getFechaCompra() {return FechaCompra.get();}

    public void setFechaCompra(LocalDate fechaCompra) {this.FechaCompra.set(fechaCompra);}

    public ObjectProperty<LocalDate> FechaCompraProperty() {return FechaCompra;}
}
