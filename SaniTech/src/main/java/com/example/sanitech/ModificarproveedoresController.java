package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class ModificarproveedoresController {

    private ProveedoresController proveedoresController;
    private Proveedor proveedorSeleccionado;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfOrganizacion;

    @FXML
    private TextField tfTelefono;

    @FXML
    private TextField tfEmail;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private void initialize() {
        btnGuardar.setOnAction(event -> modificarProveedor());
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

    public void setProveedoresController(ProveedoresController proveedoresController) { // Setter para establecer el controlador de ProveedoresController
        this.proveedoresController = proveedoresController;
    }

    public void mostrarDatosProveedor(Proveedor proveedor) {
        proveedorSeleccionado = proveedor;
        tfOrganizacion.setText(proveedor.getOrganizacion());
        tfTelefono.setText(proveedor.getTelefono());
        tfEmail.setText(proveedor.getEmail());
    }

    public void initData(Proveedor proveedor) { // Metodo para obtener el Codigo_proveedor
        this.proveedorSeleccionado = proveedor;
        // Mostrar los datos del proveedor en los textfields
        mostrarDatosProveedor(proveedor);
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        // Cerrar la ventana actual
        stage.close();
    }

    private void modificarProveedor() {
        // Obtener los datos ingresados por el usuario
        String organizacion = tfOrganizacion.getText();
        String telefono = tfTelefono.getText();
        String email = tfEmail.getText();

        // Validar que la organización no esté vacía
        if (organizacion.isEmpty()) {
            mostrarError("El campo Organización es obligatorio");
            return;
        }

        // Validar que el Telefono contenga solo números
        if (!telefono.isEmpty() && !telefono.matches("\\d+")) {
            mostrarError("Solo se admiten números en el campo Telefono");
            return;
        }

        // Validar el formato del email, que tenga al menos una letra antes y después del símbolo "@", seguida de un punto y al menos dos letras al final del email
        if (!email.isEmpty() && !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            mostrarError("El correo electrónico ingresado no es válido");
            return;
        }

        // Establecer la conexión con la base de datos
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL de actualizacion
            String sql = "UPDATE proveedores SET Organizacion = ?, Telefono = ?, EMail = ? WHERE Codigo_proveedor = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los parámetros en la consulta preparada
                statement.setString(1, organizacion);
                statement.setString(2, telefono);
                statement.setString(3, email);
                statement.setInt(4, proveedorSeleccionado.getCodigoproveedor());

                // Ejecutar la consulta
                int filasAfectadas = statement.executeUpdate();

                // Comprobar si se actualizo correctamente
                if (filasAfectadas > 0) {
                    // Cargar los datos actualizados en la tabla de ProveedoresController
                    cargarDatosProveedores();
                } else {
                    mostrarError("No se pudo modificar el proveedor");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Actualizar la tabla de proveedores en ProveedoresController despues de modificar el proveedor
        if (proveedoresController != null) {
            proveedoresController.cargarDatos();
        }

        // Cerrar la ventana
       cerrarVentana();
    }

    private void cargarDatosProveedores() {
        if (proveedoresController != null) {
            proveedoresController.cargarDatos();
        }
    }
}
