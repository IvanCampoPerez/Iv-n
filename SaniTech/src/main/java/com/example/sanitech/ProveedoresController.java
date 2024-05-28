package com.example.sanitech;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;
import java.util.Optional;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class ProveedoresController {

    @FXML
    private Button btnAnadir;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private TableView<Proveedor> tbProveedores;

    @FXML
    private TableColumn<Proveedor, Integer> colCodigoproveedor;

    @FXML
    private TableColumn<Proveedor, String> colOrganizacion;

    @FXML
    private TableColumn<Proveedor, String> colTelefono;

    @FXML
    private TableColumn<Proveedor, String> colEMail;

    @FXML
    private ComboBox<String> cbProveedores;

    @FXML
    private TextField tfProveedores;

    @FXML
    private Label lbCodigoproveedor;

    @FXML
    private Label lbOrganizacion;

    @FXML
    private Tooltip ttOrganizacion;

    @FXML
    private Tooltip ttEmail;

    @FXML
    private Label lbTelefono;

    @FXML
    private Label lbEMail;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbProveedores.setItems(FXCollections.observableArrayList("Codigo_proveedor", "Organizacion", "Telefono", "EMail"));
        
        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Proveedor
        colCodigoproveedor.setCellValueFactory(new PropertyValueFactory<>("Codigo_proveedor"));
        colOrganizacion.setCellValueFactory(new PropertyValueFactory<>("Organizacion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("Telefono"));
        colEMail.setCellValueFactory(new PropertyValueFactory<>("EMail"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirProveedor());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbProveedores.setOnAction(event -> filtrarRegistros());
        tfProveedores.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbCodigoproveedor.setText(String.valueOf(newSelection.getCodigoproveedor()));
                lbOrganizacion.setText(newSelection.getOrganizacion());
                ttOrganizacion.setText(newSelection.getOrganizacion()); // Se crean tooltips para labels que son muy largos
                lbTelefono.setText(newSelection.getTelefono());
                lbEMail.setText(newSelection.getEmail());
                ttEmail.setText(newSelection.getEmail());
            }
        });
    }

    @FXML
    private void ventanaModificarProveedor() {
        // Verificar si hay una fila seleccionada en la tabla
        Proveedor proveedorSeleccionado = tbProveedores.getSelectionModel().getSelectedItem();
        if (proveedorSeleccionado != null) {
            try {
                // Cargar el archivo FXML de modificarproveedor.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("modificarproveedor.fxml"));
                Parent root = loader.load();

                ModificarproveedoresController modificarproveedoresController = loader.getController();

                // Pasar los datos del proveedor seleccionado al controlador de la ventana de modificar proveedor
                modificarproveedoresController.initData(proveedorSeleccionado);

                // Se establece la referencia al ProveedoresController
                modificarproveedoresController.setProveedoresController(this);

                // Crear una nueva escena
                Scene scene = new Scene(root);

                // Configurar el escenario (stage)
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modificar Proveedor");

                // Mostrar la ventana
                stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay fila seleccionada, mostrar un mensaje al usuario
            mostrarError("Por favor, seleccione un proveedor para modificar.");
        }
    }

    @FXML
    private void eliminarProveedor() {
        // Obtener el proveedor seleccionado en la tabla
        Proveedor proveedorSeleccionado = tbProveedores.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado == null) {
            mostrarError("Por favor, selecciona un proveedor para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar este proveedor? Su código de proveedor en los artículos se eliminará por consiguiente.");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarProveedorBD(proveedorSeleccionado)) {
                // Si la eliminación fue exitosa, mostrar un mensaje de éxito
                mostrarInformacion("El proveedor fue eliminado correctamente");
                // Actualizar la tabla de proveedores
                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar, mostrar un mensaje de error
                mostrarError("No se pudo eliminar el proveedor debido a que tiene un articulo asociado");
            }
        }
    }

    // Método para eliminar un proveedor de la base de datos
    private boolean eliminarProveedorBD(Proveedor proveedor) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Desactivar el modo de autocommit
            connection.setAutoCommit(false);

            try {
                // Eliminar las referencias del proveedor en la tabla articulo_proveedor
                String deleteArticuloProveedorSql = "DELETE FROM articulo_proveedor WHERE codigo_proveedor = ?";
                try (PreparedStatement deleteArticuloProveedorStatement = connection.prepareStatement(deleteArticuloProveedorSql)) {
                    deleteArticuloProveedorStatement.setInt(1, proveedor.getCodigoproveedor());
                    deleteArticuloProveedorStatement.executeUpdate();
                }

                // Eliminar el proveedor de la tabla proveedores
                String deleteProveedorSql = "DELETE FROM proveedores WHERE Codigo_proveedor = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteProveedorSql)) {
                    deleteStatement.setInt(1, proveedor.getCodigoproveedor());
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarDatosDesdeBD() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "proveedores"
            String query = "SELECT Codigo_proveedor, Organizacion, Telefono, EMail FROM proveedores";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbProveedores.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int Codigo_proveedor = rs.getInt("Codigo_proveedor");
                String Organizacion = rs.getString("Organizacion");
                String Telefono = rs.getString("Telefono");
                String EMail = rs.getString("EMail");
                tbProveedores.getItems().add(new Proveedor(Codigo_proveedor, Organizacion, Telefono, EMail));
            }

            // Cerrar la conexión
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para abrir la ventana de añadir proveedor
    private void abrirVentanaAnadirProveedor() {
        try {
            // Cargar el archivo FXML de anadirproveedor.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadirproveedor.fxml"));
            Parent root = loader.load();

            AnadirproveedoresController anadirproveedoresController = loader.getController();
            anadirproveedoresController.setProveedoresController(this); // Configura la referencia al ProveedoresController

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir Proveedor");

            // Mostrar la ventana
            stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtrarRegistros() {
        String columnaSeleccionada = cbProveedores.getValue();
        String textoFiltrado = tfProveedores.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbProveedores.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "Codigo_proveedor":
                tbProveedores.getItems().removeIf(proveedor -> !String.valueOf(proveedor.getCodigoproveedor()).toLowerCase().contains(textoFiltrado));
                break;
            case "Organizacion":
                tbProveedores.getItems().removeIf(proveedor -> !proveedor.getOrganizacion().toLowerCase().contains(textoFiltrado));
                break;
            case "Telefono":
                tbProveedores.getItems().removeIf(proveedor -> {
                    String telefono = proveedor.getTelefono(); // Elimina los campos nulls
                    return telefono != null && !telefono.toLowerCase().contains(textoFiltrado); // Elimina lo que no coincida con el filtro y no sea null
                });
                break;
            case "EMail":
                tbProveedores.getItems().removeIf(proveedor -> !proveedor.getEmail().toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
