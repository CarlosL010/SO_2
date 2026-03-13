/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

/**
 *
 * @author pinto
 */
public class Proceso {
    private int id;
    private String estado; // "Nuevo", "Listo", "Ejecutando", "Bloqueado", "Terminado"
    private String operacion; // "Crear", "Leer", "Actualizar", "Eliminar"
    private String nombreArchivo;
    private int posicionBloque;

    public Proceso(int id, String operacion, String nombreArchivo) {
        this.id = id;
        this.estado = "Nuevo";
        this.operacion = operacion;
        this.nombreArchivo = nombreArchivo;
        this.posicionBloque = posicionBloque;
    }

    // Getters y Setters básicos
    public int getId() { return id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getOperacion() { return operacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getPosicionBloque() { return posicionBloque; }
}
