package example.org.cercadeti.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import example.org.cercadeti.adapters.CustomInfoWindowGoogleMap;
import example.org.cercadeti.model.InfoWindowData;
import example.org.cercadeti.MainActivity;
import example.org.cercadeti.gps.Posicion;
import example.org.cercadeti.R;
import example.org.cercadeti.view.WebViewActivity;

import static example.org.cercadeti.api.APIAyuntamiento.infoGPS;

public class MapFragment extends Fragment {
    double latitud, longitud;
    LatLng latLngTouch;
    TextView tv;
    DialogInterface.OnClickListener dialogClickListener;
    static final String LATITUDE_PATTERN = "^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$";
    static final String LONGITUDE_PATTERN = "^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))$";
    //static final String POLIGONO_COORDENADAS = "^(\\()((([-+]?)([\\d]{1,2})(((\\.)(\\d+)))(\\s*)(([-+]?)([\\d]{1,3})((\\.)(\\d+))+))(,)?)+(\\))$";
    static final String POLIGONO_COORDENADAS = "(\\()((([-+]?)([\\d]{1,2})(((\\.)(\\d+)))(\\s*)(([-+]?)([\\d]{1,3})((\\.)(\\d+))+))(,)?)+(\\))";
    static final String LONG_LAT = "((([-+]?)([\\d]{1,2})(((\\.)(\\d+)))(\\s*)(([-+]?)([\\d]{1,3})((\\.)(\\d+))+)))+";

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            latitud = getArguments().getDouble("latitud");
            longitud = getArguments().getDouble("longitud");
        }
        latLngTouch = ((MainActivity) getActivity()).getPosicionManual();
        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(getContext(), "Se utilizará la ubicación seleccionada\n" + latLngTouch.latitude + " : " +
                                latLngTouch.longitude, Toast.LENGTH_LONG).show();
                        ((MainActivity) getActivity()).setPosicion((latLngTouch), true);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getContext(), "Se utilizará la posición GPS\n", Toast.LENGTH_LONG).show();
                        //Posicion.setLocalizacionManual(null);
                        ((MainActivity) getActivity()).resetPosicion();
                        break;
                }
                ((MainActivity) getActivity()).cargaDatosGPS();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        tv = rootView.findViewById(R.id.tv_avisos);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.clear(); //clear old markers
                mMap.setMyLocationEnabled(true);
                //dibuja cículo
                /*
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(MainActivity.PosCentroValencia);
                circleOptions.radius(5000);
                circleOptions.strokeColor(Color.BLACK);
                circleOptions.fillColor(0x30ff0000);
                circleOptions.strokeWidth(2);
                mMap.addCircle(circleOptions);
                */
                final HashMap<String, String> markerMap = new HashMap<String, String>();
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        latLngTouch = latLng;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder
                                .setMessage("Utilizar posición manual o GPS")
                                .setPositiveButton("Manual", dialogClickListener)
                                .setNegativeButton("GPS", dialogClickListener).show()
                                .setTitle("Cambio de posición");
                    }
                });
                /*
                //add location button click listener
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        if (latLngTouch == null) {
                            return false;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder
                                .setMessage("¿Qué posición quieres utilizar?").setPositiveButton("Manual", dialogClickListener)
                                .setNegativeButton("GPS", dialogClickListener).show()
                                .setTitle("Cambio de posición");
                        return false;
                    }
                });
                */
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String url = markerMap.get(marker.getId());
                        if (!url.equals("")) {
                            Intent i = new Intent(getContext(), WebViewActivity.class);
                            i.putExtra("url", url);
                            startActivity(i);
                            /*
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);*/

                        }
                    }
                });

                int iconoPosicion = (latLngTouch == null) ? R.drawable.posicion : R.drawable.posicion_r;
                if (infoGPS == null || infoGPS.length == 0) {

                    CameraPosition googlePlex = CameraPosition.builder()
                            .target(new LatLng(latitud, longitud))
                            .zoom(17)
                            .bearing(0)
                            .tilt(45)
                            .build();

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitud, longitud))
                            .icon(BitmapDescriptorFactory.fromResource(iconoPosicion))
                            .title("Pos. Actual"));

                    /*
                    posicion = new LatLng(latitud, longitud);
                    mMap.addMarker(new MarkerOptions().position(posicion).title("Posición"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    */
                } else {
                    LatLng posicion;
                    posicion = new LatLng(latitud, longitud);
                    mMap.addMarker(new MarkerOptions()
                            .position(posicion)
                            .icon(BitmapDescriptorFactory.fromResource(iconoPosicion))
                            .title("Pos. Actual"));
                    int mZoom = 17;
                    if (!infoGPS[0].getBbox().equals("")) {
                        mZoom = 12;
                    }
                    CameraPosition googlePlex = CameraPosition.builder()
                            .target(new LatLng(latitud, longitud))
                            .zoom(mZoom)
                            .bearing(0)
                            .tilt(45)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
                    if (infoGPS.length == 0) {
                        Toast.makeText(getContext(), "Sin datos para tu ubicación", Toast.LENGTH_LONG).show();
                    }
                    if (infoGPS[0].getBbox().equals("")) {
                        for (int i = 0; i < infoGPS.length; i++) {
                            if (infoGPS[i].getLatDestino() + infoGPS[i].getLongDestino() > 0) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions
                                        .position(new LatLng(infoGPS[i].getLatDestino(), infoGPS[i].getLongDestino()))
                                        .icon(BitmapDescriptorFactory.fromResource(infoGPS[i].getIcon()))
                                        .title(infoGPS[i].getTitle())
                                        .snippet(infoGPS[i].getSnippet());
                                InfoWindowData info = new InfoWindowData();
                                info.setImage(getResources().getResourceEntryName(infoGPS[i].getIcon()));
                                String laURL = infoGPS[i].getUrl().equals("") ? "" : getContext().getResources().getString(R.string.prox_salidas);
                                info.setUrl(laURL);
                                info.setDireccion(infoGPS[i].getDireccion());

                                CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(getContext());
                                mMap.setInfoWindowAdapter(customInfoWindow);
                                Marker m = mMap.addMarker(markerOptions);
                                m.setTag(info);
                                if (i == 0) {
                                    m.showInfoWindow();
                                }
                                markerMap.put(m.getId(), infoGPS[i].getUrl());
                            }
                            tv.setText(infoGPS[0].getAvisos());
                        }
                    } else {
                        String msj = "";
                        for (int j = 0; j < infoGPS.length; j++) {
                            ArrayList<ArrayList<LatLng>> grupoCoordenadas = getCoordenadas(infoGPS[j].getBbox());
                            for (int i = 0; i < grupoCoordenadas.size(); i++) {
                                PolygonOptions rectOptions = new PolygonOptions();
                                rectOptions
                                        .addAll(grupoCoordenadas.get(i))
                                        .strokeColor(Color.BLUE)
                                        .fillColor(0x3FFF0000);
                                mMap.addPolygon(rectOptions);
                                msj = msj + " | " + infoGPS[j].getAvisos();
                            }
                            tv.setText(msj);
                        }
                    }
                    /*

                    for (int i = 0; i < infoGPS.length; i++) {
                        posicion = new LatLng(infoGPS[i].getLatDestino(), infoGPS[i].getLongDestino());
                        String titulo = infoGPS[i].getDireccion() + "(" + infoGPS[i].getLibres() + ")";
                        mMap.addMarker(new MarkerOptions().position(posicion).title(titulo));

                    }
                    posicion = new LatLng(39.454219, -0.356711);
                    mMap.addMarker(new MarkerOptions().position(posicion).title("Posición"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    */

                }
            }
        });


        return rootView;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private ArrayList<ArrayList<LatLng>> getCoordenadas(String coordenadas) {
//POLYGON((-0.378372182104639 39.4624357738595,
        // -0.378372182104639 39.4697773968059,
        // -0.370447355947 39.4697773968059,
        // -0.370447355947 39.4624357738595,
        // -0.378372182104639 39.4624357738595))[,POLYGON(...)]"}]
        Pattern p = Pattern.compile(POLIGONO_COORDENADAS);
        Matcher matcher = p.matcher(coordenadas);
        ArrayList<String> coincidencias = new ArrayList<String>();
        while (matcher.find()) {
            coincidencias.add(matcher.group());
        }
        ArrayList<ArrayList<LatLng>> grupoCoordenadas = new ArrayList<ArrayList<LatLng>>();
        p = Pattern.compile(LONG_LAT);
        for (int j = 0; j < coincidencias.size(); j++) {
            //String[] puntos = coincidencias.get(j).split(LONG_LAT);
            matcher = p.matcher(coincidencias.get(j));
            ArrayList<String> puntos = new ArrayList<>();
            while (matcher.find()) {
                puntos.add(matcher.group());
            }
            ArrayList<LatLng> listaCoordenadas = new ArrayList<LatLng>();
            for (int i = 0; i < puntos.size(); i++) {
                String[] latLong = puntos.get(i).split("\\s");
                listaCoordenadas.add(new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0])));
            }
            grupoCoordenadas.add(listaCoordenadas);
        }
        return grupoCoordenadas;

    }
}
