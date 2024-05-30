package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;
import java.util.function.UnaryOperator;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class ModificarclientesController {

    private ClientesController clientesController;
    private Clientes clienteSeleccionado;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField tfNombre;

    @FXML
    private TextField tfApellidos;

    @FXML
    private TextField tfCompania;

    @FXML
    private TextField tfDireccion;

    @FXML
    private TextField tfCiudad;

    @FXML
    private TextField tfComunidad;

    @FXML
    private TextField tfPais;

    @FXML
    private TextField tfCodigoPostal;

    @FXML
    private TextField tfPrefijo;

    @FXML
    private TextField tfTelefono;

    @FXML
    private TextField tfEmpleadoId;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnCerrar;

    private ObservableList<String> sugerenciasEmpleados = FXCollections.observableArrayList();
    @FXML
    private ListView<String> lvSugerencias;

    @FXML
    private void initialize() {
        btnGuardar.setOnAction(event -> modificarCliente());
        btnCancelar.setOnAction(event -> cerrarVentana());

        // Filtro que evita la modificación del "+" inicial
        UnaryOperator<TextFormatter.Change> filtroPrefijo = change -> {
            String newText = change.getControlNewText();
            if (newText.startsWith("+") && newText.substring(1).matches("[0-9]*")) {
                return change; // Permite cambios si el prefijo se mantiene como "+" y el resto son números
            }
            return null; // Rechaza cualquier cambio que no mantenga el prefijo o no sean números
        };

        // Se aplica el filtro al TextField del prefijo
        tfPrefijo.setTextFormatter(new TextFormatter<>(filtroPrefijo));

        // Listener para asegurar que el primer carácter es siempre "+" y solo permite hasta 3 números después
        tfPrefijo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.startsWith("+")) {
                tfPrefijo.setText("+" + newValue.replace("+", ""));
            } else if (!newValue.substring(1).matches("[0-9]{0,3}")) {
                tfPrefijo.setText(oldValue);
            }
        });

        // Filtro para el campo de teléfono para permitir solo números y hasta 15 dígitos
        UnaryOperator<TextFormatter.Change> filtroTelefono = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,15}")) {
                return change;
            }
            return null;
        };

        tfTelefono.setTextFormatter(new TextFormatter<>(filtroTelefono));

        // Se configura el autocompletado para tfEmpleadoId
        tfEmpleadoId.setOnKeyReleased(this::actualizarSugerencias);
        lvSugerencias.setOnMouseClicked(event -> {
            String selected = lvSugerencias.getSelectionModel().getSelectedItem();
            if (selected != null) {
                tfEmpleadoId.setText(selected);
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
        String textoIngresado = tfEmpleadoId.getText();
        if (textoIngresado.isEmpty()) {
            lvSugerencias.setVisible(false);
            return;
        }

        sugerenciasEmpleados.clear();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            String sql = "SELECT id FROM usuarios WHERE id LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, textoIngresado + "%");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    sugerenciasEmpleados.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        if (!sugerenciasEmpleados.isEmpty()) {
            lvSugerencias.setItems(sugerenciasEmpleados);
            lvSugerencias.setVisible(true);
        } else {
            lvSugerencias.setVisible(false);
        }
    }

    public void setClientesController(ClientesController clientesController) { // Setter para establecer el controlador de ClientesController
        this.clientesController = clientesController;
    }

    public void mostrarDatosCliente(Clientes clientes) {
        clienteSeleccionado = clientes;
        tfNombre.setText(clientes.getNombre());
        tfApellidos.setText(clientes.getApellidos());
        tfCompania.setText(clientes.getCompania());
        tfDireccion.setText(clientes.getDireccion());
        tfCiudad.setText(clientes.getCiudad());
        tfComunidad.setText(clientes.getComunidad());
        tfPais.setText(clientes.getPais());
        tfCodigoPostal.setText(clientes.getCodigoPostal());
        // Se obtiene el prefijo y el telefono por separado
        String telefonoCompleto = clientes.getTelefono();
        if (telefonoCompleto != null && !telefonoCompleto.isEmpty()) {
            String[] telefonoPartes = telefonoCompleto.split(" ", 2);
            tfPrefijo.setText(telefonoPartes[0]);
            if (telefonoPartes.length > 1) {
                tfTelefono.setText(telefonoPartes[1]);
            } else {
                tfTelefono.setText("");
            }
        } else {
            tfPrefijo.setText("");
            tfTelefono.setText("");
        }
        tfEmpleadoId.setText(String.valueOf(clientes.getEmpleadoId()));
    }

    public void initData(Clientes clientes) {
        this.clienteSeleccionado = clientes;
        // Mostrar los datos del cliente en los textfields
        mostrarDatosCliente(clientes);
    }

    private void cerrarVentana() {
        // Obtener el Stage (escenario) actual
        Stage stage = (Stage) btnCancelar.getScene().getWindow();

        stage.close();
    }

    private void modificarCliente() {
        // Obtener los datos ingresados por el usuario
        String nombre = tfNombre.getText();
        String apellidos = tfApellidos.getText();
        String compania = tfCompania.getText();
        String direccion = tfDireccion.getText();
        String ciudad = tfCiudad.getText();
        String comunidad = tfComunidad.getText();
        String pais = tfPais.getText();
        String codigoPostal = tfCodigoPostal.getText();
        String prefijo = tfPrefijo.getText();
        String telefono = tfTelefono.getText();
        String empleadoId = tfEmpleadoId.getText();

        if (!nombre.isEmpty() && !nombre.matches("[a-zA-Z]*")) {
            mostrarError("Nombre erroneo, el nombre solo puede contener letras.");
            return;
        }
        if (!apellidos.isEmpty() && !apellidos.matches("[a-zA-Z]*")) {
            mostrarError("Apellidos erroneos, los apellidos solo pueden contener letras.");
            return;
        }
        if (nombre.isEmpty() && apellidos.isEmpty() && compania.isEmpty()) {
            mostrarError("Datos erroneos, debe introducir nombre y apellidos, o en su defecto la compañía.");
            return;
        }
        if (direccion.isEmpty()) {
            mostrarError("Dirección erronea, la dirección no puede estar vacía.");
            return;
        }
        if (ciudad.isEmpty() || !ciudad.matches("[a-zA-Z]*")) {
            mostrarError("Ciudad erronea, la ciudad no puede estar vacía y solo puede contener letras.");
            return;
        }
        if (!comunidad.matches("[a-zA-Z ]*")) { // se puede introducir letras y espacios
            mostrarError("Comunidad erronea, la comunidad solo puede contener letras.");
            return;
        }
        if (pais.isEmpty() || !pais.matches("[a-zA-Z]*")) {
            mostrarError("País erroneo, el país no puede estar vacío y solo puede contener letras.");
            return;
        }
        if (codigoPostal.isEmpty()) {
            mostrarError("Código Postal erroneo, el código postal no puede estar vacío.");
            return;
        }

        // Si el campo teléfono está vacío, se guarda el teléfono completo como vacío si no, se guarda el prefijo mas el teléfono
        String telefonoCompleto = telefono.isEmpty() ? "" : prefijo + " " + telefono;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Preparar la consulta SQL de actualizacion
            String sql = "UPDATE clientes SET Nombre = ?, Apellidos = ?, Compania = ?, Direccion = ?, Ciudad = ?, Comunidad = ?, Pais = ?, CodigoPostal = ?, Telefono = ?, EmpleadoId = ? WHERE ClienteId = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los parámetros en la consulta preparada
                statement.setString(1, nombre);
                statement.setString(2, apellidos);
                statement.setString(3, compania);
                statement.setString(4, direccion);
                statement.setString(5, ciudad);
                statement.setString(6, comunidad);
                statement.setString(7, pais);
                statement.setString(8, codigoPostal);
                statement.setString(9, telefonoCompleto);
                statement.setString(10, empleadoId);
                statement.setString(11, String.valueOf(clienteSeleccionado.getClienteId()));

                // Ejecutar la consulta
                int filasAfectadas = statement.executeUpdate();

                // Comprobar si se actualizó correctamente
                if (filasAfectadas > 0) {
                    // Cargar los datos actualizados en la tabla de ClientesController
                    cargarDatosClientes();
                } else {
                    mostrarError("No se pudo modificar el cliente");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Actualizar la tabla de clientes en ClientesController despues de modificar el cliente
        if (clientesController != null) {
            clientesController.cargarDatos();
        }

        cerrarVentana();
    }

    private void cargarDatosClientes() {
        if (clientesController != null) {
            clientesController.cargarDatos();
        }
    }
}
