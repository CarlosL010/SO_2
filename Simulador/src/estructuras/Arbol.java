/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author josep
 */
public class Arbol {
    private NodoArbol raiz;

    public Arbol() {
        // Inicializamos el árbol con la carpeta principal
        this.raiz = new NodoArbol("Raíz", "Administrador");
    }

    public NodoArbol getRaiz() {
        return raiz;
    }

    // Método para buscar un archivo/directorio por su nombre (búsqueda lineal simple)
    public NodoArbol buscarNodo(NodoArbol actual, String nombreBuscado) {
        if (actual.getNombre().equals(nombreBuscado)) {
            return actual;
        }
        
        if (!actual.isEsArchivo()) {
            Nodo<NodoArbol> temp = actual.getHijos().getHead();
            while (temp != null) {
                NodoArbol encontrado = buscarNodo(temp.getData(), nombreBuscado);
                if (encontrado != null) return encontrado;
                temp = temp.getNext();
            }
        }
        return null;
    }
}
