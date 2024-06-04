package com.example.sanitech;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class VistaManagerController {

    private Stage stage; // Referencia a la ventana de VistaManager
    private Scene scene;
    private String empleadoId; // Atributo para almacenar el EmpleadoId del usuario conectado
    private String nombreUsuario; // Atributo para almacenar el nombre del usuario conectado
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Label nombre;

    @FXML
    private Label rol;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem proveedoresMenuItem;

    @FXML
    private MenuItem articulosMenuItem;

    @FXML
    private MenuItem comprasMenuItem;

    @FXML
    private MenuItem inventarioMenuItem;

    @FXML
    private MenuItem ventasMenuItem;

    @FXML
    private Menu administracionMenu;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button btnMinimizar;

    @FXML
    private Button btnMaximizar;

    @FXML
    private Button btnCerrar;

    @FXML
    private HBox topHbox;

    @FXML
    private AnchorPane administracionAnchorPane;

    @FXML
    private ImageView ivImagenUsuario;

    private final String rutaImagenDefecto = getClass().getResource("/user.jpg").toString();
    private VentasController ventasController;
    private InventarioController inventarioController;

    @FXML
    protected void cerrarVentana(ActionEvent event) {
        // Actualiza el estado del usuario en la base de datos
        actualizarEstadoSesionUsuario(nombreUsuario, false);

        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void minimizarVentana(ActionEvent event) {
        Stage stage = (Stage) btnMinimizar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    protected void maximizarVentana(ActionEvent event) {
        Stage stage = (Stage) btnMaximizar.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            btnMaximizar.setText("□");
        } else {
            stage.setMaximized(true);
            btnMaximizar.setText("❐");
        }
    }

    // Métodos para abrir las ventanas en la principal
    @FXML
    public void abrirVentanaAdministracion(ActionEvent event) throws IOException {
        // Carga la ventana de administración
        FXMLLoader loader = new FXMLLoader(getClass().getResource("administracion.fxml"));
        Tab tab = new Tab("Administración", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void abrirVentanaProveedores(ActionEvent event) throws IOException {
        // Carga la ventana de proveedores
        FXMLLoader loader = new FXMLLoader(getClass().getResource("proveedores.fxml"));
        Tab tab = new Tab("Proveedores", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void abrirVentanaArticulos(ActionEvent event) throws IOException {
        // Carga la ventana de articulos
        FXMLLoader loader = new FXMLLoader(getClass().getResource("articulos.fxml"));
        Tab tab = new Tab("Articulos", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void abrirVentanaCompras(ActionEvent event) throws IOException {
        // Carga la ventana de compras
        FXMLLoader loader = new FXMLLoader(getClass().getResource("compras.fxml"));
        Tab tab = new Tab("Compras", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        // Obtiene el controlador de ComprasController
        ComprasController comprasController = loader.getController();

        // Establece el inventarioController en ComprasController
        comprasController.setInventarioController(inventarioController);
    }

    @FXML
    public void abrirVentanaInventario(ActionEvent event) throws IOException {
        // Carga la ventana de inventario
        FXMLLoader loader = new FXMLLoader(getClass().getResource("inventario.fxml"));
        Tab tab = new Tab("Inventario", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        // Se consigue una referencia al controlador de InventarioController
        InventarioController inventarioController = loader.getController();

        // Se obtiene el rol del usuario actual y lo pasa al método setRolUsuario
        inventarioController.setRolUsuario(rol.getText());

        // Guarda una referencia al InventarioController
        this.inventarioController = inventarioController;
    }

    @FXML
    public void abrirVentanaVentas(ActionEvent event) throws IOException {
        // Carga la ventana de ventas
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ventas.fxml"));
        Tab tab = new Tab("Ventas", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        // Se obtiene el controlador de VentasController
        VentasController ventasController = loader.getController();

        // Se pasa el empleadoId al VentasController
        ventasController.setEmpleadoId(empleadoId);

        // Guarda una referencia al VentasController
        this.ventasController = ventasController;
    }

    @FXML
    public void abrirVentanaLineasventa(ActionEvent event) throws IOException {
        // Asegura que la ventana de ventas esté cargada primero para obtener el controlador
        if (ventasController == null) {
            abrirVentanaVentas(new ActionEvent());
        }

        // Carga la ventana de lineasventa
        FXMLLoader loader = new FXMLLoader(getClass().getResource("lineasventa.fxml"));
        Tab tab = new Tab("Lineasventa", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        // Obtiene el controlador de LineasventaController
        LineasventaController lineasventaController = loader.getController();

        // Establece el ventasController en LineasventaController
        lineasventaController.setVentasController(ventasController);

        // Establece el inventarioController en LineasventaController
        lineasventaController.setInventarioController(inventarioController);
    }

    @FXML
    public void abrirVentanaClientes(ActionEvent event) throws IOException {
        // Carga la ventana de clientes
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientes.fxml"));
        Tab tab = new Tab("Clientes", loader.load());
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void initialize() {
        // Asociar las combinaciones de teclas con los items de menú
        proveedoresMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        articulosMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        comprasMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        inventarioMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        ventasMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));


        // Configurar el evento de arrastre de la ventana
        topHbox.setOnMousePressed(event -> {
            Stage stage = (Stage) topHbox.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topHbox.setOnMouseDragged(event -> {
            Stage stage = (Stage) topHbox.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void actualizarInformacionUsuario(String nombreUsuario, String rolUsuario, String empleadoId) {
        nombre.setText(" "+ nombreUsuario + ": ");
        rol.setText(rolUsuario);

        // Actualizar visibilidad del menú de Administración
        if ("Administrador".equals(rolUsuario)) {
            administracionMenu.setVisible(true);
        } else {
            administracionMenu.setVisible(false);
        }
        this.empleadoId = empleadoId;
        this.nombreUsuario = nombreUsuario;

        // Cargar la imagen del usuario desde la base de datos
        cargarImagenUsuario(nombreUsuario);
    }

    private void cargarImagenUsuario(String nombreUsuario) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT imagen FROM usuarios WHERE nombre = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, nombreUsuario);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Blob blob = resultSet.getBlob("imagen");
                        if (blob != null) {
                            try (InputStream inputStream = blob.getBinaryStream()) {
                                Image image = new Image(inputStream);
                                ivImagenUsuario.setImage(image);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            ivImagenUsuario.setImage(new Image(rutaImagenDefecto));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ivImagenUsuario.setImage(new Image(rutaImagenDefecto));
        }
    }

    // Método para activar o desactivar el modo oscuro
    public void setModoOscuro(boolean modoOscuro) {
        if (modoOscuro) {
            // Cargar el archivo CSS para el modo oscuro
            scene.getStylesheets().add(getClass().getResource("/stylesModoOscuro.css").toExternalForm());
            // Aplicar la clase CSS modo-oscuro al AnchorPane de administración
            administracionAnchorPane.getStyleClass().add("modo-oscuro");
            // Aplicar la clase CSS modo-oscuro al TabPane
            tabPane.getStyleClass().add("modo-oscuro");
        } else {
            // Quitar el archivo CSS del modo oscuro
            scene.getStylesheets().remove(getClass().getResource("/stylesModoOscuro.css").toExternalForm());
            // Quitar la clase CSS modo-oscuro del AnchorPane de administración
            administracionAnchorPane.getStyleClass().remove("modo-oscuro");
            // Quitar la clase CSS modo-oscuro del TabPane
            tabPane.getStyleClass().remove("modo-oscuro");
        }
    }

    // Método para alternar entre el modo oscuro y el modo claro
    public void alternarModoOscuro() {
        setModoOscuro(!scene.getStylesheets().contains(getClass().getResource("/stylesModoOscuro.css").toExternalForm()));
    }

    public void cerrarSesion(ActionEvent event) throws Exception {
        // Actualiza el estado del usuario en la base de datos
        actualizarEstadoSesionUsuario(nombreUsuario, false);

        // Cierra la ventana actual
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();

        // Abre la ventana de inicio de sesión
        LoginApplication loginApp = new LoginApplication();
        loginApp.setVistaManagerController(this); // Establece la referencia al controlador de VistaManager
        loginApp.start(new Stage());
    }

    private void actualizarEstadoSesionUsuario(String nombreUsuario, boolean estaConectado) {
        String url = "jdbc:mysql://localhost:3306/saneamientos";
        String user = "root";
        String password = "rootpass";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE Usuarios SET esta_conectado = ? WHERE nombre = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {

                statement.setBoolean(1, estaConectado);
                statement.setString(2, nombreUsuario);

                statement.executeUpdate();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setResizable(true); // Permite redimensionar la ventana

        // Evento para que cuando se cierre la ventana por el Sistema Operativo se actualice el estado de sesion del usuario
        stage.setOnCloseRequest(event -> {
            actualizarEstadoSesionUsuario(nombreUsuario, false);
        });
    }

    // Método para establecer la escena
    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
