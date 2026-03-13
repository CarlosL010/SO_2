/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

public class Proceso {
    private int id;
    private String estado; // "Nuevo", "Listo", "Ejecutando", "Bloqueado", "Terminado"
    private String operacion; // "Crear", "Leer", "Actualizar", "Eliminar"
    private String nombreArchivo;
    
    // --- NUEVO ATRIBUTO ---
    private int posicionBloque; 

    // Actualizamos el constructor
    public Proceso(int id, String operacion, String nombreArchivo, int posicionBloque) {
        this.id = id;
        this.estado = "Nuevo";
        this.operacion = operacion;
        this.nombreArchivo = nombreArchivo;
        this.posicionBloque = posicionBloque;
    }

    public int getId() { return id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getOperacion() { return operacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    
    // --- NUEVOS GETTERS Y SETTERS ---
    public int getPosicionBloque() { return posicionBloque; }
    public void setPosicionBloque(int posicionBloque) { this.posicionBloque = posicionBloque; }
}