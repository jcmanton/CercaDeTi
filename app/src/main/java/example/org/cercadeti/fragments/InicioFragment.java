package example.org.cercadeti.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import example.org.cercadeti.MainActivity;
import example.org.cercadeti.R;

public class InicioFragment extends Fragment {
    TextView infoGPS;
    int[] listIcons;


    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.activity_inicial, contenedor, false);
        View backgroundimage = vista.findViewById(R.id.layout_actividad_inicial);
        Drawable background = backgroundimage.getBackground();
        background.setAlpha(20);
        infoGPS = vista.findViewById(R.id.infoGPS);
        String estado = "";
        if (((MainActivity) getActivity()).isOnline()) {
            estado = "Internet: ok. ";
        } else {
            estado = "Internet: OFF. ";
        }
        if (((MainActivity) getActivity()).isGPSEnabled()) {
            estado = estado + "GPS: ok. ";
            if (((MainActivity) getActivity()).isValencia(null)) {
                estado = estado + "Valencia: ok. ";
            } else {
                estado = estado + "Valencia: NO. ";
            }
        } else {
            estado = estado + "GPS: OFF.";
            ((MainActivity) getActivity()).setPosCentroValencia();

        }
        infoGPS.setText(estado);
        return vista;
    }
}
