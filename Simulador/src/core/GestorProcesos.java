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
import javax.swing.JOptionPane;

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
    private Disco discoVirtual; 
    
    private boolean sistemaCorriendo;
    private int contadorIdProcesos;
    
    private volatile boolean pausado;
    private volatile int tiempoVelocidadMs; 
    private int cicloActual; 
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
        this.bitacora.setDiscoVirtual(discoVirtual);
    }

    public void setPausado(boolean pausado) { this.pausado = pausado; }
    public boolean isPausado() { return pausado; }
    public void setTiempoVelocidadMs(int ms) { this.tiempoVelocidadMs = ms; }
    public PlanificadorDisco getPlanificador() { return planificador; }
    public MotorBitacora getBitacora() { return bitacora; }
    public int getCicloActual() { return cicloActual; }
    public void activarCrash() { this.forzarCrash = true; } 

    public void agregarProcesoCRUD(String operacion, String nombreArchivo, int posicionDestino, int tamano) {
        Proceso nuevoProceso = new Proceso(contadorIdProcesos++, operacion, nombreArchivo, posicionDestino, tamano);
        colaListos.enqueue(nuevoProceso);
        if(gui != null) {
            gui.agregarLog(cicloActual, "Proceso encolado: Proc-" + nuevoProceso.getId() + " (" + operacion.toUpperCase() + ")");
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
                    
                    
                    gui.agregarLog(cicloActual, "=> PLANIFICADOR: Cabezal movido al BLOQUE " + procesoAEjecutar.getPosicionBloque());
                    gui.agregarLog(cicloActual, "CPU asignada a Proc-" + procesoAEjecutar.getId() + " (" + procesoAEjecutar.getNombreArchivo() + ")");
                    
                    ejecutarOperacionReal(procesoAEjecutar);
                    if (!sistemaCorriendo) return; 

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
        boolean esEscritura = p.getOperacion().equals("Crear") || p.getOperacion().equals("CrearDir") || p.getOperacion().equals("Eliminar") || p.getOperacion().equals("Actualizar");
        
        String[] partesRuta = p.getNombreArchivo().split("/");
        String nombrePadre = partesRuta.length > 1 ? partesRuta[0] : "Raíz";
        String nombreReal = partesRuta.length > 1 ? partesRuta[1] : p.getNombreArchivo();

        
        if (esEscritura) locks.adquirirLockEscritura();
        else locks.adquirirLockLectura(); 
        
        if(gui != null) {
            gui.setEstadoLockArchivo(nombreReal, esEscritura ? "ESCRIBIENDO (Lock Exclusivo)" : "LEYENDO (Lock Compartido)");
            SwingUtilities.invokeLater(() -> gui.refrescarTodo());
        }

        Transaccion t = bitacora.registrarPendiente(p.getOperacion(), p.getNombreArchivo());
        gui.agregarLog(cicloActual, "Journaling: Transacción PENDIENTE (" + p.getOperacion() + ")");
        
        // Simulación de latencia de E/S donde el Lock es visible
        try { Thread.sleep(tiempoVelocidadMs); } catch (InterruptedException e) {}
        
        boolean exito = true;
        
        if (p.getOperacion().equals("Crear")) {
            exito = discoVirtual.crearArchivo(nombreReal, "Administrador", p.getTamanoRequerido());
            if (exito && !nombrePadre.equals("Raíz") && !nombrePadre.equals(nombreReal)) {
                moverNodoAPadre(discoVirtual.getArbolDirectorios().getRaiz(), nombrePadre, nombreReal);
            }
        } else if (p.getOperacion().equals("CrearDir")) {
            estructuras.NodoArbol nuevoDir = new estructuras.NodoArbol(nombreReal, "Administrador");
            estructuras.NodoArbol padre = buscarNodoRecursivo(discoVirtual.getArbolDirectorios().getRaiz(), nombrePadre);
            if (padre != null) padre.agregarHijo(nuevoDir);
            else discoVirtual.getArbolDirectorios().getRaiz().agregarHijo(nuevoDir);
            exito = true;
        } else if (p.getOperacion().equals("Eliminar")) {
            exito = discoVirtual.eliminarArchivo(nombreReal);
        }
        
        if (forzarCrash && esEscritura) {
            this.sistemaCorriendo = false; 
            SwingUtilities.invokeLater(() -> gui.ejecutarPantallazoCrash());
            return; 
        }
        
        if (exito) {
            bitacora.hacerCommit(t);
            gui.agregarLog(cicloActual, "Journaling: Transacción CONFIRMADA");
        } else {
            t.setEstado("FALLIDA");
            gui.agregarLog(cicloActual, "ERROR: No se pudo procesar '" + nombreReal + "'.");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui, "Error con el elemento: '" + nombreReal + "'.\nRevise la estructura del sistema.", "Operación Fallida", JOptionPane.WARNING_MESSAGE);
            });
        }
        
        // --- 2. LIBERAR LOCK Y ACTUALIZAR INTERFAZ ---
        if (gui != null) gui.setEstadoLockArchivo(nombreReal, "Libre");

        if (esEscritura) locks.liberarLockEscritura();
        else locks.liberarLockLectura();
        
        SwingUtilities.invokeLater(() -> {
            gui.refrescarTodo();
            gui.actualizarEstado(cicloActual, planificador.getPosicionCabezal());
        });
    }

    private void moverNodoAPadre(estructuras.NodoArbol raiz, String nombrePadre, String nombreArchivo) {
        estructuras.Nodo<estructuras.NodoArbol> actual = raiz.getHijos().getHead();
        estructuras.NodoArbol nodoAMover = null;
        
        while (actual != null) {
            if (actual.getData().getNombre().equals(nombreArchivo)) {
                nodoAMover = actual.getData();
                raiz.getHijos().remove(nodoAMover);
                break;
            }
            actual = actual.getNext();
        }
        
        if (nodoAMover != null) {
            estructuras.NodoArbol padre = buscarNodoRecursivo(raiz, nombrePadre);
            if (padre != null) padre.agregarHijo(nodoAMover);
            else raiz.agregarHijo(nodoAMover); 
        }
    }

    private estructuras.NodoArbol buscarNodoRecursivo(estructuras.NodoArbol actual, String nombreBuscado) {
        if (actual.getNombre().equals(nombreBuscado)) return actual;
        if (actual.getHijos() != null) {
            estructuras.Nodo<estructuras.NodoArbol> hijo = actual.getHijos().getHead();
            while (hijo != null) {
                estructuras.NodoArbol encontrado = buscarNodoRecursivo(hijo.getData(), nombreBuscado);
                if (encontrado != null) return encontrado;
                hijo = hijo.getNext();
            }
        }
        return null;
    }
}