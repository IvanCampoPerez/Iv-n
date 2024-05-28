package com.example.sanitech;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

import java.util.Optional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdministracionController {
    @FXML
    private Button btnCrear;

    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<Usuario> tbUsuarios;

    @FXML
    private TableColumn<Usuario, Integer> colId;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private void initialize() {
        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Usuario
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        btnCrear.setOnAction(event -> abrirVentanaCrearUsuario());

        // Se llena la tabla con datos de la base de datos
        cargarDatosDesdeBD();
    }

    @FXML
    private void eliminarUsuario() {
        // Obtener el usuario seleccionado en la tabla
        Usuario usuarioSeleccionado = tbUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            CrearusuariosController.mostrarError("Por favor, selecciona un usuario para eliminar");
            return;
        }

        // Verificar si el usuario tiene el rol de 'Administrador'
        if (usuarioSeleccionado.getRol().equals("Administrador")) {
            CrearusuariosController.mostrarError("No se puede eliminar a un usuario con el rol de Administrador");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de que quieres eliminar este usuario? Su EmpleadoId en clientes se eliminará por consiguiente.");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarUsuarioBD(usuarioSeleccionado)) {
                // Si la eliminación se realiza, se muestra un mensaje de éxito
                mostrarInformacion("El usuario fue eliminado correctamente");

                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar
                CrearusuariosController.mostrarError("No se pudo eliminar el usuario");
            }
        }
    }

    private void cargarDatosDesdeBD() {
        try {
            // Conexión a la base de datos "saneamientos"
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "usuarios"
            String query = "SELECT id, nombre, rol FROM usuarios";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbUsuarios.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String rol = rs.getString("rol");
                tbUsuarios.getItems().add(new Usuario(id, nombre, rol));
            }

            // Se cierra la conexión
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para abrir la ventana de creación de usuario
    private void abrirVentanaCrearUsuario() {
        try {
            // Cargar el archivo FXML de crearusuarios.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("crearusuarios.fxml"));
            Parent root = loader.load();

            CrearusuariosController crearusuariosController = loader.getController();
            crearusuariosController.setAdministracionController(this); // Configura la referencia al AdministracionController

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Crear Usuario");

            // Mostrar la ventana
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para eliminar un usuario de la base de datos
    private boolean eliminarUsuarioBD(Usuario usuario) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Se desactiva el modo de autocommit
            connection.setAutoCommit(false);

            try {
                // Actualizar los registros de la tabla clientes asociados al usuario a null
                String updateClientesSql = "UPDATE clientes SET EmpleadoId = NULL WHERE EmpleadoId = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateClientesSql)) {

                    updateStatement.setInt(1, usuario.getId());
                    // Ejecutar la consulta de actualización
                    updateStatement.executeUpdate();
                }

                // Eliminar el usuario de la tabla usuarios
                String deleteUsuarioSql = "DELETE FROM usuarios WHERE id = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteUsuarioSql)) {

                    deleteStatement.setInt(1, usuario.getId());
                    // Ejecutar la consulta de eliminación
                    int filasAfectadas = deleteStatement.executeUpdate();

                    // Comprobar si se eliminó correctamente
                    if (filasAfectadas > 0) {
                        // Confirmar los cambios en la base de datos
                        connection.commit();
                        return true;
                    } else {
                        // Si no se eliminó ninguna fila, revertir los cambios
                        connection.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                // En caso de error, revertir los cambios
                connection.rollback();
                e.printStackTrace();
                return false;
            } finally {
                // Reactivar el modo de autocommit
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para mostrar una alerta de información
    private void mostrarInformacion(String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
