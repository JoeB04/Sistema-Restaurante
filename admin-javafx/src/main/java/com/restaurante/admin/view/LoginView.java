package com.restaurante.admin.view;

import com.restaurante.admin.client.ApiClient;
import com.restaurante.admin.model.LoginResponseDTO;
import com.restaurante.admin.model.UsuarioDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView extends VBox {

    private final ApiClient apiClient;
    private final Consumer<UsuarioDTO> onLoginExitoso;

    private final TextField usuarioField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();
    private final ProgressIndicator cargando = new ProgressIndicator();

    public LoginView(ApiClient apiClient, Consumer<UsuarioDTO> onLoginExitoso) {
        this.apiClient = apiClient;
        this.onLoginExitoso = onLoginExitoso;

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(40));
        setMaxWidth(320);

        Label titulo = new Label("Sistema de Restaurante");
        titulo.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label subtitulo = new Label("Acceso Administrador");
        subtitulo.setStyle("-fx-text-fill: -color-fg-muted;");

        usuarioField.setPromptText("Usuario");
        passwordField.setPromptText("Contraseña");

        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setWrapText(true);

        cargando.setVisible(false);
        cargando.setMaxSize(24, 24);

        Button loginBtn = new Button("Ingresar");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> intentarLogin());

        // Enter en cualquiera de los dos campos tambien intenta login
        usuarioField.setOnAction(e -> intentarLogin());
        passwordField.setOnAction(e -> intentarLogin());

        getChildren().addAll(titulo, subtitulo, usuarioField, passwordField,
                loginBtn, cargando, errorLabel);
    }

    private void intentarLogin() {
        String usuario = usuarioField.getText().trim();
        String password = passwordField.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Ingresa usuario y contraseña.");
            return;
        }

        errorLabel.setText("");
        cargando.setVisible(true);

        new Thread(() -> {
            try {
                LoginResponseDTO respuesta = apiClient.login(usuario, password);

                Platform.runLater(() -> {
                    cargando.setVisible(false);

                    if (!respuesta.exito) {
                        errorLabel.setText(respuesta.mensaje);
                        return;
                    }

                    if (!"ADMIN".equals(respuesta.usuario.rol)) {
                        errorLabel.setText("Esta aplicación es solo para administradores.");
                        return;
                    }

                    onLoginExitoso.accept(respuesta.usuario);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    cargando.setVisible(false);
                    errorLabel.setText("No se pudo conectar al servidor: " + ex.getMessage());
                });
            }
        }).start();
    }
}
