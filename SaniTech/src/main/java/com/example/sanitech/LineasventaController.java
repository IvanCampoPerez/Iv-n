package com.example.sanitech;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class LineasventaController {
    @FXML
    private Button btnAnadir;

    @FXML
    private TableView<Lineasventa> tbLineasventa;

    @FXML
    private TableColumn<Lineasventa, Integer> colLineaVentaId;

    @FXML
    private TableColumn<Lineasventa, Integer> colVentaId;

    @FXML
    private TableColumn<Lineasventa, String> colCodigoArticulo;

    @FXML
    private TableColumn<Lineasventa, BigDecimal> colPrecioVenta;

    @FXML
    private TableColumn<Lineasventa,Integer> colCantidad;

    @FXML
    private ComboBox<String> cbLineasventa;

    @FXML
    private TextField tfLineasventa;

    @FXML
    private Label lbLineaVentaId;

    @FXML
    private Label lbVentaId;

    @FXML
    private Label lbCodigoArticulo;

    @FXML
    private Label lbPrecioVenta;

    @FXML
    private Label lbCantidad;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbLineasventa.setItems(FXCollections.observableArrayList("LineaVentaId", "VentaId", "CodigoArticulo", "PrecioVenta", "Cantidad"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Lineasventa
        colLineaVentaId.setCellValueFactory(new PropertyValueFactory<>("lineaVentaId"));
        colVentaId.setCellValueFactory(new PropertyValueFactory<>("ventaId"));
        colCodigoArticulo.setCellValueFactory(new PropertyValueFactory<>("codigoArticulo"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirLineasventa());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbLineasventa.setOnAction(event -> filtrarRegistros());
        tfLineasventa.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbLineasventa.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbLineaVentaId.setText(String.valueOf(newSelection.getLineaVentaId()));
                lbVentaId.setText(String.valueOf(newSelection.getVentaId()));
                lbCodigoArticulo.setText(newSelection.getCodigoArticulo());
                lbPrecioVenta.setText(String.valueOf(newSelection.getPrecioVenta()));
                lbCantidad.setText(String.valueOf(newSelection.getCantidad()));
            }
        });
    }

    @FXML
    private void eliminarLineasventa() {
        // Obtener la linea de venta seleccionada en la tabla
        Lineasventa lineaventaSeleccionada = tbLineasventa.getSelectionModel().getSelectedItem();

        if (lineaventaSeleccionada == null) {
            // Si no se seleccionó ninguna linea de venta, mostrar un mensaje de error
            mostrarError("Por favor, selecciona una linea de venta para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar esta linea de venta? Esto solo eliminara la linea de venta, no afectara a las cantidades en el inventario ni a la tabla de ventas");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarLineasventaBD(lineaventaSeleccionada)) {
                // Si la eliminación fue exitosa, mostrar un mensaje de éxito
                mostrarInformacion("La linea de venta fue eliminada correctamente");
                // Actualizar la tabla de lineas_ventas
                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar, mostrar un mensaje de error
                mostrarError("No se pudo eliminar la linea de venta");
            }
        }
    }

    // Método para eliminar una compra de la base de datos
    private boolean eliminarLineasventaBD(Lineasventa lineasventa) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Eliminar la venta de la tabla lineas_ventas
            String deleteLineaventaSql = "DELETE FROM lineas_ventas WHERE LineaVentaId = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteLineaventaSql)) {

                deleteStatement.setString(1, String.valueOf(lineasventa.getLineaVentaId()));
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
            // Conexión a la base de datos "saneamientos"
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "lineas_ventas"
            String query = "SELECT LineaVentaId, VentaId, CodigoArticulo, PrecioVenta, Cantidad FROM lineas_ventas";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbLineasventa.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int LineaVentaId = rs.getInt("LineaVentaId");
                int VentaId = rs.getInt("VentaId");
                String CodigoArticulo = rs.getString("CodigoArticulo");
                BigDecimal PrecioVenta = rs.getBigDecimal("PrecioVenta");
                int Cantidad = rs.getInt("Cantidad");
                tbLineasventa.getItems().add(new Lineasventa(LineaVentaId, VentaId, CodigoArticulo, PrecioVenta, Cantidad));
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
        String columnaSeleccionada = cbLineasventa.getValue();
        String textoFiltrado = tfLineasventa.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbLineasventa.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "LineaVentaId":
                tbLineasventa.getItems().removeIf(lineasventa -> !String.valueOf(lineasventa.getLineaVentaId()).toLowerCase().contains(textoFiltrado));
                break;
            case "VentaId":
                tbLineasventa.getItems().removeIf(lineasventa -> !String.valueOf(lineasventa.getVentaId()).toLowerCase().contains(textoFiltrado));
                break;
            case "CodigoArticulo":
                tbLineasventa.getItems().removeIf(lineasventa -> !lineasventa.getCodigoArticulo().toLowerCase().contains(textoFiltrado));
                break;
            case "PrecioVenta":
                tbLineasventa.getItems().removeIf(lineasventa -> !String.valueOf(lineasventa.getPrecioVenta()).toLowerCase().contains(textoFiltrado));
                break;
            case "Cantidad":
                tbLineasventa.getItems().removeIf(lineasventa -> !String.valueOf(lineasventa.getCantidad()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    // Metodo para abrir la ventana de añadir lineas de venta
    private void abrirVentanaAnadirLineasventa() {
        try {
            // Cargar el archivo FXML de anadirlineasventa.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadirlineasventa.fxml"));
            Parent root = loader.load();

            AnadirlineasventaController anadirlineasventaController = loader.getController();
            anadirlineasventaController.setLineasventaController(this); // Configura la referencia al LineasventaController

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir linea de venta");

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
