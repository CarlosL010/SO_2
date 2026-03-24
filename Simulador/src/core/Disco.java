/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import estructuras.Arbol;
import estructuras.NodoArbol;
import modelos.Bloque;

/**
 *
 * @author pinto
 */
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
        // 1. Buscar el nodo padre y el nodo objetivo de forma recursiva
        Object[] resultado = buscarPadreENodo(arbolDirectorios.getRaiz(), nombre);
        
        if (resultado == null) {
            return false; // NO SE ENCONTRÓ EL ARCHIVO O DIRECTORIO
        }
        
        NodoArbol padre = (NodoArbol) resultado[0];
        NodoArbol objetivo = (NodoArbol) resultado[1];

        // 2. Si es un directorio, aplicamos la eliminación recursiva (Requisito del PDF)
        if (!objetivo.isEsArchivo()) {
            vaciarDirectorioRecursivo(objetivo);
        } else {
            // 3. Si es archivo normal, liberamos sus bloques
            liberarBloquesFisicos(objetivo);
        }

        // 4. Lo eliminamos lógicamente del árbol
        padre.getHijos().remove(objetivo);
        return true;
    }
    
    private void vaciarDirectorioRecursivo(NodoArbol directorio) {
        if (directorio.getHijos() == null || directorio.getHijos().isEmpty()) return;

        estructuras.Nodo<NodoArbol> actual = directorio.getHijos().getHead();
        while (actual != null) {
            NodoArbol hijo = actual.getData();
            if (!hijo.isEsArchivo()) {
                vaciarDirectorioRecursivo(hijo); // Entra al subdirectorio a vaciarlo
            } else {
                liberarBloquesFisicos(hijo); // Libera los cuadritos de la RAM
            }
            actual = actual.getNext();
        }
    }

    private void liberarBloquesFisicos(NodoArbol archivo) {
        int actual = archivo.getPrimerBloqueAsignado();
        while (actual != -1 && actual < bloques.length) {
            int siguiente = bloques[actual].getSiguienteBloque();
            bloques[actual].setLibre(true);
            bloques[actual].setPerteneceA(null);
            bloques[actual].setSiguienteBloque(-1);
            actual = siguiente;
        }
    }

    private Object[] buscarPadreENodo(NodoArbol actual, String nombreBuscado) {
        if (actual.getHijos() == null) return null;
        
        estructuras.Nodo<NodoArbol> nodoHijo = actual.getHijos().getHead();
        while (nodoHijo != null) {
            NodoArbol hijo = nodoHijo.getData();
            
            // ¿Es el que buscamos? Retornamos un mini arreglo: [Padre, Hijo]
            if (hijo.getNombre().equals(nombreBuscado)) {
                return new Object[]{actual, hijo};
            }
            
            // Si es una carpeta, buscamos dentro de ella recursivamente
            if (!hijo.isEsArchivo()) {
                Object[] hallado = buscarPadreENodo(hijo, nombreBuscado);
                if (hallado != null) return hallado;
            }
            nodoHijo = nodoHijo.getNext();
        }
        return null;
    }

    // Getters
    public Bloque[] getBloques() { return bloques; }
    public Arbol getArbolDirectorios() { return arbolDirectorios; }
}