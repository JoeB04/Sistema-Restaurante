package com.restaurante.admin.view;

import com.restaurante.admin.client.ApiClient;
import com.restaurante.admin.model.CategoriaDTO;
import com.restaurante.admin.model.PlatoDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class MenuView extends BorderPane {

    private final ApiClient apiClient;

    private final ObservableList<PlatoDTO> platos = FXCollections.observableArrayList();
    private final ObservableList<CategoriaDTO> categorias = FXCollections.observableArrayList();

    private final Label estadoLabel = new Label("Cargando menú...");
    private final TableView<PlatoDTO> tabla = new TableView<>();

    public MenuView(ApiClient apiClient) {
        this.apiClient = apiClient;

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        root.getChildren().add(new Label("Gestión del Menú"));
        root.getChildren().add(estadoLabel);
        root.getChildren().add(construirFormularioCategoria());
        root.getChildren().add(new Separator());
        root.getChildren().add(construirFormularioPlato());
        root.getChildren().add(new Separator());
        root.getChildren().add(construirTablaPlatos());

        setCenter(root);

        cargarDatos();
    }

    // ---------- Formulario: nueva categoria ----------

    private HBox construirFormularioCategoria() {
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre de categoría (ej. Postres)");

        Button crearBtn = new Button("Crear categoría");
        crearBtn.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            if (nombre.isEmpty()) return;

            new Thread(() -> {
                try {
                    apiClient.crearCategoria(nombre);
                    Platform.runLater(() -> {
                        nombreField.clear();
                        cargarDatos();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo crear la categoría: " + ex.getMessage()));
                }
            }).start();
        });

        HBox box = new HBox(10, new Label("Nueva categoría:"), nombreField, crearBtn);
        return box;
    }

    // ---------- Formulario: nuevo plato ----------

    private VBox construirFormularioPlato() {
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del plato");

        TextField descripcionField = new TextField();
        descripcionField.setPromptText("Descripción");

        TextField precioField = new TextField();
        precioField.setPromptText("Precio (ej. 25.00)");

        ComboBox<CategoriaDTO> categoriaCombo = new ComboBox<>(categorias);
        categoriaCombo.setPromptText("Categoría");

        Button crearBtn = new Button("Agregar plato");
        crearBtn.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            String descripcion = descripcionField.getText().trim();
            CategoriaDTO categoria = categoriaCombo.getValue();

            if (nombre.isEmpty() || categoria == null) {
                mostrarError("Nombre y categoría son obligatorios.");
                return;
            }

            double precio;
            try {
                precio = Double.parseDouble(precioField.getText().trim());
            } catch (NumberFormatException ex) {
                mostrarError("El precio debe ser un número (ej. 25.00).");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.crearPlato(nombre, descripcion, precio, categoria.id);
                    Platform.runLater(() -> {
                        nombreField.clear();
                        descripcionField.clear();
                        precioField.clear();
                        cargarDatos();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo crear el plato: " + ex.getMessage()));
                }
            }).start();
        });

        HBox fila1 = new HBox(10, nombreField, descripcionField);
        HBox fila2 = new HBox(10, precioField, categoriaCombo, crearBtn);

        return new VBox(8, new Label("Nuevo plato:"), fila1, fila2);
    }

    // ---------- Tabla de platos ----------

    @SuppressWarnings("unchecked")
    private TableView<PlatoDTO> construirTablaPlatos() {
        TableColumn<PlatoDTO, String> nombreCol = new TableColumn<>("Plato");
        nombreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nombre));
        nombreCol.setPrefWidth(180);

        TableColumn<PlatoDTO, String> categoriaCol = new TableColumn<>("Categoría");
        categoriaCol.setCellValueFactory(data -> {
            CategoriaDTO cat = data.getValue().categoria;
            return new SimpleStringProperty(cat != null ? cat.nombre : "-");
        });
        categoriaCol.setPrefWidth(140);

        TableColumn<PlatoDTO, String> precioCol = new TableColumn<>("Precio");
        precioCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("S/ %.2f", data.getValue().precio)));
        precioCol.setPrefWidth(100);

        TableColumn<PlatoDTO, Void> disponibleCol = new TableColumn<>("Disponibilidad");
        disponibleCol.setPrefWidth(160);
        disponibleCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();

            {
                toggleBtn.setOnAction(e -> {
                    PlatoDTO plato = getTableView().getItems().get(getIndex());
                    boolean nuevoValor = !Boolean.TRUE.equals(plato.disponible);
                    new Thread(() -> {
                        try {
                            apiClient.cambiarDisponibilidad(plato.id, nuevoValor);
                            Platform.runLater(() -> cargarDatos());
                        } catch (Exception ex) {
                            Platform.runLater(() -> mostrarError("No se pudo actualizar: " + ex.getMessage()));
                        }
                    }).start();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                PlatoDTO plato = getTableView().getItems().get(getIndex());
                boolean disponible = Boolean.TRUE.equals(plato.disponible);
                toggleBtn.setText(disponible ? "✅ Disponible" : "❌ Agotado");
                setGraphic(toggleBtn);
            }
        });

        TableColumn<PlatoDTO, Void> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setPrefWidth(160);
        accionesCol.setCellFactory(col -> new TableCell<>() {
            private final Button editarBtn = new Button("✏️ Editar");
            private final Button eliminarBtn = new Button("✖");
            private final HBox contenedor = new HBox(6, editarBtn, eliminarBtn);

            {
                editarBtn.setOnAction(e -> {
                    PlatoDTO plato = getTableView().getItems().get(getIndex());
                    abrirFormularioEditarPlato(plato);
                });
                eliminarBtn.setOnAction(e -> {
                    PlatoDTO plato = getTableView().getItems().get(getIndex());
                    confirmarYEliminarPlato(plato);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tabla.getColumns().addAll(List.of(nombreCol, categoriaCol, precioCol, disponibleCol, accionesCol));
        tabla.setItems(platos);
        tabla.setPrefHeight(280);
        return tabla;
    }

    // ---------- Carga de datos ----------

    private void cargarDatos() {
        estadoLabel.setText("Cargando menú...");
        new Thread(() -> {
            try {
                List<CategoriaDTO> categoriasDesdeApi = apiClient.obtenerCategorias();
                List<PlatoDTO> platosDesdeApi = apiClient.obtenerPlatos();

                Platform.runLater(() -> {
                    categorias.setAll(categoriasDesdeApi);
                    platos.setAll(platosDesdeApi);
                    estadoLabel.setText(platosDesdeApi.size() + " platos en " + categoriasDesdeApi.size() + " categorías");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> estadoLabel.setText("Error al cargar el menú: " + ex.getMessage()));
            }
        }).start();
    }

    // ---------- Editar plato ----------

    private void abrirFormularioEditarPlato(PlatoDTO plato) {
        TextField nombreField = new TextField(plato.nombre);
        TextField descripcionField = new TextField(plato.descripcion != null ? plato.descripcion : "");
        TextField precioField = new TextField(String.valueOf(plato.precio));

        ComboBox<CategoriaDTO> categoriaCombo = new ComboBox<>(categorias);
        categorias.stream()
                .filter(c -> plato.categoria != null && c.id.equals(plato.categoria.id))
                .findFirst()
                .ifPresent(categoriaCombo::setValue);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button guardarBtn = new Button("Guardar cambios");

        VBox contenido = new VBox(10,
                new Label("Editar plato"),
                nombreField, descripcionField, precioField, categoriaCombo,
                errorLabel, guardarBtn);
        contenido.setPadding(new Insets(20));

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Editar plato");
        ventana.setScene(new Scene(contenido, 320, 320));

        guardarBtn.setOnAction(e -> {
            CategoriaDTO categoria = categoriaCombo.getValue();
            if (nombreField.getText().isBlank() || categoria == null) {
                errorLabel.setText("Nombre y categoría son obligatorios.");
                return;
            }
            double precio;
            try {
                precio = Double.parseDouble(precioField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("El precio debe ser un número.");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.editarPlato(plato.id, nombreField.getText().trim(),
                            descripcionField.getText().trim(), precio, categoria.id);
                    Platform.runLater(() -> {
                        ventana.close();
                        cargarDatos();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        ventana.show();
    }

    // ---------- Eliminar plato ----------

    private void confirmarYEliminarPlato(PlatoDTO plato) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar plato");
        confirm.setContentText("¿Seguro que quieres eliminar \"" + plato.nombre + "\"? Esta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        apiClient.eliminarPlato(plato.id);
                        Platform.runLater(this::cargarDatos);
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarError("No se pudo eliminar: " + ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
