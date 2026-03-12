/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;
import estructuras.Cola;
import modelos.Proceso;

        
/**
 *
 * @author pinto
 */
public class PlanificadorDisco {
    private int posicionCabezalActual;
    private String politicaActual; // "FIFO", "SSTF", "SCAN", "C-SCAN"

    public PlanificadorDisco(int posicionInicial) {
        this.posicionCabezalActual = posicionInicial;
        this.politicaActual = "FIFO"; // Por defecto
    }

    public void setPolitica(String politica) {
        this.politicaActual = politica;
    }

    public int getPosicionCabezal() {
        return posicionCabezalActual;
    }

    // Método principal que será llamado por el hilo del sistema
    public void procesarCola(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return;

        switch (politicaActual) {
            case "FIFO":
                ejecutarFIFO(colaListos);
                break;
            case "SSTF":
                ejecutarSSTF(colaListos);
                break;
            case "SCAN":
                ejecutarSCAN(colaListos);
                break;
            case "C-SCAN":
                ejecutarCSCAN(colaListos);
                break;
        }
    }

    private void ejecutarFIFO(Cola<Proceso> colaListos) {
        // La lógica de FIFO es simplemente sacar el primero de la cola
        Proceso p = colaListos.dequeue();
        if (p != null) {
            p.setEstado("Ejecutando");
            // Aquí simularíamos el movimiento del cabezal...
            // posicionCabezalActual = nuevaPosicion;
            p.setEstado("Terminado");
        }
    }

    private void ejecutarSSTF(Cola<Proceso> colaListos) {
        // TODO: Lógica matemática para buscar el bloque más cercano al cabezal
    }

    private void ejecutarSCAN(Cola<Proceso> colaListos) {
        // TODO: Lógica del algoritmo del ascensor
    }

    private void ejecutarCSCAN(Cola<Proceso> colaListos) {
        // TODO: Lógica SCAN circular
    }
}
