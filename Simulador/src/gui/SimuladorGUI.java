/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class SimuladorGUI extends JFrame {

    // Componentes principales solicitados en el proyecto
    private JTree arbolDirectorios;
    private JPanel panelDisco;
    private JTable tablaFAT;
    private JComboBox<String> comboModoUsuario;
    private JButton btnCrear, btnEliminar, btnActualizar;

    public SimuladorGUI() {
        // Configuración básica de la ventana
        setTitle("Simulador de Sistema de Archivos - Trimestre 2526-2");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10)); // Layout principal

        // 1. PANEL SUPERIOR (Controles y Modo de Usuario)
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboModoUsuario = new JComboBox<>(new String[]{"Modo Administrador", "Modo Usuario"});
        btnCrear = new JButton("Crear Archivo/Dir");
        btnActualizar = new JButton("Actualizar Nombre");
        btnEliminar = new JButton("Eliminar");

        panelNorte.add(new JLabel("Modo:"));
        panelNorte.add(comboModoUsuario);
        panelNorte.add(btnCrear);
        panelNorte.add(btnActualizar);
        panelNorte.add(btnEliminar);
        add(panelNorte, BorderLayout.NORTH);

        // 2. PANEL IZQUIERDO (JTree - Estructura del sistema)
        // Datos dummy iniciales (luego lo conectarás con tu clase Arbol)
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("C: (Raíz)");
        DefaultMutableTreeNode carpeta1 = new DefaultMutableTreeNode("Documentos");
        DefaultMutableTreeNode archivo1 = new DefaultMutableTreeNode("readme.txt (1 bloque)");
        carpeta1.add(archivo1);
        raiz.add(carpeta1);
        
        arbolDirectorios = new JTree(new DefaultTreeModel(raiz));
        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(250, 0));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Estructura de Archivos"));
        add(scrollArbol, BorderLayout.WEST);

        // 3. PANEL CENTRAL (Simulación del Disco SD y Bloques)
        panelDisco = new JPanel();
        // Supongamos un disco de 100 bloques (10x10)
        panelDisco.setLayout(new GridLayout(10, 10, 2, 2)); 
        panelDisco.setBorder(BorderFactory.createTitledBorder("Simulación del Disco (SD)"));
        
        // Llenar el disco con bloques visuales "Libres"
        for (int i = 0; i < 100; i++) {
            JLabel bloque = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            bloque.setOpaque(true);
            bloque.setBackground(Color.LIGHT_GRAY); // Color de bloque libre
            bloque.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            panelDisco.add(bloque);
        }
        add(panelDisco, BorderLayout.CENTER);

        // 4. PANEL INFERIOR (JTable - Tabla de Asignación de Archivos)
        // Columnas requeridas por el proyecto
        String[] columnas = {"Nombre del Archivo", "Cantidad de Bloques", "Dirección Primer Bloque", "Color"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        tablaFAT = new JTable(modeloTabla);
        
        // Agregamos una fila dummy de prueba
        modeloTabla.addRow(new Object[]{"readme.txt", 1, 34, "Rojo"});

        JScrollPane scrollTabla = new JScrollPane(tablaFAT);
        scrollTabla.setPreferredSize(new Dimension(0, 150));
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Asignación de Archivos (FAT)"));
        add(scrollTabla, BorderLayout.SOUTH);
    }
}