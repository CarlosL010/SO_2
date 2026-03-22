package core;

import estructuras.Cola;
import modelos.Proceso;

/**
 *
 * @author pinto (Actualizado para integración completa)
 */
public class PlanificadorDisco {
    private String politicaActual;
    private int posicionCabezal;
    private boolean moviendoArriba; // true = hacia bloques mayores (↑), false = hacia menores (↓)

    public PlanificadorDisco(int posicionInicial) {
        this.posicionCabezal = posicionInicial;
        this.politicaActual = "FIFO";
        this.moviendoArriba = true; // Por defecto inicia hacia arriba según el PDF
    }

    public void setPolitica(String politica) {
        this.politicaActual = politica;
        this.moviendoArriba = true; // Reiniciamos la dirección al cambiar de algoritmo
    }

    public String getPolitica() { return politicaActual; }
    public int getPosicionCabezal() { return posicionCabezal; }
    public void setPosicionCabezal(int posicion) { this.posicionCabezal = posicion; }

    // El motor principal que el Gestor llama en cada ciclo
    public Proceso procesarCola(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return null;

        // 1. Caso FIFO: Es directo, sacamos el primero que llegó y listo.
        if (politicaActual.equals("FIFO")) {
            Proceso p = colaListos.dequeue();
            this.posicionCabezal = p.getPosicionBloque();
            return p;
        }

        // 2. Para SSTF, SCAN y C-SCAN: Necesitamos ver toda la cola.
        // Como no podemos usar ArrayList, vaciamos la cola en un arreglo primitivo temporal.
        int cantidad = 0;
        Cola<Proceso> colaTemporal = new Cola<>();
        
        while (!colaListos.isEmpty()) {
            colaTemporal.enqueue(colaListos.dequeue());
            cantidad++;
        }

        Proceso[] procesos = new Proceso[cantidad];
        for (int i = 0; i < cantidad; i++) {
            procesos[i] = colaTemporal.dequeue();
        }

        Proceso elegido = null;
        int indiceElegido = -1;

        // --- MATEMÁTICA DE LOS ALGORITMOS ---
        
        if (politicaActual.equals("SSTF")) {
            // SSTF: El más cercano sin importar la dirección
            int menorDistancia = Integer.MAX_VALUE;
            for (int i = 0; i < cantidad; i++) {
                int distancia = Math.abs(procesos[i].getPosicionBloque() - posicionCabezal);
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    elegido = procesos[i];
                    indiceElegido = i;
                }
            }
        } 
        else if (politicaActual.equals("SCAN")) {
            // SCAN (Ascensor): Sube hasta que no haya más, luego baja.
            elegido = buscarScan(procesos);
            if (elegido != null) {
                for(int i=0; i < cantidad; i++) { if(procesos[i] == elegido) { indiceElegido = i; break; } }
            }
        } 
        else if (politicaActual.equals("C-SCAN")) {
            // C-SCAN (Ascensor Circular): Sube hasta el final, y luego salta al bloque 0
            elegido = buscarCScan(procesos);
             if (elegido != null) {
                for(int i=0; i < cantidad; i++) { if(procesos[i] == elegido) { indiceElegido = i; break; } }
            }
        }

        // Sistema anti-fallos por si la lógica no encuentra candidato
        if (elegido == null) {
            elegido = procesos[0];
            indiceElegido = 0;
        }

        // 3. Reconstruimos la cola ORIGINAL para el siguiente ciclo, OMITIENDO al elegido
        for (int i = 0; i < cantidad; i++) {
            if (i != indiceElegido) {
                colaListos.enqueue(procesos[i]);
            }
        }

        // Actualizamos la posición física de la cabeza lectora
        this.posicionCabezal = elegido.getPosicionBloque();
        return elegido;
    }

    // --- MÉTODOS AUXILIARES MATEMÁTICOS ---

    private Proceso buscarScan(Proceso[] procesos) {
        Proceso candidato = null;
        int menorDistancia = Integer.MAX_VALUE;

        // Fase 1: Buscar el más cercano en la dirección actual
        for (Proceso p : procesos) {
            int pos = p.getPosicionBloque();
            if (moviendoArriba && pos >= posicionCabezal) {
                int dist = pos - posicionCabezal;
                if (dist < menorDistancia) { menorDistancia = dist; candidato = p; }
            } else if (!moviendoArriba && pos <= posicionCabezal) {
                int dist = posicionCabezal - pos;
                if (dist < menorDistancia) { menorDistancia = dist; candidato = p; }
            }
        }

        // Fase 2: Si no hay nadie más en esta dirección, invertimos y buscamos de nuevo
        if (candidato == null) {
            moviendoArriba = !moviendoArriba; // Cambiamos de ↑ a ↓ o viceversa
            menorDistancia = Integer.MAX_VALUE;
            
            for (Proceso p : procesos) {
                int pos = p.getPosicionBloque();
                if (moviendoArriba && pos >= posicionCabezal) {
                    int dist = pos - posicionCabezal;
                    if (dist < menorDistancia) { menorDistancia = dist; candidato = p; }
                } else if (!moviendoArriba && pos <= posicionCabezal) {
                    int dist = posicionCabezal - pos;
                    if (dist < menorDistancia) { menorDistancia = dist; candidato = p; }
                }
            }
        }
        return candidato;
    }

    private Proceso buscarCScan(Proceso[] procesos) {
        Proceso candidato = null;
        int menorDistancia = Integer.MAX_VALUE;

        // Fase 1: Solo miramos hacia adelante (moviendoArriba = true siempre en C-SCAN)
        for (Proceso p : procesos) {
            int pos = p.getPosicionBloque();
            if (pos >= posicionCabezal) {
                int dist = pos - posicionCabezal;
                if (dist < menorDistancia) {
                    menorDistancia = dist;
                    candidato = p;
                }
            }
        }

        // Fase 2: Si no hay nadie adelante, simulamos el salto circular al bloque 0
        // Buscamos el proceso que tenga la menor posición absoluta en el disco
        if (candidato == null) {
            int menorPosicion = Integer.MAX_VALUE;
            for (Proceso p : procesos) {
                int pos = p.getPosicionBloque();
                if (pos < menorPosicion) {
                    menorPosicion = pos;
                    candidato = p;
                }
            }
        }
        return candidato;
    }
}