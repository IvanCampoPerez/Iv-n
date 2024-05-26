package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class ModificarinventarioController {

    private InventarioController inventarioController;
    private Inventario inventarioSeleccionado;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfCantidaddisponible;

    @FXML
    private javafx.scene.control.Button btnGuardar;

    @FXML
    private javafx.scene.control.Button btnCancelar;

    @FXML
    private javafx.scene.control.Button btnMinimizar;

    @FXML
    private javafx.scene.control.Button btnCerrar;

    @FXML
    private void initialize() {
        btnGuardar.setOnAction(event -> modificarInventario());
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

    public void setInventarioController(InventarioController inventarioController) { // Setter para establecer el controlador de InventarioController
        this.inventarioController = inventarioController;
    }

    public void mostrarDatosInventario(Inventario inventario) {
        inventarioSeleccionado = inventario;
        tfCantidaddisponible.setText(String.valueOf(inventario.getCantidadDisponible()));
    }

    public void initData(Inventario inventario) {
        this.inventarioSeleccionado = inventario;
        // Mostrar los datos del inventario en los textfields
        mostrarDatosInventario(inventario);
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        // Cerrar la ventana actual
        stage.close();
    }

    private void modificarInventario() {
        // Obtener los datos ingresados por el usuario
        String cantidadDisponible = tfCantidaddisponible.getText();

        // Validar que la cantidadDisponible no esté vacía
        if (cantidadDisponible.isEmpty()) {
            mostrarError("El campo CantidadDisponible es obligatorio");
            return;
        }

        try {
            // Convierto la cantidad a un entero para realizar las validaciones
            int cantidadInt = Integer.parseInt(cantidadDisponible);

            if (cantidadInt < 0) {
                mostrarError("Debes introducir una cantidad igual o mayor a cero");
                return;
            }

            if (cantidadInt > inventarioSeleccionado.getCantidadDisponible()) {
                mostrarError("No se puede añadir mas cantidad desde el inventario");
                return;
            }

            if (cantidadDisponible.length() > 3) {
                mostrarError("La cantidad no puede tener más de 3 dígitos");
                return;
            }

            // Si la cantidad disponible es cero, mostrar un mensaje de confirmación
            if (cantidadInt == 0) {
                if (!confirmarEliminacion()) {
                    return; // Si el usuario cancela, se sale del metodo
                }
            }

            // Establecer la conexión con la base de datos
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
                // Preparar la consulta SQL de actualizacion o eliminacion
                String sql;
                if (cantidadInt == 0) {
                    // Si la cantidad es cero, ejecutar la sentencia DELETE
                    sql = "DELETE FROM inventario WHERE InventarioId = ?";
                } else {
                    // Si la cantidad no es cero, ejecutar la sentencia UPDATE
                    sql = "UPDATE inventario SET CantidadDisponible = ? WHERE InventarioId = ?";
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    // Establecer los parámetros en la consulta preparada
                    if (cantidadInt == 0) {
                        statement.setString(1, String.valueOf(inventarioSeleccionado.getInventarioId()));
                    } else {
                        statement.setString(1, cantidadDisponible);
                        statement.setString(2, String.valueOf(inventarioSeleccionado.getInventarioId()));
                    }

                    // Ejecutar la consulta
                    int filasAfectadas = statement.executeUpdate();

                    // Comprobar si se actualizo correctamente
                    if (filasAfectadas > 0) {
                        // Cargar los datos actualizados en la tabla de InventarioController
                        cargarDatosInventario();
                    } else {
                        mostrarError("No se pudo modificar el artículo");
                    }
                }
            } catch (SQLException e) {
                mostrarError("Error al conectar a la base de datos: " + e.getMessage());
            }

            // Actualizar la tabla de inventario en InventarioController despues de modificar el articulo
            if (inventarioController != null) {
                inventarioController.cargarDatos();
            }

            // Cerrar la ventana
            cerrarVentana();

        } catch (NumberFormatException e) {
            // Capturar excepción si la cantidad no es un número valido, como un decimal
            mostrarError("La cantidad debe ser un número entero valido");
        }
    }

    private boolean confirmarEliminacion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("La cantidad disponible será establecida a cero. ¿Estás seguro de que quieres eliminar este registro?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void cargarDatosInventario() {
        if (inventarioController != null) {
            inventarioController.cargarDatos();
        }
    }
}
