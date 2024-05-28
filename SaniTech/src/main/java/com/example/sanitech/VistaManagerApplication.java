package com.example.sanitech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.*;

public class VistaManagerApplication extends Application {
    private LoginController loginController;
    private static boolean modoOscuro = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("vistamanager.fxml"));
        Parent root = fxmlLoader.load();
        VistaManagerController controller = fxmlLoader.getController(); // Obtener instancia del controlador
        controller.setStage(primaryStage); // Pasar la referencia al Stage

        // Obtener una referencia al controlador de la vista de inventario después de cargar el archivo FXML
        FXMLLoader inventarioLoader = new FXMLLoader(LoginApplication.class.getResource("inventario.fxml"));
        inventarioLoader.load();
        InventarioController inventarioController = inventarioLoader.getController();

        Scene scene = new Scene(root, 320, 240);
        primaryStage.setTitle("Manager");
        primaryStage.setScene(scene);
        controller.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        // Establecer tamaño mínimo para el Stage después de que la escena se haya establecido en el Stage
        primaryStage.setMinWidth(1400);
        primaryStage.setMinHeight(700);

        // Obtener información del usuario desde el login y actualizar los labels
        String usuarioConectado = loginController.obtenerUsuarioConectado();
        if (usuarioConectado != null) {
            obtenerInformacionUsuario(usuarioConectado, controller);
        }

        // Se aplica el modo oscuro si está activado
        if (modoOscuro) {
            controller.setModoOscuro(true);
        }

        // Se centra la ventana en la pantalla
        primaryStage.centerOnScreen();
    }

    private void obtenerInformacionUsuario(String usuario, VistaManagerController controller) {
        // Lógica para obtener la información del usuario desde la base de datos y luego llamar al método en el controlador para actualizar los labels
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT nombre, rol, id FROM usuarios WHERE nombre = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                // Establece los parámetros de la consulta
                statement.setString(1, usuario);
                // Ejecuta la consulta
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Si se encuentra una fila en el resultado, actualiza la información del usuario en la vista
                    if (resultSet.next()) {
                        String nombreUsuario = resultSet.getString("nombre");
                        String rolUsuario = resultSet.getString("rol");
                        String idUsuario = resultSet.getString("id");
                        controller.actualizarInformacionUsuario(nombreUsuario, rolUsuario, idUsuario);
                    } else {
                        // Maneja el caso en que el usuario no se encuentre en la base de datos
                        System.out.println("Usuario no encontrado en la base de datos");
                    }
                }
            }
        } catch (SQLException e) {
            // Maneja cualquier error de SQL
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
}
