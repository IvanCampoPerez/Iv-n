package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.math.BigDecimal;

import java.sql.*;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class AnadirarticulosController {

    private ArticulosController articulosController;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfCodigoarticulo;

    @FXML
    private TextField tfCodigoproveedor;

    @FXML
    private TextField tfArticulo;

    @FXML
    private TextField tfPreciocompra;

    @FXML
    private TextField tfPrecioventa;

    @FXML
    private DatePicker dpFechaalta;

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

    public void setArticulosController(ArticulosController articulosController) {
        this.articulosController = articulosController;
    }

    private ObservableList<String> sugerenciasProveedores = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lvSugerencias;

    @FXML
    private void initialize() {
        // Configura el evento de clic para el botón "Guardar" y "Cancelar"
        btnGuardar.setOnAction(event -> anadirArticulo());
        btnCancelar.setOnAction(event -> cerrarVentana());

        // Se configura el autocompletado para tfCodigoproveedor
        tfCodigoproveedor.setOnKeyReleased(this::actualizarSugerencias);
        lvSugerencias.setOnMouseClicked(event -> {
            String selected = lvSugerencias.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfCodigoproveedor.setText(selected);
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
        String textoIngresado = tfCodigoproveedor.getText();
        if (textoIngresado.isEmpty()) {
            lvSugerencias.setVisible(false);
            return;
        }

        sugerenciasProveedores.clear();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            String sql = "SELECT Codigo_proveedor FROM proveedores WHERE Codigo_proveedor LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, textoIngresado + "%");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    sugerenciasProveedores.add(rs.getString("Codigo_proveedor"));
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        if (!sugerenciasProveedores.isEmpty()) {
            lvSugerencias.setItems(sugerenciasProveedores);
            lvSugerencias.setVisible(true);
        } else {
            lvSugerencias.setVisible(false);
        }
    }

    private void anadirArticulo() {
        // Se obtienen los datos ingresados por el usuario de los textfields
        String codigoArticulo = tfCodigoarticulo.getText();
        String articulo = tfArticulo.getText();
        String codigoProveedor = tfCodigoproveedor.getText();
        String precioCompra = tfPreciocompra.getText();
        String precioVenta = tfPrecioventa.getText();
        String fechaAlta = null;
        if (dpFechaalta.getValue() != null) {
            fechaAlta = dpFechaalta.getValue().toString();
        }

        if (codigoArticulo.isEmpty() || codigoArticulo.contains(" ")) {
            mostrarError("El campo código_articulo es obligatorio y no puede contener espacios en blanco");
            return;
        }

        if (articulo.isEmpty()) {
            mostrarError("El campo articulo es obligatorio");
            return;
        }

        if (!codigoProveedor.isEmpty() && !codigoProveedor.matches("\\d+")) {
            mostrarError("Solo se admiten números en el campo codigo_proveedor");
            return;
        }

        // Validar que el precio de compra y el precio de venta sea un numero decimal
        try {
            // Convertir los precios a números decimales
            BigDecimal precioCompraDecimal = new BigDecimal(precioCompra);
            BigDecimal precioVentaDecimal = new BigDecimal(precioVenta);

            // Validar que los precios sean positivos
            if (precioCompraDecimal.compareTo(BigDecimal.ZERO) < 0 || precioVentaDecimal.compareTo(BigDecimal.ZERO) < 0) {
                mostrarError("Los precios no pueden ser negativos");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Los precios deben ser números decimales");
            return;
        }

        // Validar que la fecha de alta tenga el formato "año-mes-día"
        if (fechaAlta == null || !fechaAlta.matches("\\d{4}-\\d{2}-\\d{2}")) {
            mostrarError("El formato de la fecha de alta debe ser dia/mes/año (por ejemplo, 21/01/2024)");
            return;
        }

        // Conexión con la base de datos
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL de inserción
            String sql = "INSERT INTO articulos (codigo_articulo, articulo, codigo_proveedor, precio_compra, precio_venta, fecha_alta) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los parámetros en la consulta preparada
                statement.setString(1, codigoArticulo);
                statement.setString(2, articulo);
                statement.setString(3, codigoProveedor);
                statement.setString(4, precioCompra);
                statement.setString(5, precioVenta);
                statement.setString(6, fechaAlta);

                // Ejecutar la consulta
                int filasAfectadas = statement.executeUpdate();

                // Comprobar si se insertó correctamente
                if (filasAfectadas > 0) {
                    // Cargar los datos actualizados en la tabla de ArticulosController
                    cargarDatosArticulos();
                } else {
                    mostrarError("No se pudo crear el articulo");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Cerrar la ventana
        cerrarVentana();
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        stage.close();
    }

    private void cargarDatosArticulos() {
        if (articulosController != null) {
            articulosController.cargarDatos();
        }
    }
}
