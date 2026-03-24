/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author pinto
 */
public class Cola<T> {
    private Nodo<T> head;
    private Nodo<T> tail;
    private int size;

    public Cola() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void enqueue(T data) {
        Nodo<T> newNode = new Nodo<>(data);
        if (isEmpty()) {
            head = newNode;
        } else {
            tail.setNext(newNode);
        }
        tail = newNode;
        size++;
    }

    public T dequeue() {
        if (isEmpty()) return null;
        T data = head.getData();
        head = head.getNext();
        if (head == null) tail = null;
        size--;
        return data;
    }

    public boolean isEmpty() { return head == null; }
    public int getSize() { return size; }
}