/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simulador;


import core.PlanificadorDisco;
import core.GestorProcesos;
import bitacora.MotorBitacora;
import concurrencia.GestorLocks;


import gui.SimuladorGUI;
import core.Disco;

import javax.swing.SwingUtilities;

/**
 *
 * @author pinto
 */
public class Simulador {

    public static void main(String[] args) {
        
        System.out.println("=== INICIANDO SISTEMA OPERATIVO VIRTUAL ===");

        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                
                MotorBitacora bitacora = new MotorBitacora();
                bitacora.recuperarSistema(); // Requisito del PDF: Revisar el log al iniciar

                
                PlanificadorDisco planificador = new PlanificadorDisco(50); // Cabezal inicia en 50 según los casos de prueba
                GestorLocks locks = new GestorLocks();

                
                SimuladorGUI ventana = new SimuladorGUI();

                
                Disco discoVirtual = ventana.getDiscoVirtual();

                
                GestorProcesos gestor = new GestorProcesos(planificador, bitacora, locks, ventana, discoVirtual);

                
                ventana.setGestorProcesos(gestor);

                
                gestor.iniciarSistema();

                
                ventana.setVisible(true);
                System.out.println("Sistema en línea. Esperando interrupciones del usuario...");
            }
        });
    }
}