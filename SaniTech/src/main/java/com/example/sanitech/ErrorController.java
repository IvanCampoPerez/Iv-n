package com.example.sanitech;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorController {
    @FXML
    private Label lbError;

    private Stage stage;

    @FXML
    private void cerrarVentana() {
        ((Stage) lbError.getScene().getWindow()).close();
    }

    public void setError(String mensaje) {
        lbError.setText(mensaje);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
