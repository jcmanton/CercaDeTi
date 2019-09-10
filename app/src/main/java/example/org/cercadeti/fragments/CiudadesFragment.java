package example.org.cercadeti.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Locale;


import example.org.cercadeti.adapters.AdaptadadorRV;
import example.org.cercadeti.R;

import static example.org.cercadeti.api.APICiudades.listaCiudades;


public class CiudadesFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdaptadadorRV adaptador;


    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.ciudades, contenedor, false);
        recyclerView = (RecyclerView) vista.findViewById(R.id.recycler_view);
        adaptador = new AdaptadadorRV(getContext(), listaCiudades);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = recyclerView.getChildAdapterPosition(v);
                String s = listaCiudades.get(pos).getNombre()+ " ("+String.format("%.1f", listaCiudades.get(pos).getTemperatura())+"ºC)";
                //Toast.makeText(getContext(), "Selección: " + pos + " - " + s, Toast.LENGTH_LONG).show();
                //geo:0,0?q=lat,lng(label)
                //String uri = String.format(Locale.getDefault(), "geo:0,0?q=%f,%f("+s+")", listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud());
                //String uri = String.format(Locale.getDefault(), "geo:%f,%f", listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud());
                //String uri = String.format(Locale.getDefault(), "geo:%f,%f", 39.494259, -0.374014);
                //39.494259,-0.374014
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"));
                //String uri = String.format(Locale.US, "geo:<%f>,<%f>?z=8&q=<%f>,<%f>("+s+")", listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud(),listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud());
                String uri = String.format(Locale.US, "geo:<%f>,<%f>?q=<%f>,<%f>("+s+")&z=8", listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud(),listaCiudades.get(pos).getLatitud(), listaCiudades.get(pos).getLongitud());
                Toast.makeText(getContext(), "uri: " + uri, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
        return vista;
    }
}
