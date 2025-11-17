package com.reproceso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class ConsultaService {

    private static final String USER = "**";
    private static final String PASS = "*********";

    private static final List<Sucursal> SUCURSALES = List.of(
            new Sucursal("10.10.1.2", 49697, "VM"),
            new Sucursal("10.1.1.2", 49222, "FM"),
            new Sucursal("10.1.2.25", 49689, "ES"),
            new Sucursal("10.1.3.2", 1433, "JC"),
            new Sucursal("10.1.4.2", 49701, "CE"),
            new Sucursal("10.1.5.2", 49729, "ML"),
            new Sucursal("10.1.6.2", 59306, "SP"),
            new Sucursal("10.1.7.2", 49804, "PG"),
            new Sucursal("10.1.8.2", 49690, "SM"),
            new Sucursal("10.1.12.2", 49718, "SB"),
            new Sucursal("10.1.13.2", 49715, "SS"),
            new Sucursal("10.1.14.2", 49714, "PM")
    );

    public static Sucursal obtenerSucursalPorNombre(String nombre) {
        return SUCURSALES.stream()
                .filter(s -> s.nombre.equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + nombre));
    }

    public static String eliminarCDC(Sucursal sucursal) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(sucursal.getUrl(), USER, PASS);
                 Statement stmt = conn.createStatement()) {
                int count = stmt.executeUpdate("DELETE FROM dbo.cdc1");
                return "✅ Registros eliminados: " + count;
            }
        } catch (Exception e) {
            return "⛔ Error al eliminar CDC: " + e.getMessage();
        }
    }

    public static String insertarCDC(Sucursal sucursal, List<LocalDate> fechas) {
        StringBuilder log = new StringBuilder();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(sucursal.getUrl(), USER, PASS);
                 Statement stmt = conn.createStatement()) {

                for (LocalDate fecha : fechas) {
                    String query = String.format(
                            "INSERT INTO dbo.cdc1 " +
                                    "SELECT fiscal, tipo, cdc " +
                                    "FROM dbo.CabeceraVenta " +
                                    "WHERE FECHA = '%s' AND cdc IS NOT NULL",
                            fecha.toString()
                    );
                    int count = stmt.executeUpdate(query);
                    log.append("📅 ").append(fecha).append(": ").append(count).append(" filas insertadas\n");
                }
                return log.toString();
            }
        } catch (Exception e) {
            return "⛔ Error al insertar CDC: " + e.getMessage();
        }
    }

    public static String actualizarCDC(Sucursal sucursal, List<LocalDate> fechas) {
        StringBuilder log = new StringBuilder();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(sucursal.getUrl(), USER, PASS);
                 Statement stmt = conn.createStatement()) {

                for (LocalDate fecha : fechas) {
                    String query = String.format(
                            "UPDATE [bd_vienesa].[dbo].[CabeceraVenta] " +
                                    "SET cdc = (SELECT cdc FROM dbo.cdc1 WHERE dbo.cdc1.fiscal = bd_vienesa.dbo.CabeceraVenta.fiscal " +
                                    "AND dbo.cdc1.tipo = bd_vienesa.dbo.CabeceraVenta.tipo) " +
                                    "WHERE fecha = '%s'",
                            fecha.toString()
                    );
                    int count = stmt.executeUpdate(query);
                    log.append("📅 ").append(fecha).append(": ").append(count).append(" filas actualizadas\n");
                }
                return log.toString();
            }
        } catch (Exception e) {
            return "⛔ Error al actualizar CDC: " + e.getMessage();
        }
    }

    public static String realizarConsulta(String fecha, String sucursalNombre) {
        try {
            Sucursal sucursal = obtenerSucursalPorNombre(sucursalNombre);
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            try (Connection conn = DriverManager.getConnection(sucursal.getUrl(), USER, PASS);
                 Statement stmt = conn.createStatement()) {

                String query = String.format(
                        "SELECT * FROM dbo.CabeceraVenta WHERE Fecha = '%s'",
                        fecha
                );

                ResultSet rs = stmt.executeQuery(query);

                StringBuilder resultados = new StringBuilder();
                int contador = 0;
                while (rs.next()) {
                    String fiscal = rs.getString("fiscal");
                    String tipo = rs.getString("tipo");
                    String cdc = rs.getString("cdc");
                    LocalDate fechaRs = rs.getDate("fecha").toLocalDate();

                    resultados.append(String.format("Fiscal: %s, Tipo: %s, CDC: %s, Fecha: %s\n",
                            fiscal, tipo, cdc, fechaRs));
                    contador++;
                }

                if (contador == 0) {
                    return "ℹ️ No se encontraron registros para la consulta.";
                }

                return resultados.toString();

            }
        } catch (Exception e) {
            return "⛔ Error al realizar consulta: " + e.getMessage();
        }
    }

    public static String realizarConteoEnColumnas(LocalDate fecha) {
        StringBuilder resultados = new StringBuilder();
        resultados.append("📅 Fecha: ").append(fecha).append("\n\n");

        for (Sucursal sucursal : SUCURSALES) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                try (Connection conn = DriverManager.getConnection(sucursal.getUrl(), USER, PASS);
                     Statement stmt = conn.createStatement()) {

                    String queryTotal = String.format(
                            "SELECT COUNT(*) AS total FROM dbo.CabeceraVenta WHERE Fecha = '%s'",
                            fecha.toString()
                    );

                    String queryCDC = String.format(
                            "SELECT COUNT(*) AS total FROM dbo.CabeceraVenta WHERE Fecha = '%s' AND cdc IS NOT NULL",
                            fecha.toString()
                    );

                    int total = 0, totalCDC = 0;

                    ResultSet rsTotal = stmt.executeQuery(queryTotal);
                    if (rsTotal.next()) {
                        total = rsTotal.getInt("total");
                    }

                    ResultSet rsCDC = stmt.executeQuery(queryCDC);
                    if (rsCDC.next()) {
                        totalCDC = rsCDC.getInt("total");
                    }

                    resultados.append(String.format("📡 %-13s %-6d %-3s\n", sucursal.ip, sucursal.puerto, sucursal.nombre));
                    resultados.append(String.format("%20d\n", total));
                    resultados.append(String.format("%20d\n\n", totalCDC));
                }
            } catch (Exception e) {
                resultados.append(String.format("⛔ Error en sucursal %s: %s\n\n", sucursal.nombre, e.getMessage()));
            }
        }

        return resultados.toString();
    }
}

