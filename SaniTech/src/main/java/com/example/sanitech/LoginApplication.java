package com.example.sanitech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginApplication extends Application {
    private Stage primaryStage;

    private VistaManagerController vistaManagerController; // Referencia al controlador de VistaManager

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 320, 240);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.initStyle(StageStyle.UNDECORATED); // Eliminar la barra de titulo
        primaryStage.show();

        // Se establece el tamaño minimo para el Stage despues de que la escena se haya establecido
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);

        // Se obtiene el controlador del FXMLLoader
        LoginController loginController = fxmlLoader.getController();
        // Se configura la referencia al stage
        loginController.setPrimaryStage(primaryStage);

        primaryStage.centerOnScreen();
    }

    public void setVistaManagerController(VistaManagerController vistaManagerController) {
        this.vistaManagerController = vistaManagerController;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
