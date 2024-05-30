package com.example.sanitech;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;

import static com.example.sanitech.CrearusuariosController.mostrarError;

public class InventarioController {

    @FXML
    private Button btnModificar;

    @FXML
    private TableView<Inventario> tbInventario;

    @FXML
    private TableColumn<Inventario, Integer> colInventarioId;

    @FXML
    private TableColumn<Inventario, String> colCodigoArticulo;

    @FXML
    private TableColumn<Inventario, Integer> colCantidadDisponible;

    @FXML
    private ComboBox<String> cbInventario;

    @FXML
    private TextField tfInventario;

    @FXML
    private Label lbInventarioId;

    @FXML
    private Label lbCodigoArticulo;

    @FXML
    private Label lbCantidadDisponible;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbInventario.setItems(FXCollections.observableArrayList("InventarioId", "CodigoArticulo", "CantidadDisponible"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Inventario
        colInventarioId.setCellValueFactory(new PropertyValueFactory<>("inventarioId"));
        colCodigoArticulo.setCellValueFactory(new PropertyValueFactory<>("codigoArticulo"));
        colCantidadDisponible.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbInventario.setOnAction(event -> filtrarRegistros());
        tfInventario.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbInventario.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbInventarioId.setText(String.valueOf(newSelection.getInventarioId()));
                lbCodigoArticulo.setText(newSelection.getCodigoArticulo());
                lbCantidadDisponible.setText(String.valueOf(newSelection.getCantidadDisponible()));
            }
        });
        // Ajusta automaticamente el tamaño de las columnas
        tbInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void ventanaModificarInventario() {
        // Verificar si hay una fila seleccionada en la tabla
        Inventario inventarioSeleccionado = tbInventario.getSelectionModel().getSelectedItem();
        if (inventarioSeleccionado != null) {
            try {
                // Cargar el archivo FXML de modificarinventario.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("modificarinventario.fxml"));
                Parent root = loader.load();

                ModificarinventarioController modificarinventarioController = loader.getController();

                // Pasar los datos del articulo seleccionado al controlador de la ventana de modificar inventario
                modificarinventarioController.initData(inventarioSeleccionado);

                // Se establece la referencia al InventarioController
                modificarinventarioController.setInventarioController(this);

                // Crear una nueva escena
                Scene scene = new Scene(root);

                // Configurar el escenario (stage)
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modificar Inventario");

                // Mostrar la ventana
                stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay fila seleccionada, mostrar un mensaje al usuario
            mostrarError("Por favor, seleccione un articulo del inventario para modificar.");
        }
    }

    private void cargarDatosDesdeBD() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass");

            // Consulta SQL para obtener los datos de la tabla "inventario"
            String query = "SELECT InventarioId, CodigoArticulo, CantidadDisponible FROM inventario";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbInventario.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                int InventarioId = rs.getInt("InventarioId");
                String CodigoArticulo = rs.getString("CodigoArticulo");
                int CantidadDisponible = rs.getInt("CantidadDisponible");
                tbInventario.getItems().add(new Inventario(InventarioId, CodigoArticulo, CantidadDisponible));
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
        String columnaSeleccionada = cbInventario.getValue();
        String textoFiltrado = tfInventario.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbInventario.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "InventarioId":
                tbInventario.getItems().removeIf(inventario -> !String.valueOf(inventario.getInventarioId()).toLowerCase().contains(textoFiltrado));
                break;
            case "CodigoArticulo":
                tbInventario.getItems().removeIf(inventario -> !inventario.getCodigoArticulo().toLowerCase().contains(textoFiltrado));
                break;
            case "CantidadDisponible":
                tbInventario.getItems().removeIf(inventario -> !String.valueOf(inventario.getCantidadDisponible()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

   public void setRolUsuario(String rolUsuario) { // Metodo para establecer el boton de modificar inventario como no visible si no eres el admin
        if ("Administrador".equals(rolUsuario)) {
            btnModificar.setVisible(true);
        } else {
            btnModificar.setVisible(false);
        }
    }

    public void cargarDatos() {
        cargarDatosDesdeBD();
    }
}
