package example.org.cercadeti.model;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoGPS implements Parcelable {
    double latDestino = 0;
    double longDestino = 0;
    int distancia = 0;
    String direccion = "";
    int disponibles = 0;
    int libres = 0;
    int icon = 0;
    String title = "";
    String snippet = "";
    String avisos = "";
    String bbox = "";
    String url = "";

    public InfoGPS() {
    }

    protected InfoGPS(Parcel in) {
        latDestino = in.readInt();
        longDestino = in.readInt();
        distancia = in.readInt();
        direccion = in.readString();
        disponibles = in.readInt();
        libres = in.readInt();
        icon = in.readInt();
        title = in.readString();
        snippet = in.readString();
        avisos = in.readString();
        bbox = in.readString();
        url = in.readString();
    }

    public static final Creator<InfoGPS> CREATOR = new Creator<InfoGPS>() {
        @Override
        public InfoGPS createFromParcel(Parcel in) {
            return new InfoGPS(in);
        }

        @Override
        public InfoGPS[] newArray(int size) {
            return new InfoGPS[size];
        }
    };

    public double getLatDestino() {
        return latDestino;
    }

    public void setLatDestino(double latDestino) {
        this.latDestino = latDestino;
    }

    public double getLongDestino() {
        return longDestino;
    }

    public void setLongDestino(double longDestino) {
        this.longDestino = longDestino;
    }

    public int getDistancia() {
        return distancia;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }

    public int getLibres() {
        return libres;
    }

    public void setLibres(int libres) {
        this.libres = libres;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getAvisos() {
        return avisos;
    }

    public void setAvisos(String avisos) {
        this.avisos = avisos;
    }

    public String getBbox() {
        return bbox;
    }

    public void setBbox(String bbox) {
        this.bbox = bbox;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latDestino);
        dest.writeDouble(longDestino);
        dest.writeInt(distancia);
        dest.writeString(direccion);
        dest.writeInt(disponibles);
        dest.writeInt(libres);
        dest.writeInt(icon);
        dest.writeString(title);
        dest.writeString(snippet);
        dest.writeString(avisos);
        dest.writeString(bbox);
        dest.writeString(url);
    }
}
