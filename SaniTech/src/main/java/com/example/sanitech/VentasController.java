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

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class VentasController {
    private String empleadoId;

    @FXML
    private Button btnAnadir;

    @FXML
    private TableView<Ventas> tbVentas;

    @FXML
    private TableColumn<Ventas, Integer> colVentaId;

    @FXML
    private TableColumn<Ventas, Integer> colClienteId;

    @FXML
    private TableColumn<Ventas, LocalDate> colFechaFactura;

    @FXML
    private TableColumn<Ventas, BigDecimal> colTotalNeto;

    @FXML
    private TableColumn<Ventas, BigDecimal> colTotalIVA;

    @FXML
    private TableColumn<Ventas, BigDecimal> colTotal;

    @FXML
    private ComboBox<String> cbVentas;

    @FXML
    private TextField tfVentas;

    @FXML
    private Label lbVentaId;

    @FXML
    private Label lbClienteId;

    @FXML
    private Label lbFechaFactura;

    @FXML
    private Label lbTotalNeto;

    @FXML
    private Label lbTotalIVA;

    @FXML
    private Label lbTotal;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbVentas.setItems(FXCollections.observableArrayList("VentaId", "ClienteId", "FechaFactura", "TotalNeto", "TotalIVA", "Total"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Ventas
        colVentaId.setCellValueFactory(new PropertyValueFactory<>("ventaId"));
        colClienteId.setCellValueFactory(new PropertyValueFactory<>("clienteId"));
        colFechaFactura.setCellValueFactory(new PropertyValueFactory<>("fechaFactura"));
        colTotalNeto.setCellValueFactory(new PropertyValueFactory<>("totalNeto"));
        colTotalIVA.setCellValueFactory(new PropertyValueFactory<>("totalIVA"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirVenta());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbVentas.setOnAction(event -> filtrarRegistros());
        tfVentas.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbVentaId.setText(String.valueOf(newSelection.getVentaId()));
                lbClienteId.setText(String.valueOf(newSelection.getClienteId()));
                lbFechaFactura.setText(String.valueOf(newSelection.getFechaFactura()));
                lbTotalNeto.setText(String.valueOf(newSelection.getTotalNeto()));
                lbTotalIVA.setText(String.valueOf(newSelection.getTotalIVA()));
                lbTotal.setText(String.valueOf(newSelection.getTotal()));
            }
        });
    }

    @FXML
    private void eliminarVenta() {
        // Obtener la venta seleccionada en la tabla
        Ventas ventaSeleccionada = tbVentas.getSelectionModel().getSelectedItem();

        if (ventaSeleccionada == null) {
            // Si no se seleccionó ninguna venta, mostrar un mensaje de error
            mostrarError("Por favor, selecciona una venta para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar esta venta? La VentaId en lineas de venta se eliminará por consiguiente");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarVentaBD(ventaSeleccionada)) {
                // Si la eliminación fue exitosa, mostrar un mensaje de éxito
                mostrarInformacion("La venta fue eliminada correctamente");
                // Actualizar la tabla de ventas
                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar, mostrar un mensaje de error
                mostrarError("No se pudo eliminar la venta");
            }
        }
    }

    // Método para eliminar una venta de la base de datos
    private boolean eliminarVentaBD(Ventas ventas) {
        // Obtener el ClienteId
        String clienteId = lbClienteId.getText().trim();

        // Validar que el ClienteId esté asociado al EmpleadoId del usuario conectado
        if (!clienteAsociadoAlEmpleado(clienteId, empleadoId)) {
            mostrarError("El ClienteId no está asociado a su EmpleadoId");
            return false;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Desactivar el modo de autocommit
            connection.setAutoCommit(false);

            try {
                // Actualizar los registros de la tabla lineas_ventas asociados a la venta a null
                String updateVentasSql = "UPDATE lineas_ventas SET VentaId = NULL WHERE VentaId = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateVentasSql)) {

                    updateStatement.setInt(1, ventas.getVentaId());
                    // Ejecutar la consulta de actualización
                    updateStatement.executeUpdate();
                }

                // Eliminar la venta de la tabla ventas
                String deleteVentasSql = "DELETE FROM ventas WHERE VentaId = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteVentasSql)) {

                    deleteStatement.setInt(1, ventas.getVentaId());
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
            // Conexión a la base de datos "saneamientos"
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "ventas"
            String query = "SELECT VentaId, ClienteId, FechaFactura, TotalNeto, TotalIVA, Total FROM ventas";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbVentas.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int VentaId = rs.getInt("VentaId");
                int ClienteId = rs.getInt("ClienteId");
                // Obtener la fecha de la base de datos como java.sql.Date
                Date FechaFacturaDB = rs.getDate("FechaFactura");
                // Convertir java.sql.Date a LocalDate
                LocalDate FechaFactura = FechaFacturaDB.toLocalDate();
                BigDecimal TotalNeto = rs.getBigDecimal("TotalNeto");
                BigDecimal TotalIVA = rs.getBigDecimal("TotalIVA");
                BigDecimal Total = rs.getBigDecimal("Total");
                tbVentas.getItems().add(new Ventas(VentaId, ClienteId, FechaFactura, TotalNeto, TotalIVA, Total));
            }

            // Cerrar la conexión y liberar los recursos
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filtrarRegistros() {
        String columnaSeleccionada = cbVentas.getValue();
        String textoFiltrado = tfVentas.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbVentas.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "VentaId":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getVentaId()).toLowerCase().contains(textoFiltrado));
                break;
            case "ClienteId":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getClienteId()).toLowerCase().contains(textoFiltrado));
                break;
            case "FechaFactura":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getFechaFactura()).toLowerCase().contains(textoFiltrado));
                break;
            case "TotalNeto":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getTotalNeto()).toLowerCase().contains(textoFiltrado));
                break;
            case "TotalIVA":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getTotalIVA()).toLowerCase().contains(textoFiltrado));
                break;
            case "Total":
                tbVentas.getItems().removeIf(ventas -> !String.valueOf(ventas.getTotal()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    // Metodo para abrir la ventana de añadir venta
    private void abrirVentanaAnadirVenta() {
        try {
            // Cargar el archivo FXML de anadirventa.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadirventa.fxml"));
            Parent root = loader.load();

            AnadirventasController anadirventasController = loader.getController();
            anadirventasController.setVentasController(this); // Configura la referencia al VentasController
            anadirventasController.setEmpleadoId(empleadoId);

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir Venta");

            // Mostrar la ventana
            stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean clienteAsociadoAlEmpleado(String clienteId, String empleadoId) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        // Consulta SQL para verificar si el ClienteId está asociado al EmpleadoId
        String sql = "SELECT COUNT(*) AS count FROM clientes WHERE ClienteId = ? AND EmpleadoId = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, clienteId);
            statement.setString(2, empleadoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                // Si se encuentra una fila en el resultado, significa que el ClienteId está asociado al EmpleadoId
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si no se encontraron resultados o hubo un error, devuelve false
        return false;
    }

    public void setEmpleadoId(String empleadoId) {
        this.empleadoId = empleadoId;
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
