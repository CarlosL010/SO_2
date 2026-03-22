/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bitacora;
import estructuras.ListaEnlazada;
import estructuras.Nodo;
import core.Disco;
import estructuras.ListaEnlazada;
import estructuras.Nodo;

/**
 *
 * @author pinto
 */
public class MotorBitacora {
    private ListaEnlazada<Transaccion> logTransacciones;
    private Disco discoVirtual; 

    public MotorBitacora() {
        this.logTransacciones = new ListaEnlazada<>();
    }

    public void setDiscoVirtual(Disco disco) {
        this.discoVirtual = disco;
    }

    public Transaccion registrarPendiente(String operacion, String nombreArchivo) {
        Transaccion t = new Transaccion(operacion, nombreArchivo);
        logTransacciones.add(t);
        return t;
    }

    public void hacerCommit(Transaccion t) {
        t.setEstado("CONFIRMADA");
    }

    public void recuperarSistema() {
        System.out.println("--- Iniciando Recuperación del Sistema (Journaling) ---");
        Nodo<Transaccion> actual = logTransacciones.getHead();
        boolean hubieronFallos = false;

        while (actual != null) {
            Transaccion t = actual.getData();
            if (t.getEstado().equals("PENDIENTE")) {
                hubieronFallos = true;
                System.out.println("Journal: ¡Alerta! Detectada transacción incompleta. Aplicando UNDO...");
                aplicarUndo(t);
                t.setEstado("DESHECHA (UNDO)"); 
            }
            actual = actual.getNext();
        }
        if (!hubieronFallos) System.out.println("Journal: Sistema limpio. No hay fallos.");
    }

    private void aplicarUndo(Transaccion t) {
        if (t.getTipoOperacion().equals("Crear")) {
            System.out.println("UNDO: Revirtiendo CREATE. Eliminando rastros de '" + t.getNombreArchivo() + "'.");
            if (discoVirtual != null) {
                
                discoVirtual.eliminarArchivo(t.getNombreArchivo()); 
            }
        } else if (t.getTipoOperacion().equals("Eliminar")) {
            System.out.println("UNDO: Revirtiendo DELETE (Requiere respaldo de bloques).");
        }
    }

    public ListaEnlazada<Transaccion> getLog() {
        return logTransacciones;
    }
}