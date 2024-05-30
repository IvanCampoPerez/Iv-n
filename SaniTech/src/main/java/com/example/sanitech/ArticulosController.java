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

public class ArticulosController {

    @FXML
    private Button btnAnadir;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private TableView<Articulos> tbArticulos;

    @FXML
    private TableColumn<Articulos, String> colCodigoarticulo;

    @FXML
    private TableColumn<Articulos, String> colArticulo;

    @FXML
    private TableColumn<Articulos, Integer> colCodigoproveedor;

    @FXML
    private TableColumn<Articulos, BigDecimal> colPreciocompra;

    @FXML
    private TableColumn<Articulos, BigDecimal> colPrecioventa;

    @FXML
    private TableColumn<Articulos, LocalDate> colFechaalta;

    @FXML
    private ComboBox<String> cbArticulos;

    @FXML
    private TextField tfArticulos;

    @FXML
    private Label lbCodigoarticulo;

    @FXML
    private Label lbArticulo;

    @FXML
    private Label lbCodigoproveedor;

    @FXML
    private Label lbPreciocompra;

    @FXML
    private Label lbPrecioventa;

    @FXML
    private Label lbFechaalta;

    @FXML
    private Tooltip ttArticulo;

    @FXML
    private void initialize() {
        // Se establece los items del ComboBox
        cbArticulos.setItems(FXCollections.observableArrayList("codigo_articulo", "articulo", "codigo_proveedor", "codigo_articulo_proveedor", "precio_venta", "fecha_alta"));

        // Se configura las columnas de la tabla para que se correspondan con las propiedades del modelo Articulos
        colCodigoarticulo.setCellValueFactory(new PropertyValueFactory<>("codigoarticulo"));
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colCodigoproveedor.setCellValueFactory(new PropertyValueFactory<>("codigoproveedor"));
        colPreciocompra.setCellValueFactory(new PropertyValueFactory<>("preciocompra"));
        colPrecioventa.setCellValueFactory(new PropertyValueFactory<>("precioventa"));
        colFechaalta.setCellValueFactory(new PropertyValueFactory<>("fechaalta"));
        btnAnadir.setOnAction(event -> abrirVentanaAnadirArticulo());

        // Llena la tabla con datos desde la base de datos
        cargarDatosDesdeBD();

        // Configurar listeners para el ComboBox y el TextField
        cbArticulos.setOnAction(event -> filtrarRegistros());
        tfArticulos.textProperty().addListener((observable, oldValue, newValue) -> filtrarRegistros());

        // Listener para la selección de la tabla
        tbArticulos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Actualizar los labels con los valores de la fila seleccionada
                lbCodigoarticulo.setText(newSelection.getCodigoarticulo());
                lbArticulo.setText(newSelection.getArticulo());
                ttArticulo.setText(newSelection.getArticulo()); // Se crean tooltips para labels que son muy largos
                lbCodigoproveedor.setText(String.valueOf(newSelection.getCodigoproveedor()));
                lbPreciocompra.setText(String.valueOf(newSelection.getPreciocompra()));
                lbPrecioventa.setText(String.valueOf(newSelection.getPrecioventa()));
                lbFechaalta.setText(String.valueOf(newSelection.getFechaalta()));
            }
        });
        // Ajusta automaticamente el tamaño de las columnas
        tbArticulos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void ventanaModificarArticulo() {
        // Se verifica si hay una fila seleccionada en la tabla
        Articulos articuloSeleccionado = tbArticulos.getSelectionModel().getSelectedItem();
        if (articuloSeleccionado != null) {
            try {
                // Cargar el archivo FXML de modificararticulo.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("modificararticulo.fxml"));
                Parent root = loader.load();

                ModificararticulosController modificararticulosController = loader.getController();

                // Pasar los datos del articulo seleccionado al controlador de la ventana de modificar articulo
                modificararticulosController.initData(articuloSeleccionado);

                // Se establece la referencia al ArticulosController
                modificararticulosController.setArticulosController(this);

                // Crear una nueva escena
                Scene scene = new Scene(root);

                // Configurar el escenario (stage)
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modificar Articulo");

                // Mostrar la ventana
                stage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de título
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay fila seleccionada, mostrar un mensaje al usuario
            mostrarError("Por favor, seleccione un articulo para modificar.");
        }
    }

    @FXML
    private void eliminarArticulo() {
        // Obtener el articulo seleccionado en la tabla
        Articulos articuloSeleccionado = tbArticulos.getSelectionModel().getSelectedItem();

        if (articuloSeleccionado == null) {
            // Si no se seleccionó ningún artículo, mostrar un mensaje de error
            mostrarError("Por favor, selecciona un articulo para eliminar");
            return;
        }

        // Mostrar una alerta de confirmación para confirmar la eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estas seguro de que quieres eliminar este artículo? Su código de artículo en compras, inventario y ventas se eliminarán por consiguiente.");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait(); // Se espera a que el usuario interactue con la alerta
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Si el usuario confirma la eliminación se procede con esta
            if (eliminarArticuloBD(articuloSeleccionado)) {
                // Si la eliminación fue exitosa, mostrar un mensaje de éxito
                mostrarInformacion("El artículo fue eliminado correctamente");
                // Actualizar la tabla de articulos
                cargarDatosDesdeBD();
            } else {
                // Si hubo un error al eliminar, mostrar un mensaje de error
                mostrarError("No se pudo eliminar el artículo debido a que tiene una compra, venta o inventario asociado");
            }
        }
    }

    // Método para eliminar un articulo de la base de datos
    private boolean eliminarArticuloBD(Articulos articulos) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/saneamientos", "root", "rootpass")) {
            // Desactivar el modo de autocommit
            connection.setAutoCommit(false);

            try {
                // Actualizar los registros de las tablas lineas_ventas, inventario y lineas_compras asociados al articulo a null
                String[] updateSqls = {
                        "UPDATE lineas_ventas SET CodigoArticulo = NULL WHERE CodigoArticulo = ?",
                        "UPDATE inventario SET CodigoArticulo = NULL WHERE CodigoArticulo = ?",
                        "UPDATE lineas_compras SET CodigoArticulo = NULL WHERE CodigoArticulo = ?"
                };

                for (String updateSql : updateSqls) {
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setString(1, articulos.getCodigoarticulo());
                        // Ejecutar la consulta de actualización
                        updateStatement.executeUpdate();
                    }
                }

                // Eliminar el articulo de la tabla articulos
                String deleteArticuloSql = "DELETE FROM articulos WHERE codigo_articulo = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteArticuloSql)) {

                    deleteStatement.setString(1, articulos.getCodigoarticulo());
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

            // Consulta SQL para obtener los datos de la tabla "articulos"
            String query = "SELECT codigo_articulo, articulo, codigo_proveedor, precio_compra, precio_venta, fecha_alta FROM articulos";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            // Limpiar la tabla antes de cargar los nuevos datos
            tbArticulos.getItems().clear();

            // Llenar la tabla con los datos de la consulta
            while (rs.next()) {
                String codigo_articulo = rs.getString("codigo_articulo");
                String articulo = rs.getString("articulo");
                int codigo_proveedor = rs.getInt("codigo_proveedor");
                BigDecimal precio_compra = rs.getBigDecimal("precio_compra");
                BigDecimal precio_venta = rs.getBigDecimal("precio_venta");
                // Obtener la fecha de la base de datos como java.sql.Date
                Date fecha_altaDB = rs.getDate("fecha_alta");
                // Convertir java.sql.Date a LocalDate
                LocalDate fecha_alta = fecha_altaDB.toLocalDate();
                tbArticulos.getItems().add(new Articulos(codigo_articulo, articulo, codigo_proveedor, precio_compra, precio_venta, fecha_alta));
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
        String columnaSeleccionada = cbArticulos.getValue();
        String textoFiltrado = tfArticulos.getText().toLowerCase();

        // Limpiar el filtro anterior
        tbArticulos.getItems().clear();

        // Volver a cargar los datos desde la BD y aplicar el filtro
        cargarDatosDesdeBD();

        // Aplicar el filtro según la columna seleccionada y el texto ingresado
        switch (columnaSeleccionada) {
            case "codigo_articulo":
                tbArticulos.getItems().removeIf(articulos -> !articulos.getCodigoarticulo().toLowerCase().contains(textoFiltrado));
                break;
            case "articulo":
                tbArticulos.getItems().removeIf(articulos -> !articulos.getArticulo().toLowerCase().contains(textoFiltrado));
                break;
            case "codigo_proveedor":
                tbArticulos.getItems().removeIf(articulos -> !String.valueOf(articulos.getCodigoproveedor()).toLowerCase().contains(textoFiltrado));
                break;
            case "precio_compra":
                tbArticulos.getItems().removeIf(articulos -> !String.valueOf(articulos.getPreciocompra()).toLowerCase().contains(textoFiltrado));
                break;
            case "precio_venta":
                tbArticulos.getItems().removeIf(articulos -> !String.valueOf(articulos.getPrecioventa()).toLowerCase().contains(textoFiltrado));
                break;
            case "fecha_alta":
                tbArticulos.getItems().removeIf(articulos -> !String.valueOf(articulos.getFechaalta()).toLowerCase().contains(textoFiltrado));
                break;
        }
    }

    // Metodo para abrir la ventana de añadir articulo
    private void abrirVentanaAnadirArticulo() {
        try {
            // Cargar el archivo FXML de anadirarticulo.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anadirarticulo.fxml"));
            Parent root = loader.load();

            AnadirarticulosController anadirarticulosController = loader.getController();
            anadirarticulosController.setArticulosController(this); // Configura la referencia al ArticulosController

            // Crear una nueva escena
            Scene scene = new Scene(root);

            // Configurar el escenario (stage)
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Añadir Artículo");

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
