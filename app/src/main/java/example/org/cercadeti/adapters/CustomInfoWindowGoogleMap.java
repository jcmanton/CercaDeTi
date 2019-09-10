package example.org.cercadeti.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import example.org.cercadeti.R;
import example.org.cercadeti.model.InfoWindowData;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.map_infowindow, null);

        TextView titulo_tv = view.findViewById(R.id.titulo_marker);
        TextView detalles_tv = view.findViewById(R.id.detalles);
        ImageView img = view.findViewById(R.id.pic);
        ImageView imgWWW = view.findViewById(R.id.imgInternet);

        TextView url_tv = view.findViewById(R.id.url);
        TextView direccion_tv = view.findViewById(R.id.direccion);

        titulo_tv.setText(marker.getTitle());
        detalles_tv.setText(marker.getSnippet());

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        int imageId = context.getResources().getIdentifier(infoWindowData.getImage().toLowerCase(), "drawable", context.getPackageName());
        img.setImageResource(imageId);

        url_tv.setText(infoWindowData.getUrl());
        direccion_tv.setText(infoWindowData.getDireccion());
        if (url_tv.getText().equals("")) {
            imgWWW.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}