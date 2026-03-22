/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

public class Proceso {
    private int id;
    private String estado; 
    private String operacion; 
    private String nombreArchivo;
    private int posicionBloque; 
    private int tamanoRequerido; 

    public Proceso(int id, String operacion, String nombreArchivo, int posicionBloque, int tamanoRequerido) {
        this.id = id;
        this.estado = "Nuevo";
        this.operacion = operacion;
        this.nombreArchivo = nombreArchivo;
        this.posicionBloque = posicionBloque;
        this.tamanoRequerido = tamanoRequerido; 
    }

    
    public int getId() { return id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getOperacion() { return operacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getPosicionBloque() { return posicionBloque; }
    public void setPosicionBloque(int posicionBloque) { this.posicionBloque = posicionBloque; }  
    public int getTamanoRequerido() { return tamanoRequerido; }
}