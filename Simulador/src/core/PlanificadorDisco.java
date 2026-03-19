package core;

import estructuras.Cola;
import modelos.Proceso;

/**
 *
 * @author pinto (Actualizado para integración completa)
 */
public class PlanificadorDisco {
    private int posicionCabezalActual;
    private String politicaActual; // "FIFO", "SSTF", "SCAN", "C-SCAN"
    private boolean direccionArriba;

    public PlanificadorDisco(int posicionInicial) {
        this.posicionCabezalActual = posicionInicial;
        this.politicaActual = "FIFO"; // Por defecto
        this.direccionArriba = true;
    }

    public void setPolitica(String politica) {
        this.politicaActual = politica;
    }

    public int getPosicionCabezal() {
        return posicionCabezalActual;
    }

    // --- MÉTODO PRINCIPAL ---
    // Ahora devuelve el Proceso que fue seleccionado para que el Gestor lo ejecute
    public Proceso procesarCola(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return null;

        switch (politicaActual) {
            case "FIFO":
                return ejecutarFIFO(colaListos);
            case "SSTF":
                return ejecutarSSTF(colaListos);
            case "SCAN":
                return ejecutarSCAN(colaListos);
            case "C-SCAN":
                return ejecutarCSCAN(colaListos);
            default:
                return null;
        }
    }

    // --- ALGORITMOS DE PLANIFICACIÓN ---

    private Proceso ejecutarFIFO(Cola<Proceso> colaListos) {
        // La lógica de FIFO es simplemente sacar el primero de la cola
        Proceso p = colaListos.dequeue();
        if (p != null) {
            this.posicionCabezalActual = p.getPosicionBloque();
            System.out.println("Planificador (FIFO): Cabezal movido a " + this.posicionCabezalActual);
        }
        return p;
    }

    private Proceso ejecutarSSTF(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return null;
        
        Proceso procesoMasCercano = null;
        int menorDistancia = Integer.MAX_VALUE;
        Cola<Proceso> colaTemporal = new Cola<>();

        // 1. Recorremos toda la cola para encontrar el más cercano
        while (!colaListos.isEmpty()) {
            Proceso procesoActual = colaListos.dequeue();
            int distancia = Math.abs(this.posicionCabezalActual - procesoActual.getPosicionBloque());

            if (distancia < menorDistancia) {
                // Si ya teníamos un candidato anterior, lo mandamos a la cola temporal
                if (procesoMasCercano != null) {
                    colaTemporal.enqueue(procesoMasCercano);
                }
                // Actualizamos nuestro nuevo mejor candidato
                procesoMasCercano = procesoActual;
                menorDistancia = distancia;
            } else {
                // No es el más cercano, lo guardamos para no perderlo
                colaTemporal.enqueue(procesoActual);
            }
        }

        // 2. Devolvemos los procesos no seleccionados a la cola original
        while (!colaTemporal.isEmpty()) {
            colaListos.enqueue(colaTemporal.dequeue());
        }

        // 3. Devolvemos el ganador al Gestor
        if (procesoMasCercano != null) {
            this.posicionCabezalActual = procesoMasCercano.getPosicionBloque();
            System.out.println("Planificador (SSTF): Cabezal movido a " + this.posicionCabezalActual);
        }
        return procesoMasCercano;
    }

    private Proceso ejecutarSCAN(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return null;

        Proceso mejorCandidato = null;
        int menorDistancia = Integer.MAX_VALUE;
        Cola<Proceso> colaTemporal = new Cola<>();

        // 1. Buscamos el más cercano en la dirección actual
        while (!colaListos.isEmpty()) {
            Proceso p = colaListos.dequeue();
            int pos = p.getPosicionBloque();

            // Verificamos si el proceso está en nuestra ruta
            boolean esCandidatoValido = (direccionArriba && pos >= posicionCabezalActual) ||
                                        (!direccionArriba && pos <= posicionCabezalActual);

            if (esCandidatoValido) {
                int distancia = Math.abs(posicionCabezalActual - pos);
                if (distancia < menorDistancia) {
                    if (mejorCandidato != null) colaTemporal.enqueue(mejorCandidato);
                    mejorCandidato = p;
                    menorDistancia = distancia;
                } else {
                    colaTemporal.enqueue(p);
                }
            } else {
                colaTemporal.enqueue(p); // No está en la ruta actual, a la cola temporal
            }
        }

        // 2. Si no encontramos nada en esta dirección, invertimos y volvemos a intentar
        if (mejorCandidato == null) {
            direccionArriba = !direccionArriba; // Cambiamos de ↑ a ↓ o viceversa
            while (!colaTemporal.isEmpty()) {
                colaListos.enqueue(colaTemporal.dequeue()); // Devolvemos todo
            }
            return ejecutarSCAN(colaListos); // Llamada recursiva con la nueva dirección
        }

        // 3. Devolvemos los "perdedores" a la cola principal
        while (!colaTemporal.isEmpty()) {
            colaListos.enqueue(colaTemporal.dequeue());
        }

        // 4. Retornamos el ganador
        this.posicionCabezalActual = mejorCandidato.getPosicionBloque();
        System.out.println("Planificador SCAN (" + (direccionArriba ? "↑" : "↓") + "): Cabezal movido a " + this.posicionCabezalActual);
        return mejorCandidato;
    }

    private Proceso ejecutarCSCAN(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return null;

        Proceso mejorCandidato = null;
        int menorDistancia = Integer.MAX_VALUE;
        Cola<Proceso> colaTemporal = new Cola<>();

        // 1. En C-SCAN SIEMPRE buscamos hacia arriba (posiciones mayores)
        while (!colaListos.isEmpty()) {
            Proceso p = colaListos.dequeue();
            int pos = p.getPosicionBloque();

            if (pos >= posicionCabezalActual) {
                int distancia = pos - posicionCabezalActual;
                if (distancia < menorDistancia) {
                    if (mejorCandidato != null) colaTemporal.enqueue(mejorCandidato);
                    mejorCandidato = p;
                    menorDistancia = distancia;
                } else {
                    colaTemporal.enqueue(p);
                }
            } else {
                colaTemporal.enqueue(p); // Es menor, lo ignoramos por ahora
            }
        }

        // 2. Si no hay más procesos hacia arriba, saltamos al inicio del disco (posición 0)
        if (mejorCandidato == null) {
            System.out.println("Planificador C-SCAN: Límite alcanzado, cabezal salta a la posición 0");
            this.posicionCabezalActual = 0; // El "salto circular"
            
            while (!colaTemporal.isEmpty()) {
                colaListos.enqueue(colaTemporal.dequeue());
            }
            return ejecutarCSCAN(colaListos); // Volvemos a buscar pero ahora desde 0
        }

        // 3. Devolvemos a los no elegidos a la cola
        while (!colaTemporal.isEmpty()) {
            colaListos.enqueue(colaTemporal.dequeue());
        }

        // 4. Retornamos el ganador
        this.posicionCabezalActual = mejorCandidato.getPosicionBloque();
        System.out.println("Planificador C-SCAN (↑): Cabezal movido a " + this.posicionCabezalActual);
        return mejorCandidato;
    }
}