/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author josep
 */
public class NodoArbol {
    private String nombre;
    private boolean esArchivo; 
    private String propietario; 
    private int tamanoEnBloques;
    private int primerBloqueAsignado; 
    
    
    private ListaEnlazada<NodoArbol> hijos; 

    
    public NodoArbol(String nombre, String propietario) {
        this.nombre = nombre;
        this.esArchivo = false;
        this.propietario = propietario;
        this.tamanoEnBloques = 0;
        this.primerBloqueAsignado = -1; 
        this.hijos = new ListaEnlazada<>();
    }

    
    public NodoArbol(String nombre, String propietario, int tamanoEnBloques, int primerBloqueAsignado) {
        this.nombre = nombre;
        this.esArchivo = true;
        this.propietario = propietario;
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloqueAsignado = primerBloqueAsignado;
        this.hijos = null; // Los archivos no tienen hijos
    }

    
    public void agregarHijo(NodoArbol hijo) {
        if (!this.esArchivo) {
            this.hijos.add(hijo);
        } else {
            System.out.println("Error: Un archivo no puede tener hijos.");
        }
    }

   
    public String getNombre() { return nombre; }
    public boolean isEsArchivo() { return esArchivo; }
    public String getPropietario() { return propietario; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloqueAsignado() { return primerBloqueAsignado; }
    public ListaEnlazada<NodoArbol> getHijos() { return hijos; }

    
    @Override
    public String toString() {
        return nombre;
    }
}