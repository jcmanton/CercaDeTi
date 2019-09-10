package example.org.cercadeti.model;

public class InfoCiudad {
    int id = 0;
    String nombre;
    String pais;
    double temperatura;
    String icono_tiempo;
    boolean ready = false;
    double distancia = 0;
    double latitud = 0;
    double longitud = 0;

    public InfoCiudad() {
        //Genera objeto en blanco
    }

    public InfoCiudad(String nombre, String pais, double temperatura, String icono_tiempo) {
        this.nombre = nombre;
        this.pais = pais;
        this.temperatura = temperatura;
        this.icono_tiempo = icono_tiempo;
        this.ready = true;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public String getIcono_tiempo() {
        return icono_tiempo;
    }

    public void setIcono_tiempo(String icono_tiempo) {
        this.icono_tiempo = icono_tiempo;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double logitud) {
        this.longitud = logitud;
    }


    @Override
    public String toString() {
        return nombre + "\n" + pais + "\n" + temperatura + "\n" + icono_tiempo;
    }
}