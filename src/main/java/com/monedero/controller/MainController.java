package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.app.MonederoApplication;
import com.monedero.domain.model.Cliente;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainController {
    @FXML
    private StackPane contenedorVista;

    @FXML
    private Label lblUsuarioActivo;

    @FXML
    private Label lblRangoActual;

    @FXML
    private Label lblPuntosActuales;

    @FXML
    private Button btnNavClientes;
    @FXML
    private Button btnNavMonederos;
    @FXML
    private Button btnNavTransacciones;
    @FXML
    private Button btnNavPuntos;
    @FXML
    private Button btnNavProgramadas;
    @FXML
    private Button btnNavReportes;
    @FXML
    private Button btnNavTareas;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        context.setOnActualizar(this::actualizarResumenSuperior);
        actualizarResumenSuperior();
        mostrarMonederos();
    }

    private void actualizarResumenSuperior() {
        Cliente activo = context.getClienteActivo();
        if (activo != null) {
            lblUsuarioActivo.setText("Hola, " + activo.getNombre());
            lblRangoActual.setText("Rango: " + activo.getRango().name());
            lblPuntosActuales.setText(activo.getPuntosAcumulados() + " pts");
        }
    }

    @FXML
    public void cerrarSesion() {
        context.cerrarSesion();
        MonederoApplication.mostrarLogin();
    }

    @FXML
    public void mostrarClientes() {
        marcarBotonActivo(btnNavClientes);
        cargarVista("/fxml/clientes-view.fxml");
    }

    @FXML
    public void mostrarMonederos() {
        marcarBotonActivo(btnNavMonederos);
        cargarVista("/fxml/monederos-view.fxml");
    }

    @FXML
    public void mostrarTransacciones() {
        marcarBotonActivo(btnNavTransacciones);
        cargarVista("/fxml/transacciones-view.fxml");
    }

    @FXML
    public void mostrarPuntos() {
        marcarBotonActivo(btnNavPuntos);
        cargarVista("/fxml/puntos-view.fxml");
    }

    @FXML
    public void mostrarProgramadas() {
        marcarBotonActivo(btnNavProgramadas);
        cargarVista("/fxml/programadas-view.fxml");
    }

    @FXML
    public void mostrarReportes() {
        marcarBotonActivo(btnNavReportes);
        cargarVista("/fxml/reportes-view.fxml");
    }

    @FXML
    public void mostrarTareas() {
        marcarBotonActivo(btnNavTareas);
        cargarVista("/fxml/tareas-view.fxml");
    }

    private void marcarBotonActivo(Button activo) {
        List<Button> botones = List.of(
                btnNavClientes, btnNavMonederos, btnNavTransacciones,
                btnNavPuntos, btnNavProgramadas, btnNavReportes, btnNavTareas
        );
        for (Button b : botones) {
            if (b != null) {
                b.getStyleClass().remove("nav-btn-active");
            }
        }
        if (activo != null && !activo.getStyleClass().contains("nav-btn-active")) {
            activo.getStyleClass().add("nav-btn-active");
        }
    }

    private void cargarVista(String rutaFxml) {
        URL recurso = getClass().getResource(rutaFxml);
        if (recurso == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(recurso);
            Node vista = loader.load();
            contenedorVista.getChildren().setAll(vista);
            animarEntrada(vista);
            actualizarResumenSuperior();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Animacion suave de entrada al cambiar de pantalla
    private void animarEntrada(Node nodo) {
        nodo.setOpacity(0);
        nodo.setTranslateY(10);

        FadeTransition fade = new FadeTransition(Duration.millis(200), nodo);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(200), nodo);
        slide.setFromY(10);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }
}
