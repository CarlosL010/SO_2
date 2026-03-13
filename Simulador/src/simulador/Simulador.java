/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simulador;

import core.PlanificadorDisco;
import estructuras.Cola;
import modelos.Proceso;

public class Simulador {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEL PLANIFICADOR DE DISCO ===\n");

        // 1. Creamos la cola de procesos listos
        Cola<Proceso> colaListos = new Cola<>();

        // 2. Simulamos varios procesos que quieren acceder a distintos bloques del disco
        // Digamos que el disco tiene 200 bloques (0 al 199)
        colaListos.enqueue(new Proceso(1, "Leer", "archivoA", 98));
        colaListos.enqueue(new Proceso(2, "Escribir", "archivoB", 183));
        colaListos.enqueue(new Proceso(3, "Leer", "archivoC", 37));
        colaListos.enqueue(new Proceso(4, "Eliminar", "archivoD", 122));
        colaListos.enqueue(new Proceso(5, "Leer", "archivoE", 14));

        // 3. Inicializamos el planificador.
        // Supongamos que el cabezal del disco está actualmente en la posición 53
        PlanificadorDisco planificador = new PlanificadorDisco(53);

        // 4. Elegimos el algoritmo a probar (Cámbialo a "FIFO", "SCAN" o "C-SCAN" para ver la diferencia)
        String algoritmo = "SSTF"; 
        planificador.setPolitica(algoritmo);
        
        System.out.println("Algoritmo seleccionado: " + algoritmo);
        System.out.println("Posición inicial del cabezal: " + planificador.getPosicionCabezal());
        System.out.println("Procesos en espera: " + colaListos.getSize() + "\n");

        // 5. El código de tu compañero procesa UN proceso a la vez. 
        // Así que usamos un bucle para procesarlos todos hasta que la cola quede vacía.
        while (!colaListos.isEmpty()) {
            planificador.procesarCola(colaListos);
        }

        System.out.println("\n¡Todos los procesos han sido atendidos!");
    }
}