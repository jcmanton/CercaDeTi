package example.org.cercadeti.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import example.org.cercadeti.api.APIAyuntamiento;
import example.org.cercadeti.adapters.ContenedoresAdapter;
import example.org.cercadeti.MainActivity;
import example.org.cercadeti.R;

public class ContenedoresFragment extends Fragment {
    ListView listView;
    String[] listItem;
    String[] listTitles;
    int[] listIcons;


    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.contenedores, contenedor, false);
        listView = vista.findViewById(R.id.lv_contenedores);
        listItem = getResources().getStringArray(R.array.array_contenedor);
        listTitles = getResources().getStringArray(R.array.array_titContenedor);
        listIcons = new int[listItem.length];
        for (int i = 0; i < listIcons.length; i++) {
            listIcons[i] = getContext().getResources().getIdentifier(listItem[i] + "menu", "drawable", getContext().getPackageName());
        }
        ContenedoresAdapter contenedoresAdapter = new ContenedoresAdapter(getContext(), listItem, listTitles, listIcons);
        listView.setAdapter(contenedoresAdapter);
        /*
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, listItem);
        listView.setAdapter(adapter);
        */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                String value = listItem[position];
                Toast.makeText(getContext(), value, Toast.LENGTH_SHORT).show();
                APIAyuntamiento apiAyuntamiento = new APIAyuntamiento((MainActivity) getActivity());
                try {
                    ((MainActivity) getActivity()).setClaseConsulta(value);
                    apiAyuntamiento.cargaDatosGPS(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return vista;
    }
}
