package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class AnadircomprasController {
    private ComprasController comprasController;
    private InventarioController inventarioController;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfCodigoArticulo;

    @FXML
    private TextField tfCantidad;

    @FXML
    private DatePicker dpFechaCompra;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private HBox topHbox;

    public void setComprasController(ComprasController comprasController) {
        this.comprasController = comprasController;
    }

    public void setInventarioController(InventarioController inventarioController) {
        this.inventarioController = inventarioController;
    }

    private ObservableList<String> sugerenciasArticulos = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lvSugerencias;

    @FXML
    private void initialize() {
        // Configura el evento de clic para el botón "Guardar" y "Cancelar"
        btnGuardar.setOnAction(event -> anadirCompra());
        btnCancelar.setOnAction(event -> cerrarVentana());

        // Se configura el autocompletado para tfCodigoArticulo
        tfCodigoArticulo.setOnKeyReleased(this::actualizarSugerencias);
        lvSugerencias.setOnMouseClicked(event -> {
            String selected = lvSugerencias.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfCodigoArticulo.setText(selected);
                lvSugerencias.setVisible(false);
            }
        });

        // Se asegura que el ListView este al frente cuando se muestre
        lvSugerencias.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                lvSugerencias.toFront();
            }
        });
    }

    @FXML
    private void onMousePressed(MouseEvent event) { // Metodo para mantener pulsado usando MouseEvent debido a la escena
        Stage stage = (Stage) ((AnchorPane) event.getSource()).getScene().getWindow();
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onMouseDragged(MouseEvent event) { // Metodo para arrastrar usando MouseEvent debido a la escena
        Stage stage = (Stage) ((AnchorPane) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    protected void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void minimizarVentana(ActionEvent event) {
        Stage stage = (Stage) btnMinimizar.getScene().getWindow();
        stage.setIconified(true);
    }

    private void actualizarSugerencias(KeyEvent event) {
        String textoIngresado = tfCodigoArticulo.getText();
        if (textoIngresado.isEmpty()) {
            lvSugerencias.setVisible(false);
            return;
        }

        sugerenciasArticulos.clear();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            String sql = "SELECT codigo_articulo FROM articulos WHERE codigo_articulo LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, textoIngresado + "%");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    sugerenciasArticulos.add(rs.getString("codigo_articulo"));
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        if (!sugerenciasArticulos.isEmpty()) {
            lvSugerencias.setItems(sugerenciasArticulos);
            lvSugerencias.setVisible(true);
        } else {
            lvSugerencias.setVisible(false);
        }
    }

    private void anadirCompra() {
        // Obtener los datos ingresados por el usuario
        String codigoArticulo = tfCodigoArticulo.getText();
        String cantidad = tfCantidad.getText();
        BigDecimal precioCompra = BigDecimal.ZERO; // Se inicializan los precios como cero
        BigDecimal totalCompra = BigDecimal.ZERO;
        String fechaCompra = null;
        if (dpFechaCompra.getValue() != null) {
            fechaCompra = dpFechaCompra.getValue().toString();
        }

        if (codigoArticulo.isEmpty() || codigoArticulo.contains(" ")) {
            mostrarError("El campo código_articulo es obligatorio y no puede contener espacios en blanco");
            return;
        }

        if (cantidad.isEmpty()) {
            mostrarError("El campo cantidad es obligatorio");
            return;
        }

        try {
            // Convierto la cantidad a un entero para realizar las validaciones
            int cantidadInt = Integer.parseInt(cantidad);

            if (cantidadInt <= 0) {
                mostrarError("Debes introducir una cantidad mayor que cero");
                return;
            }

            if (cantidad.length() > 3) {
                mostrarError("La cantidad no puede tener más de 3 dígitos");
                return;
            }

            // Obtener el precio de compra del artículo de la tabla "articulos"
            Articulos articulo = obtenerArticuloPorCodigo(codigoArticulo);
            if (articulo != null) {
                precioCompra = articulo.getPreciocompra();
            } else {
                mostrarError("El articulo con el codigo especificado no se encontro en la base de datos");
                return;
            }

            // Calcular el total de la compra multiplicando el precio de compra por la cantidad
            totalCompra = precioCompra.multiply(new BigDecimal(cantidad));

            // Establecer la conexión con la base de datos
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
                // Validar que la fecha de compra tenga el formato "año-mes-día"
                if (fechaCompra == null || !fechaCompra.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    mostrarError("El formato de la fecha de compra debe ser dia/mes/año (por ejemplo, 21/01/2024)");
                    return;
                }
                // Comprobar si el artículo ya existe en la tabla "inventario"
                boolean articuloExiste = existeArticuloEnInventario(connection, codigoArticulo);

                // Insertar o actualizar en la tabla "inventario"
                if (articuloExiste) {
                    // Actualizar la cantidad disponible sumando la cantidad ingresada por el usuario
                    actualizarCantidadEnInventario(connection, codigoArticulo, cantidadInt);
                } else {
                    // Insertar un nuevo registro en la tabla "inventario"
                    insertarNuevoArticuloEnInventario(connection, codigoArticulo, cantidadInt);
                }

                // Preparar la consulta SQL de inserción en la tabla "lineas_compras"
                String sql = "INSERT INTO lineas_compras (CodigoArticulo, PrecioCompra, Cantidad, TotalCompra, FechaCompra) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    // Establecer los parámetros en la consulta preparada
                    statement.setString(1, codigoArticulo);
                    statement.setBigDecimal(2, precioCompra);
                    statement.setInt(3, cantidadInt);
                    statement.setBigDecimal(4, totalCompra);
                    statement.setString(5, fechaCompra);

                    // Ejecutar la consulta
                    int filasAfectadas = statement.executeUpdate();

                    // Comprobar si se insertó correctamente
                    if (filasAfectadas > 0) {
                        // Cargar los datos actualizados en la tabla de ComprasController
                        cargarDatosCompras();

                        // Notificar al InventarioController que actualice sus datos
                        if (inventarioController != null) {
                            inventarioController.cargarDatos();
                        }
                    } else {
                        mostrarError("No se pudo crear la compra");
                    }
                }
                // Cargar los datos actualizados en la tabla de ComprasController
                cargarDatosCompras();
            } catch (SQLException e) {
                mostrarError("Error al conectar a la base de datos: " + e.getMessage());
            }

            cerrarVentana();

        } catch (NumberFormatException e) {
            // Capturar excepción si la cantidad no es un número valido, como un decimal
            mostrarError("La cantidad debe ser un número entero valido");
        }
    }

    // Metodo para obtener un artículo de la tabla "articulos" por su codigo de articulo
    private Articulos obtenerArticuloPorCodigo(String codigoArticulo) {
        Articulos articulo = null;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL para seleccionar el artículo por su código
            String sql = "SELECT * FROM articulos WHERE codigo_articulo = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer el parámetro en la consulta preparada
                statement.setString(1, codigoArticulo);

                try (ResultSet resultSet = statement.executeQuery()) {
                    // Verificar si se encontró algún resultado
                    if (resultSet.next()) {
                        // Se crea un objeto Articulos con los datos obtenidos de la consulta
                        articulo = new Articulos(
                                resultSet.getString("codigo_articulo"),
                                resultSet.getString("articulo"),
                                resultSet.getInt("codigo_proveedor"),
                                resultSet.getBigDecimal("precio_compra"),
                                resultSet.getBigDecimal("precio_venta"),
                                resultSet.getDate("fecha_alta").toLocalDate()
                        );
                    }
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }
        return articulo;
    }

    private boolean existeArticuloEnInventario(Connection connection, String codigoArticulo) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM inventario WHERE CodigoArticulo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigoArticulo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }

    private void actualizarCantidadEnInventario(Connection connection, String codigoArticulo, int cantidad) throws SQLException {
        String sql = "UPDATE inventario SET CantidadDisponible = CantidadDisponible + ? WHERE CodigoArticulo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cantidad);
            statement.setString(2, codigoArticulo);
            statement.executeUpdate();
        }
    }

    private void insertarNuevoArticuloEnInventario(Connection connection, String codigoArticulo, int cantidad) throws SQLException {
        String sql = "INSERT INTO inventario (CodigoArticulo, CantidadDisponible) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigoArticulo);
            statement.setInt(2, cantidad);
            statement.executeUpdate();
        }
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        
        stage.close();
    }

    private void cargarDatosCompras() {
        if (comprasController != null) {
            comprasController.cargarDatos();
        }
    }
}
