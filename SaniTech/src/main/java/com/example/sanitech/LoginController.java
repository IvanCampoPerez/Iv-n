package com.example.sanitech;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class LoginController {

    private Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField usuarioTextField;

    @FXML
    private TextField contraseniaTextField;

    @FXML
    private PasswordField contraseniaPasswordField;

    @FXML
    private CheckBox mostrarContraseniaCheckBox;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private HBox topHbox;

    @FXML
    private void initialize() {
        // Evento para presionar y arrastrar la ventana
        topHbox.setOnMousePressed(event -> {
            Stage stage = (Stage) topHbox.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topHbox.setOnMouseDragged(event -> {
            Stage stage = (Stage) topHbox.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Configuración del evento del CheckBox
        mostrarContraseniaCheckBox.setOnAction(event -> {
            if (mostrarContraseniaCheckBox.isSelected()) {
                // Se muestra el TextField mientras se oculta el PasswordField
                contraseniaTextField.setText(contraseniaPasswordField.getText());
                contraseniaPasswordField.setVisible(false);
                contraseniaTextField.setVisible(true);
            } else {
                // Se muestra el PasswordField mientras se oculta el TextField
                contraseniaPasswordField.setText(contraseniaTextField.getText());
                contraseniaTextField.setVisible(false);
                contraseniaPasswordField.setVisible(true);
            }
        });
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

    public void iniciarSesion() { // Metodo para verificar las credenciales
        String usuario = usuarioTextField.getText();
        String contrasenia = contraseniaPasswordField.getText();
        boolean usuarioCorrecto = verificarUsuario(usuario);

        if (usuario.isEmpty() && !contrasenia.isEmpty()) {
            mostrarError("El campo usuario está vacío");
            return;
        }

        if (contrasenia.isEmpty() && !usuario.isEmpty()) {
            mostrarError("El campo de contraseña está vacío");
            return;
        }

        if (usuario.isEmpty() && contrasenia.isEmpty()) {
            mostrarError("Ambos campos están vacíos");
            return;
        }

        if (usuarioCorrecto) {
            // Si el usuario es correcto, verifica la contraseña
            boolean contraseniaCorrecta = verificarContrasenia(usuario, contrasenia);
            if (contraseniaCorrecta) {
                // Si tanto el usuario como la contraseña son correctos, cierra la ventana de inicio de sesión
                primaryStage.close();
                // Se abre la ventana de la aplicación principal
                VistaManagerApplication vistaManagerApp = new VistaManagerApplication();
                vistaManagerApp.setLoginController(this);
                Platform.runLater(() -> {
                    try {
                        vistaManagerApp.start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                mostrarError("Contraseña incorrecta");
            }
        } else {
            mostrarError("Usuario incorrecto");
        }
    }

    private boolean verificarUsuario(String usuario) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM usuarios WHERE nombre = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, usuario);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return !resultSet.getBoolean("esta_conectado");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verificarContrasenia(String usuario, String contrasenia) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT * FROM usuarios WHERE nombre = ? AND password = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, usuario);
                statement.setString(2, contrasenia);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Actualizar esta_conectado a TRUE
                        String updateSql = "UPDATE usuarios SET esta_conectado = TRUE WHERE nombre = ?";
                        try (PreparedStatement updateStatement = conn.prepareStatement(updateSql)) {
                            updateStatement.setString(1, usuario);
                            updateStatement.executeUpdate();
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String obtenerUsuarioConectado() {
        String usuario = usuarioTextField.getText();
        String contrasenia = contraseniaPasswordField.getText();
        boolean credencialesCorrectas = verificarContrasenia(usuario, contrasenia);

        if (credencialesCorrectas) {
            return usuario; // Devuelve el usuario conectado si las credenciales son correctas
        } else {
            System.out.println("Credenciales incorrectas");
            return null; // Devuelve null si las credenciales son incorrectas
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
