package example.org.cercadeti.api;

import android.location.Location;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.org.cercadeti.MainActivity;
import example.org.cercadeti.gps.Posicion;
import example.org.cercadeti.R;
import example.org.cercadeti.fragments.CiudadesFragment;
import example.org.cercadeti.model.InfoCiudad;

public class APICiudades {

    public static List<InfoCiudad> listaCiudades = new ArrayList<InfoCiudad>();
    // private LatLng posicionActual = new LatLng(39.481106, -0.340987);
    MainActivity activity;
    private Location mejorLocaliz;
    private RequestQueue colaPeticiones;
    private DatosPeticion[] listaPeticiones;
    private static final int NUM_CIUDADES = 6;
    private boolean cargandoCiudades = false;
    private Posicion posicion;

    public APICiudades(MainActivity activity) {
        posicion = Posicion.getInstance(null);
        this.activity=activity;
        colaPeticiones = Volley.newRequestQueue(activity);
        listaPeticiones = new DatosPeticion[NUM_CIUDADES];
        mejorLocaliz = posicion.getMejorLocaliz();
    }

    public void consultaTiempo(View v) {
        if (!cargandoCiudades) {
            try {
                cargandoCiudades = true;
                listaCiudades = new ArrayList<InfoCiudad>();
                InfoCiudad ciudad = new InfoCiudad();
                ciudad.setDistancia(0);
                ciudad.setLongitud(mejorLocaliz.getLongitude());
                ciudad.setLatitud(mejorLocaliz.getLatitude());
                listaCiudades.add(ciudad);
                ciudadesCercanas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void completaTiempo() {
        //Completa datos de ciudades cercanas
        listaPeticiones[0] = new DatosPeticion();
        listaPeticiones[0].setLatitud(mejorLocaliz.getLatitude());
        listaPeticiones[0].setLongitud(mejorLocaliz.getLongitude());
        for (int i = 1; i < NUM_CIUDADES; i++) {
            listaPeticiones[i] = new DatosPeticion();
            listaPeticiones[i].setLatitud(listaCiudades.get(i).getLatitud());
            listaPeticiones[i].setLongitud(listaCiudades.get(i).getLongitud());
        }
        for (int i = 0; i < NUM_CIUDADES; i++) {
            try {
                consultaTiempo(i, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mostrarCiudades(View view) {
        //activity.addFragment();
    }

    void consultaTiempo(final int npeticion, boolean esGrupo) throws Exception {
        //http://data.fixer.io/api/latest?access_key=23d8f7http://data.fixer.io/api/latest?access_key=23d8f74b03abd0cedc5262b9d2fd8c5c&symbols=USD,AUD,CAD,PLN,MXN&format=14b03abd0cedc5262b9d2fd8c5c&symbols=USD,AUD,CAD,PLN,MXN&format=1

        String cadConexion = "";

        cadConexion = "http://api.openweathermap.org/data/2.5/weather?lat=" + listaPeticiones[npeticion].getLatitud() + "&lon=" + listaPeticiones[npeticion].getLongitud() + "&units=metric&APPID=" + activity.getString(R.string.openWeatherID);
        Log.d("***cadena: ", "- " + cadConexion);
        StringRequest peticion = new StringRequest(Request.Method.GET, cadConexion,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        Log.d("***onResponse: ", respuesta);
                        String string = "";
                        try {
                            JSONObject json_obj = new JSONObject(respuesta);
                            if (json_obj.getString("cod").equals("200")) {
                                JSONArray json_weather = json_obj.getJSONArray("weather");
                                JSONObject json_main = json_obj.getJSONObject("main");
                                JSONObject json_sys = json_obj.getJSONObject("sys");
                                JSONObject json_coord = json_obj.getJSONObject("coord");
                                //Almacena la información de la ciudad que tenemos
                                listaCiudades.get(npeticion).setNombre(json_obj.getString("name"));
                                listaCiudades.get(npeticion).setPais(json_sys.getString("country"));
                                listaCiudades.get(npeticion).setId(json_obj.getInt("id"));
                                listaCiudades.get(npeticion).setLatitud(json_coord.getDouble("lat"));
                                listaCiudades.get(npeticion).setLongitud(json_coord.getDouble("lon"));
                                listaCiudades.get(npeticion).setTemperatura(json_main.getDouble("temp"));
                                listaCiudades.get(npeticion).setIcono_tiempo(json_weather.getJSONObject(0).getString("icon"));
                                listaCiudades.get(npeticion).setReady(true);
                                listaPeticiones[npeticion].setFinalizado(true);
                                if (finalizadasPeticiones()) {
                                    //String ultimaCiudad = listaCiudades.get(npeticion).toString();
                                    activity.addFragment(new CiudadesFragment(),false,"");
                                }
                            } else {
                                Log.d("***json ERR: ", "NO success" + respuesta);
                            }
                        } catch (
                                JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //salida.append("Error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> cabeceras = new HashMap<String, String>();
                cabeceras.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
                return cabeceras;
            }
        };
        colaPeticiones.add(peticion);
    }


    void ciudadesCercanas() throws Exception {
        //http://data.fixer.io/api/latest?access_key=23d8f7http://data.fixer.io/api/latest?access_key=23d8f74b03abd0cedc5262b9d2fd8c5c&symbols=USD,AUD,CAD,PLN,MXN&format=14b03abd0cedc5262b9d2fd8c5c&symbols=USD,AUD,CAD,PLN,MXN&format=1

        String cadConexion = "";

        cadConexion = "http://getnearbycities.geobytes.com/GetNearbyCities?radius=100km&minradius=10&limit=4&latitude=" + mejorLocaliz.getLatitude() + "&longitude=" + mejorLocaliz.getLongitude();
        Log.d("***cadena: ", "- " + cadConexion);
        StringRequest peticion = new StringRequest(Request.Method.GET, cadConexion,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        Log.d("***onResponse: ", respuesta);
                        String string = "";
                        try {
                            JSONArray json_array = new JSONArray(respuesta);
                            if (json_array.length() > 0) {
                                for (int i = 0; i < json_array.length(); i++) {
                                    InfoCiudad ciudadAux = new InfoCiudad();
                                    ciudadAux.setNombre((String) ((JSONArray) json_array.get(i)).get(1));
                                    ciudadAux.setPais((String) ((JSONArray) json_array.get(i)).get(6));
                                    ciudadAux.setLatitud(Double.parseDouble((String) ((JSONArray) json_array.get(i)).get(8)));
                                    ciudadAux.setLongitud(Double.parseDouble((String) ((JSONArray) json_array.get(i)).get(10)));
                                    ciudadAux.setDistancia(Double.parseDouble((String) ((JSONArray) json_array.get(i)).get(7)));
                                    listaCiudades.add(ciudadAux);
                                }
                                //Tenemos las ciudades. Se carga el tiempo
                                completaTiempo();

                            } else {
                                Log.d("***json ERR: ", "NO success" + respuesta);
                            }
                        } catch (
                                JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //salida.append("Error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> cabeceras = new HashMap<String, String>();
                cabeceras.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
                return cabeceras;
            }
        };
        colaPeticiones.add(peticion);
    }

    boolean finalizadasPeticiones() {
        boolean fin = true;
        for (DatosPeticion peticion : listaPeticiones) {
            fin = fin && peticion.isFinalizado();
        }
        return fin;
    }


    class DatosPeticion {
        double latitud;
        double longitud;
        boolean finalizado;

        public DatosPeticion() {
            this.longitud = 0.0;
            this.latitud = 0.0;
            this.finalizado = false;
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

        public void setLongitud(double longitud) {
            this.longitud = longitud;
        }

        public boolean isFinalizado() {
            return finalizado;
        }

        public void setFinalizado(boolean finalizado) {
            this.finalizado = finalizado;
        }
    }
}
