/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import estructuras.Arbol;
import estructuras.NodoArbol;
import modelos.Bloque;

public class Disco {
    private Bloque[] bloques;
    private int capacidad;
    private Arbol arbolDirectorios;

    public Disco(int capacidad) {
        this.capacidad = capacidad;
        this.bloques = new Bloque[capacidad];
        this.arbolDirectorios = new Arbol();

        // Inicializar todos los bloques del disco como "Libres"
        for (int i = 0; i < capacidad; i++) {
            bloques[i] = new Bloque(i);
        }
    }

    // --- MÓDULO CRUD: CREAR ARCHIVO (ASIGNACIÓN ENCADENADA) ---
    public boolean crearArchivo(String nombre, String propietario, int tamanoRequerido) {
        // 1. Verificar si hay espacio disponible
        int bloquesLibres = 0;
        for (int i = 0; i < capacidad; i++) {
            if (bloques[i].isLibre()) bloquesLibres++;
        }

        if (bloquesLibres < tamanoRequerido) {
            System.out.println("Error: No hay suficiente espacio para el archivo " + nombre);
            return false;
        }

        // 2. Asignación encadenada
        int primerBloque = -1;
        int bloqueAnterior = -1;
        int asignados = 0;

        for (int i = 0; i < capacidad && asignados < tamanoRequerido; i++) {
            if (bloques[i].isLibre()) {
                bloques[i].setLibre(false);
                bloques[i].setPerteneceA(nombre);

                if (asignados == 0) {
                    primerBloque = i; // Guardamos el inicio del archivo
                } else {
                    bloques[bloqueAnterior].setSiguienteBloque(i); // Enlazamos el anterior con el actual
                }

                bloqueAnterior = i;
                asignados++;
            }
        }

        // El último bloque debe indicar Fin de Archivo (-1)
        if (bloqueAnterior != -1) {
            bloques[bloqueAnterior].setSiguienteBloque(-1);
        }

        // 3. Registrar el archivo en el Árbol de Directorios
        NodoArbol nuevoArchivo = new NodoArbol(nombre, propietario, tamanoRequerido, primerBloque);
        arbolDirectorios.getRaiz().agregarHijo(nuevoArchivo);

        System.out.println("Éxito: Archivo '" + nombre + "' creado desde el bloque " + primerBloque);
        return true;
    }

    // --- MÓDULO CRUD: ELIMINAR ARCHIVO ---
    public boolean eliminarArchivo(String nombre) {
        NodoArbol archivo = arbolDirectorios.buscarNodo(arbolDirectorios.getRaiz(), nombre);
        if (archivo == null || !archivo.isEsArchivo()) {
            System.out.println("Error: Archivo no encontrado.");
            return false;
        }

        // 1. Liberar los bloques enlazados en el disco
        int bloqueActual = archivo.getPrimerBloqueAsignado();
        while (bloqueActual != -1) {
            int siguiente = bloques[bloqueActual].getSiguienteBloque();
            bloques[bloqueActual].setLibre(true);
            bloques[bloqueActual].setPerteneceA("");
            bloques[bloqueActual].setSiguienteBloque(-1);
            bloqueActual = siguiente;
        }

        // 2. Eliminar lógicamente del árbol (Nota: la ListaEnlazada actual no tiene método remove() 
        // Tendrás que agregarlo a ListaEnlazada.java más adelante, por ahora lo dejamos en el disco)
        System.out.println("Éxito: Archivo '" + nombre + "' eliminado y bloques liberados.");
        return true;
    }

    // Getters
    public Bloque[] getBloques() { return bloques; }
    public Arbol getArbolDirectorios() { return arbolDirectorios; }
}