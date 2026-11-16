package com.monedero.controller;

import com.monedero.app.AppContext;
import com.monedero.app.MonederoApplication;
import com.monedero.domain.model.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class LoginController {
    @FXML
    private TextField txtCorreoLogin;
    @FXML
    private PasswordField txtPinLogin;
    @FXML
    private Label lblMensajeLogin;

    @FXML
    private TextField txtNombreRegistro;
    @FXML
    private TextField txtCorreoRegistro;
    @FXML
    private TextField txtTelefonoRegistro;
    @FXML
    private PasswordField txtPinRegistro;
    @FXML
    private Label lblMensajeRegistro;

    private final AppContext context = AppContext.getInstancia();

    @FXML
    public void iniciarSesion() {
        try {
            Cliente cliente = context.getClienteService().autenticar(
                    txtCorreoLogin.getText(),
                    txtPinLogin.getText()
            );
            context.setClienteActivo(cliente);
            MonederoApplication.mostrarPrincipal();
        } catch (Exception e) {
            lblMensajeLogin.setText(e.getMessage());
        }
    }

    @FXML
    public void registrarCuenta() {
        try {
            Cliente nuevo = context.getClienteService().registrarCliente(
                    txtNombreRegistro.getText(),
                    txtCorreoRegistro.getText(),
                    txtTelefonoRegistro.getText(),
                    txtPinRegistro.getText()
            );
            // Cada cliente nuevo inicia con un monedero principal activo
            context.getMonederoService().crearMonedero(nuevo.getId(), "Monedero Principal", BigDecimal.ZERO);
            context.setClienteActivo(nuevo);
            MonederoApplication.mostrarPrincipal();
        } catch (Exception e) {
            lblMensajeRegistro.setText(e.getMessage());
        }
    }

    @FXML
    public void usarCuentaAna() {
        txtCorreoLogin.setText("ana.martinez@correo.com");
        txtPinLogin.setText("1234");
    }

    @FXML
    public void usarCuentaCarlos() {
        txtCorreoLogin.setText("carlos.gomez@correo.com");
        txtPinLogin.setText("5678");
    }
}
