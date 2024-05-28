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

public class ClientesController {
    @FXML
    private Button btnAnadir;

    @FXML
    private Button btnModificar;

    @FXML
    private TableView<Clientes> tbClientes;

    @FXML
    private TableColumn<Clientes, Integer> colClienteId;

    @FXML
    private TableColumn<Clientes, String> colNombre;

    @FXML
    private TableColumn<Clientes, String> colApellidos;

    @FXML
    private TableColumn<Clientes, String> colCompania;

    @FXML
    private TableColumn<Clientes, String> colDireccion;

    @FXML
    private TableColumn<Clientes, String> colCiudad;

    @FXML
    private TableColumn<Clientes, String> colComunidad;

    @FXML
    private TableColumn<Clientes, String> colPais;

    @FXML
    private TableColumn<Clientes, String> colCodigoPostal;

    @FXML
    private TableColumn<Clientes, String> colTelefono;

    @FXML
    private TableColumn<Clientes, Integer> colEmpleadoId;

    @FXML
    private ComboBox<String> cbClientes;

    @FXML
    private TextField tfClientes;

    @FXML
    private Label lbClienteId;

    @FXML
    private Label lbNombre;

    @FXML
    private Label lbApellidos;

    @FXML
    private Label lbCompania;

    @FXML
    private Label lbDireccion;

    @FXML
    private Label lbCiudad;

    @FXML
    private Label lbComunidad;

    @FXML
    private Label lbPais;

    @FXML
    private Label lbCodigoPostal;

    @FXML
    private Label lbTelefono;

    @FXML
    private Label lbEmpleadoId;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbClientes.setItems(FXCollections.observableArrayList("ClienteId", "Nombre", "Apellidos", "Compañia", "Direccion", "Ciudad", "Comunidad", "Pais", "CodigoPostal", "Telefono", "EmpleadoId"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Clientes
        colClienteId.setCellValueFactory(new PropertyValueFactory<>("clienteId"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colCompania.setCellValueFactory(new PropertyValueFactory<>("compania"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colComunidad.setCellValueFactory(new PropertyValueFactory<>("comunidad"));
        colPais.setCellValueFactory(new PropertyValueFactory<>("pais"));
        colCodigoPostal.setCellValueFactory(new PropertyValueFactory<>("codigoPostal"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmpleadoId.setCellValueFactory(new PropertyValueFactory<>("empleadoId"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirCliente());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbClientes.setOnAction(event -> filtrarRegistros());
        tfClientes.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbClienteId.setText(String.valueOf(newSelection.getClienteId()));
                lbNombre.setText(newSelection.getNombre());
                lbApellidos.setText(newSelection.getApellidos());
                lbCompania.setText(newSelection.getCompania());
                lbDireccion.setText(newSelection.getDireccion());
                lbCiudad.setText(newSelection.getCiudad());
                lbComunidad.setText(newSelection.getComunidad());
                lbPais.setText(newSelection.getPais());
                lbCodigoPostal.setText(newSelection.getCodigoPostal());
                lbTelefono.setText(newSelection.getTelefono());
                lbEmpleadoId.setText(String.valueOf(newSelection.getEmpleadoId()));
            }
        });
    }

    @FXML
    private void eliminarCliente() {
        // Obtener el cliente seleccionado en la tabla
        Clientes clienteSeleccionado = tbClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarError("Por favor, selecciona un cliente para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar este cliente? Los ClienteIds en ventas se eliminarán por consiguiente");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarClienteBD(clienteSeleccionado)) {
                // Si la eliminación fue exitosa, mostrar un mensaje de éxito
                mostrarInformacion("El cliente fue eliminado correctamente");
                // Actualizar la tabla de clientes
                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar, mostrar un mensaje de error
                mostrarError("No se pudo eliminar el cliente");
            }
        }
    }

    // Método para eliminar un cliente de la base de datos
    private boolean eliminarClienteBD(Clientes clientes) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Desactivar el modo de autocommit
            connection.setAutoCommit(false);

            try {
                // Actualizar los registros de la tabla ventas asociados al cliente a null
                String updateVentasSql = "UPDATE ventas SET ClienteId = NULL WHERE ClienteId = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateVentasSql)) {

                    updateStatement.setInt(1, clientes.getClienteId());
                    // Ejecutar la consulta de actualización
                    updateStatement.executeUpdate();
                }

                // Eliminar el cliente de la tabla clientes
                String deleteClientesSql = "DELETE FROM clientes WHERE ClienteId = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteClientesSql)) {

                    deleteStatement.setInt(1, clientes.getClienteId());
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

    @FXML
    private void ventanaModificarCliente() {
        // Verificar si hay una fila seleccionada en la tabla
        Clientes clienteSeleccionado = tbClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            try {
                // Cargar el archivo FXML de modificarcliente.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("modificarcliente.fxml"));
                Parent root = loader.load();

                ModificarclientesController modificarclientesController = loader.getController();

                // Pasar los datos del cliente seleccionado al controlador de la ventana de modificar cliente
                modificarclientesController.initData(clienteSeleccionado);

                // Se establece la referencia al ClientesController
                modificarclientesController.setClientesController(this);

                // Crear una nueva escena
                Scene scene = new Scene(root);

                // Configurar el escenario (stage)
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modificar Cliente");

                // Mostrar la ventana
                stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay fila seleccionada, mostrar un mensaje al usuario
            mostrarError("Por favor, seleccione un cliente para modificar.");
        }
    }

    private void cargarDatosDesdeBD() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "clientes"
            String query = "SELECT ClienteId, Nombre, Apellidos, Compania, Direccion, Ciudad, Comunidad, Pais, CodigoPostal, Telefono, EmpleadoId FROM clientes";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbClientes.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int ClienteId = rs.getInt("ClienteId");
                String Nombre = rs.getString("Nombre");
                String Apellidos = rs.getString("Apellidos");
                String Compania = rs.getString("Compania");
                String Direccion = rs.getString("Direccion");
                String Ciudad = rs.getString("Ciudad");
                String Comunidad = rs.getString("Comunidad");
                String Pais = rs.getString("Pais");
                String CodigoPostal = rs.getString("CodigoPostal");
                String Telefono = rs.getString("Telefono");
                int EmpleadoId = rs.getInt("EmpleadoId");
                tbClientes.getItems().add(new Clientes(ClienteId, Nombre, Apellidos, Compania, Direccion, Ciudad, Comunidad, Pais, CodigoPostal, Telefono, EmpleadoId));
            }

            // Cerrar la conexión
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filtrarRegistros() {
        String columnaSeleccionada = cbClientes.getValue();
        String textoFiltrado = tfClientes.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbClientes.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "ClienteId":
                tbClientes.getItems().removeIf(clientes -> !String.valueOf(clientes.getClienteId()).toLowerCase().contains(textoFiltrado));
                break;
            case "Nombre":
                tbClientes.getItems().removeIf(clientes -> !evitarNulls(clientes.getNombre()).contains(textoFiltrado));
                break;
            case "Apellidos":
                tbClientes.getItems().removeIf(clientes -> !evitarNulls(clientes.getApellidos()).contains(textoFiltrado));
                break;
            case "Compañia":
                tbClientes.getItems().removeIf(clientes -> !evitarNulls(clientes.getCompania()).contains(textoFiltrado));
                break;
            case "Direccion":
                tbClientes.getItems().removeIf(clientes -> !clientes.getDireccion().toLowerCase().contains(textoFiltrado));
                break;
            case "Ciudad":
                tbClientes.getItems().removeIf(clientes -> !clientes.getCiudad().toLowerCase().contains(textoFiltrado));
                break;
            case "Comunidad":
                tbClientes.getItems().removeIf(clientes -> !evitarNulls(clientes.getComunidad()).contains(textoFiltrado));
                break;
            case "Pais":
                tbClientes.getItems().removeIf(clientes -> !clientes.getPais().toLowerCase().contains(textoFiltrado));
                break;
            case "CodigoPostal":
                tbClientes.getItems().removeIf(clientes -> !clientes.getCodigoPostal().toLowerCase().contains(textoFiltrado));
                break;
            case "Telefono":
                tbClientes.getItems().removeIf(clientes -> !evitarNulls(clientes.getTelefono()).contains(textoFiltrado));
                break;
            case "EmpleadoId":
                tbClientes.getItems().removeIf(clientes -> !String.valueOf(clientes.getEmpleadoId()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    // Metodo para abrir la ventana de añadir cliente
    private void abrirVentanaAnadirCliente() {
        try {
            // Cargar el archivo FXML de anadircliente.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadircliente.fxml"));
            Parent root = loader.load();

            AnadirclientesController anadirclientesController = loader.getController();
            anadirclientesController.setClientesController(this); // Configura la referencia al ClientesController

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir Cliente");

            // Mostrar la ventana
            stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo para que no salten nullpointerexceptions en el combobox
    private String evitarNulls(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
