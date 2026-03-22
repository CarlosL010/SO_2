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

    
    if (head.getData().equals(dataBuscado)) {
        head = head.getNext();
        size--;
        return true;
    }

    
    Nodo<T> actual = head;
    while (actual.getNext() != null) {
        
        if (actual.getNext().getData().equals(dataBuscado)) {
            
            actual.setNext(actual.getNext().getNext());
            size--;
            return true;
        }
        actual = actual.getNext();
    }

    return false; 
}

    public boolean isEmpty() { return head == null; }
    public int getSize() { return size; }
    public Nodo<T> getHead() { return head; }
}
