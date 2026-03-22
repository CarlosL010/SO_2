/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bitacora;

/**
 *
 * @author pinto
 */
public class Transaccion {
    private static int contadorIds = 1; 
    private int id;
    private String tipoOperacion; 
    private String nombreArchivo;
    private String estado; 

    public Transaccion(String tipoOperacion, String nombreArchivo) {
        this.id = contadorIds++;
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.estado = "PENDIENTE"; 
    }

    
    public int getId() { return id; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getEstado() { return estado; }

    
    public void setEstado(String estado) { this.estado = estado; }
}
