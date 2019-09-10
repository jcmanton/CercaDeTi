package example.org.cercadeti.gps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import example.org.cercadeti.MainActivity;
import example.org.cercadeti.R;

import static android.content.Context.LOCATION_SERVICE;

public class Posicion implements LocationListener {
    private LocationManager manejador;
    private Location mejorLocaliz;
    private static Location localizacionManual;
    private String proveedor;
    private static final long TIEMPO_MIN = 5000;//10 * 1000; // 10 segundos
    private static final long DISTANCIA_MIN =150;// 100;
    private static final long T_ACTUALIZACION =0;// 5 * 1000;
    private static Context context;
    private boolean pteActualizar = true;
    private static boolean preguntarGPS = true;
    private static Posicion ourInstance;
    private MainActivity activity;
    private long ultimaActualizacion;

    public static Posicion getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new Posicion(context);
        }
        return ourInstance;
    }

    private Posicion(Context context) {
        this.context = context;
        manejador = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        proveedor = manejador.getBestProvider(criterio, true);
        activarProveedores();
        ultimaLocalizacion();
        if (proveedor != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
            }
            ultimaLocalizacion();
        }

        activity = (MainActivity) context;
        ultimaActualizacion = System.currentTimeMillis();
    }

    //*** Activación de proveedores y gestión de permisos
    public void activarProveedores() {
        Log.d("***activarProveedores ", "activarProveedores");
        if (manejador==null || proveedor==null) return;
        if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //manejador.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIEMPO_MIN, DISTANCIA_MIN, this);
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
            Log.d("***activarProveedores ", "GPS");
        }
        if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //manejador.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIEMPO_MIN, DISTANCIA_MIN, this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("***onLocation ", "Nueva localización: " + location);
/*
        if (System.currentTimeMillis() - ultimaActualizacion > 5000) {
            if (!activity.isPosicionManual()) {
                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                double distancia = getDistanceBetween(getMejorLocalizLatLng(), latlng);
                if (distancia > 300) {
                    localizacionManual = null;
                    actualizaMejorLocaliz(location);
                    activity.onPosicionChanged();
                }
            }
            ultimaActualizacion = System.currentTimeMillis();
            return;
        }
        */
        //actualizaMejorLocaliz(location);

        activity.compruebaPosicionInicial(location);
        actualizaMejorLocaliz(location);
        activity.cargaDatosGPS();

        //tv_localizacion.setText("Loc: lat " + String.format("%.4f", mejorLocaliz.getLatitude()) + ", long " + String.format("%.4f", mejorLocaliz.getLongitude()));
    }

    @Override
    public void onProviderDisabled(String proveedor) {
        Log.d("***onProviderDisabled", " habilitado: " + proveedor);
        activarProveedores();
    }

    @Override
    public void onProviderEnabled(String proveedor) {
        Log.d("***onProviderEnabled", " habilitado: " + proveedor);
        activarProveedores();
    }

    @Override
    public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        Log.d("***onStatusChanged", "Cambia estado: " + proveedor);
        activarProveedores();
    }


    public void ultimaLocalizacion() {
        Log.d("***ultimaLocalizacion: ", "ultimaLocalizacion");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                actualizaMejorLocaliz(manejador.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            }
        }
    }

    public LatLng ultimaLocalizacionGPS() {
        Location location = null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = manejador.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = manejador.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if (location == null) return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void actualizaMejorLocaliz(Location localiz) {
        //Log.d("***", "" + localiz.getLatitude());

        if (localizacionManual != null) {
            //Se utiliza la localización seleccionada manualmente
            mejorLocaliz = localizacionManual;
            return;
        }
        if (localiz != null && (mejorLocaliz == null || localiz.getAccuracy() < 2 * mejorLocaliz.getAccuracy() || localiz.getTime() - mejorLocaliz.getTime() > T_ACTUALIZACION)) {
            //Log.d(Lugares.TAG, "Nueva mejor localización");
            mejorLocaliz = localiz;
            //consultaTiempo(null);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //manejador.removeUpdates(this);
            }
        }
    }

    public void removeUpdates() {
        Log.d("***removeUpdates: ", "");
        manejador.removeUpdates(this);
    }

    public void updatePosicion() {
        if (mejorLocaliz == null) {
            ultimaLocalizacion();
            activarProveedores();
        }
    }

    public Location getMejorLocaliz() {
        if (localizacionManual != null) {
            mejorLocaliz = localizacionManual;
        }
        return mejorLocaliz;
    }

    public LatLng getMejorLocalizLatLng() {
        return new LatLng(getMejorLocaliz().getLatitude(), getMejorLocaliz().getLongitude());
    }

    public void setMejorLocaliz(Location mejorLocaliz) {
        this.mejorLocaliz = mejorLocaliz;
    }

    public static LatLng getLocalizacionManual() {
        if (localizacionManual == null) return null;
        return new LatLng(localizacionManual.getLatitude(), localizacionManual.getLongitude());
    }

    public static void setLocalizacionManual(LatLng localizacionManual) {
        if (localizacionManual == null) {
            Posicion.localizacionManual = null;
            return;
        }
        Posicion.localizacionManual = new Location("");
        Posicion.localizacionManual.setLatitude(localizacionManual.latitude);
        Posicion.localizacionManual.setLongitude(localizacionManual.longitude);
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1, p0 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
            double distancia;
            int menor = 0;
            p0 = new LatLng(getMejorLocaliz().getLatitude(), getMejorLocaliz().getLongitude());
            distancia = getDistanceBetween(p0, p1);
            for (int i = 1; i < address.size(); i++) {
                location = address.get(i);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
                if (distancia > getDistanceBetween(p0, p1)) {
                    distancia = getDistanceBetween(p0, p1);
                    menor = i;
                }
            }
            location = address.get(menor);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
            return p1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Double getDistanceBetween(LatLng latLon1, LatLng latLon2) {
        if (latLon1 == null || latLon2 == null)
            return null;
        float[] result = new float[1];
        Location.distanceBetween(latLon1.latitude, latLon1.longitude,
                latLon2.latitude, latLon2.longitude, result);
        return (double) result[0];
    }

    public boolean isGPSEnable() {
        if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(context, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            showGPSDisabledAlertToUser();
            return false;
        }
    }

    private void showGPSDisabledAlertToUser() {
        if (!preguntarGPS) return;
        View checkBoxView = View.inflate(context, R.layout.checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    preguntarGPS = false;
                }
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.msg_GPS_OFF))
                .setCancelable(false)
                .setView(checkBoxView)
                .setPositiveButton(context.getResources().getString(R.string.msg_irGPS),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.bot_cancelar),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
