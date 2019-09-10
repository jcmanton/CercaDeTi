package example.org.cercadeti.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import example.org.cercadeti.R;
import example.org.cercadeti.model.InfoCiudad;

public class AdaptadadorRV extends RecyclerView.Adapter<AdaptadadorRV.ViewHolder> {
    private LayoutInflater inflador;
    private List<InfoCiudad> lista;
    protected View.OnClickListener onClickListener;
    private Context context;

    public AdaptadadorRV(Context context, List<InfoCiudad> lista) {
        this.lista = lista;
        this.context = context;
        inflador = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflador.inflate(R.layout.ciudad_lista, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        holder.titulo.setText(lista.get(i).getNombre());
        int drawableResourceId = context.getResources().getIdentifier("i" + lista.get(i).getIcono_tiempo(), "drawable", context.getPackageName());
        holder.icon.setImageResource(drawableResourceId);
        holder.subtitutlo.setText(String.format("%.1f", lista.get(i).getDistancia()) + "KM" + " (" + String.format("%.1f", lista.get(i).getTemperatura()) + "ºC)");
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titulo, subtitutlo;
        public ImageView icon;


        ViewHolder(View itemView) {
            super(itemView);
            titulo = (TextView) itemView.findViewById(R.id.titulo);
            subtitutlo = (TextView) itemView.findViewById(R.id.subtitulo);
            icon = itemView.findViewById(R.id.icono);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
