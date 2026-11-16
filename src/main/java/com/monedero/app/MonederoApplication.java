package com.monedero.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MonederoApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Monedero Virtual - JavaFX");
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(600);
        mostrarLogin();
        primaryStage.show();
    }

    public static void mostrarLogin() {
        cargarEscena("/fxml/login-view.fxml", 980, 680);
    }

    public static void mostrarPrincipal() {
        cargarEscena("/fxml/main-view.fxml", 1200, 760);
    }

    private static void cargarEscena(String rutaFxml, double ancho, double alto) {
        URL fxmlUrl = MonederoApplication.class.getResource(rutaFxml);
        if (fxmlUrl == null) {
            throw new IllegalStateException("No se encontro el archivo " + rutaFxml);
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, ancho, alto);
                URL cssUrl = MonederoApplication.class.getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista: " + rutaFxml, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
