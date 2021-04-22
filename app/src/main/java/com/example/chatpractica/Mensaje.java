package com.example.chatpractica;

public class Mensaje {
    public String autorEmail;
    public String autorNombre;
    public String autorFoto;
    public String mensaje;
    public String fecha;
    public String adjunto;

    public Mensaje(String autorEmail, String autorNombre, String autorFoto, String mensaje, String fecha, String adjunto) {
        this.autorEmail = autorEmail;
        this.autorNombre = autorNombre;
        this.autorFoto = autorFoto;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.adjunto = adjunto;
    }
}
