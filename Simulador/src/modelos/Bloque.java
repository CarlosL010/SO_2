/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

public class Bloque {
    private int id;                 // Número de bloque (ej. 0 al 99)
    private boolean libre;          // true si está vacío, false si está ocupado
    private int siguienteBloque;    // -1 si es el último bloque del archivo, o el ID del siguiente
    private String perteneceA;      // Nombre del archivo dueño (útil para pintarlo en la GUI)

    public Bloque(int id) {
        this.id = id;
        this.libre = true;          // Todo bloque nace libre
        this.siguienteBloque = -1;  // -1 significa "Fin de Archivo" (EOF)
        this.perteneceA = "";
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    
    public boolean isLibre() { return libre; }
    public void setLibre(boolean libre) { this.libre = libre; }
    
    public int getSiguienteBloque() { return siguienteBloque; }
    public void setSiguienteBloque(int siguienteBloque) { this.siguienteBloque = siguienteBloque; }
    
    public String getPerteneceA() { return perteneceA; }
    public void setPerteneceA(String perteneceA) { this.perteneceA = perteneceA; }
}