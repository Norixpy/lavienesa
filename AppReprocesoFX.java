package com.reproceso;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppReprocesoFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Controles
        TextField fechasField = new TextField(LocalDate.now().toString());
        fechasField.setPromptText("Ej: 2025-10-05");

        ComboBox<String> sucursalCombo = new ComboBox<>();
        sucursalCombo.getItems().add("Todas");
        sucursalCombo.getItems().addAll("VM", "FM", "ES", "JC", "CE", "ML", "SP", "PG", "SM", "SB", "SS", "PM");
        sucursalCombo.setValue("Todas");

        Button consultarBtn = new Button("Consulta por Sucursal");
        Button conteoBtn = new Button("Control De Reproceso General");
        Button exportarBtn = new Button("Exportar Libro Venta a CSV");

        Button btnDeleteCDC = new Button("🗑️ Eliminar CDC");
        Button btnInsertCDC = new Button("📥 Insertar CDC");
        Button btnUpdateCDC = new Button("♻️ Actualizar CDC");

        TextArea resultadoArea = new TextArea();
        resultadoArea.setEditable(false);
        resultadoArea.setWrapText(true);

        // Validación sucursal única para CDC
        Runnable validarSucursalUnica = () -> {
            if (sucursalCombo.getValue().equals("Todas")) {
                throw new IllegalArgumentException("⚠️ Debe seleccionar una sola sucursal para esta operación.");
            }
        };

        // Botón: Eliminar CDC
        btnDeleteCDC.setOnAction(e -> {
            try {
                validarSucursalUnica.run();
                Sucursal sucursal = ConsultaService.obtenerSucursalPorNombre(sucursalCombo.getValue());
                resultadoArea.setText("🗑️ Eliminando registros CDC...\n");
                new Thread(() -> {
                    String result = ConsultaService.eliminarCDC(sucursal);
                    Platform.runLater(() -> resultadoArea.setText(result));
                }).start();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        // Botón: Insertar CDC
        btnInsertCDC.setOnAction(e -> {
            try {
                validarSucursalUnica.run();
                Sucursal sucursal = ConsultaService.obtenerSucursalPorNombre(sucursalCombo.getValue());
                List<LocalDate> fechas = parseFechas(fechasField.getText());
                resultadoArea.setText("📥 Insertando CDC por fechas...\n");
                new Thread(() -> {
                    String result = ConsultaService.insertarCDC(sucursal, fechas);
                    Platform.runLater(() -> resultadoArea.setText(result));
                }).start();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        // Botón: Actualizar CDC
        btnUpdateCDC.setOnAction(e -> {
            try {
                validarSucursalUnica.run();
                Sucursal sucursal = ConsultaService.obtenerSucursalPorNombre(sucursalCombo.getValue());
                List<LocalDate> fechas = parseFechas(fechasField.getText());
                resultadoArea.setText("♻️ Actualizando CDC...\n");
                new Thread(() -> {
                    String result = ConsultaService.actualizarCDC(sucursal, fechas);
                    Platform.runLater(() -> resultadoArea.setText(result));
                }).start();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        // Botón: Consulta por Sucursal
        consultarBtn.setOnAction(e -> {
            String fecha = fechasField.getText().trim();
            String sucursal = sucursalCombo.getValue();

            if (sucursal.equals("Todas")) {
                mostrarError("⚠️ Debe seleccionar una sucursal específica para la consulta.");
                return;
            }

            resultadoArea.setText("⏳ Consultando datos por sucursal...\n");
            new Thread(() -> {
                String resultados;
                try {
                    resultados = ConsultaService.realizarConsulta(fecha, sucursal);
                } catch (Exception ex) {
                    resultados = "⛔ Error en consulta: " + ex.getMessage();
                }
                final String resFinal = resultados;
                Platform.runLater(() -> resultadoArea.setText(resFinal));
            }).start();
        });

        // Botón: Conteo general
        conteoBtn.setOnAction(e -> {
            try {
                LocalDate fecha = LocalDate.parse(fechasField.getText().trim());
                resultadoArea.setText("⏳ Ejecutando conteo por sucursal...\n");
                new Thread(() -> {
                    String resultado;
                    try {
                        resultado = ConsultaService.realizarConteoEnColumnas(fecha);
                    } catch (Exception ex) {
                        resultado = "⛔ Error en conteo: " + ex.getMessage();
                    }
                    final String resFinal = resultado;
                    Platform.runLater(() -> resultadoArea.setText(resFinal));
                }).start();
            } catch (Exception ex) {
                mostrarError("⚠️ Formato de fecha inválido. Ej: 2025-10-01");
            }
        });

        // Botón: Exportar CSV
        exportarBtn.setOnAction(e -> {
            String sucursal = sucursalCombo.getValue();
            String rutaArchivo;
            try {
                rutaArchivo = ExportadorLibroVenta.generarCSV(
                        LocalDate.now().toString(),
                        LocalDate.now().toString(),
                        sucursal
                );
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Exportación completada");
                alerta.setContentText("✅ CSV generado en:\n" + rutaArchivo);
                alerta.showAndWait();

                Desktop.getDesktop().open(new File(rutaArchivo));
            } catch (Exception ex) {
                mostrarError("⛔ Error al exportar: " + ex.getMessage());
            }
        });

        // Layout
        HBox fechaSucursalBox = new HBox(10,
                new Label("Fechas (YYYY-MM-DD):"),
                fechasField,
                new Label("Sucursal:"),
                sucursalCombo);
        fechaSucursalBox.setPadding(new Insets(5));

        VBox botonesIzquierda = new VBox(10, consultarBtn, conteoBtn, exportarBtn);
        VBox botonesDerecha = new VBox(10, btnDeleteCDC, btnInsertCDC, btnUpdateCDC);
        HBox botonesBox = new HBox(20, botonesIzquierda, botonesDerecha);
        botonesBox.setPadding(new Insets(5));

        VBox root = new VBox(15, fechaSucursalBox, botonesBox, resultadoArea);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("REPROCESO POR SUCURSAL");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<LocalDate> parseFechas(String texto) {
        try {
            return Arrays.stream(texto.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(LocalDate::parse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            mostrarError("⚠️ Formato de fechas inválido. Deben ser fechas válidas separadas por coma. Ej: 2025-10-01,2025-10-02");
            return List.of();
        }
    }

    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
