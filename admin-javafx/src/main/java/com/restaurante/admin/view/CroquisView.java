package com.restaurante.admin.view;

import com.restaurante.admin.client.ApiClient;
import com.restaurante.admin.model.ElementoDTO;
import com.restaurante.admin.model.ItemDTO;
import com.restaurante.admin.model.MesaDTO;
import com.restaurante.admin.model.OrdenDTO;
import com.restaurante.admin.model.ZonaDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class CroquisView extends BorderPane {

    private static final int TAMANO_MESA_DEFECTO = 90;
    private static final int TAMANO_MINIMO_MESA = 50;
    private static final int TAMANO_MINIMO_ZONA_ANCHO = 100;
    private static final int TAMANO_MINIMO_ZONA_ALTO = 80;
    private static final int TAMANO_MINIMO_ELEMENTO = 30;
    private static final int TAMANO_GRID = 40;
    private static final int ANCHO_CROQUIS = 700;
    private static final int ALTO_CROQUIS = 480;

    private static final Color[] COLORES_ZONA = {
            Color.web("#E3F2FD"), Color.web("#FCE4EC"), Color.web("#E8F5E9"),
            Color.web("#FFF3E0"), Color.web("#F3E5F5"), Color.web("#FFFDE7")
    };

    // Iconos sugeridos para elementos decorativos
    private static final String[] ICONOS_SUGERIDOS = {"🚪", "🍳", "🍹", "🚻", "❄", "🔥", "⊞", "🧺"};

    private final ApiClient apiClient;
    private final Pane croquisPane = new Pane();
    private final Label estadoLabel = new Label("Cargando...");
    private final ToggleButton modoEdicionToggle = new ToggleButton("🔒 Bloqueado");

    public CroquisView(ApiClient apiClient) {
        this.apiClient = apiClient;

        Button refrescarBtn = new Button("Refrescar");
        refrescarBtn.setOnAction(e -> cargarTodo());

        Button agregarMesaBtn = new Button("+ Mesa");
        agregarMesaBtn.setOnAction(e -> abrirFormularioNuevaMesa());

        Button agregarElementoBtn = new Button("+ Elemento (Cocina, Barra...)");
        agregarElementoBtn.setOnAction(e -> abrirFormularioNuevoElemento());

        modoEdicionToggle.setOnAction(e -> {
            boolean activo = modoEdicionToggle.isSelected();
            modoEdicionToggle.setText(activo
                    ? "🔓 Editando (arrastra para mover, esquina para redimensionar)"
                    : "🔒 Bloqueado");
            cargarTodo();
        });

        HBox botones = new HBox(10, refrescarBtn, agregarMesaBtn, agregarElementoBtn, modoEdicionToggle);

        VBox topBar = new VBox(8,
                new Label("Croquis del Restaurante"),
                estadoLabel,
                botones);
        topBar.setStyle("-fx-padding: 16;");

        croquisPane.setPrefSize(ANCHO_CROQUIS, ALTO_CROQUIS);
        croquisPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        setTop(topBar);
        setCenter(croquisPane);

        cargarTodo();
    }

    // Carga mesas, zonas y elementos decorativos juntos
    private void cargarTodo() {
        estadoLabel.setText("Cargando...");
        new Thread(() -> {
            try {
                List<MesaDTO> mesas = apiClient.obtenerMesas();
                List<ZonaDTO> zonas = apiClient.obtenerZonas();
                List<ElementoDTO> elementos = apiClient.obtenerElementos();
                Platform.runLater(() -> dibujarTodo(mesas, zonas, elementos));
            } catch (Exception ex) {
                Platform.runLater(() ->
                        estadoLabel.setText("Error al conectar con el backend: " + ex.getMessage()));
            }
        }).start();
    }

    private void dibujarTodo(List<MesaDTO> mesas, List<ZonaDTO> zonas, List<ElementoDTO> elementos) {
        croquisPane.getChildren().clear();

        boolean editando = modoEdicionToggle.isSelected();
        estadoLabel.setText(mesas.size() + " mesas, " + zonas.size() + " zonas, " + elementos.size() + " elementos" +
                (editando ? " · arrastra para mover, esquina para redimensionar, click para editar" : " · click en una mesa para ver detalle"));

        dibujarCuadricula();

        int colorIndex = 0;
        for (ZonaDTO zona : zonas) {
            Group zonaGroup = crearZonaVisual(zona, COLORES_ZONA[colorIndex % COLORES_ZONA.length]);
            zonaGroup.setLayoutX(zona.posicionX);
            zonaGroup.setLayoutY(zona.posicionY);
            croquisPane.getChildren().add(zonaGroup);
            colorIndex++;
        }

        for (ElementoDTO elemento : elementos) {
            Group elementoGroup = crearElementoVisual(elemento);
            elementoGroup.setLayoutX(elemento.posicionX);
            elementoGroup.setLayoutY(elemento.posicionY);
            croquisPane.getChildren().add(elementoGroup);
        }

        for (MesaDTO mesa : mesas) {
            Group mesaGroup = crearMesaVisual(mesa, zonas);
            mesaGroup.setLayoutX(mesa.posicionX);
            mesaGroup.setLayoutY(mesa.posicionY);
            croquisPane.getChildren().add(mesaGroup);
        }
    }

    private void dibujarCuadricula() {
        for (int x = 0; x <= ANCHO_CROQUIS; x += TAMANO_GRID) {
            Line linea = new Line(x, 0, x, ALTO_CROQUIS);
            linea.setStroke(Color.web("#00000012"));
            croquisPane.getChildren().add(linea);
        }
        for (int y = 0; y <= ALTO_CROQUIS; y += TAMANO_GRID) {
            Line linea = new Line(0, y, ANCHO_CROQUIS, y);
            linea.setStroke(Color.web("#00000012"));
            croquisPane.getChildren().add(linea);
        }
    }

    // ---------- Zona: mover y redimensionar ----------

    private Group crearZonaVisual(ZonaDTO zona, Color color) {
        double ancho = zona.ancho != null ? zona.ancho : 300;
        double alto = zona.alto != null ? zona.alto : 200;

        Rectangle rect = new Rectangle(ancho, alto);
        rect.setArcWidth(24);
        rect.setArcHeight(24);
        rect.setFill(color);
        rect.setStroke(color.darker());
        rect.getStrokeDashArray().addAll(6.0, 5.0);
        rect.setStrokeWidth(1.5);

        Label nombreLabel = new Label(zona.nombre);
        nombreLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #555555; " +
                "-fx-background-color: white; -fx-padding: 2 8; -fx-background-radius: 4; " +
                "-fx-border-color: #dddddd; -fx-border-radius: 4;");
        nombreLabel.setLayoutX(8);
        nombreLabel.setLayoutY(8);

        boolean editando = modoEdicionToggle.isSelected();

        Rectangle manija = new Rectangle(14, 14);
        manija.setFill(color.darker());
        manija.setArcWidth(4);
        manija.setArcHeight(4);
        manija.layoutXProperty().bind(rect.widthProperty().subtract(14));
        manija.layoutYProperty().bind(rect.heightProperty().subtract(14));
        manija.setCursor(Cursor.SE_RESIZE);
        manija.setVisible(editando);

        Group box = new Group(rect, nombreLabel, manija);
        box.setCursor(editando ? Cursor.OPEN_HAND : Cursor.DEFAULT);

        final double[] inicio = new double[2];
        final boolean[] seMovio = {false};

        box.setOnMousePressed(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            inicio[0] = e.getSceneX() - box.getLayoutX();
            inicio[1] = e.getSceneY() - box.getLayoutY();
            seMovio[0] = false;
            box.setCursor(Cursor.CLOSED_HAND);
        });

        box.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            box.setLayoutX(Math.max(0, e.getSceneX() - inicio[0]));
            box.setLayoutY(Math.max(0, e.getSceneY() - inicio[1]));
            seMovio[0] = true;
        });

        box.setOnMouseReleased(e -> {
            if (!modoEdicionToggle.isSelected() || !seMovio[0]) return;
            box.setCursor(Cursor.OPEN_HAND);
            int nuevaX = (int) box.getLayoutX();
            int nuevaY = (int) box.getLayoutY();
            new Thread(() -> {
                try {
                    apiClient.actualizarGeometriaZona(zona.id, nuevaX, nuevaY, (int) rect.getWidth(), (int) rect.getHeight());
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo mover la zona: " + ex.getMessage()));
                }
            }).start();
        });

        manija.setOnMousePressed(e -> {
            if (modoEdicionToggle.isSelected()) e.consume();
        });

        manija.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            Point2D local = box.sceneToLocal(e.getSceneX(), e.getSceneY());
            rect.setWidth(Math.max(TAMANO_MINIMO_ZONA_ANCHO, local.getX()));
            rect.setHeight(Math.max(TAMANO_MINIMO_ZONA_ALTO, local.getY()));
            e.consume();
        });

        manija.setOnMouseReleased(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            new Thread(() -> {
                try {
                    apiClient.actualizarGeometriaZona(zona.id, (int) box.getLayoutX(), (int) box.getLayoutY(),
                            (int) rect.getWidth(), (int) rect.getHeight());
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo redimensionar la zona: " + ex.getMessage()));
                }
            }).start();
            e.consume();
        });

        return box;
    }

    // ---------- Elemento decorativo: mover, redimensionar, click para editar/eliminar ----------

    private Group crearElementoVisual(ElementoDTO elemento) {
        double ancho = elemento.ancho != null ? elemento.ancho : 90;
        double alto = elemento.alto != null ? elemento.alto : 50;

        Rectangle rect = new Rectangle(ancho, alto);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setFill(Color.web("#37474F"));

        Label texto = new Label(elemento.icono + " " + elemento.nombre);
        texto.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white;");
        texto.layoutXProperty().bind(rect.widthProperty().subtract(texto.widthProperty()).divide(2));
        texto.layoutYProperty().bind(rect.heightProperty().subtract(texto.heightProperty()).divide(2));

        boolean editando = modoEdicionToggle.isSelected();

        Rectangle manija = new Rectangle(12, 12);
        manija.setFill(Color.web("#FFFFFFAA"));
        manija.setArcWidth(4);
        manija.setArcHeight(4);
        manija.layoutXProperty().bind(rect.widthProperty().subtract(12));
        manija.layoutYProperty().bind(rect.heightProperty().subtract(12));
        manija.setCursor(Cursor.SE_RESIZE);
        manija.setVisible(editando);

        Group box = new Group(rect, texto, manija);
        box.setCursor(editando ? Cursor.OPEN_HAND : Cursor.DEFAULT);

        final double[] inicio = new double[2];
        final boolean[] seMovio = {false};

        box.setOnMousePressed(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            inicio[0] = e.getSceneX() - box.getLayoutX();
            inicio[1] = e.getSceneY() - box.getLayoutY();
            seMovio[0] = false;
            box.setCursor(Cursor.CLOSED_HAND);
        });

        box.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            box.setLayoutX(Math.max(0, e.getSceneX() - inicio[0]));
            box.setLayoutY(Math.max(0, e.getSceneY() - inicio[1]));
            seMovio[0] = true;
        });

        box.setOnMouseReleased(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            if (seMovio[0]) {
                box.setCursor(Cursor.OPEN_HAND);
                int nuevaX = (int) box.getLayoutX();
                int nuevaY = (int) box.getLayoutY();
                new Thread(() -> {
                    try {
                        apiClient.actualizarGeometriaElemento(elemento.id, nuevaX, nuevaY, (int) rect.getWidth(), (int) rect.getHeight());
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarError("No se pudo mover: " + ex.getMessage()));
                    }
                }).start();
            } else {
                abrirFormularioEditarElemento(elemento);
            }
        });

        manija.setOnMousePressed(e -> {
            if (modoEdicionToggle.isSelected()) e.consume();
        });

        manija.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            Point2D local = box.sceneToLocal(e.getSceneX(), e.getSceneY());
            rect.setWidth(Math.max(TAMANO_MINIMO_ELEMENTO, local.getX()));
            rect.setHeight(Math.max(TAMANO_MINIMO_ELEMENTO, local.getY()));
            e.consume();
        });

        manija.setOnMouseReleased(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            int anchoFinal = (int) rect.getWidth();
            int altoFinal = (int) rect.getHeight();
            new Thread(() -> {
                try {
                    apiClient.actualizarGeometriaElemento(elemento.id, (int) box.getLayoutX(), (int) box.getLayoutY(), anchoFinal, altoFinal);
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo redimensionar: " + ex.getMessage()));
                }
            }).start();
            e.consume();
        });

        return box;
    }

    // ---------- Mesa: mover, redimensionar, click para editar (en modo edicion) ----------

    private Group crearMesaVisual(MesaDTO mesa, List<ZonaDTO> zonasDisponibles) {
        double anchoInicial = mesa.ancho != null ? mesa.ancho : TAMANO_MESA_DEFECTO;
        double altoInicial = mesa.alto != null ? mesa.alto : TAMANO_MESA_DEFECTO;

        Rectangle rect = new Rectangle(anchoInicial, altoInicial);
        rect.setArcWidth(14);
        rect.setArcHeight(14);
        rect.setFill(colorSegunEstado(mesa.estado));
        rect.setStroke(Color.web("#00000033"));

        Label numeroLabel = new Label("Mesa " + mesa.numero);
        numeroLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label estadoMesaLabel = new Label(mesa.estado);
        estadoMesaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10;");

        VBox textos = new VBox(2, numeroLabel, estadoMesaLabel);
        textos.setAlignment(Pos.CENTER);
        textos.layoutXProperty().bind(rect.widthProperty().subtract(textos.widthProperty()).divide(2));
        textos.layoutYProperty().bind(rect.heightProperty().subtract(textos.heightProperty()).divide(2));

        boolean editando = modoEdicionToggle.isSelected();

        Rectangle manija = new Rectangle(12, 12);
        manija.setFill(Color.web("#000000AA"));
        manija.setArcWidth(4);
        manija.setArcHeight(4);
        manija.layoutXProperty().bind(rect.widthProperty().subtract(12));
        manija.layoutYProperty().bind(rect.heightProperty().subtract(12));
        manija.setCursor(Cursor.SE_RESIZE);
        manija.setVisible(editando);

        Group box = new Group(rect, textos, manija);
        box.setCursor(editando ? Cursor.OPEN_HAND : Cursor.HAND);

        final double[] inicio = new double[2];
        final boolean[] seMovio = {false};

        box.setOnMousePressed(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            inicio[0] = e.getSceneX() - box.getLayoutX();
            inicio[1] = e.getSceneY() - box.getLayoutY();
            seMovio[0] = false;
            box.setCursor(Cursor.CLOSED_HAND);
        });

        box.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            box.setLayoutX(Math.max(0, e.getSceneX() - inicio[0]));
            box.setLayoutY(Math.max(0, e.getSceneY() - inicio[1]));
            seMovio[0] = true;
        });

        box.setOnMouseReleased(e -> {
            boolean edicionActiva = modoEdicionToggle.isSelected();

            if (edicionActiva && seMovio[0]) {
                box.setCursor(Cursor.OPEN_HAND);
                int nuevaX = (int) box.getLayoutX();
                int nuevaY = (int) box.getLayoutY();
                new Thread(() -> {
                    try {
                        apiClient.moverMesa(mesa.id, nuevaX, nuevaY);
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarError("No se pudo guardar la posición: " + ex.getMessage()));
                    }
                }).start();
            } else if (edicionActiva) {
                abrirFormularioEditarMesa(mesa, zonasDisponibles);
            } else {
                mostrarDetalleMesa(mesa);
            }
        });

        manija.setOnMousePressed(e -> {
            if (modoEdicionToggle.isSelected()) e.consume();
        });

        manija.setOnMouseDragged(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            Point2D local = box.sceneToLocal(e.getSceneX(), e.getSceneY());
            double nuevoAncho = Math.max(TAMANO_MINIMO_MESA, Math.min(220, local.getX()));
            double nuevoAlto = Math.max(TAMANO_MINIMO_MESA, Math.min(220, local.getY()));
            rect.setWidth(nuevoAncho);
            rect.setHeight(nuevoAlto);
            e.consume();
        });

        manija.setOnMouseReleased(e -> {
            if (!modoEdicionToggle.isSelected()) return;
            int anchoFinal = (int) rect.getWidth();
            int altoFinal = (int) rect.getHeight();
            new Thread(() -> {
                try {
                    apiClient.cambiarTamanoMesa(mesa.id, anchoFinal, altoFinal);
                } catch (Exception ex) {
                    Platform.runLater(() -> mostrarError("No se pudo guardar el tamaño: " + ex.getMessage()));
                }
            }).start();
            e.consume();
        });

        return box;
    }

    private Color colorSegunEstado(String estado) {
        return switch (estado) {
            case "LIBRE" -> Color.web("#4CAF50");
            case "OCUPADA" -> Color.web("#FF9800");
            case "EN_PROCESO" -> Color.web("#F44336");
            case "POR_PAGAR" -> Color.web("#2196F3");
            case "RESERVADA" -> Color.web("#9C27B0");
            default -> Color.GRAY;
        };
    }

    // ---------- Ver detalle de orden (modo bloqueado) ----------

    private void mostrarDetalleMesa(MesaDTO mesa) {
        if ("LIBRE".equals(mesa.estado)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mesa " + mesa.numero);
            alert.setHeaderText("Zona: " + (mesa.zona != null ? mesa.zona.nombre : "N/A"));
            alert.setContentText("Esta mesa está libre. Capacidad: " + mesa.capacidad + " personas.");
            alert.showAndWait();
            return;
        }

        new Thread(() -> {
            try {
                List<OrdenDTO> ordenes = apiClient.obtenerOrdenesDeMesa(mesa.id);
                Optional<OrdenDTO> ordenActiva = ordenes.stream()
                        .filter(o -> !"CERRADA".equals(o.estado) && !"CANCELADA".equals(o.estado))
                        .findFirst();

                Platform.runLater(() -> {
                    if (ordenActiva.isPresent()) {
                        abrirVentanaDetalleOrden(mesa, ordenActiva.get());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Mesa " + mesa.numero);
                        alert.setContentText("La mesa está marcada como " + mesa.estado +
                                " pero no se encontró una orden activa.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Error al cargar la orden: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void abrirVentanaDetalleOrden(MesaDTO mesa, OrdenDTO orden) {
        VBox contenido = new VBox(10);
        contenido.setPadding(new Insets(20));

        Label titulo = new Label("Orden #" + orden.id + " - Mesa " + mesa.numero);
        titulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitulo = new Label("Estado: " + orden.estado + "  ·  Comensales: " + orden.numeroComensales);

        contenido.getChildren().addAll(titulo, subtitulo, new Separator());

        double total = 0;
        if (orden.items != null) {
            for (ItemDTO item : orden.items) {
                double subtotalItem = item.precioUnitario * item.cantidad;
                if (!"CANCELADO".equals(item.estadoItem)) {
                    total += subtotalItem;
                }

                Label nombre = new Label(item.cantidad + "x " + item.plato.nombre);
                nombre.setStyle("-fx-font-weight: bold;");
                if (item.notas != null && !item.notas.isBlank()) {
                    nombre.setText(nombre.getText() + "  (" + item.notas + ")");
                }

                Label precio = new Label(String.format("S/ %.2f", subtotalItem));
                Label estadoItem = new Label(item.estadoItem);
                estadoItem.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11;");

                HBox fila = new HBox(12, nombre, estadoItem);
                HBox.setHgrow(nombre, Priority.ALWAYS);
                HBox filaCompleta = new HBox(fila, precio);
                HBox.setHgrow(fila, Priority.ALWAYS);

                contenido.getChildren().add(filaCompleta);
            }
        }

        contenido.getChildren().add(new Separator());
        Label totalLabel = new Label(String.format("Total: S/ %.2f", total));
        totalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        contenido.getChildren().add(totalLabel);

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Detalle de Orden");
        ventana.setScene(new Scene(contenido, 380, 400));
        ventana.show();
    }

    // ---------- Formulario: agregar mesa nueva ----------

    private void abrirFormularioNuevaMesa() {
        new Thread(() -> {
            try {
                List<ZonaDTO> zonas = apiClient.obtenerZonas();
                Platform.runLater(() -> mostrarFormularioNuevaMesa(zonas));
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarError("No se pudieron cargar las zonas: " + ex.getMessage()));
            }
        }).start();
    }

    private void mostrarFormularioNuevaMesa(List<ZonaDTO> zonas) {
        TextField numeroField = new TextField();
        numeroField.setPromptText("Número de mesa (ej. 6)");

        TextField capacidadField = new TextField();
        capacidadField.setPromptText("Capacidad (ej. 4)");

        ComboBox<ZonaDTO> zonaCombo = new ComboBox<>(FXCollections.observableArrayList(zonas));
        zonaCombo.setPromptText("Zona");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button guardarBtn = new Button("Crear mesa");

        VBox contenido = new VBox(12,
                new Label("Nueva mesa"),
                numeroField, capacidadField, zonaCombo,
                errorLabel, guardarBtn);
        contenido.setPadding(new Insets(20));

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Agregar mesa");
        ventana.setScene(new Scene(contenido, 300, 280));

        guardarBtn.setOnAction(e -> {
            ZonaDTO zona = zonaCombo.getValue();
            if (zona == null) {
                errorLabel.setText("Selecciona una zona.");
                return;
            }
            int numero, capacidad;
            try {
                numero = Integer.parseInt(numeroField.getText().trim());
                capacidad = Integer.parseInt(capacidadField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Número y capacidad deben ser numéricos.");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.crearMesa(numero, capacidad, zona.id, 30, 30);
                    Platform.runLater(() -> {
                        ventana.close();
                        cargarTodo();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        ventana.show();
    }

    // ---------- Formulario: editar / eliminar una mesa existente ----------

    private void abrirFormularioEditarMesa(MesaDTO mesa, List<ZonaDTO> zonasDisponibles) {
        TextField numeroField = new TextField(String.valueOf(mesa.numero));
        TextField capacidadField = new TextField(String.valueOf(mesa.capacidad));

        ComboBox<ZonaDTO> zonaCombo = new ComboBox<>(FXCollections.observableArrayList(zonasDisponibles));
        zonasDisponibles.stream()
                .filter(z -> mesa.zona != null && z.id.equals(mesa.zona.id))
                .findFirst()
                .ifPresent(zonaCombo::setValue);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button guardarBtn = new Button("Guardar cambios");
        Button eliminarBtn = new Button("🗑️ Eliminar mesa");
        eliminarBtn.setStyle("-fx-text-fill: #c62828;");

        VBox contenido = new VBox(10,
                new Label("Editar Mesa " + mesa.numero),
                numeroField, capacidadField, zonaCombo,
                errorLabel, guardarBtn, eliminarBtn);
        contenido.setPadding(new Insets(20));

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Editar mesa");
        ventana.setScene(new Scene(contenido, 300, 340));

        guardarBtn.setOnAction(e -> {
            ZonaDTO zona = zonaCombo.getValue();
            if (zona == null) {
                errorLabel.setText("Selecciona una zona.");
                return;
            }
            int numero, capacidad;
            try {
                numero = Integer.parseInt(numeroField.getText().trim());
                capacidad = Integer.parseInt(capacidadField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Número y capacidad deben ser numéricos.");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.editarMesa(mesa.id, numero, capacidad, zona.id);
                    Platform.runLater(() -> {
                        ventana.close();
                        cargarTodo();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        eliminarBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar mesa");
            confirm.setContentText("¿Seguro que quieres eliminar la Mesa " + mesa.numero + "?");
            confirm.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            apiClient.eliminarMesa(mesa.id);
                            Platform.runLater(() -> {
                                ventana.close();
                                cargarTodo();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                        }
                    }).start();
                }
            });
        });

        ventana.show();
    }

    // ---------- Formulario: agregar elemento decorativo nuevo ----------

    private void abrirFormularioNuevoElemento() {
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre (ej. Cocina, Barra, Baño)");

        ComboBox<String> iconoCombo = new ComboBox<>(FXCollections.observableArrayList(ICONOS_SUGERIDOS));
        iconoCombo.setPromptText("Ícono");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button guardarBtn = new Button("Crear elemento");

        VBox contenido = new VBox(12,
                new Label("Nuevo elemento"),
                nombreField, iconoCombo,
                errorLabel, guardarBtn);
        contenido.setPadding(new Insets(20));

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Agregar elemento");
        ventana.setScene(new Scene(contenido, 280, 240));

        guardarBtn.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            String icono = iconoCombo.getValue();
            if (nombre.isEmpty() || icono == null) {
                errorLabel.setText("Completa el nombre y elige un ícono.");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.crearElemento(nombre, icono, 30, 30);
                    Platform.runLater(() -> {
                        ventana.close();
                        cargarTodo();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        ventana.show();
    }

    // ---------- Formulario: editar / eliminar un elemento existente ----------

    private void abrirFormularioEditarElemento(ElementoDTO elemento) {
        TextField nombreField = new TextField(elemento.nombre);

        ComboBox<String> iconoCombo = new ComboBox<>(FXCollections.observableArrayList(ICONOS_SUGERIDOS));
        iconoCombo.setValue(elemento.icono);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button guardarBtn = new Button("Guardar cambios");
        Button eliminarBtn = new Button("🗑️ Eliminar");
        eliminarBtn.setStyle("-fx-text-fill: #c62828;");

        VBox contenido = new VBox(10,
                new Label("Editar elemento"),
                nombreField, iconoCombo,
                errorLabel, guardarBtn, eliminarBtn);
        contenido.setPadding(new Insets(20));

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Editar elemento");
        ventana.setScene(new Scene(contenido, 280, 280));

        guardarBtn.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            String icono = iconoCombo.getValue();
            if (nombre.isEmpty() || icono == null) {
                errorLabel.setText("Completa el nombre y elige un ícono.");
                return;
            }

            new Thread(() -> {
                try {
                    apiClient.editarElemento(elemento.id, nombre, icono);
                    Platform.runLater(() -> {
                        ventana.close();
                        cargarTodo();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        eliminarBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar elemento");
            confirm.setContentText("¿Seguro que quieres eliminar \"" + elemento.nombre + "\"?");
            confirm.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            apiClient.eliminarElemento(elemento.id);
                            Platform.runLater(() -> {
                                ventana.close();
                                cargarTodo();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> errorLabel.setText("Error: " + ex.getMessage()));
                        }
                    }).start();
                }
            });
        });

        ventana.show();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}