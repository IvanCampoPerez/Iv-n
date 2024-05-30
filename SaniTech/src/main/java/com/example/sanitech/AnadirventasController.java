package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.*;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class AnadirventasController {
    private VentasController ventasController;
    private double xOffset = 0;
    private double yOffset = 0;
    private String empleadoId;

    @FXML
    private TextField tfClienteId;

    @FXML
    private DatePicker dpFechaFactura;

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

    public void setVentasController(VentasController ventasController) {
        this.ventasController = ventasController;
    }

    private ObservableList<String> sugerenciasClientes = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lvSugerencias;

    @FXML
    private void initialize() {
        // Configura el evento de clic para el botón "Guardar" y "Cancelar"
        btnGuardar.setOnAction(event -> anadirVenta());
        btnCancelar.setOnAction(event -> cerrarVentana());

        // Se configura el autocompletado para tfClienteId
        tfClienteId.setOnKeyReleased(this::actualizarSugerencias);
        lvSugerencias.setOnMouseClicked(event -> {
            String selected = lvSugerencias.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfClienteId.setText(selected);
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
        String textoIngresado = tfClienteId.getText();
        if (textoIngresado.isEmpty()) {
            lvSugerencias.setVisible(false);
            return;
        }

        sugerenciasClientes.clear();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            String sql = "SELECT ClienteId FROM clientes WHERE ClienteId LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, textoIngresado + "%");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    sugerenciasClientes.add(rs.getString("ClienteId"));
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        if (!sugerenciasClientes.isEmpty()) {
            lvSugerencias.setItems(sugerenciasClientes);
            lvSugerencias.setVisible(true);
        } else {
            lvSugerencias.setVisible(false);
        }
    }

    private void anadirVenta() {
        // Obtener los datos ingresados por el usuario
        String clienteId = tfClienteId.getText().trim();
        String fechaFactura = null;
        if (dpFechaFactura.getValue() != null) {
            fechaFactura = dpFechaFactura.getValue().toString();
        }

        if (clienteId.isEmpty()) {
            mostrarError("El campo ClienteId está vacío");
            return;
        }

        try {
            Integer.parseInt(clienteId);
        } catch (NumberFormatException e) {
            mostrarError("El ClienteId debe ser un número entero");
            return; // Se sale del método si el ClienteId no es un número entero
        }

        // Validar que la fecha de factura tenga el formato "año-mes-día"
        if (fechaFactura == null || !fechaFactura.matches("\\d{4}-\\d{2}-\\d{2}")) {
            mostrarError("El formato de la fecha de factura debe ser dia/mes/año (por ejemplo, 21/01/2024)");
            return;
        }

        // Validar que el ClienteId esté asociado al EmpleadoId del usuario conectado
        if (!clienteAsociadoAlEmpleado(clienteId, empleadoId)) {
            mostrarError("El ClienteId no está asociado a su EmpleadoId");
            return;
        }

        // Insertar el registro en la tabla de ventas
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        String sql = "INSERT INTO ventas (ClienteId, FechaFactura, TotalBase, TotalIVA, Total) VALUES (?, ?, 0.00, 0.00, 0.00)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, clienteId);
            statement.setString(2, fechaFactura);

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                cargarDatosVentas(); // Actualizar los datos en la ventana de ventas
            } else {
                mostrarError("Error al añadir la venta");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al añadir la venta: " + e.getMessage());
        }

        cerrarVentana();
    }


    private boolean clienteAsociadoAlEmpleado(String clienteId, String empleadoId) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        // Consulta SQL para verificar si el ClienteId está asociado al EmpleadoId
        String sql = "SELECT COUNT(*) AS count FROM clientes WHERE ClienteId = ? AND EmpleadoId = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, clienteId);
            statement.setString(2, empleadoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                // Si se encuentra una fila en el resultado, significa que el ClienteId está asociado al EmpleadoId
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si no se encontraron resultados o hubo un error, devuelve false
        return false;
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        stage.close();
    }

    private void cargarDatosVentas() {
        if (ventasController != null) {
            ventasController.cargarDatos();
        }
    }

    public void setEmpleadoId(String empleadoId) {
        this.empleadoId = empleadoId;
    }
}
