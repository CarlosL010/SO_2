/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrencia;

import java.util.concurrent.Semaphore;

/**
 *
 * @author pinto
 */
public class GestorLocks {
    private int lectoresActivos;
    private Semaphore mutex; // Protege la variable lectoresActivos
    private Semaphore lockEscritura; // Bloquea la escritura si alguien está leyendo o escribiendo

    public GestorLocks() {
        this.lectoresActivos = 0;
        this.mutex = new Semaphore(1);
        this.lockEscritura = new Semaphore(1);
    }

    public void adquirirLockLectura() {
        try {
            mutex.acquire();
            lectoresActivos++;
            if (lectoresActivos == 1) {
                // Si soy el primer lector, bloqueo a los escritores
                lockEscritura.acquire();
            }
            mutex.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void liberarLockLectura() {
        try {
            mutex.acquire();
            lectoresActivos--;
            if (lectoresActivos == 0) {
                // Si soy el último lector, libero a los escritores
                lockEscritura.release();
            }
            mutex.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void adquirirLockEscritura() {
        try {
            lockEscritura.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void liberarLockEscritura() {
        lockEscritura.release();
    }
    
}
