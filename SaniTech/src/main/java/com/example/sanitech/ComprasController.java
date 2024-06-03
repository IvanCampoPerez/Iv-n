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

public class ComprasController {
    private InventarioController inventarioController;
    @FXML
    private Button btnAnadir;

    @FXML
    private TableView<Compras> tbCompras;

    @FXML
    private TableColumn<Compras, Integer> colLineaCompraId;

    @FXML
    private TableColumn<Compras, String> colCodigoArticulo;

    @FXML
    private TableColumn<Compras, BigDecimal> colPrecioCompra;

    @FXML
    private TableColumn<Compras, Integer> colCantidad;

    @FXML
    private TableColumn<Compras, BigDecimal> colTotalCompra;

    @FXML
    private TableColumn<Compras, LocalDate> colFechaCompra;

    @FXML
    private ComboBox<String> cbCompras;

    @FXML
    private TextField tfCompras;

    @FXML
    private Label lbLineaCompraId;

    @FXML
    private Label lbCodigoArticulo;

    @FXML
    private Label lbPrecioCompra;

    @FXML
    private Label lbCantidad;

    @FXML
    private Label lbTotalCompra;

    @FXML
    private Label lbFechaCompra;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbCompras.setItems(FXCollections.observableArrayList("LineaCompraId", "CodigoArticulo", "PrecioCompra", "Cantidad", "TotalCompra", "FechaCompra"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Compras
        colLineaCompraId.setCellValueFactory(new PropertyValueFactory<>("lineaCompraId"));
        colCodigoArticulo.setCellValueFactory(new PropertyValueFactory<>("codigoArticulo"));
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotalCompra.setCellValueFactory(new PropertyValueFactory<>("totalCompra"));
        colFechaCompra.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirCompra());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbCompras.setOnAction(event -> filtrarRegistros());
        tfCompras.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbCompras.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbLineaCompraId.setText(String.valueOf(newSelection.getLineaCompraId()));
                lbCodigoArticulo.setText(newSelection.getCodigoArticulo());
                lbPrecioCompra.setText(String.valueOf(newSelection.getPrecioCompra()));
                lbCantidad.setText(String.valueOf(newSelection.getCantidad()));
                lbTotalCompra.setText(String.valueOf(newSelection.getTotalCompra()));
                lbFechaCompra.setText(String.valueOf(newSelection.getFechaCompra()));
            }
        });
        // Ajusta automaticamente el tamaño de las columnas
        tbCompras.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void eliminarCompra() {
        // Obtener la compra seleccionada en la tabla
        Compras compraSeleccionada = tbCompras.getSelectionModel().getSelectedItem();

        if (compraSeleccionada == null) {
            // Si no se seleccionó ninguna compra, mostrar un mensaje de error
            mostrarError("Por favor, selecciona una factura de compra para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar esta compra? Esto solo eliminara la factura, no afectara a las cantidades en el inventario");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarCompraBD(compraSeleccionada)) {
                mostrarInformacion("La factura de compra fue eliminada correctamente");
                // Actualizar la tabla de lineas_compras
                cargarDatosDesdeBD();
            } else {
                mostrarError("No se pudo eliminar la factura de compra");
            }
        }
    }

    public void setInventarioController(InventarioController inventarioController) {
        this.inventarioController = inventarioController;
    }

    // Método para eliminar una compra de la base de datos
    private boolean eliminarCompraBD(Compras compras) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
                // Eliminar la compra de la tabla lineas_compras
                String deleteCompraSql = "DELETE FROM lineas_compras WHERE LineaCompraId = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteCompraSql)) {

                    deleteStatement.setString(1, String.valueOf(compras.getLineaCompraId()));
                    // Ejecutar la consulta de eliminación
                    int filasAfectadas = deleteStatement.executeUpdate();

                    // Comprobar si se eliminó correctamente
                    if (filasAfectadas > 0) {
                        // Si se elimino alguna fila, el metodo devuelve true
                        return true;
                    } else {
                        // Si no se eliminó ninguna fila, el metodo devuelve false
                        return false;
                    }
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

            // Consulta SQL para obtener los datos de la tabla "lineas_compras"
            String query = "SELECT LineaCompraId, CodigoArticulo, PrecioCompra, Cantidad, TotalCompra, FechaCompra FROM lineas_compras";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbCompras.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int LineaCompraId = rs.getInt("LineaCompraId");
                String CodigoArticulo = rs.getString("CodigoArticulo");
                BigDecimal PrecioCompra = rs.getBigDecimal("PrecioCompra");
                int Cantidad = rs.getInt("Cantidad");
                BigDecimal TotalCompra = rs.getBigDecimal("TotalCompra");
                // Obtener la fecha de la base de datos como java.sql.Date
                Date FechaCompraDB = rs.getDate("FechaCompra");
                // Convertir java.sql.Date a LocalDate
                LocalDate FechaCompra = FechaCompraDB.toLocalDate();
                tbCompras.getItems().add(new Compras(LineaCompraId, CodigoArticulo, PrecioCompra, Cantidad, TotalCompra, FechaCompra));
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
        String columnaSeleccionada = cbCompras.getValue();
        String textoFiltrado = tfCompras.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbCompras.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "LineaCompraId":
                tbCompras.getItems().removeIf(compras -> !String.valueOf(compras.getLineaCompraId()).toLowerCase().contains(textoFiltrado));
                break;
            case "CodigoArticulo":
                tbCompras.getItems().removeIf(compras -> !compras.getCodigoArticulo().toLowerCase().contains(textoFiltrado));
                break;
            case "PrecioCompra":
                tbCompras.getItems().removeIf(compras -> !String.valueOf(compras.getPrecioCompra()).toLowerCase().contains(textoFiltrado));
                break;
            case "Cantidad":
                tbCompras.getItems().removeIf(compras -> !String.valueOf(compras.getCantidad()).toLowerCase().contains(textoFiltrado));
                break;
            case "TotalCompra":
                tbCompras.getItems().removeIf(compras -> !String.valueOf(compras.getTotalCompra()).toLowerCase().contains(textoFiltrado));
                break;
            case "FechaCompra":
                tbCompras.getItems().removeIf(compras -> !String.valueOf(compras.getFechaCompra()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    // Metodo para abrir la ventana de añadir compra
    private void abrirVentanaAnadirCompra() {
        try {
            // Cargar el archivo FXML de anadircompra.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadircompra.fxml"));
            Parent root = loader.load();

            AnadircomprasController anadircomprasController = loader.getController();
            anadircomprasController.setComprasController(this); // Configura la referencia al ComprasController
            anadircomprasController.setInventarioController(inventarioController);

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir Compra");

            // Mostrar la ventana
            stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
