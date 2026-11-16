package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Beneficio;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.PuntosMovimiento;
import com.monedero.util.FormateadorMoneda;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PuntosController {
    @FXML
    private Label lblPuntos;
    @FXML
    private Label lblRango;
    @FXML
    private Label lblMensaje;

    @FXML
    private TableView<Beneficio> tablaBeneficios;
    @FXML
    private TableColumn<Beneficio, String> colBeneficioNombre;
    @FXML
    private TableColumn<Beneficio, String> colBeneficioDesc;
    @FXML
    private TableColumn<Beneficio, Number> colBeneficioCosto;

    @FXML
    private TableView<PuntosMovimiento> tablaMovimientos;
    @FXML
    private TableColumn<PuntosMovimiento, String> colMovFecha;
    @FXML
    private TableColumn<PuntosMovimiento, String> colMovTipo;
    @FXML
    private TableColumn<PuntosMovimiento, Number> colMovCantidad;
    @FXML
    private TableColumn<PuntosMovimiento, String> colMovMotivo;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        colBeneficioNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colBeneficioDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colBeneficioCosto.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCostoPuntos()));

        colMovFecha.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatearFecha(d.getValue().getFecha())));
        colMovTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colMovCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()));
        colMovMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));

        cargarDatos();
    }

    @FXML
    public void canjearBeneficio() {
        Cliente activo = context.getClienteActivo();
        Beneficio seleccionado = tablaBeneficios.getSelectionModel().getSelectedItem();
        if (activo == null || seleccionado == null) {
            lblMensaje.setText("Seleccione un beneficio de la lista para canjear.");
            return;
        }
        try {
            context.getBeneficioService().canjearBeneficio(activo.getId(), seleccionado.getId());
            lblMensaje.setText("Beneficio canjeado con exito: " + seleccionado.getNombre());
            context.notificarActualizacion();
            cargarDatos();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        tablaBeneficios.getItems().setAll(context.getBeneficioService().listarDisponibles());
        Cliente activo = context.getClienteActivo();
        if (activo != null) {
            lblPuntos.setText(activo.getPuntosAcumulados() + " pts");
            lblRango.setText(activo.getRango().name());
            tablaMovimientos.getItems().setAll(activo.getHistorialPuntos());
        }
    }
}
