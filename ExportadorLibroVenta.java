package com.reproceso;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class ExportadorLibroVenta {

    public static String generarCSV(String fechaInicio, String fechaFin, String sucursalSeleccionada) {
        String usuario = "sa";
        String contraseña = "Primavera0.";
        String carpeta = System.getProperty("user.home") + File.separator + "Downloads";
        new File(carpeta).mkdirs();
        String archivoCSV = carpeta + File.separator + "LibroVenta_" + fechaInicio + "_a_" + fechaFin + ".csv";

        List<Sucursal> sucursales = Arrays.asList(
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

        try (PrintWriter writer = new PrintWriter(new File(archivoCSV))) {
            writer.println("SUC;FECHA;CODIGO;CAJERO;NOMBRE;APELLIDO;RUC;CLIENTE;CHECKNUM;TIPO;SERIE;NUMERO;FACTURA;REFERENCIA;EXENTAS;G5;I5;G10;I10;VENTATOTAL;TIMBRADO;ServiceCharge");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            for (Sucursal sucursal : sucursales) {
                if (!sucursalSeleccionada.equals("Todas") && !sucursal.nombre.equals(sucursalSeleccionada)) {
                    continue;
                }

                try (
                        Connection conn = DriverManager.getConnection(sucursal.getUrl(), usuario, contraseña);
                        Statement stmt = conn.createStatement();
                ) {
                    String query = String.format(
                            "SELECT '%s' AS SUC, CAST(a.DOB AS DATE) AS FECHA, c.Number AS CODIGO, a.Cajero AS CAJERO, " +
                                    "c.FirstName AS NOMBRE, c.LastName AS APELLIDO, b.CliID AS RUC, b.CliName AS CLIENTE, " +
                                    "a.CheckNumb AS CHECKNUM, " +
                                    "CASE WHEN a.FORMA_PAGO=1 AND a.tipo=1 THEN 'FC' " +
                                    "WHEN a.FORMA_PAGO=2 AND a.Tipo=1 THEN 'CR' ELSE 'NC' END AS TIPO, " +
                                    "a.Serie AS SERIE, a.Counter AS NUMERO, a.FormattedInvoiceNo AS FACTURA, " +
                                    "a.ReferenceInvoiceNo AS REFERENCIA, " +
                                    "CASE WHEN ROUND(a.Impuesto*100/a.VentaNeta,0) = 0 THEN CAST(a.VentaTotal AS INT) ELSE 0 END AS EXENTAS, " +
                                    "CASE WHEN ROUND(a.Impuesto*100/a.VentaNeta,0) = 5 THEN CAST(a.VentaNeta AS INT) ELSE 0 END AS G5, " +
                                    "CASE WHEN ROUND(a.Impuesto*100/a.VentaNeta,0) = 5 THEN CAST(a.Impuesto AS INT) ELSE 0 END AS I5, " +
                                    "CASE WHEN ROUND(a.Impuesto*100/a.VentaNeta,0) = 10 THEN CAST(a.VentaNeta AS INT) ELSE 0 END AS G10, " +
                                    "CASE WHEN ROUND(a.Impuesto*100/a.VentaNeta,0) = 10 THEN CAST(a.Impuesto AS INT) ELSE 0 END AS I10, " +
                                    "CAST(a.VentaTotal AS INT) AS VENTATOTAL, '16614853' AS TIMBRADO, CAST(a.ServiceCharge AS INT) AS ServiceCharge " +
                                    "FROM [DBFISCAL].[dbo].[hInvoice] a " +
                                    "INNER JOIN [DBFISCAL].[dbo].[Cliente] b ON a.CliID = b.CliID " +
                                    "INNER JOIN [CFCInStoreDB].[dbo].[Employee] c ON a.Cajero = c.NickName " +
                                    "WHERE a.DOB BETWEEN '%s' AND '%s'",
                            sucursal.nombre, fechaInicio, fechaFin
                    );

                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                                rs.getString("SUC"), rs.getString("FECHA"), rs.getString("CODIGO"), rs.getString("CAJERO"),
                                rs.getString("NOMBRE"), rs.getString("APELLIDO"), rs.getString("RUC"), rs.getString("CLIENTE"),
                                rs.getString("CHECKNUM"), rs.getString("TIPO"), rs.getString("SERIE"), rs.getString("NUMERO"),
                                rs.getString("FACTURA"), rs.getString("REFERENCIA"), rs.getString("EXENTAS"), rs.getString("G5"),
                                rs.getString("I5"), rs.getString("G10"), rs.getString("I10"), rs.getString("VENTATOTAL"),
                                rs.getString("TIMBRADO"), rs.getString("ServiceCharge")
                        );
                    }
                } catch (SQLException e) {
                    System.out.println("⛔ Error en sucursal " + sucursal.nombre + ": " + e.getMessage());
                }
            }

            System.out.println("✅ CSV generado correctamente: " + archivoCSV);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return archivoCSV;
    }
}
