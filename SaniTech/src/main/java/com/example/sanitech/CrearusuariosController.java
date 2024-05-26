package com.example.sanitech;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CrearusuariosController {

    @FXML
    private TextField tfId;

    @FXML
    private TextField tfNombre;

    @FXML
    private TextField tfRol;

    @FXML
    private TextField tfContrasena;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Label lbError;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private ComboBox<String> cbRol;

    @FXML
    private HBox topHbox;

    @FXML
    private ImageView ivImagenUsuario;

    private File archivoImagenSeleccionado;
    private AdministracionController adminController;
    private double xOffset = 0;
    private double yOffset = 0;

    public void setAdministracionController(AdministracionController adminController) {
        this.adminController = adminController;
    }

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbRol.setItems(FXCollections.observableArrayList("Administrador", "Empleado"));

        // Configura el evento de clic para el botón "Guardar" y "Cancelar"
        btnGuardar.setOnAction(event -> guardarUsuario());
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

    @FXML
    private void cargarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagenes", "*.jpg", "*.jpeg", "*.png")
        );
        archivoImagenSeleccionado = fileChooser.showOpenDialog(null);
        if (archivoImagenSeleccionado != null) {
            Image image = new Image(archivoImagenSeleccionado.toURI().toString());
            ivImagenUsuario.setImage(image);
        }
    }

    private void guardarUsuario() {
        // Obtener los datos ingresados por el usuario
        String id = tfId.getText();
        String nombre = tfNombre.getText();
        String contrasena = tfContrasena.getText();
        String rol = cbRol.getValue();

        // Validar que los campos no estén vacíos
        if (id.isEmpty() || nombre.isEmpty() || contrasena.isEmpty() || rol.isEmpty()) {
            // Mostrar un mensaje de error o realizar alguna acción adecuada
            mostrarError("Todos los campos son obligatorios");
            return;
        }

        // Validar que el Id y la contraseña contengan solo números
        if (!id.matches("\\d+")) {
            mostrarError("Solo se admiten números en el campo ID");
            return;
        }

        if (!contrasena.matches("\\d+")) {
            mostrarError("Solo se admiten números en el campo Contraseña");
            return;
        }

        // Establecer la conexión con la base de datos
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL de inserción
            String sql = "INSERT INTO usuarios (id, nombre, password, rol, imagen) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los parámetros en la consulta preparada
                statement.setString(1, id);
                statement.setString(2, nombre);
                statement.setString(3, contrasena);
                statement.setString(4, rol);

                // Manejo del archivo de imagen
                if (archivoImagenSeleccionado != null) {
                    try (InputStream inputStream = new FileInputStream(archivoImagenSeleccionado)) {
                        statement.setBlob(5, inputStream);
                        // Ejecutar la consulta
                        int filasAfectadas = statement.executeUpdate();
                        // Comprobar si se insertó correctamente
                        if (filasAfectadas > 0) {
                            // Cargar los datos actualizados en la tabla de AdministracionController
                            cargarDatosAdministracion();
                        } else {
                            mostrarError("No se pudo crear el usuario");
                        }
                    } catch (FileNotFoundException e) {
                        mostrarError("Archivo de imagen no encontrado: " + e.getMessage());
                        return;
                    } catch (IOException e) {
                        mostrarError("Error al leer el archivo de imagen: " + e.getMessage());
                        return;
                    }
                } else {
                    // Si no se selecciona ninguna imagen, establecer el campo de imagen como NULL
                    statement.setNull(5, java.sql.Types.BLOB);
                    // Ejecutar la consulta
                    int filasAfectadas = statement.executeUpdate();
                    // Comprobar si se insertó correctamente
                    if (filasAfectadas > 0) {
                        // Cargar los datos actualizados en la tabla de AdministracionController
                        cargarDatosAdministracion();
                    } else {
                        mostrarError("No se pudo crear el usuario");
                    }
                }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Cerrar la ventana
        cerrarVentana();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        // Cerrar la ventana actual
        stage.close();
    }

    static void mostrarError(String mensaje) {
        try {
            FXMLLoader loader = new FXMLLoader(CrearusuariosController.class.getResource("error.fxml"));
            Parent root = loader.load();

            ErrorController errorController = loader.getController();
            errorController.setError(mensaje);
            errorController.setStage(new Stage()); // Establece el stage en el controlador de error

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Error");
            stage.setResizable(false); // Para que no se pueda agrandar
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarDatosAdministracion() {
        if (adminController != null) {
            adminController.cargarDatos();
        }
    }
}
