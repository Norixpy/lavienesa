package com.reproceso;

public class Sucursal {
    public String ip;
    public int puerto;
    public String nombre;

    public Sucursal(String ip, int puerto, String nombre) {
        this.ip = ip;
        this.puerto = puerto;
        this.nombre = nombre;
    }

    public String getUrl() {
        // Importante: agregar encrypt=false y trustServerCertificate=true para evitar problemas TLS
        return String.format("jdbc:sqlserver://%s:%d;databaseName=bd_vienesa;encrypt=false;trustServerCertificate=true", ip, puerto);
    }
}
