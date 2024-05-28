package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class AnadirproveedoresController {

    private ProveedoresController proveedoresController;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfOrganizacion;

    @FXML
    private TextField tfTelefono;

    @FXML
    private TextField tfEmail;

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

    public void setProveedoresController(ProveedoresController proveedoresController) {
        this.proveedoresController = proveedoresController;
    }

    @FXML
    private void initialize() {
        // Configura el evento de clic para el botón "Guardar" y "Cancelar"
        btnGuardar.setOnAction(event -> anadirProveedor());
        btnCancelar.setOnAction(event -> cerrarVentana());
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

    private void anadirProveedor() {
        // Obtener los datos ingresados por el usuario
        String organizacion = tfOrganizacion.getText();
        String telefono = tfTelefono.getText();
        String email = tfEmail.getText();

        if (organizacion.isEmpty()) {
            mostrarError("El campo Organización es obligatorio");
            return;
        }

        if (!telefono.isEmpty() && !telefono.matches("\\d+")) {
            mostrarError("Solo se admiten números en el campo Telefono");
            return;
        }

        // Se valida que el formato del email tenga al menos un caracter antes y después del símbolo "@", seguida de un punto y al menos dos letras al final del email
        if (!email.isEmpty() && !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            mostrarError("El correo electrónico ingresado no es válido");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL de inserción
            String sql = "INSERT INTO proveedores (Organizacion, Telefono, Email) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los parámetros en la consulta preparada
                statement.setString(1, organizacion);
                statement.setString(2, telefono);
                statement.setString(3, email);

                // Ejecutar la consulta
                int filasAfectadas = statement.executeUpdate();

                // Comprobar si se insertó correctamente
                if (filasAfectadas > 0) {
                    // Cargar los datos actualizados en la tabla de ProveedoresController
                    cargarDatosProveedores();
                } else {
                    mostrarError("No se pudo crear el proveedor");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        cerrarVentana();
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        stage.close();
    }

    private void cargarDatosProveedores() {
        if (proveedoresController != null) {
            proveedoresController.cargarDatos();
        }
    }
}
