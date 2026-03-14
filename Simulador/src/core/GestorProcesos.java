/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;
import estructuras.Cola;
import modelos.Proceso;
import bitacora.MotorBitacora;
import bitacora.Transaccion;
import concurrencia.GestorLocks;
import gui.SimuladorGUI; 
import javax.swing.SwingUtilities; 
/**
 *
 * @author pinto
 */
public class GestorProcesos implements Runnable {
    private Cola<Proceso> colaListos;
    private PlanificadorDisco planificador;
    private MotorBitacora bitacora;
    private GestorLocks locks;
    private SimuladorGUI gui; 
    private boolean sistemaCorriendo;
    private int contadorIdProcesos;

    // Actualizamos el constructor para recibir todo
    public GestorProcesos(PlanificadorDisco planificador, MotorBitacora bitacora, GestorLocks locks, SimuladorGUI gui) {
        this.colaListos = new Cola<>();
        this.planificador = planificador;
        this.bitacora = bitacora;
        this.locks = locks;
        this.gui = gui;
        this.sistemaCorriendo = false;
        this.contadorIdProcesos = 1;
    }

    public void agregarProcesoCRUD(String operacion, String nombreArchivo, int posicionDestino) {
        Proceso nuevoProceso = new Proceso(contadorIdProcesos++, operacion, nombreArchivo, posicionDestino);
        colaListos.enqueue(nuevoProceso);
        System.out.println("Gestor: Proceso " + operacion + " encolado.");
    }

    public void iniciarSistema() {
        this.sistemaCorriendo = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (sistemaCorriendo) {
            if (!colaListos.isEmpty()) {
                // 1. El planificador nos dice dónde se movió el cabezal 
                planificador.procesarCola(colaListos); 
                
                // NOTA: Para hacer esto perfecto, tu planificador debería devolverte el Proceso 
                // que acaba de ejecutar para que tú le apliques los locks y el journaling.
                // Como simulación rápida, asumiremos que procesamos el flujo aquí:
                ejecutarSimulacionSegura();
            }

            try { Thread.sleep(1000); } catch (InterruptedException e) { } // Pausa de 1 seg
        }
    }

    
    private void ejecutarSimulacionSegura() {
        // Ejemplo de cómo debes mezclar el Journaling y los Locks:
        
        // 1. Pedir permiso (Lock Exclusivo si es crear/eliminar, Compartido si es leer) 
        locks.adquirirLockEscritura(); 
        
        // 2. Anotar en la bitácora ANTES de hacer el cambio (Journaling) 
        Transaccion t = bitacora.registrarPendiente("CREATE", "archivo_nuevo.txt");
        
        // 3. Simular el tiempo de E/S del disco
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // 4. Confirmar que todo salió bien (Commit)
        bitacora.hacerCommit(t);
        
        // 5. Liberar el recurso para otros procesos 
        locks.liberarLockEscritura();
        
        // 6. Avisarle a la GUI de Persona A que se actualice 
        SwingUtilities.invokeLater(() -> {
            // gui.actualizarPosicionCabezal(planificador.getPosicionCabezal());
            System.out.println("GUI Actualizada.");
        });
    }
}
