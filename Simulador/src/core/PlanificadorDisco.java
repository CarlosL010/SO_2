/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;
import estructuras.Cola;
import modelos.Proceso;

        
/**
 *
 * @author pinto
 */
public class PlanificadorDisco {
    private int posicionCabezalActual;
    private String politicaActual; // "FIFO", "SSTF", "SCAN", "C-SCAN"
    private boolean direccionArriba;

    public PlanificadorDisco(int posicionInicial) {
        this.posicionCabezalActual = posicionInicial;
        this.politicaActual = "FIFO"; // Por defecto
        this.direccionArriba= true;
    }

    public void setPolitica(String politica) {
        this.politicaActual = politica;
    }

    public int getPosicionCabezal() {
        return posicionCabezalActual;
    }

    // Método principal que será llamado por el hilo del sistema
    public void procesarCola(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return;

        switch (politicaActual) {
            case "FIFO":
                ejecutarFIFO(colaListos);
                break;
            case "SSTF":
                ejecutarSSTF(colaListos);
                break;
            case "SCAN":
                ejecutarSCAN(colaListos);
                break;
            case "C-SCAN":
                ejecutarCSCAN(colaListos);
                break;
        }
    }

    private void ejecutarFIFO(Cola<Proceso> colaListos) {
        // La lógica de FIFO es simplemente sacar el primero de la cola
        Proceso p = colaListos.dequeue();
        if (p != null) {
            p.setEstado("Ejecutando");
            // Aquí simularíamos el movimiento del cabezal...
            // posicionCabezalActual = nuevaPosicion;
            p.setEstado("Terminado");
        }
    }

    private void ejecutarSSTF(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return;
        
        Proceso procesoMasCercano = null;
    int menorDistancia = Integer.MAX_VALUE;
    Cola<Proceso> colaTemporal = new Cola<>();

    // 1. Recorremos toda la cola para encontrar el más cercano
    while (!colaListos.isEmpty()) {
        Proceso procesoActual = colaListos.dequeue();
        
        // Calculamos la distancia absoluta: | posicion actual - posicion destino |
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

    // 3. Ejecutamos el proceso seleccionado
    if (procesoMasCercano != null) {
        procesoMasCercano.setEstado("Ejecutando");
        
        // ¡El cabezal se mueve a la nueva posición!
        this.posicionCabezalActual = procesoMasCercano.getPosicionBloque();
        
        System.out.println("Ejecutando SSTF: Proceso " + procesoMasCercano.getId() + 
                           " movió el cabezal a la posición " + this.posicionCabezalActual);
                           
        procesoMasCercano.setEstado("Terminado");
    }
    }

    private void ejecutarSCAN(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return;

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
            ejecutarSCAN(colaListos); // Llamada recursiva con la nueva dirección
            return;
        }

        // 3. Devolvemos los "perdedores" a la cola principal
        while (!colaTemporal.isEmpty()) {
            colaListos.enqueue(colaTemporal.dequeue());
        }

        // 4. Ejecutamos el ganador
        mejorCandidato.setEstado("Ejecutando");
        this.posicionCabezalActual = mejorCandidato.getPosicionBloque();
        System.out.println("SCAN (" + (direccionArriba ? "↑" : "↓") + "): Cabezal movido a la posición " + this.posicionCabezalActual);
        mejorCandidato.setEstado("Terminado");
    }

    private void ejecutarCSCAN(Cola<Proceso> colaListos) {
        if (colaListos.isEmpty()) return;

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
            System.out.println("C-SCAN: Límite alcanzado, cabezal salta a la posición 0");
            this.posicionCabezalActual = 0; // El "salto circular"
            
            while (!colaTemporal.isEmpty()) {
                colaListos.enqueue(colaTemporal.dequeue());
            }
            ejecutarCSCAN(colaListos); // Volvemos a buscar pero ahora desde 0
            return;
        }

        // 3. Devolvemos a los no elegidos a la cola
        while (!colaTemporal.isEmpty()) {
            colaListos.enqueue(colaTemporal.dequeue());
        }

        // 4. Ejecutamos el ganador
        mejorCandidato.setEstado("Ejecutando");
        this.posicionCabezalActual = mejorCandidato.getPosicionBloque();
        System.out.println("C-SCAN (↑): Cabezal movido a la posición " + this.posicionCabezalActual);
        mejorCandidato.setEstado("Terminado");
    }
}
