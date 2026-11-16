package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Tarea;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class TareaController {
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private DatePicker dateLimite;
    @FXML
    private Label lblMensaje;

    @FXML
    private TableView<Tarea> tablaTareas;
    @FXML
    private TableColumn<Tarea, String> colTitulo;
    @FXML
    private TableColumn<Tarea, String> colDescripcion;
    @FXML
    private TableColumn<Tarea, String> colFecha;
    @FXML
    private TableColumn<Tarea, String> colEstado;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        dateLimite.setValue(LocalDate.now());
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaLimite() == null ? "-" : d.getValue().getFechaLimite().toString()
        ));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isCompletada() ? "Completada" : "Pendiente"
        ));

        tablaTareas.getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            if (sel != null) {
                txtTitulo.setText(sel.getTitulo());
                txtDescripcion.setText(sel.getDescripcion());
                dateLimite.setValue(sel.getFechaLimite());
            }
        });

        cargarTabla();
    }

    @FXML
    public void agregarTarea() {
        if (context.getClienteActivo() == null) {
            return;
        }
        try {
            context.getTareaService().crear(
                    context.getClienteActivo().getId(),
                    txtTitulo.getText(),
                    txtDescripcion.getText(),
                    dateLimite.getValue()
            );
            limpiarCampos();
            lblMensaje.setText("Tarea agregada.");
            cargarTabla();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void actualizarTarea() {
        Tarea sel = tablaTareas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione una tarea para editar.");
            return;
        }
        try {
            context.getTareaService().actualizar(sel.getId(), txtTitulo.getText(), txtDescripcion.getText(), dateLimite.getValue());
            limpiarCampos();
            lblMensaje.setText("Tarea actualizada.");
            cargarTabla();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void alternarCompletada() {
        Tarea sel = tablaTareas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione una tarea.");
            return;
        }
        context.getTareaService().cambiarEstado(sel.getId(), !sel.isCompletada());
        cargarTabla();
    }

    @FXML
    public void eliminarTarea() {
        Tarea sel = tablaTareas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione una tarea para eliminar.");
            return;
        }
        context.getTareaService().eliminar(sel.getId());
        limpiarCampos();
        lblMensaje.setText("Tarea eliminada.");
        cargarTabla();
    }

    private void limpiarCampos() {
        txtTitulo.clear();
        txtDescripcion.clear();
        dateLimite.setValue(LocalDate.now());
    }

    private void cargarTabla() {
        if (context.getClienteActivo() == null) {
            tablaTareas.getItems().clear();
            return;
        }
        tablaTareas.getItems().setAll(
                context.getTareaService().listarPorCliente(context.getClienteActivo().getId())
        );
    }
}
