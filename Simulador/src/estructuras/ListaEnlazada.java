/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author pinto
 */
public class ListaEnlazada<T> {
    private Nodo<T> head;
    private int size;

    public ListaEnlazada() {
        this.head = null;
        this.size = 0;
    }

    public void add(T data) {
        Nodo<T> newNode = new Nodo<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Nodo<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
        size++;
    }
    
    public boolean remove(T dataBuscado) {
    if (isEmpty()) return false;

    // Caso A: El elemento a borrar es la cabeza (el primero)
    if (head.getData().equals(dataBuscado)) {
        head = head.getNext();
        size--;
        return true;
    }

    // Caso B: Recorrer la lista para encontrar el elemento en el medio o final
    Nodo<T> actual = head;
    while (actual.getNext() != null) {
        // Verificamos si el dato del SIGUIENTE nodo es el que buscamos
        if (actual.getNext().getData().equals(dataBuscado)) {
            // Hacemos el "puente": saltamos el nodo actual.getNext()
            actual.setNext(actual.getNext().getNext());
            size--;
            return true;
        }
        actual = actual.getNext();
    }

    return false; // No se encontró el elemento
}

    public boolean isEmpty() { return head == null; }
    public int getSize() { return size; }
    public Nodo<T> getHead() { return head; }
}
