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
public class GestorProcesos implements Runnable {
    private Cola<Proceso> colaListos;
    private PlanificadorDisco planificador;
    private boolean sistemaCorriendo;
    private int contadorIdProcesos;

    public GestorProcesos(PlanificadorDisco planificador) {
        this.colaListos = new Cola<>();
        this.planificador = planificador;
        this.sistemaCorriendo = false;
        this.contadorIdProcesos = 1;
    }

    // Método que llamará tu compañero desde la GUI cuando presione un botón
    public void agregarProcesoCRUD(String operacion, String nombreArchivo, int posicionDestino) {
        Proceso nuevoProceso = new Proceso(contadorIdProcesos++, operacion, nombreArchivo, posicionDestino);
        nuevoProceso.setEstado("Listo");
        colaListos.enqueue(nuevoProceso);
        System.out.println("Gestor: Nuevo proceso [" + operacion + " " + nombreArchivo + "] agregado a la cola de listos.");
    }

    // Para encender el motor del SO
    public void iniciarSistema() {
        this.sistemaCorriendo = true;
        Thread hiloSistema = new Thread(this);
        hiloSistema.start();
        System.out.println("Gestor: Hilo del Sistema Operativo iniciado.");
    }

    public void detenerSistema() {
        this.sistemaCorriendo = false;
    }

    // Este es el ciclo de vida del Sistema Operativo (El Hilo en background)
    @Override
    public void run() {
        while (sistemaCorriendo) {
            if (!colaListos.isEmpty()) {
                // Si hay procesos en la cola, llamamos al algoritmo que diseñaste antes
                // El planificador decidirá a quién atiende primero según la política actual (SSTF, SCAN, etc.)
                planificador.procesarCola(colaListos);
            }

            // Pausa de medio segundo para no quemar el procesador real y para que
            // en la defensa se pueda ver la animación del cabezal moviéndose en la GUI
            try {
                Thread.sleep(500); 
            } catch (InterruptedException e) {
                System.out.println("El hilo del sistema fue interrumpido.");
            }
        }
    }
}
