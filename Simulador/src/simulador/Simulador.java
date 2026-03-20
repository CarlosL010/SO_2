/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simulador;

// --- Imports de la Persona B (Backend) ---
import core.PlanificadorDisco;
import core.GestorProcesos;
import bitacora.MotorBitacora;
import concurrencia.GestorLocks;

// --- Imports de la Persona A (Frontend y Disco) ---
import gui.SimuladorGUI;
import core.Disco;

import javax.swing.SwingUtilities;

public class Simulador {

    public static void main(String[] args) {
        
        System.out.println("=== INICIANDO SISTEMA OPERATIVO VIRTUAL ===");

        // Toda interfaz gráfica en Java debe arrancar dentro de este hilo especial (SwingUtilities)
        // para evitar que la ventana se congele.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                // 1. Instanciar la Bitácora (Journaling) y revisar si hubo fallos previos
                MotorBitacora bitacora = new MotorBitacora();
                bitacora.recuperarSistema(); // Requisito del PDF: Revisar el log al iniciar

                // 2. Crear las herramientas lógicas del Backend (Persona B)
                PlanificadorDisco planificador = new PlanificadorDisco(50); // Cabezal inicia en 50 según los casos de prueba
                GestorLocks locks = new GestorLocks();

                // 3. Crear la Interfaz Gráfica (Persona A)
                // Al hacer esto, internamente la GUI también crea el Disco de 100 bloques
                SimuladorGUI ventana = new SimuladorGUI();

                // 4. Extraer el disco que la GUI acaba de crear
                // (Es vital que tu Gestor maneje el MISMO disco que se ve en la pantalla)
                Disco discoVirtual = ventana.getDiscoVirtual();

                // 5. Crear el Gestor de Procesos (El gran coordinador)
                // Le pasamos todas las herramientas para que pueda hacer su trabajo
                GestorProcesos gestor = new GestorProcesos(planificador, bitacora, locks, ventana, discoVirtual);

                // 6. Conectar la GUI con el Gestor
                // Así los botones sabrán a qué cola enviar los procesos
                ventana.setGestorProcesos(gestor);

                // 7. ¡Encender el motor de concurrencia en segundo plano!
                gestor.iniciarSistema();

                // 8. Mostrar la ventana finalmente al usuario
                ventana.setVisible(true);
                System.out.println("Sistema en línea. Esperando interrupciones del usuario...");
            }
        });
    }
}