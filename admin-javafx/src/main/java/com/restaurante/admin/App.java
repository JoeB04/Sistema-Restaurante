package com.restaurante.admin;

import atlantafx.base.theme.PrimerLight;
import com.restaurante.admin.client.ApiClient;
import com.restaurante.admin.model.UsuarioDTO;
import com.restaurante.admin.view.CroquisView;
import com.restaurante.admin.view.LoginView;
import com.restaurante.admin.view.MenuView;
import com.restaurante.admin.view.ReportesView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class App extends Application {

    private final ApiClient apiClient = new ApiClient();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // Tema visual moderno (en vez del JavaFX gris por defecto)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        stage.setTitle("Sistema de Restaurante - Login");
        mostrarLogin();
        stage.show();
    }

    private void mostrarLogin() {
        LoginView loginView = new LoginView(apiClient, this::mostrarAppPrincipal);
        stage.setScene(new Scene(loginView, 780, 640));
    }

    private void mostrarAppPrincipal(UsuarioDTO usuario) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab croquisTab = new Tab("Croquis", new CroquisView(apiClient));
        Tab menuTab = new Tab("Menú", new MenuView(apiClient));
        Tab reportesTab = new Tab("Reportes", new ReportesView(apiClient));
        tabPane.getTabs().addAll(croquisTab, menuTab, reportesTab);

        Label bienvenida = new Label("👤 " + usuario.nombre + " (Admin)");
        bienvenida.setStyle("-fx-padding: 8 16; -fx-font-weight: bold;");
        HBox topBar = new HBox(bienvenida);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);

        stage.setTitle("Sistema de Restaurante - Admin");
        stage.setScene(new Scene(root, 780, 640));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
