package gui;

import core.Disco;
import core.GestorProcesos;
import core.PlanificadorDisco;
import concurrencia.GestorLocks;
import bitacora.MotorBitacora;
import bitacora.Transaccion;
import estructuras.NodoArbol;
import estructuras.Nodo;
import modelos.Bloque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.HashMap;

public class SimuladorGUI extends JFrame {

    private Disco discoVirtual;
    private GestorProcesos gestorProcesos; 
    
    private JTree arbolDirectorios;
    private DefaultTreeModel modeloArbol;
    private JPanel panelDisco;
    private JLabel[] etiquetasBloques; 
    private JTable tablaFAT;
    private DefaultTableModel modeloTabla;
    
    // --- NUEVO: TABLA PARA EL JOURNAL ---
    private JTable tablaJournal;
    private DefaultTableModel modeloJournal;
    
    private JComboBox<String> comboModoUsuario;
    private JComboBox<String> comboPlanificador;
    private JSlider sliderVelocidad;
    private JLabel lblCiclo, lblCabeza, lblVelocidadText;
    private JButton btnCrear, btnDirectorio, btnLeer, btnRenombrar, btnEliminar, btnPausar, btnCargarJSON; 
    private JButton btnSimularFallo; // --- NUEVO BOTÓN ---

    private JTextArea txtLogEventos;
    private JTextArea txtColaProcesos;
    private JButton btnLimpiarLog;

    private HashMap<String, Color> coloresArchivos; 
    private int indiceColor = 0;
    private final Color[] paletaSegura = {
        new Color(100, 200, 255), new Color(255, 150, 150), new Color(150, 255, 150), 
        new Color(255, 200, 100), new Color(255, 150, 255), new Color(200, 200, 100), 
        new Color(150, 255, 255), new Color(200, 150, 255)
    };

    public SimuladorGUI() {
        this.discoVirtual = new Disco(250);
        this.coloresArchivos = new HashMap<>();

        setTitle("Simulador de Sistema de Archivos - Trimestre 2526-2");
        setSize(1250, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelControles = new JPanel(new GridLayout(2, 1, 5, 5));
        panelControles.setBorder(BorderFactory.createTitledBorder("Controles"));

        JPanel panelFila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFila1.add(new JLabel("Modo:"));
        comboModoUsuario = new JComboBox<>(new String[]{"Administrador", "Usuario"});
        panelFila1.add(comboModoUsuario);

        panelFila1.add(Box.createHorizontalStrut(10));
        panelFila1.add(new JLabel("Planificador:"));
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        panelFila1.add(comboPlanificador);

        panelFila1.add(Box.createHorizontalStrut(10));
        btnCrear = new JButton("Crear");
        btnDirectorio = new JButton("Directorio");
        btnLeer = new JButton("Leer");
        btnRenombrar = new JButton("Renombrar");
        btnEliminar = new JButton("Eliminar");
        btnPausar = new JButton("Pausar");
        btnCargarJSON = new JButton("Cargar JSON");
        
        // --- BOTÓN DE FALLO ---
        btnSimularFallo = new JButton("Simular Fallo");
        btnSimularFallo.setBackground(new Color(255, 100, 100));
        btnSimularFallo.setForeground(Color.WHITE);

        panelFila1.add(btnCrear); panelFila1.add(btnDirectorio); panelFila1.add(btnLeer);
        panelFila1.add(btnRenombrar); panelFila1.add(btnEliminar); panelFila1.add(btnPausar);
        panelFila1.add(btnCargarJSON); panelFila1.add(btnSimularFallo);

        JPanel panelFila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFila2.add(new JLabel("Velocidad (ms):"));
        sliderVelocidad = new JSlider(100, 2000, 500); 
        lblVelocidadText = new JLabel("500 ms");
        panelFila2.add(sliderVelocidad);
        panelFila2.add(lblVelocidadText);

        panelFila2.add(Box.createHorizontalStrut(40));
        lblCiclo = new JLabel("Ciclo: 0");
        lblCiclo.setFont(new Font("Arial", Font.BOLD, 12));
        panelFila2.add(lblCiclo);

        panelFila2.add(Box.createHorizontalStrut(20));
        lblCabeza = new JLabel("Cabeza: 50");
        lblCabeza.setFont(new Font("Arial", Font.BOLD, 12));
        panelFila2.add(lblCabeza);

        panelControles.add(panelFila1);
        panelControles.add(panelFila2);
        add(panelControles, BorderLayout.NORTH);

        DefaultMutableTreeNode raizVisual = construirNodosVisuales(discoVirtual.getArbolDirectorios().getRaiz());
        modeloArbol = new DefaultTreeModel(raizVisual);
        arbolDirectorios = new JTree(modeloArbol);
        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(250, 0));
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        add(scrollArbol, BorderLayout.WEST);

        // --- PESTAÑAS CENTRALES (Añadido el Journal) ---
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
        
        String[] columnasFAT = {"Nombre del Archivo", "Cantidad Bloques", "Bloque Inicial", "Propietario"};
        modeloTabla = new DefaultTableModel(columnasFAT, 0);
        tablaFAT = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaFAT);

        // Nueva Tabla del Journal
        String[] colJournal = {"ID Transacción", "Operación", "Archivo Afectado", "Estado del Journal"};
        modeloJournal = new DefaultTableModel(colJournal, 0);
        tablaJournal = new JTable(modeloJournal);
        JScrollPane scrollJournal = new JScrollPane(tablaJournal);

        tabCentro.addTab("Simulación de Disco", scrollDisco);
        tabCentro.addTab("Tabla de Asignación", scrollTabla);
        tabCentro.addTab("Journal (Bitácora)", scrollJournal); // <-- NUEVA PESTAÑA
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

    // --- LA RUTINA DE CAÍDA Y REINICIO DEL SISTEMA ---
    public void ejecutarPantallazoCrash() {
        agregarLog(gestorProcesos.getCicloActual(), "¡KERNEL PANIC! El sistema ha colapsado a mitad de una operación de E/S.");
        JOptionPane.showMessageDialog(this, "¡Fallo Simulado Exitoso!\nEl sistema se apagó inesperadamente antes del Commit.\n\nPor favor, presione 'Reiniciar Sistema' para aplicar la recuperación por Journaling.", "CRASH DEL SISTEMA", JOptionPane.ERROR_MESSAGE);
        
        // Bloqueamos la interfaz
        comboModoUsuario.setEnabled(false); btnCrear.setEnabled(false); btnDirectorio.setEnabled(false);
        btnLeer.setEnabled(false); btnRenombrar.setEnabled(false); btnEliminar.setEnabled(false);
        btnCargarJSON.setEnabled(false); btnPausar.setEnabled(false);
        
        // Transformamos el botón de Fallo en el botón de Reinicio
        btnSimularFallo.setText("Reiniciar Sistema");
        btnSimularFallo.setBackground(new Color(100, 255, 100)); // Verde
        btnSimularFallo.setForeground(Color.BLACK);
    }

    private void reiniciarSistema() {
        agregarLog(gestorProcesos.getCicloActual(), "=== REINICIANDO SISTEMA ===");
        
        // 1. Ejecutar el protocolo de emergencia (UNDO)
        MotorBitacora bitacora = gestorProcesos.getBitacora();
        bitacora.recuperarSistema(); // Aquí ocurre la magia de borrar la basura
        
        // 2. Crear un nuevo Gestor (para resetear hilos y locks muertos)
        PlanificadorDisco nuevoPlanificador = new PlanificadorDisco(gestorProcesos.getPlanificador().getPosicionCabezal());
        nuevoPlanificador.setPolitica((String)comboPlanificador.getSelectedItem());
        
        this.gestorProcesos = new GestorProcesos(nuevoPlanificador, bitacora, new GestorLocks(), this, discoVirtual);
        this.gestorProcesos.iniciarSistema();
        
        // 3. Restaurar UI
        refrescarTodo();
        btnSimularFallo.setText("Simular Fallo");
        btnSimularFallo.setBackground(new Color(255, 100, 100));
        btnSimularFallo.setForeground(Color.WHITE);
        
        comboModoUsuario.setEnabled(true); btnCrear.setEnabled(true); btnDirectorio.setEnabled(true);
        btnLeer.setEnabled(true); btnRenombrar.setEnabled(true); btnEliminar.setEnabled(true);
        btnCargarJSON.setEnabled(true); btnPausar.setEnabled(true);
        
        JOptionPane.showMessageDialog(this, "Sistema recuperado con éxito.\nLas operaciones pendientes fueron deshechas gracias al Journal.", "Recuperación Completa", JOptionPane.INFORMATION_MESSAGE);
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
            modelo.addRow(new Object[]{nodo.getNombre(), nodo.getTamanoEnBloques(), nodo.getPrimerBloqueAsignado(), nodo.getPropietario()});
        } else if (nodo.getHijos() != null) {
            estructuras.Nodo<NodoArbol> actual = nodo.getHijos().getHead();
            while (actual != null) {
                poblarTablaFAT(actual.getData(), modelo);
                actual = actual.getNext();
            }
        }
    }

    // --- NUEVO: POBLAR LA TABLA DEL JOURNAL ---
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
        
        poblarTablaJournal(); // Refrescar el log visual
        
        Bloque[] bloques = discoVirtual.getBloques();
        for (int i = 0; i < bloques.length; i++) {
            if (bloques[i].isLibre()) {
                etiquetasBloques[i].setBackground(Color.LIGHT_GRAY);
                etiquetasBloques[i].setText(String.valueOf(i));
            } else {
                String nombreArchivo = bloques[i].getPerteneceA();
                if (!coloresArchivos.containsKey(nombreArchivo)) {
                    coloresArchivos.put(nombreArchivo, paletaSegura[indiceColor % paletaSegura.length]);
                    indiceColor++;
                }
                etiquetasBloques[i].setBackground(coloresArchivos.get(nombreArchivo));
                int siguiente = bloques[i].getSiguienteBloque();
                etiquetasBloques[i].setText(i + (siguiente != -1 ? "->" + siguiente : "-> EOF"));
            }
        }
    }

    private void configurarEventos() {
        
        btnSimularFallo.addActionListener(e -> {
            if (btnSimularFallo.getText().equals("Reiniciar Sistema")) {
                reiniciarSistema();
            } else {
                if (gestorProcesos != null) {
                    gestorProcesos.activarCrash();
                    JOptionPane.showMessageDialog(this, "Trampa activada.\nEl sistema colapsará en la próxima operación de escritura.", "Alerta de Fallo Inminente", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        comboModoUsuario.addActionListener(e -> {
            boolean esAdmin = comboModoUsuario.getSelectedIndex() == 0;
            btnCrear.setEnabled(esAdmin); btnDirectorio.setEnabled(esAdmin);
            btnRenombrar.setEnabled(esAdmin); btnEliminar.setEnabled(esAdmin); btnCargarJSON.setEnabled(esAdmin); 
            if (gestorProcesos != null) agregarLog(gestorProcesos.getCicloActual(), esAdmin ? "SISTEMA: Modo Administrador." : "SISTEMA: Modo Usuario.");
        });

        btnCrear.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del archivo:");
            if (nombre != null && !nombre.trim().isEmpty()) {
                try {
                    int tamano = Integer.parseInt(JOptionPane.showInputDialog("¿Cuántos bloques ocupará?"));
                    if (gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Crear", nombre, 0, tamano);
                } catch (NumberFormatException ex) {}
            }
        });

        btnEliminar.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del archivo a eliminar:");
            if (nombre != null && !nombre.trim().isEmpty() && gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Eliminar", nombre, 0, 0);
        });

        btnLeer.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Archivo a leer (simulado):");
            if (nombre != null && gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Leer", nombre, 0, 0);
        });

        btnRenombrar.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Archivo a renombrar:");
            if (nombre != null && gestorProcesos != null) gestorProcesos.agregarProcesoCRUD("Actualizar", nombre, 0, 0);
        });

        btnDirectorio.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog("Nombre del nuevo directorio:");
            if (nombre != null) {
                discoVirtual.getArbolDirectorios().getRaiz().agregarHijo(new NodoArbol(nombre, "Administrador"));
                refrescarTodo();
            }
        });

        btnLimpiarLog.addActionListener(e -> txtLogEventos.setText(""));

        btnCargarJSON.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                if (gestorProcesos != null) {
                    utilidades.CargadorPruebas.cargarJSON(fileChooser.getSelectedFile().getAbsolutePath(), discoVirtual, gestorProcesos, gestorProcesos.getPlanificador());
                    refrescarTodo(); 
                }
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