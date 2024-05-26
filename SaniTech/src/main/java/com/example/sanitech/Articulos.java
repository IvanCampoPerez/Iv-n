package com.example.sanitech;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Articulos {
    private final StringProperty codigo_articulo = new SimpleStringProperty();
    private final StringProperty articulo = new SimpleStringProperty();
    private final IntegerProperty codigo_proveedor = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> precio_compra = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> precio_venta = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> fecha_alta = new SimpleObjectProperty<>();

    public Articulos() {}

    public Articulos(String codigo_articulo, String articulo, int codigo_proveedor, BigDecimal precio_compra, BigDecimal precio_venta, LocalDate fecha_alta) {
        setCodigoarticulo(codigo_articulo);
        setArticulo(articulo);
        setCodigoproveedor(codigo_proveedor);
        setPreciocompra(precio_compra);
        setPrecioventa(precio_venta);
        setFechaalta(fecha_alta);
    }

    public String getCodigoarticulo() {return codigo_articulo.get();}

    public void setCodigoarticulo(String codigoarticulo) {this.codigo_articulo.set(codigoarticulo);}

    public StringProperty CodigoarticuloProperty(){return codigo_articulo;}

    public String getArticulo() {return articulo.get();}

    public void setArticulo(String articulo) {this.articulo.set(articulo);}

    public StringProperty ArticuloProperty(){return articulo;}

    public int getCodigoproveedor() {return codigo_proveedor.get();}

    public void setCodigoproveedor(int codigoproveedor) {this.codigo_proveedor.set(codigoproveedor);}

    public IntegerProperty CodigoproveedorProperty(){return codigo_proveedor;}

    public BigDecimal getPreciocompra() {return precio_compra.get();}

    public void setPreciocompra(BigDecimal preciocompra) {this.precio_compra.set(preciocompra);}

    public ObjectProperty<BigDecimal> PreciocompraProperty(){return precio_compra;}

    public BigDecimal getPrecioventa() {return precio_venta.get();}

    public void setPrecioventa(BigDecimal precioventa) {this.precio_venta.set(precioventa);}

    public ObjectProperty<BigDecimal> PrecioventaProperty(){return precio_venta;}

    public LocalDate getFechaalta() {return fecha_alta.get();}

    public void setFechaalta(LocalDate fechaalta) {this.fecha_alta.set(fechaalta);}

    public ObjectProperty<LocalDate> FechaaltaProperty(){return fecha_alta;}
}
