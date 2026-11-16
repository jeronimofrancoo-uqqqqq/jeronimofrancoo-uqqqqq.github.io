package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.domain.model.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ClienteController {
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCorreo;
    @FXML
    private TextField txtTelefono;
    @FXML
    private Label lblRango;
    @FXML
    private Label lblPuntos;
    @FXML
    private Label lblBeneficiosRango;
    @FXML
    private Label lblMensaje;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void initialize() {
        cargarPerfil();
    }

    @FXML
    public void guardarCambios() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            return;
        }
        try {
            context.getClienteService().actualizarPerfil(
                    activo.getId(),
                    txtNombre.getText(),
                    txtTelefono.getText()
            );
            context.notificarActualizacion();
            lblMensaje.setText("Datos de tu cuenta actualizados.");
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    private void cargarPerfil() {
        Cliente activo = context.getClienteActivo();
        if (activo == null) {
            return;
        }
        txtNombre.setText(activo.getNombre());
        txtCorreo.setText(activo.getCorreo());
        txtTelefono.setText(activo.getTelefono());
        lblRango.setText(activo.getRango().name());
        lblPuntos.setText(activo.getPuntosAcumulados() + " pts");

        int descComision = (int) Math.round(activo.getRango().getDescuentoComision() * 100);
        int bonoPuntos = (int) Math.round(activo.getRango().getBonoPuntos() * 100);
        lblBeneficiosRango.setText(
                "Descuento en comisiones: " + descComision + "%  |  Bono adicional en puntos: +" + bonoPuntos + "%"
        );
    }
}
