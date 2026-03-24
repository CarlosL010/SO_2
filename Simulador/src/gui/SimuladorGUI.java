/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import core.Disco;
import core.GestorProcesos;
import core.PlanificadorDisco;
import concurrencia.GestorLocks;
import bitacora.MotorBitacora;
import bitacora.Transaccion;
import estructuras.NodoArbol;
import modelos.Bloque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 *
 * @author josep
 */
public class SimuladorGUI extends JFrame {

    private Disco discoVirtual;
    private GestorProcesos gestorProcesos; 
    
    private JTree arbolDirectorios;
    private DefaultTreeModel modeloArbol;
    private JPanel panelDisco;
    private JLabel[] etiquetasBloques; 
    private JTable tablaFAT;
    private DefaultTableModel modeloTabla;
    private JTable tablaJournal;
    private DefaultTableModel modeloJournal;
    
    private JComboBox<String> comboModoUsuario;
    private JComboBox<String> comboPlanificador;
    private JSlider sliderVelocidad;
    private JLabel lblCiclo, lblCabeza, lblVelocidadText;
    
    private JButton btnCrear, btnDirectorio, btnLeer, btnRenombrar, btnEliminar, btnPausar, btnCargarJSON, btnGuardarJSON, btnLimpiarDisco, btnSimularFallo; 

    private JTextArea txtLogEventos;
    private JTextArea txtColaProcesos;
    private JButton btnLimpiarLog;

    private JPopupMenu popupMenuArbol;
    private JMenuItem menuCrearArch;
    private JMenuItem menuCrearDir;
    private JMenuItem menuEliminarItem;

    // --- REEMPLAZO DE HASHMAPS POR ARREGLOS PRIMITIVOS ---
    private String[] llavesColores = new String[250];
    private Color[] valoresColores = new Color[250];
    private int totalColoresGuardados = 0;

    private String[] llavesLocks = new String[50];
    private String[] valoresLocks = new String[50];
    private int totalLocksGuardados = 0;

    private int indiceColor = 0;
    private final Color[] paletaSegura = {
        new Color(100, 200, 255), new Color(255, 150, 150), new Color(150, 255, 150), 
        new Color(255, 200, 100), new Color(255, 150, 255), new Color(200, 200, 100), 
        new Color(150, 255, 255), new Color(200, 150, 255)
    };

    public SimuladorGUI() {
        this.discoVirtual = new Disco(250);

        setTitle("Simulador de Sistema de Archivos");
        setSize(1350, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- PANELES SUPERIORES  ---
        JPanel panelControles = new JPanel(new GridLayout(2, 1, 5, 5));
        panelControles.setBorder(BorderFactory.createTitledBorder("Controles"));

        // FILA 1: Operaciones de Archivos y Modos
        JPanel panelFila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFila1.add(new JLabel("Modo:"));
        comboModoUsuario = new JComboBox<>(new String[]{"Administrador", "Usuario"});
        panelFila1.add(comboModoUsuario);

        panelFila1.add(Box.createHorizontalStrut(10));
        panelFila1.add(new JLabel("Planificador:"));
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        panelFila1.add(comboPlanificador);

        panelFila1.add(Box.createHorizontalStrut(10));
        btnCrear = new JButton("Crear Archivo");
        btnDirectorio = new JButton("Directorio");
        btnLeer = new JButton("Leer");
        btnRenombrar = new JButton("Renombrar");
        btnEliminar = new JButton("Eliminar");
        btnPausar = new JButton("Pausar");
        
        panelFila1.add(btnCrear); panelFila1.add(btnDirectorio); panelFila1.add(btnLeer);
        panelFila1.add(btnRenombrar); panelFila1.add(btnEliminar); panelFila1.add(btnPausar);

        // FILA 2: Sistema, JSON, Fallos y Velocidad
        JPanel panelFila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFila2.add(new JLabel("Velocidad (ms):"));
        sliderVelocidad = new JSlider(100, 2000, 500); 
        lblVelocidadText = new JLabel("500 ms");
        panelFila2.add(sliderVelocidad);
        panelFila2.add(lblVelocidadText);

        panelFila2.add(Box.createHorizontalStrut(20));
        lblCiclo = new JLabel("Ciclo: 0");
        lblCiclo.setFont(new Font("Arial", Font.BOLD, 12));
        panelFila2.add(lblCiclo);

        panelFila2.add(Box.createHorizontalStrut(20));
        lblCabeza = new JLabel("Cabeza: 50");
        lblCabeza.setFont(new Font("Arial", Font.BOLD, 12));
        panelFila2.add(lblCabeza);

        panelFila2.add(Box.createHorizontalStrut(40)); // Separador visual
        
        btnCargarJSON = new JButton("Cargar JSON");
        btnGuardarJSON = new JButton("Guardar JSON");
        
        btnLimpiarDisco = new JButton("Limpiar Disco");
        btnLimpiarDisco.setBackground(new Color(255, 200, 100)); // Naranja
        
        btnSimularFallo = new JButton("Simular Fallo");
        btnSimularFallo.setBackground(new Color(255, 100, 100)); // Rojo
        btnSimularFallo.setForeground(Color.WHITE);

        panelFila2.add(btnCargarJSON); panelFila2.add(btnGuardarJSON); 
        panelFila2.add(btnLimpiarDisco); panelFila2.add(btnSimularFallo);

        panelControles.add(panelFila1);
        panelControles.add(panelFila2);
        add(panelControles, BorderLayout.NORTH);

        // --- PANEL IZQUIERDO Y MENÚ CONTEXTUAL ---
        DefaultMutableTreeNode raizVisual = construirNodosVisuales(discoVirtual.getArbolDirectorios().getRaiz());
        modeloArbol = new DefaultTreeModel(raizVisual);
        arbolDirectorios = new JTree(modeloArbol);
        
        popupMenuArbol = new JPopupMenu();
        menuCrearArch = new JMenuItem("Nuevo Archivo Aquí");
        menuCrearDir = new JMenuItem("Nuevo Directorio Aquí");
        menuEliminarItem = new JMenuItem("Eliminar Elemento");
        popupMenuArbol.add(menuCrearArch);
        popupMenuArbol.add(menuCrearDir);
        popupMenuArbol.addSeparator();
        popupMenuArbol.add(menuEliminarItem);

        arbolDirectorios.addMouseListener(new MouseAdapter() {
            private void comprobarPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = arbolDirectorios.getRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        arbolDirectorios.setSelectionRow(row);
                        popupMenuArbol.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
            @Override public void mousePressed(MouseEvent e) { comprobarPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { comprobarPopup(e); }
        });

        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(250, 0));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        add(scrollArbol, BorderLayout.WEST);

        // --- PESTAÑAS CENTRALES ---
        JTabbedPane tabCentro = new JTabbedPane();
        
        panelDisco = new JPanel(new GridLayout(0, 10, 2, 2)); 
        etiquetasBloques = new JLabel[250];
        for (int i = 0; i < 250; i++) {
            etiquetasBloques[i] = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            etiquetasBloques[i].setOpaque(true);
            etiquetasBloques[i].setBackground(Color.LIGHT_GRAY);
            etiquetasBloques[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            panelDisco.add(etiquetasBloques[i]);
        }
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        
        String[] columnasFAT = {"Nombre", "Bloques", "Inicio", "Propietario", "Estado de Lock"};
        modeloTabla = new DefaultTableModel(columnasFAT, 0);
        tablaFAT = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaFAT);

        String[] colJournal = {"ID Trans.", "Operación", "Archivo", "Estado"};
        modeloJournal = new DefaultTableModel(colJournal, 0);
        tablaJournal = new JTable(modeloJournal);
        JScrollPane scrollJournal = new JScrollPane(tablaJournal);

        tabCentro.addTab("Simulación de Disco", scrollDisco);
        tabCentro.addTab("Tabla de Asignación", scrollTabla);
        tabCentro.addTab("Journal (Bitácora)", scrollJournal);
        add(tabCentro, BorderLayout.CENTER);

        // --- PANELES INFERIORES ---
        JPanel panelSur = new JPanel(new GridLayout(1, 2, 10, 0));
        panelSur.setPreferredSize(new Dimension(0, 200));

        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        txtLogEventos = new JTextArea();
        txtLogEventos.setEditable(false);
        txtLogEventos.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(txtLogEventos);
        btnLimpiarLog = new JButton("Limpiar Log");
        panelLog.add(scrollLog, BorderLayout.CENTER);
        panelLog.add(btnLimpiarLog, BorderLayout.SOUTH);

        JPanel panelCola = new JPanel(new BorderLayout());
        panelCola.setBorder(BorderFactory.createTitledBorder("Cola de Procesos"));
        txtColaProcesos = new JTextArea();
        txtColaProcesos.setEditable(false);
        txtColaProcesos.setFont(new Font("Monospaced", Font.BOLD, 12));
        JScrollPane scrollCola = new JScrollPane(txtColaProcesos);
        panelCola.add(scrollCola, BorderLayout.CENTER);

        panelSur.add(panelLog);
        panelSur.add(panelCola);
        add(panelSur, BorderLayout.SOUTH);

        configurarEventos();
    }

    public Disco getDiscoVirtual() { return discoVirtual; }
    public void setGestorProcesos(GestorProcesos gestor) { this.gestorProcesos = gestor; }

    public void agregarLog(int ciclo, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLogEventos.append("[Ciclo " + ciclo + "] " + mensaje + "\n");
            txtLogEventos.setCaretPosition(txtLogEventos.getDocument().getLength()); 
        });
    }

    public void actualizarColaUI(String contenido) {
        SwingUtilities.invokeLater(() -> txtColaProcesos.setText(contenido));
    }

    public void actualizarEstado(int ciclo, int cabeza) {
        lblCiclo.setText("Ciclo: " + ciclo);
        lblCabeza.setText("Cabeza: " + cabeza);
    }

    // --- MÉTODOS DE BUSCADOR  ---
    public void setEstadoLockArchivo(String nombreArchivo, String estado) {
        if (estado.equals("Libre")) {
            for (int i = 0; i < totalLocksGuardados; i++) {
                if (llavesLocks[i] != null && llavesLocks[i].equals(nombreArchivo)) {
                    valoresLocks[i] = "Libre";
                    return;
                }
            }
        } else {
            for (int i = 0; i < totalLocksGuardados; i++) {
                if (llavesLocks[i] != null && llavesLocks[i].equals(nombreArchivo)) {
                    valoresLocks[i] = estado;
                    return;
                }
            }
            if (totalLocksGuardados < 50) {
                llavesLocks[totalLocksGuardados] = nombreArchivo;
                valoresLocks[totalLocksGuardados] = estado;
                totalLocksGuardados++;
            }
        }
    }

    private String getEstadoLockSeguro(String nombreArchivo) {
        for (int i = 0; i < totalLocksGuardados; i++) {
            if (llavesLocks[i] != null && llavesLocks[i].equals(nombreArchivo)) {
                return valoresLocks[i];
            }
        }
        return "Libre";
    }

    private Color getColorSeguro(String nombreArchivo) {
        for (int i = 0; i < totalColoresGuardados; i++) {
            if (llavesColores[i] != null && llavesColores[i].equals(nombreArchivo)) {
                return valoresColores[i];
            }
        }
        Color nuevoColor = paletaSegura[indiceColor % paletaSegura.length];
        if (totalColoresGuardados < 250) {
            llavesColores[totalColoresGuardados] = nombreArchivo;
            valoresColores[totalColoresGuardados] = nuevoColor;
            totalColoresGuardados++;
            indiceColor++;
        }
        return nuevoColor;
    }
    // -----------------------------------------------------------

    private String obtenerPadreSeleccionado() {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolDirectorios.getLastSelectedPathComponent();
        if (nodo == null) return "Raíz";
        return nodo.getUserObject().toString();
    }

    private void dispararCreacion(boolean esArchivo) {
        if (gestorProcesos == null) return;
        String padre = obtenerPadreSeleccionado();
        String tipo = esArchivo ? "archivo" : "directorio";
        String nombre = JOptionPane.showInputDialog("Ingrese nombre del nuevo " + tipo + " en [" + padre + "]:");
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            if (esArchivo) {
                try {
                    int tamano = Integer.parseInt(JOptionPane.showInputDialog("¿Cuántos bloques ocupará?"));
                    if (tamano <= 0 || tamano > 250) {
                        JOptionPane.showMessageDialog(this, "Error: El tamaño debe ser entre 1 y 250 bloques.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    gestorProcesos.agregarProcesoCRUD("Crear", padre + "/" + nombre, 0, tamano);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Error: Debe ingresar un número entero válido.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                gestorProcesos.agregarProcesoCRUD("CrearDir", padre + "/" + nombre, 0, 0);
            }
        }
    }

    private void dispararEliminacionLocal() {
        if (gestorProcesos == null) return;
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) arbolDirectorios.getLastSelectedPathComponent();
        String nombreAEliminar = null;

        if (nodo != null && !nodo.getUserObject().toString().equals("Raíz")) {
            String nombreSeleccionado = nodo.getUserObject().toString();
            int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar '" + nombreSeleccionado + "'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) nombreAEliminar = nombreSeleccionado;
        } else {
            nombreAEliminar = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo o directorio a eliminar:");
        }

        if (nombreAEliminar != null && !nombreAEliminar.trim().isEmpty()) {
            gestorProcesos.agregarProcesoCRUD("Eliminar", nombreAEliminar, 0, 0);
        }
    }

    public void ejecutarPantallazoCrash() {
        agregarLog(gestorProcesos.getCicloActual(), "¡KERNEL PANIC! El sistema ha colapsado.");
        JOptionPane.showMessageDialog(this, "¡Fallo Simulado Exitoso!\nPor favor, presione 'Reiniciar Sistema'.", "CRASH DEL SISTEMA", JOptionPane.ERROR_MESSAGE);
        
        comboModoUsuario.setEnabled(false); btnCrear.setEnabled(false); btnDirectorio.setEnabled(false);
        btnLeer.setEnabled(false); btnRenombrar.setEnabled(false); btnEliminar.setEnabled(false);
        btnCargarJSON.setEnabled(false); btnGuardarJSON.setEnabled(false); btnPausar.setEnabled(false); btnLimpiarDisco.setEnabled(false);
        
        btnSimularFallo.setText("Reiniciar Sistema");
        btnSimularFallo.setBackground(new Color(100, 255, 100));
        btnSimularFallo.setForeground(Color.BLACK);
    }

    private void reiniciarSistema() {
        MotorBitacora bitacora = gestorProcesos.getBitacora();
        bitacora.recuperarSistema(); 
        
        PlanificadorDisco nuevoPlanificador = new PlanificadorDisco(gestorProcesos.getPlanificador().getPosicionCabezal());
        nuevoPlanificador.setPolitica((String)comboPlanificador.getSelectedItem());
        
        this.gestorProcesos = new GestorProcesos(nuevoPlanificador, bitacora, new GestorLocks(), this, discoVirtual);
        this.gestorProcesos.setUsuarioSesion(comboModoUsuario.getSelectedItem().toString()); 
        this.gestorProcesos.iniciarSistema();
        
        
        this.totalLocksGuardados = 0;
        this.llavesLocks = new String[50];
        this.valoresLocks = new String[50];
        
        refrescarTodo();
        
        btnSimularFallo.setText("Simular Fallo");
        btnSimularFallo.setBackground(new Color(255, 100, 100));
        btnSimularFallo.setForeground(Color.WHITE);
        
        comboModoUsuario.setEnabled(true); btnCrear.setEnabled(true); btnDirectorio.setEnabled(true);
        btnLeer.setEnabled(true); btnRenombrar.setEnabled(true); btnEliminar.setEnabled(true);
        btnCargarJSON.setEnabled(true); btnGuardarJSON.setEnabled(true); btnPausar.setEnabled(true); btnLimpiarDisco.setEnabled(true);
    }

    private void formatearDiscoTotalmente() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea limpiar el disco por completo?\nSe perderán todos los archivos y configuraciones actuales.", "Confirmar Formateo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (gestorProcesos != null) {
                gestorProcesos.setPausado(true); 
            }

            this.discoVirtual = new Disco(250);
            
            
            this.totalColoresGuardados = 0;
            this.totalLocksGuardados = 0;
            this.llavesColores = new String[250];
            this.valoresColores = new Color[250];
            this.llavesLocks = new String[50];
            this.valoresLocks = new String[50];
            this.indiceColor = 0;
            
            this.txtLogEventos.setText("");
            this.txtColaProcesos.setText("");

            MotorBitacora bitacoraLimpia = new MotorBitacora();
            bitacoraLimpia.setDiscoVirtual(this.discoVirtual);
            
            PlanificadorDisco nuevoPlanificador = new PlanificadorDisco(50); 
            nuevoPlanificador.setPolitica((String)comboPlanificador.getSelectedItem());
            
            this.gestorProcesos = new GestorProcesos(nuevoPlanificador, bitacoraLimpia, new GestorLocks(), this, this.discoVirtual);
            this.gestorProcesos.setUsuarioSesion(comboModoUsuario.getSelectedItem().toString());
            this.gestorProcesos.iniciarSistema();
            
            refrescarTodo();
            agregarLog(0, "SISTEMA: Disco formateado exitosamente. Se ha restablecido el estado inicial.");
        }
    }

    private DefaultMutableTreeNode construirNodosVisuales(NodoArbol nodoLogico) {
        DefaultMutableTreeNode nodoVisual = new DefaultMutableTreeNode(nodoLogico.getNombre());
        if (!nodoLogico.isEsArchivo() && nodoLogico.getHijos() != null) {
            estructuras.Nodo<NodoArbol> actual = nodoLogico.getHijos().getHead();
            while (actual != null) {
                nodoVisual.add(construirNodosVisuales(actual.getData()));
                actual = actual.getNext();
            }
        }
        return nodoVisual;
    }

    private void poblarTablaFAT(NodoArbol nodo, DefaultTableModel modelo) {
        if (nodo.isEsArchivo()) {
            String lockActivo = getEstadoLockSeguro(nodo.getNombre());
            modelo.addRow(new Object[]{nodo.getNombre(), nodo.getTamanoEnBloques(), nodo.getPrimerBloqueAsignado(), nodo.getPropietario(), lockActivo});
        } else if (nodo.getHijos() != null) {
            estructuras.Nodo<NodoArbol> actual = nodo.getHijos().getHead();
            while (actual != null) {
                poblarTablaFAT(actual.getData(), modelo);
                actual = actual.getNext();
            }
        }
    }

    private void poblarTablaJournal() {
        modeloJournal.setRowCount(0);
        if (gestorProcesos != null) {
            estructuras.Nodo<Transaccion> actual = gestorProcesos.getBitacora().getLog().getHead();
            while (actual != null) {
                Transaccion t = actual.getData();
                modeloJournal.addRow(new Object[]{t.getId(), t.getTipoOperacion(), t.getNombreArchivo(), t.getEstado()});
                actual = actual.getNext();
            }
        }
    }

    public void refrescarTodo() {
        DefaultMutableTreeNode nuevaRaiz = construirNodosVisuales(discoVirtual.getArbolDirectorios().getRaiz());
        modeloArbol.setRoot(nuevaRaiz);
        modeloArbol.reload();
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) arbolDirectorios.expandRow(i);

        modeloTabla.setRowCount(0); 
        poblarTablaFAT(discoVirtual.getArbolDirectorios().getRaiz(), modeloTabla);
        poblarTablaJournal(); 
        
        Bloque[] bloques = discoVirtual.getBloques();
        for (int i = 0; i < bloques.length; i++) {
            if (bloques[i].isLibre()) {
                etiquetasBloques[i].setBackground(Color.LIGHT_GRAY);
                etiquetasBloques[i].setText(String.valueOf(i));
            } else {
                String nombreArchivo = bloques[i].getPerteneceA();
                etiquetasBloques[i].setBackground(getColorSeguro(nombreArchivo));
                int siguiente = bloques[i].getSiguienteBloque();
                etiquetasBloques[i].setText(i + (siguiente != -1 ? "->" + siguiente : "-> EOF"));
            }
        }
    }

    private void configurarEventos() {
        
        menuCrearArch.addActionListener(e -> dispararCreacion(true));
        menuCrearDir.addActionListener(e -> dispararCreacion(false));
        menuEliminarItem.addActionListener(e -> dispararEliminacionLocal());
        
        btnCrear.addActionListener(e -> dispararCreacion(true));
        btnDirectorio.addActionListener(e -> dispararCreacion(false));
        btnEliminar.addActionListener(e -> dispararEliminacionLocal());

        comboModoUsuario.addActionListener(e -> {
            String modoSeleccionado = comboModoUsuario.getSelectedItem().toString();
            if (gestorProcesos != null) {
                gestorProcesos.setUsuarioSesion(modoSeleccionado);
                agregarLog(gestorProcesos.getCicloActual(), "SISTEMA: Sesión cambiada a modo " + modoSeleccionado.toUpperCase());
            }
        });

        btnLeer.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Archivo a leer:");
            if (nombre != null && gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Leer", nombre, 0, 0);
        });

        btnRenombrar.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Archivo a renombrar:");
            if (nombre != null && gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Actualizar", nombre, 0, 0);
        });

        btnLimpiarLog.addActionListener(e -> txtLogEventos.setText(""));

        btnLimpiarDisco.addActionListener(e -> formatearDiscoTotalmente());

        btnCargarJSON.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION && gestorProcesos != null) {
                utilidades.CargadorPruebas.cargarJSON(fileChooser.getSelectedFile().getAbsolutePath(), discoVirtual, gestorProcesos, gestorProcesos.getPlanificador());
                refrescarTodo(); 
            }
        });

        btnGuardarJSON.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION && gestorProcesos != null) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                if (!ruta.endsWith(".json")) ruta += ".json";
                utilidades.CargadorPruebas.guardarEstado(ruta, discoVirtual, gestorProcesos.getPlanificador().getPosicionCabezal());
                JOptionPane.showMessageDialog(this, "Estado del sistema guardado con éxito.", "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSimularFallo.addActionListener(e -> {
            if (btnSimularFallo.getText().equals("Reiniciar Sistema")) reiniciarSistema();
            else if (gestorProcesos != null) {
                gestorProcesos.activarCrash();
                JOptionPane.showMessageDialog(this, "Trampa activada.\nEl sistema colapsará en la próxima operación de escritura.", "Alerta", JOptionPane.WARNING_MESSAGE);
            }
        });

        comboPlanificador.addActionListener(e -> {
            if (gestorProcesos != null) gestorProcesos.getPlanificador().setPolitica((String) comboPlanificador.getSelectedItem());
        });

        btnPausar.addActionListener(e -> {
            if (gestorProcesos != null) {
                boolean estaPausado = gestorProcesos.isPausado();
                gestorProcesos.setPausado(!estaPausado);
                btnPausar.setText(estaPausado ? "Reanudar" : "Pausar");
                btnPausar.setBackground(estaPausado ? Color.GREEN : Color.ORANGE);
            }
        });

        sliderVelocidad.addChangeListener(e -> {
            int valor = sliderVelocidad.getValue();
            lblVelocidadText.setText(valor + " ms");
            if (gestorProcesos != null && !sliderVelocidad.getValueIsAdjusting()) gestorProcesos.setTiempoVelocidadMs(valor);
        });
    }
}