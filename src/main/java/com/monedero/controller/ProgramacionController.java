package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Frecuencia;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.TransaccionProgramada;
import com.monedero.util.FormateadorMoneda;
import com.monedero.util.Validaciones;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProgramacionController {
    @FXML
    private ComboBox<TipoTransaccion> comboTipo;
    @FXML
    private ComboBox<Frecuencia> comboFrecuencia;
    @FXML
    private ComboBox<Monedero> comboOrigen;
    @FXML
    private ComboBox<Monedero> comboDestino;
    @FXML
    private DatePicker dateFecha;
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private Label lblMensaje;

    @FXML
    private TableView<TransaccionProgramada> tablaProgramadas;
    @FXML
    private TableColumn<TransaccionProgramada, String> colFecha;
    @FXML
    private TableColumn<TransaccionProgramada, String> colTipo;
    @FXML
    private TableColumn<TransaccionProgramada, String> colMonto;
    @FXML
    private TableColumn<TransaccionProgramada, String> colFrecuencia;
    @FXML
    private TableColumn<TransaccionProgramada, String> colEstado;
    @FXML
    private TableColumn<TransaccionProgramada, String> colResultado;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        comboTipo.getItems().setAll(TipoTransaccion.values());
        comboTipo.setValue(TipoTransaccion.DEPOSITO);
        comboTipo.setOnAction(e -> actualizarCamposPorTipo());
        comboFrecuencia.getItems().setAll(Frecuencia.values());
        comboFrecuencia.setValue(Frecuencia.MENSUAL);
        dateFecha.setValue(LocalDate.now());

        colFecha.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatearFecha(d.getValue().getProximaEjecucion())));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colMonto.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatear(d.getValue().getMonto())));
        colFrecuencia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFrecuencia().name()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActiva() ? "Activa" : "Inactiva"));
        colResultado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUltimoResultado()));

        cargarCombos();
        actualizarCamposPorTipo();
        cargarTabla();
    }

    private void actualizarCamposPorTipo() {
        boolean esTransferencia = comboTipo.getValue() == TipoTransaccion.TRANSFERENCIA;
        comboDestino.setVisible(esTransferencia);
        comboDestino.setManaged(esTransferencia);
    }

    @FXML
    public void programarOperacion() {
        try {
            BigDecimal monto = Validaciones.parsearMontoPositivo(txtMonto.getText());
            LocalDate fecha = dateFecha.getValue() == null ? LocalDate.now() : dateFecha.getValue();
            LocalDateTime ejecucion = fecha.atTime(9, 0);
            TipoTransaccion tipo = comboTipo.getValue();
            Monedero origen = comboOrigen.getValue();
            Monedero destino = comboDestino.getValue();

            if (tipo == TipoTransaccion.DEPOSITO) {
                context.getProgramacionService().programar(tipo, monto, ejecucion, comboFrecuencia.getValue(), null, origen, txtDescripcion.getText());
            } else if (tipo == TipoTransaccion.RETIRO) {
                context.getProgramacionService().programar(tipo, monto, ejecucion, comboFrecuencia.getValue(), origen, null, txtDescripcion.getText());
            } else {
                context.getProgramacionService().programar(tipo, monto, ejecucion, comboFrecuencia.getValue(), origen, destino, txtDescripcion.getText());
            }

            txtMonto.clear();
            txtDescripcion.clear();
            lblMensaje.setText("Operacion programada correctamente.");
            cargarTabla();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void ejecutarVencidas() {
        int ejecutadas = context.getProgramacionService().ejecutarPendientes(LocalDateTime.now().plusDays(1));
        lblMensaje.setText("Se procesaron " + ejecutadas + " operaciones programadas.");
        cargarTabla();
    }

    @FXML
    public void cancelarSeleccionada() {
        TransaccionProgramada sel = tablaProgramadas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione una operacion programada.");
            return;
        }
        context.getProgramacionService().cancelar(sel.getId());
        lblMensaje.setText("Operacion cancelada.");
        cargarTabla();
    }

    private void cargarCombos() {
        Cliente activo = context.getClienteActivo();
        if (activo != null) {
            List<Monedero> propios = context.getMonederoService().obtenerActivosPorCliente(activo.getId());
            comboOrigen.getItems().setAll(propios);
            if (!propios.isEmpty()) {
                comboOrigen.setValue(propios.get(0));
            }
        }
        comboOrigen.setOnAction(e -> actualizarDestinosDisponibles());
        actualizarDestinosDisponibles();
    }

    private void actualizarDestinosDisponibles() {
        Cliente activo = context.getClienteActivo();
        Monedero origenSel = comboOrigen.getValue();
        List<Monedero> propiosOtros = activo == null ? List.of() : context.getMonederoService()
                .obtenerActivosPorCliente(activo.getId()).stream()
                .filter(m -> origenSel == null || !m.getId().equals(origenSel.getId()))
                .toList();
        List<Monedero> externos = context.getMonederoService().listarTodos().stream()
                .filter(Monedero::isActivo)
                .filter(m -> activo == null || m.getPropietario() == null || !m.getPropietario().getId().equals(activo.getId()))
                .toList();

        List<Monedero> todos = new java.util.ArrayList<>();
        todos.addAll(propiosOtros);
        todos.addAll(externos);
        comboDestino.getItems().setAll(todos);

        javafx.util.Callback<javafx.scene.control.ListView<Monedero>, javafx.scene.control.ListCell<Monedero>> factory =
                lv -> new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(Monedero item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (activo != null && item.getPropietario() != null
                                && item.getPropietario().getId().equals(activo.getId())) {
                            setText("[Mi monedero] " + item.getNombre() + " (" + FormateadorMoneda.formatear(item.consultarSaldo()) + ")");
                        } else {
                            String prop = item.getPropietario() != null ? item.getPropietario().getNombre() : "";
                            setText(item.getNombre() + " - " + prop);
                        }
                    }
                };
        comboDestino.setCellFactory(factory);
        comboDestino.setButtonCell(factory.call(null));

        if (!todos.isEmpty()) {
            comboDestino.setValue(todos.get(0));
        }
    }

    private void cargarTabla() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            tablaProgramadas.getItems().clear();
            return;
        }
        tablaProgramadas.getItems().setAll(
                context.getProgramacionService().obtenerOrdenadasPorCliente(activo.getId())
        );
    }
}
