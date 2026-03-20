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
    private Disco discoVirtual; 
    
    private boolean sistemaCorriendo;
    private int contadorIdProcesos;
    
    private volatile boolean pausado;
    private volatile int tiempoVelocidadMs; 
    private int cicloActual; 

    // --- NUEVO: GATILLO DE FALLO ---
    private volatile boolean forzarCrash = false;

    public GestorProcesos(PlanificadorDisco planificador, MotorBitacora bitacora, GestorLocks locks, SimuladorGUI gui, Disco discoVirtual) {
        this.colaListos = new Cola<>();
        this.planificador = planificador;
        this.bitacora = bitacora;
        this.locks = locks;
        this.gui = gui;
        this.discoVirtual = discoVirtual;
        
        this.sistemaCorriendo = false;
        this.contadorIdProcesos = 1;
        this.pausado = false;
        this.tiempoVelocidadMs = 500; 
        this.cicloActual = 0;
        
        // Le pasamos el disco a la bitácora para que pueda hacer el Undo después
        this.bitacora.setDiscoVirtual(discoVirtual);
    }

    public void setPausado(boolean pausado) { this.pausado = pausado; }
    public boolean isPausado() { return pausado; }
    public void setTiempoVelocidadMs(int ms) { this.tiempoVelocidadMs = ms; }
    public PlanificadorDisco getPlanificador() { return planificador; }
    public MotorBitacora getBitacora() { return bitacora; }
    public int getCicloActual() { return cicloActual; }

    public void activarCrash() { this.forzarCrash = true; } // Activa la trampa

    public void agregarProcesoCRUD(String operacion, String nombreArchivo, int posicionDestino, int tamano) {
        Proceso nuevoProceso = new Proceso(contadorIdProcesos++, operacion, nombreArchivo, posicionDestino, tamano);
        colaListos.enqueue(nuevoProceso);
        if(gui != null) {
            gui.agregarLog(cicloActual, "Proceso recibido: Proc-" + nuevoProceso.getId() + " (" + operacion.toUpperCase() + ")");
            actualizarVistaCola(null);
        }
    }

    public void iniciarSistema() {
        this.sistemaCorriendo = true;
        new Thread(this).start();
    }

    private void actualizarVistaCola(Proceso pEjecucion) {
        if(gui == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("=== LISTOS ===\nProcesos esperando E/S: ").append(colaListos.isEmpty() ? "0" : "Varios en cola").append("\n\n");
        sb.append("=== EN CPU / EJECUCIÓN ===\n");
        if (pEjecucion != null) sb.append("Proc-").append(pEjecucion.getId()).append(" [").append(pEjecucion.getOperacion()).append(" ").append(pEjecucion.getNombreArchivo()).append("]\n");
        else sb.append("Ninguno (Idle)\n");
        gui.actualizarColaUI(sb.toString());
    }

    @Override
    public void run() {
        while (sistemaCorriendo) {
            if (pausado) {
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                continue; 
            }
            cicloActual++;
            SwingUtilities.invokeLater(() -> {
                if(gui != null) gui.actualizarEstado(cicloActual, planificador.getPosicionCabezal());
            });

            if (!colaListos.isEmpty()) {
                Proceso procesoAEjecutar = planificador.procesarCola(colaListos); 
                if (procesoAEjecutar != null) {
                    procesoAEjecutar.setEstado("Ejecutando");
                    actualizarVistaCola(procesoAEjecutar);
                    gui.agregarLog(cicloActual, "CPU asignada a Proc-" + procesoAEjecutar.getId());
                    
                    ejecutarOperacionReal(procesoAEjecutar);
                    
                    if (!sistemaCorriendo) return; // Si hubo crash, el hilo muere aquí mismo.

                    procesoAEjecutar.setEstado("Terminado");
                    actualizarVistaCola(null);
                }
            } else {
                actualizarVistaCola(null);
            }
            try { Thread.sleep(tiempoVelocidadMs); } catch (InterruptedException e) {} 
        }
    }

    private void ejecutarOperacionReal(Proceso p) {
        boolean esEscritura = p.getOperacion().equals("Crear") || p.getOperacion().equals("Eliminar") || p.getOperacion().equals("Actualizar");

        if (esEscritura) locks.adquirirLockEscritura();
        else locks.adquirirLockLectura(); 
        
        Transaccion t = bitacora.registrarPendiente(p.getOperacion(), p.getNombreArchivo());
        gui.agregarLog(cicloActual, "Journaling: Transacción PENDIENTE (" + p.getOperacion() + ")");
        
        try { Thread.sleep(tiempoVelocidadMs / 2); } catch (InterruptedException e) {}
        
        boolean exito = true;
        if (p.getOperacion().equals("Crear")) exito = discoVirtual.crearArchivo(p.getNombreArchivo(), "Administrador", p.getTamanoRequerido());
        else if (p.getOperacion().equals("Eliminar")) exito = discoVirtual.eliminarArchivo(p.getNombreArchivo());
        
        // --- ¡EL MOMENTO DEL FALLO! ---
        if (forzarCrash && esEscritura) {
            this.sistemaCorriendo = false; // Matamos el Hilo
            SwingUtilities.invokeLater(() -> gui.ejecutarPantallazoCrash());
            return; // Salimos SIN HACER COMMIT Y SIN LIBERAR LOCKS (Simulando fallo real)
        }
        
        if (exito) {
            bitacora.hacerCommit(t);
            gui.agregarLog(cicloActual, "Journaling: Transacción CONFIRMADA (Commit)");
        } else {
            t.setEstado("FALLIDA");
        }
        
        if (esEscritura) locks.liberarLockEscritura();
        else locks.liberarLockLectura();
        
        SwingUtilities.invokeLater(() -> {
            gui.refrescarTodo();
            gui.actualizarEstado(cicloActual, planificador.getPosicionCabezal());
        });
    }
}