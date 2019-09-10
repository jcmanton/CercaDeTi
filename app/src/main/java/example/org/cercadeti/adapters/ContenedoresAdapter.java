package example.org.cercadeti.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import example.org.cercadeti.R;

public class ContenedoresAdapter extends BaseAdapter {
    Context context;
    String contenedoresList[];
    String contenedoresTitle[];
    int iconosList[];
    LayoutInflater inflter;

    public ContenedoresAdapter(Context applicationContext, String[] contenedoresList, String[] contenedoresTitle, int[] iconosList) {
        this.context = context;
        this.contenedoresList = contenedoresList;
        this.contenedoresTitle = contenedoresTitle;
        this.iconosList = iconosList;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return contenedoresList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.contenedor_listview, null);
        TextView country = (TextView) view.findViewById(R.id.textView);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        country.setText(contenedoresTitle[i]);
        icon.setImageResource(iconosList[i]);
        return view;
    }
}