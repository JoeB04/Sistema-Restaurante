package com.restaurante.admin.view;

import com.restaurante.admin.client.ApiClient;
import com.restaurante.admin.model.BoletaDTO;
import com.restaurante.admin.model.ItemDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ReportesView extends BorderPane {

    private final ApiClient apiClient;
    private final ObservableList<BoletaDTO> boletas = FXCollections.observableArrayList();

    private final Label totalLabel = new Label("Total vendido: S/ 0.00");
    private final Label estadoLabel = new Label("Cargando reportes...");

    public ReportesView(ApiClient apiClient) {
        this.apiClient = apiClient;

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Button refrescarBtn = new Button("Refrescar");
        refrescarBtn.setOnAction(e -> cargarBoletas());

        root.getChildren().addAll(
                new Label("Reportes de Ventas"),
                estadoLabel,
                totalLabel,
                refrescarBtn,
                construirTabla()
        );

        setCenter(root);

        cargarBoletas();
    }

    @SuppressWarnings("unchecked")
    private TableView<BoletaDTO> construirTabla() {
        TableView<BoletaDTO> tabla = new TableView<>();

        TableColumn<BoletaDTO, String> idCol = new TableColumn<>("Boleta #");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().id)));
        idCol.setPrefWidth(70);

        TableColumn<BoletaDTO, String> mesaCol = new TableColumn<>("Mesa");
        mesaCol.setCellValueFactory(data -> {
            var orden = data.getValue().orden;
            String mesa = (orden != null && orden.mesa != null) ? "Mesa " + orden.mesa.numero : "-";
            return new SimpleStringProperty(mesa);
        });
        mesaCol.setPrefWidth(90);

        TableColumn<BoletaDTO, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("S/ %.2f", data.getValue().total)));
        totalCol.setPrefWidth(90);

        TableColumn<BoletaDTO, String> metodoCol = new TableColumn<>("Método de pago");
        metodoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().metodoPago));
        metodoCol.setPrefWidth(130);

        TableColumn<BoletaDTO, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().fechaEmision));
        fechaCol.setPrefWidth(160);

        TableColumn<BoletaDTO, Void> detalleCol = new TableColumn<>("Detalle");
        detalleCol.setPrefWidth(110);
        detalleCol.setCellFactory(col -> new TableCell<>() {
            private final Button verBtn = new Button("🧾 Ver platos");

            {
                verBtn.setOnAction(e -> {
                    BoletaDTO boleta = getTableView().getItems().get(getIndex());
                    abrirDetalleBoleta(boleta);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : verBtn);
            }
        });

        tabla.getColumns().addAll(List.of(idCol, mesaCol, totalCol, metodoCol, fechaCol, detalleCol));
        tabla.setItems(boletas);
        tabla.setPrefHeight(320);
        return tabla;
    }

    // Ventana con los platos vendidos en esa boleta (igual estilo que el detalle del croquis)
    private void abrirDetalleBoleta(BoletaDTO boleta) {
        VBox contenido = new VBox(10);
        contenido.setPadding(new Insets(20));

        String mesaTexto = (boleta.orden != null && boleta.orden.mesa != null)
                ? "Mesa " + boleta.orden.mesa.numero : "Mesa N/A";

        Label titulo = new Label("Boleta #" + boleta.id + " - " + mesaTexto);
        titulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitulo = new Label(boleta.fechaEmision + "  ·  " + boleta.metodoPago);
        subtitulo.setStyle("-fx-text-fill: -color-fg-muted;");

        contenido.getChildren().addAll(titulo, subtitulo, new Separator());

        List<ItemDTO> items = (boleta.orden != null) ? boleta.orden.items : null;
        if (items == null || items.isEmpty()) {
            contenido.getChildren().add(new Label("No hay platos registrados para esta orden."));
        } else {
            for (ItemDTO item : items) {
                double subtotalItem = item.precioUnitario * item.cantidad;

                Label nombre = new Label(item.cantidad + "x " + item.plato.nombre);
                nombre.setStyle("-fx-font-weight: bold;");
                if (item.notas != null && !item.notas.isBlank()) {
                    nombre.setText(nombre.getText() + "  (" + item.notas + ")");
                }

                Label precio = new Label(String.format("S/ %.2f", subtotalItem));

                HBox fila = new HBox(nombre);
                HBox.setHgrow(nombre, Priority.ALWAYS);
                HBox filaCompleta = new HBox(fila, precio);
                HBox.setHgrow(fila, Priority.ALWAYS);

                contenido.getChildren().add(filaCompleta);
            }
        }

        contenido.getChildren().add(new Separator());
        Label totalBoleta = new Label(String.format("Total: S/ %.2f", boleta.total));
        totalBoleta.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        contenido.getChildren().add(totalBoleta);

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Detalle de Boleta");
        ventana.setScene(new Scene(contenido, 380, 400));
        ventana.show();
    }

    private void cargarBoletas() {
        estadoLabel.setText("Cargando reportes...");
        new Thread(() -> {
            try {
                List<BoletaDTO> lista = apiClient.obtenerBoletas();
                double total = lista.stream().mapToDouble(b -> b.total).sum();

                Platform.runLater(() -> {
                    boletas.setAll(lista);
                    estadoLabel.setText(lista.size() + " boletas emitidas");
                    totalLabel.setText(String.format("Total vendido: S/ %.2f", total));
                });
            } catch (Exception ex) {
                Platform.runLater(() -> estadoLabel.setText("Error al cargar reportes: " + ex.getMessage()));
            }
        }).start();
    }
}