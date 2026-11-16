package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.Transaccion;
import com.monedero.util.FormateadorMoneda;
import com.monedero.util.Validaciones;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.List;

public class TransaccionController {
    @FXML
    private ComboBox<TipoTransaccion> comboTipo;
    @FXML
    private ComboBox<Monedero> comboOrigen;
    @FXML
    private ComboBox<Monedero> comboDestino;
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private ComboBox<String> comboFiltroTipo;
    @FXML
    private Label lblMensaje;

    @FXML
    private TableView<Transaccion> tablaTransacciones;
    @FXML
    private TableColumn<Transaccion, String> colFecha;
    @FXML
    private TableColumn<Transaccion, String> colTipo;
    @FXML
    private TableColumn<Transaccion, String> colMonto;
    @FXML
    private TableColumn<Transaccion, String> colEstado;
    @FXML
    private TableColumn<Transaccion, Number> colPuntos;
    @FXML
    private TableColumn<Transaccion, String> colAtipica;
    @FXML
    private TableColumn<Transaccion, String> colDescripcion;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        comboTipo.getItems().setAll(TipoTransaccion.values());
        comboTipo.setValue(TipoTransaccion.DEPOSITO);
        comboTipo.setOnAction(e -> actualizarVisibilidadCampos());

        comboFiltroTipo.getItems().setAll("TODOS", "DEPOSITO", "RETIRO", "TRANSFERENCIA");
        comboFiltroTipo.setValue("TODOS");
        comboFiltroTipo.setOnAction(e -> cargarHistorial());

        colFecha.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatearFecha(d.getValue().getFecha())));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().obtenerTipo().name()));
        colMonto.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatear(d.getValue().getMonto())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        colPuntos.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getPuntosGenerados()));
        colAtipica.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isSospechosa() ? "SI" : "No"));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

        cargarCombosMonederos();
        actualizarVisibilidadCampos();
        cargarHistorial();
    }

    private void actualizarVisibilidadCampos() {
        boolean esTransferencia = comboTipo.getValue() == TipoTransaccion.TRANSFERENCIA;
        comboDestino.setVisible(esTransferencia);
        comboDestino.setManaged(esTransferencia);
    }

    @FXML
    public void ejecutarOperacion() {
        try {
            TipoTransaccion tipo = comboTipo.getValue();
            BigDecimal monto = Validaciones.parsearMontoPositivo(txtMonto.getText());
            String desc = txtDescripcion.getText();
            Monedero origen = comboOrigen.getValue();
            Monedero destino = comboDestino.getValue();

            Transaccion resultado;
            if (tipo == TipoTransaccion.DEPOSITO) {
                if (origen == null) {
                    throw new IllegalArgumentException("Seleccione un monedero.");
                }
                resultado = context.getTransaccionService().depositar(origen.getId(), monto, desc);
            } else if (tipo == TipoTransaccion.RETIRO) {
                if (origen == null) {
                    throw new IllegalArgumentException("Seleccione el monedero origen.");
                }
                resultado = context.getTransaccionService().retirar(origen.getId(), monto, desc);
            } else {
                if (origen == null || destino == null) {
                    throw new IllegalArgumentException("Seleccione monedero origen y destino.");
                }
                resultado = context.getTransaccionService().transferir(origen.getId(), destino.getId(), monto, desc);
            }

            txtMonto.clear();
            txtDescripcion.clear();
            String aviso = resultado.isSospechosa() ? " [ALERTA: monto atipico]" : "";
            lblMensaje.setText("Operacion completada (+ " + resultado.getPuntosGenerados() + " pts)." + aviso);
            context.notificarActualizacion();
            cargarCombosMonederos();
            cargarHistorial();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void revertirSeleccionada() {
        Transaccion seleccionada = tablaTransacciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            lblMensaje.setText("Seleccione una transaccion de la tabla para revertir.");
            return;
        }
        try {
            context.getTransaccionService().revertirTransaccion(seleccionada.getId());
            lblMensaje.setText("Transaccion revertida y puntos actualizados.");
            context.notificarActualizacion();
            cargarCombosMonederos();
            cargarHistorial();
        } catch (Exception e) {
            lblMensaje.setText("Error al revertir: " + e.getMessage());
        }
    }

    private void cargarCombosMonederos() {
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

    private void cargarHistorial() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            tablaTransacciones.getItems().clear();
            return;
        }
        String filtro = comboFiltroTipo.getValue();
        TipoTransaccion tipo = (filtro == null || "TODOS".equals(filtro)) ? null : TipoTransaccion.valueOf(filtro);
        List<Transaccion> lista = context.getTransaccionService().filtrarHistorial(activo.getId(), tipo);
        tablaTransacciones.getItems().setAll(lista);
    }
}
