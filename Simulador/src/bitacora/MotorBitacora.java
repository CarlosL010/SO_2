/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bitacora;
import estructuras.ListaEnlazada;
import estructuras.Nodo;
/**
 *
 * @author pinto
 */
public class MotorBitacora {
    private ListaEnlazada<Transaccion> logTransacciones;

    public MotorBitacora() {
        this.logTransacciones = new ListaEnlazada<>();
    }

    // Paso 1: Registrar ANTES de hacer la operación
    public Transaccion registrarPendiente(String operacion, String nombreArchivo) {
        Transaccion t = new Transaccion(operacion, nombreArchivo);
        logTransacciones.add(t);
        System.out.println("Journal: Registrada operación " + operacion + " sobre " + nombreArchivo + " como PENDIENTE.");
        return t;
    }

    // Paso 2: Confirmar DESPUÉS de hacer la operación con éxito
    public void hacerCommit(Transaccion t) {
        t.setEstado("CONFIRMADA");
        System.out.println("Journal: Transacción " + t.getId() + " CONFIRMADA (Commit).");
    }

    // Paso 3: El protocolo de emergencia al reiniciar el sistema
    public void recuperarSistema() {
        System.out.println("--- Iniciando Recuperación del Sistema (Journaling) ---");
        
        Nodo<Transaccion> actual = logTransacciones.getHead();
        boolean hubieronFallos = false;

        // Recorremos todo el historial buscando operaciones a medias
        while (actual != null) {
            Transaccion t = actual.getData();
            
            if (t.getEstado().equals("PENDIENTE")) {
                hubieronFallos = true;
                System.out.println("Journal: ¡Alerta! Detectada transacción " + t.getId() + 
                                   " incompleta (" + t.getTipoOperacion() + "). Aplicando UNDO...");
                aplicarUndo(t);
                t.setEstado("DESHECHA"); // Marcamos para no volver a deshacerla en el futuro
            }
            actual = actual.getNext();
        }

        if (!hubieronFallos) {
            System.out.println("Journal: El sistema se cerró correctamente. No hay fallos que recuperar.");
        }
        System.out.println("--- Recuperación Finalizada ---");
    }

    // La lógica para revertir el daño
    private void aplicarUndo(Transaccion t) {
        if (t.getTipoOperacion().equals("CREATE")) {
            System.out.println("UNDO: Revirtiendo CREATE. Eliminando rastros de '" + t.getNombreArchivo() + "' y liberando sus bloques.");
            // NOTA: Aquí más adelante llamaremos al método de la Persona A para liberar los bloques del disco.
        } else if (t.getTipoOperacion().equals("DELETE")) {
            System.out.println("UNDO: Revirtiendo DELETE. Restaurando punteros y bloques del archivo '" + t.getNombreArchivo() + "'.");
            // NOTA: Aquí restauraremos el archivo si se borró a medias.
        }
    }

    // Método para que la Persona A pueda mostrar el log en una tabla de la interfaz
    public ListaEnlazada<Transaccion> getLog() {
        return logTransacciones;
    }
}
