/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simulador;

import gui.SimuladorGUI;
import javax.swing.SwingUtilities;

public class Simulador {
    public static void main(String[] args) {
        // En Swing, las interfaces gráficas deben ejecutarse en un hilo especial
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SimuladorGUI ventana = new SimuladorGUI();
                ventana.setVisible(true);
            }
        });
    }
}