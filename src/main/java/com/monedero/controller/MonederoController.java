package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Monedero;
import com.monedero.util.FormateadorMoneda;
import com.monedero.util.Validaciones;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.List;

public class MonederoController {
    @FXML
    private TextField txtNombreMonedero;
    @FXML
    private TextField txtSaldoInicial;
    @FXML
    private Label lblTotalSaldo;
    @FXML
    private Label lblMensaje;

    @FXML
    private ComboBox<Monedero> comboOrigenPropio;
    @FXML
    private ComboBox<Monedero> comboDestinoPropio;
    @FXML
    private TextField txtMontoTransferencia;
    @FXML
    private TextField txtDescTransferencia;
    @FXML
    private Label lblMensajeTransferencia;

    @FXML
    private TableView<Monedero> tablaMonederos;
    @FXML
    private TableColumn<Monedero, String> colNombre;
    @FXML
    private TableColumn<Monedero, String> colSaldo;
    @FXML
    private TableColumn<Monedero, String> colEstado;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colSaldo.setCellValueFactory(d -> new SimpleStringProperty(FormateadorMoneda.formatear(d.getValue().consultarSaldo())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));
        comboOrigenPropio.setOnAction(e -> actualizarDestinosPropios());
        cargarMonederos();
    }

    @FXML
    public void crearMonedero() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            lblMensaje.setText("Inicia sesion primero.");
            return;
        }
        try {
            String saldoTexto = txtSaldoInicial.getText().trim();
            BigDecimal saldo = saldoTexto.isEmpty() ? BigDecimal.ZERO : new BigDecimal(saldoTexto.replace(",", "."));
            Validaciones.validarTextoNoVacio(txtNombreMonedero.getText(), "nombre del monedero");
            Monedero nuevo = context.getMonederoService().crearMonedero(activo.getId(), txtNombreMonedero.getText(), saldo);
            txtNombreMonedero.clear();
            txtSaldoInicial.clear();
            lblMensaje.setText("Monedero creado: " + nuevo.getNombre());
            cargarMonederos();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void alternarEstado() {
        Monedero seleccionado = tablaMonederos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblMensaje.setText("Seleccione un monedero de la tabla.");
            return;
        }
        if (seleccionado.isActivo()) {
            seleccionado.desactivar();
        } else {
            seleccionado.activar();
        }
        cargarMonederos();
    }

    @FXML
    public void transferirEntrePropios() {
        Monedero origen = comboOrigenPropio.getValue();
        Monedero destino = comboDestinoPropio.getValue();
        if (origen == null || destino == null) {
            lblMensajeTransferencia.setText("Necesitas al menos dos monederos activos para transferir entre ellos.");
            return;
        }
        try {
            BigDecimal monto = Validaciones.parsearMontoPositivo(txtMontoTransferencia.getText());
            String desc = txtDescTransferencia.getText().trim().isEmpty()
                    ? "Movimiento entre mis monederos"
                    : txtDescTransferencia.getText().trim();
            context.getTransaccionService().transferir(origen.getId(), destino.getId(), monto, desc);
            txtMontoTransferencia.clear();
            txtDescTransferencia.clear();
            lblMensajeTransferencia.setText("Transferencia realizada de " + origen.getNombre() + " a " + destino.getNombre() + ".");
            context.notificarActualizacion();
            cargarMonederos();
        } catch (Exception e) {
            lblMensajeTransferencia.setText("Error: " + e.getMessage());
        }
    }

    private void actualizarDestinosPropios() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            comboDestinoPropio.getItems().clear();
            return;
        }
        Monedero origen = comboOrigenPropio.getValue();
        List<Monedero> otros = context.getMonederoService().obtenerActivosPorCliente(activo.getId()).stream()
                .filter(m -> origen == null || !m.getId().equals(origen.getId()))
                .toList();
        comboDestinoPropio.getItems().setAll(otros);
        if (!otros.isEmpty()) {
            comboDestinoPropio.setValue(otros.get(0));
        }
    }

    private void cargarMonederos() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            tablaMonederos.getItems().clear();
            comboOrigenPropio.getItems().clear();
            comboDestinoPropio.getItems().clear();
            lblTotalSaldo.setText("Saldo total: $0.00");
            return;
        }
        List<Monedero> lista = context.getMonederoService().obtenerPorCliente(activo.getId());
        tablaMonederos.getItems().setAll(lista);

        List<Monedero> activos = context.getMonederoService().obtenerActivosPorCliente(activo.getId());
        comboOrigenPropio.getItems().setAll(activos);
        if (!activos.isEmpty()) {
            comboOrigenPropio.setValue(activos.get(0));
        }
        actualizarDestinosPropios();

        BigDecimal total = lista.stream()
                .map(Monedero::consultarSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalSaldo.setText("Saldo total: " + FormateadorMoneda.formatear(total));
    }
}
