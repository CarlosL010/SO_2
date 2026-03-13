package estructuras;

public class NodoArbol {
    private String nombre;
    private boolean esArchivo; // false = Directorio, true = Archivo
    private String propietario; // "Administrador" o "Usuario"
    private int tamanoEnBloques;
    private int primerBloqueAsignado; // Dónde empieza en el disco (apunta al ID de un Bloque)
    
    // ¡La magia para no usar ArrayList!
    private ListaEnlazada<NodoArbol> hijos; 

    // Constructor para Directorios
    public NodoArbol(String nombre, String propietario) {
        this.nombre = nombre;
        this.esArchivo = false;
        this.propietario = propietario;
        this.tamanoEnBloques = 0;
        this.primerBloqueAsignado = -1; // Los directorios no ocupan bloques en este simulador
        this.hijos = new ListaEnlazada<>();
    }

    // Constructor para Archivos
    public NodoArbol(String nombre, String propietario, int tamanoEnBloques, int primerBloqueAsignado) {
        this.nombre = nombre;
        this.esArchivo = true;
        this.propietario = propietario;
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloqueAsignado = primerBloqueAsignado;
        this.hijos = null; // Los archivos no tienen hijos
    }

    // --- Métodos Útiles ---
    public void agregarHijo(NodoArbol hijo) {
        if (!this.esArchivo) {
            this.hijos.add(hijo);
        } else {
            System.out.println("Error: Un archivo no puede tener hijos.");
        }
    }

    // --- Getters ---
    public String getNombre() { return nombre; }
    public boolean isEsArchivo() { return esArchivo; }
    public String getPropietario() { return propietario; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloqueAsignado() { return primerBloqueAsignado; }
    public ListaEnlazada<NodoArbol> getHijos() { return hijos; }

    // Este método es crucial para que el JTree de Java muestre el nombre del archivo y no un código raro
    @Override
    public String toString() {
        return nombre;
    }
}