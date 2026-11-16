package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Alerta;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.Transaccion;
import com.monedero.util.FormateadorMoneda;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ReporteController {
    @FXML
    private Label lblTotalIngresos;
    @FXML
    private Label lblTotalGastos;
    @FXML
    private Label lblPromedio;
    @FXML
    private Label lblUmbral;
    @FXML
    private Label lblResumenTipos;

    @FXML
    private TableView<Transaccion> tablaAtipicas;
    @FXML
    private TableColumn<Transaccion, String> colAtipicaFecha;
    @FXML
    private TableColumn<Transaccion, String> colAtipicaTipo;
    @FXML
    private TableColumn<Transaccion, String> colAtipicaMonto;
    @FXML
    private TableColumn<Transaccion, String> colAtipicaDesc;

    @FXML
    private TableView<Alerta> tablaAlertas;
    @FXML
    private TableColumn<Alerta, String> colAlertaFecha;
    @FXML
    private TableColumn<Alerta, String> colAlertaTipo;
    @FXML
    private TableColumn<Alerta, String> colAlertaMensaje;
    @FXML
    private TableColumn<Alerta, String> colAlertaEstado;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        colAtipicaFecha.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatearFecha(d.getValue().getFecha())));
        colAtipicaTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().obtenerTipo().name()));
        colAtipicaMonto.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatear(d.getValue().getMonto())));
        colAtipicaDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

        colAlertaFecha.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatearFecha(d.getValue().getFecha())));
        colAlertaTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colAlertaMensaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMensaje()));
        colAlertaEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isLeida() ? "Leida" : "Nueva"));

        cargarReportes();
    }

    @FXML
    public void marcarAlertasLeidas() {
        Cliente activo = context.getClienteActivo();
        if (activo != null) {
            activo.getAlertas().forEach(Alerta::marcarComoLeida);
            cargarReportes();
        }
    }

    private void cargarReportes() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            return;
        }
        BigDecimal ingresos = context.getAnalisisGastosService().calcularTotalIngresos(activo.getId());
        BigDecimal gastos = context.getAnalisisGastosService().calcularTotalGastos(activo.getId());
        lblTotalIngresos.setText(FormateadorMoneda.formatear(ingresos));
        lblTotalGastos.setText(FormateadorMoneda.formatear(gastos));

        List<Double> montos = context.getDeteccionAtipicasService().obtenerMontosHistoricos(activo.getId(), null);
        double promedio = context.getDeteccionAtipicasService().calcularPromedio(montos);
        double umbral = context.getDeteccionAtipicasService().calcularUmbral(montos);
        lblPromedio.setText(String.format("$%.2f", promedio));
        lblUmbral.setText(montos.size() < 3 ? "Requiere 3+ movs." : String.format("$%.2f", umbral));

        Map<TipoTransaccion, BigDecimal> porTipo = context.getAnalisisGastosService().agruparGastosPorTipo(activo.getId());
        if (porTipo.isEmpty()) {
            lblResumenTipos.setText("Sin egresos registrados.");
        } else {
            StringBuilder sb = new StringBuilder();
            porTipo.forEach((k, v) -> sb.append(k.name()).append(": ").append(FormateadorMoneda.formatear(v)).append("   "));
            lblResumenTipos.setText(sb.toString());
        }

        tablaAtipicas.getItems().setAll(
                context.getAnalisisGastosService().obtenerTransaccionesSospechosas(activo.getId())
        );
        tablaAlertas.getItems().setAll(activo.getAlertas());
    }
}
