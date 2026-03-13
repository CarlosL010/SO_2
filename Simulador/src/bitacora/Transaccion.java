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
    private static int contadorIds = 1; // Para generar IDs únicos automáticamente
    private int id;
    private String tipoOperacion; // "CREATE" o "DELETE"
    private String nombreArchivo;
    private String estado; // "PENDIENTE", "CONFIRMADA" o "DESHECHA"

    public Transaccion(String tipoOperacion, String nombreArchivo) {
        this.id = contadorIds++;
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.estado = "PENDIENTE"; // Toda operación nace pendiente
    }

    // Getters
    public int getId() { return id; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getEstado() { return estado; }

    // Setters
    public void setEstado(String estado) { this.estado = estado; }
}
