/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import core.Disco;
import core.GestorProcesos;
import core.PlanificadorDisco;
import modelos.Bloque;
import estructuras.NodoArbol;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CargadorPruebas {

    public static void cargarJSON(String rutaArchivo, Disco disco, GestorProcesos gestor, PlanificadorDisco planificador) {
        try {
            System.out.println("--- Cargando Caso de Prueba ---");
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo))).replaceAll("\\s+", "");

            int headInicial = extraerEntero(contenido, "\"initial_head\":", ",");
            if (headInicial != -1) {
                System.out.println("Cabezal inicial detectado: " + headInicial);
                // planificador.setPosicionCabezalActual(headInicial); // Descomenta si tienes este método
            }

            System.out.println("Cargando System Files al Disco...");
            String bloqueFiles = extraerBloque(contenido, "\"system_files\":{");
            
            // --- EXTRACCIÓN SEGURA CON EXPRESIONES REGULARES ---
            // Busca patrones exactos como: "11":{"name":"boot_sect.bin","blocks":2}
            Pattern pattern = Pattern.compile("\"([0-9]+)\":\\{\"name\":\"([^\"]+)\",\"blocks\":([0-9]+)\\}");
            Matcher matcher = pattern.matcher(bloqueFiles);
            
            while (matcher.find()) {
                int bloqueInicio = Integer.parseInt(matcher.group(1));
                String nombre = matcher.group(2);
                int bloques = Integer.parseInt(matcher.group(3));
                
                forzarArchivoEnDisco(disco, nombre, bloqueInicio, bloques);
            }

            System.out.println("Encolando Requests en el Gestor...");
            String bloqueRequests = extraerBloqueArray(contenido, "\"requests\":[");
            
            Pattern reqPattern = Pattern.compile("\\{\"pos\":([0-9]+),\"op\":\"([^\"]+)\"\\}");
            Matcher reqMatcher = reqPattern.matcher(bloqueRequests);
            
            while (reqMatcher.find()) {
                int pos = Integer.parseInt(reqMatcher.group(1));
                String op = reqMatcher.group(2);
                
                String nombreSimulado = "req_pos_" + pos;
                int tamanoSimulado = (op.equals("CREATE")) ? 1 : 0;
                
                gestor.agregarProcesoCRUD(mapearOperacion(op), nombreSimulado, pos, tamanoSimulado);
            }
            System.out.println("--- Carga de Prueba Finalizada ---");

        } catch (Exception e) {
            System.out.println("Error crítico al leer JSON: " + e.getMessage());
        }
    }

    private static void forzarArchivoEnDisco(Disco disco, String nombre, int bloqueInicio, int cantidad) {
        Bloque[] bloques = disco.getBloques();
        NodoArbol nuevoArchivo = new NodoArbol(nombre, "Administrador", cantidad, bloqueInicio);
        disco.getArbolDirectorios().getRaiz().agregarHijo(nuevoArchivo);

        int actual = bloqueInicio;
        for (int i = 0; i < cantidad; i++) {
            if (actual >= bloques.length) break; 
            bloques[actual].setLibre(false);
            bloques[actual].setPerteneceA(nombre);
            if (i < cantidad - 1) bloques[actual].setSiguienteBloque(actual + 1);
            else bloques[actual].setSiguienteBloque(-1);
            actual++;
        }
    }

    // Métodos auxiliares simples
    private static int extraerEntero(String texto, String clave, String delimitadorFinal) {
        int inicio = texto.indexOf(clave);
        if (inicio == -1) return -1;
        inicio += clave.length();
        int fin = texto.indexOf(delimitadorFinal, inicio);
        if (fin == -1 || fin > inicio + 10) fin = texto.indexOf("}", inicio); 
        try { return Integer.parseInt(texto.substring(inicio, fin).replaceAll("[^0-9]", "")); } catch (Exception e) { return -1; }
    }

    private static String extraerBloque(String texto, String clave) {
        int inicio = texto.indexOf(clave);
        if (inicio == -1) return "";
        inicio += clave.length();
        int llavesAbiertas = 1; int fin = inicio;
        while (fin < texto.length() && llavesAbiertas > 0) {
            if (texto.charAt(fin) == '{') llavesAbiertas++;
            else if (texto.charAt(fin) == '}') llavesAbiertas--;
            fin++;
        }
        return texto.substring(inicio, fin - 1);
    }

    private static String extraerBloqueArray(String texto, String clave) {
        int inicio = texto.indexOf(clave);
        if (inicio == -1) return "";
        inicio += clave.length();
        int fin = texto.indexOf("]", inicio);
        return texto.substring(inicio, fin);
    }

    private static String mapearOperacion(String opIngles) {
        switch (opIngles) {
            case "READ": return "Leer"; case "UPDATE": return "Actualizar"; case "DELETE": return "Eliminar"; case "CREATE": return "Crear"; default: return "Leer";
        }
    }
}