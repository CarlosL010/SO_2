/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import core.Disco;
import estructuras.NodoArbol;
import modelos.Bloque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class SimuladorGUI extends JFrame {

    // --- EL CEREBRO DE TU PARTE ---
    private Disco discoVirtual;
    
    // --- COMPONENTES VISUALES ---
    private JTree arbolDirectorios;
    private DefaultTreeModel modeloArbol;
    private JPanel panelDisco;
    private JLabel[] etiquetasBloques; // Para controlar los cuadritos individualmente
    private JTable tablaFAT;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> comboModoUsuario;
    private JButton btnCrear, btnEliminar, btnActualizar;

    // Para pintar cada archivo de un color distinto
    private HashMap<String, Color> coloresArchivos; 

    public SimuladorGUI() {
        // Inicializamos nuestro disco con 100 bloques
        this.discoVirtual = new Disco(100);
        this.coloresArchivos = new HashMap<>();

        // Configuración básica de la ventana
        setTitle("Simulador de Sistema de Archivos - Trimestre 2526-2");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. PANEL SUPERIOR (Controles) ---
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboModoUsuario = new JComboBox<>(new String[]{"Modo Administrador", "Modo Usuario"});
        btnCrear = new JButton("Crear Archivo");
        btnActualizar = new JButton("Actualizar Nombre");
        btnEliminar = new JButton("Eliminar Archivo");

        panelNorte.add(new JLabel("Modo:"));
        panelNorte.add(comboModoUsuario);
        panelNorte.add(btnCrear);
        panelNorte.add(btnActualizar);
        panelNorte.add(btnEliminar);
        add(panelNorte, BorderLayout.NORTH);

        // --- 2. PANEL IZQUIERDO (JTree) ---
        DefaultMutableTreeNode raizVisual = construirNodosVisuales(discoVirtual.getArbolDirectorios().getRaiz());
        modeloArbol = new DefaultTreeModel(raizVisual);
        arbolDirectorios = new JTree(modeloArbol);
        
        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(250, 0));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Estructura de Archivos"));
        add(scrollArbol, BorderLayout.WEST);

        // --- 3. PANEL CENTRAL (Simulación del Disco SD) ---
        panelDisco = new JPanel(new GridLayout(10, 10, 2, 2)); 
        panelDisco.setBorder(BorderFactory.createTitledBorder("Simulación del Disco (SD)"));
        etiquetasBloques = new JLabel[100];
        
        for (int i = 0; i < 100; i++) {
            etiquetasBloques[i] = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            etiquetasBloques[i].setOpaque(true);
            etiquetasBloques[i].setBackground(Color.LIGHT_GRAY);
            etiquetasBloques[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            panelDisco.add(etiquetasBloques[i]);
        }
        add(panelDisco, BorderLayout.CENTER);

        // --- 4. PANEL INFERIOR (Tabla FAT) ---
        String[] columnas = {"Nombre del Archivo", "Cantidad Bloques", "Bloque Inicial", "Propietario"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaFAT = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaFAT);
        scrollTabla.setPreferredSize(new Dimension(0, 150));
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Asignación de Archivos (FAT)"));
        add(scrollTabla, BorderLayout.SOUTH);

        // --- 5. EVENTOS DE LOS BOTONES ---
        configurarEventos();
    }

    // --- MÉTODOS PARA ACTUALIZAR LA VISTA ---

    // Este método lee tu ListaEnlazada personalizada y la convierte al JTree de Java
    private DefaultMutableTreeNode construirNodosVisuales(NodoArbol nodoLogico) {
        DefaultMutableTreeNode nodoVisual = new DefaultMutableTreeNode(nodoLogico.getNombre());
        
        if (!nodoLogico.isEsArchivo() && nodoLogico.getHijos() != null) {
            estructuras.Nodo<NodoArbol> actual = nodoLogico.getHijos().getHead();
            while (actual != null) {
                nodoVisual.add(construirNodosVisuales(actual.getData()));
                actual = actual.getNext(); // Recorremos tu ListaEnlazada
            }
        }
        return nodoVisual;
    }

    public void refrescarTodo() {
        // 1. Refrescar el Árbol
        DefaultMutableTreeNode nuevaRaiz = construirNodosVisuales(discoVirtual.getArbolDirectorios().getRaiz());
        modeloArbol.setRoot(nuevaRaiz);
        modeloArbol.reload();
        
        // Expandir el árbol para que se vean los archivos nuevos
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
            arbolDirectorios.expandRow(i);
        }

        // 2. Refrescar el Disco (Cuadritos) y la Tabla
        modeloTabla.setRowCount(0); // Limpiamos la tabla
        Bloque[] bloques = discoVirtual.getBloques();
        
        for (int i = 0; i < bloques.length; i++) {
            if (bloques[i].isLibre()) {
                etiquetasBloques[i].setBackground(Color.LIGHT_GRAY);
                etiquetasBloques[i].setText(String.valueOf(i));
            } else {
                String nombreArchivo = bloques[i].getPerteneceA();
                // Asignar un color aleatorio la primera vez que vemos un archivo
                coloresArchivos.putIfAbsent(nombreArchivo, new Color((int)(Math.random() * 0x1000000)).brighter());
                
                etiquetasBloques[i].setBackground(coloresArchivos.get(nombreArchivo));
                
                // Si el bloque apunta a otro, lo mostramos (Ej: "3 -> 5")
                int siguiente = bloques[i].getSiguienteBloque();
                String texto = i + (siguiente != -1 ? "->" + siguiente : "-> EOF");
                etiquetasBloques[i].setText(texto);
            }
        }
    }

    // --- LÓGICA DE LOS BOTONES ---
    private void configurarEventos() {
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre = JOptionPane.showInputDialog("Ingrese el nombre del archivo (ej. tarea.pdf):");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    String tamanoStr = JOptionPane.showInputDialog("¿Cuántos bloques ocupará?");
                    try {
                        int tamano = Integer.parseInt(tamanoStr);
                        String modo = (String) comboModoUsuario.getSelectedItem();
                        
                        // ¡LLAMAMOS AL CEREBRO!
                        boolean exito = discoVirtual.crearArchivo(nombre, modo, tamano);
                        
                        if (exito) {
                            refrescarTodo(); // Actualizamos la pantalla
                        } else {
                            JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "El tamaño debe ser un número entero.");
                    }
                }
            }
        });

        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre = JOptionPane.showInputDialog("Ingrese el nombre del archivo a eliminar:");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    boolean exito = discoVirtual.eliminarArchivo(nombre);
                    if (exito) {
                        refrescarTodo();
                    } else {
                        JOptionPane.showMessageDialog(null, "Archivo no encontrado.");
                    }
                }
            }
        });
    }
}