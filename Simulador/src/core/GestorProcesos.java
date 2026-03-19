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

public class GestorProcesos implements Runnable {
    private Cola<Proceso> colaListos;
    private PlanificadorDisco planificador;
    private MotorBitacora bitacora;
    private GestorLocks locks;
    private SimuladorGUI gui; 
    
    // --- EL CEREBRO DE LA PERSONA A ---
    private Disco discoVirtual; 
    
    private boolean sistemaCorriendo;
    private int contadorIdProcesos;

    // Actualizamos el constructor para recibir TODO, incluido el Disco
    public GestorProcesos(PlanificadorDisco planificador, MotorBitacora bitacora, GestorLocks locks, SimuladorGUI gui, Disco discoVirtual) {
        this.colaListos = new Cola<>();
        this.planificador = planificador;
        this.bitacora = bitacora;
        this.locks = locks;
        this.gui = gui;
        this.discoVirtual = discoVirtual;
        this.sistemaCorriendo = false;
        this.contadorIdProcesos = 1;
    }

    public void agregarProcesoCRUD(String operacion, String nombreArchivo, int posicionDestino, int tamano) {
        Proceso nuevoProceso = new Proceso(contadorIdProcesos++, operacion, nombreArchivo, posicionDestino, tamano);
        colaListos.enqueue(nuevoProceso);
        System.out.println("Gestor: Proceso " + operacion + " sobre '" + nombreArchivo + "' encolado.");
    }

    public void iniciarSistema() {
        this.sistemaCorriendo = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (sistemaCorriendo) {
            if (!colaListos.isEmpty()) {
                // 1. El planificador mueve el cabezal y nos da el proceso ganador
                Proceso procesoAEjecutar = planificador.procesarCola(colaListos); 
                
                if (procesoAEjecutar != null) {
                    procesoAEjecutar.setEstado("Ejecutando");
                    ejecutarOperacionReal(procesoAEjecutar);
                    procesoAEjecutar.setEstado("Terminado");
                }
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) { } 
        }
    }

    private void ejecutarOperacionReal(Proceso p) {
        boolean esEscritura = p.getOperacion().equals("Crear") || p.getOperacion().equals("Eliminar");

        // 1. Pedir permiso (Locks de Persona B)
        if (esEscritura) locks.adquirirLockEscritura();
        else locks.adquirirLockLectura(); 
        
        // 2. Anotar en la bitácora ANTES de hacer el cambio (Journaling de Persona B)
        Transaccion t = bitacora.registrarPendiente(p.getOperacion(), p.getNombreArchivo());
        
        // 3. Simular el tiempo de E/S del disco
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // 4. EJECUTAR EN EL DISCO REAL (Lógica de Persona A)
        boolean exito = false;
        if (p.getOperacion().equals("Crear")) {
            // Buscamos espacio y creamos
            exito = discoVirtual.crearArchivo(p.getNombreArchivo(), "Administrador", p.getTamanoRequerido());
        } else if (p.getOperacion().equals("Eliminar")) {
            // Liberamos los bloques
            exito = discoVirtual.eliminarArchivo(p.getNombreArchivo());
        }
        
        // 5. Confirmar en la bitácora
        if (exito) {
            bitacora.hacerCommit(t);
        } else {
            t.setEstado("FALLIDA - SIN ESPACIO/NO ENCONTRADO");
        }
        
        // 6. Liberar el recurso 
        if (esEscritura) locks.liberarLockEscritura();
        else locks.liberarLockLectura();
        
        // 7. Avisarle a la GUI que se actualice 
        SwingUtilities.invokeLater(() -> {
            gui.refrescarTodo();
        });
    }
}