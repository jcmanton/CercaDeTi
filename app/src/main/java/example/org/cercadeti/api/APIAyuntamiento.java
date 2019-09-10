package example.org.cercadeti.api;

import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import example.org.cercadeti.MainActivity;
import example.org.cercadeti.gps.Posicion;
import example.org.cercadeti.R;
import example.org.cercadeti.fragments.MapFragment;
import example.org.cercadeti.model.InfoGPS;

public class APIAyuntamiento {

    private RequestQueue colaPeticiones;
    //private Location mejorLocaliz;
    Posicion posicion;
    MainActivity activity;
    public static InfoGPS[] infoGPS = new InfoGPS[0];
    HashMap<String, String> contenedores = new HashMap<String, String>();
    //"pilas aceite ropa residuos envases carton vidrio";


    public APIAyuntamiento(MainActivity activity) {
        this.activity = activity;
        posicion = Posicion.getInstance(null);
        colaPeticiones = Volley.newRequestQueue(activity);
        contenedores.put("pilas","cells");
        contenedores.put("aceite","oil");
        contenedores.put("ropa","clothes");
        contenedores.put("residuos","waste");
        contenedores.put("envases","can");
        contenedores.put("carton","box");
        contenedores.put("vidrio","glass");
        /*
        try {
            cargaDatosGPS("libres");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    //*** Servicio Web para recuperar datos API Ayuntamiento
    public void cargaDatosGPS(final String clase) throws Exception {
        //clase: libres disponibles
        String cadConexion = "";
        final String tiposContenedores = "pilas aceite ropa residuos envases carton vidrio";


        //St. Domingo 39494259/-374014
        //Ayuntamiento 39469729/-376815
        //http://mapas.valencia.es/lanzadera/gps/valenbisi/disponibles/39454219/-356711
        //http://mapas.valencia.es/lanzadera/gps/valenbisi/libres/39454219/-356711
        //http://mapas.valencia.es/lanzadera/gps/aparcamientos/39454219/-356711
        //http://mapas.valencia.es/lanzadera/gps/taxis/39454219/-356711
        //http://mapas.valencia.es/lanzadera/gps/contenedores/pilas/39454219/-356711
        //{tipo} pilas aceite ropa residuos envases carton vidrio
        //http://mapas.valencia.es/lanzadera/gps/wifi/39454219/-356711
        //http://mapas.valencia.es/lanzadera/gps/trafico/-2/-2
        //[{"titulo":"TRÁFICO","mensaje":"RUZAFA (Denso)",
        // "bbox":"POLYGON((-0.378372182104639 39.4624357738595,-0.378372182104639 39.4697773968059,-0.370447355947 39.4697773968059,-0.370447355947 39.4624357738595,-0.378372182104639 39.4624357738595))"}]
        //http://mapas.valencia.es/lanzadera/puntoInteres/config
        //http://mapas.valencia.es/lanzadera/puntoInteres/transportepub?radio=500&lang=es&lat=39465212&lon=-374521&filtros=1
        //filtro= 1 EMT, 2 FGV,
        //mejorLocaliz.getLatitude()
        //http://mapas.valencia.es/lanzadera/gps/aparcamientos/{lat}/{lon}
        //cadConexion = "http://mapas.valencia.es/lanzadera/gps/valenbisi/libres/" + (int) (mejorLocaliz.getLatitude() * 1000000) + "/" + (int) (mejorLocaliz.getLongitude() * 1000000);
        if (clase.equals("bus") || clase.equals("metro")) {
            cadConexion = "http://mapas.valencia.es/lanzadera/puntoInteres/transportepub?radio=500&lang=es&lat=" + (int) (posicion.getMejorLocaliz().getLatitude() * 1000000) + "&lon=" + (int) (posicion.getMejorLocaliz().getLongitude() * 1000000);
            if (clase.equals("bus")) {
                cadConexion = cadConexion + "&filtros=1";
            } else {
                cadConexion = cadConexion + "&filtros=2";
            }
        } else {
            if (tiposContenedores.indexOf(clase) > 0) {
                cadConexion = "http://mapas.valencia.es/lanzadera/gps/contenedores/" + clase + "/" + (int) (posicion.getMejorLocaliz().getLatitude() * 1000000) + "/" + (int) (posicion.getMejorLocaliz().getLongitude() * 1000000);
            } else {
                String servicio = clase.equals("libres") || clase.equals("disponibles") ? "valenbisi/" : "";
                cadConexion = "http://mapas.valencia.es/lanzadera/gps/" + servicio + clase + "/" + (int) (posicion.getMejorLocaliz().getLatitude() * 1000000) + "/" + (int) (posicion.getMejorLocaliz().getLongitude() * 1000000);
                Log.d("***cadConexion", cadConexion);
                //cadConexion="http://mapas.valencia.es/lanzadera/gps/wifi/39454219/-356711";
                if (clase.equals("trafico")) {
                    cadConexion = "http://mapas.valencia.es/lanzadera/gps/trafico/-2/-2";
                }
            }
        }
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
                                Bundle bundle = new Bundle();
                                MapFragment primerFragment = new MapFragment();
                                switch (clase) {
                                    case "bus":
                                    case "metro":
                                        int iconoBusMetro = clase.equals("bus") ? R.drawable.bus : R.drawable.metro;
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            infoGPS[i] = new InfoGPS();
                                            procesaTexto(infoGPS[i], json_obj.getString("texto"));
                                            infoGPS[i].setLatDestino(json_obj.getInt("lat") * 1.0 / 1000000);
                                            infoGPS[i].setLongDestino(json_obj.getInt("lon") * 1.0 / 1000000);
                                            infoGPS[i].setDistancia(json_obj.getInt("distancia"));
                                            infoGPS[i].setIcon(iconoBusMetro);
                                            infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + json_obj.getString("titulo"));
                                            JSONArray json_arrayAcciones = json_obj.getJSONArray("acciones");
                                            infoGPS[i].setUrl(((JSONObject) json_arrayAcciones.get(json_arrayAcciones.length() - 1)).getString("uri"));
                                        }
                                        bundle = new Bundle();
                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;

                                    case "libres":
                                    case "disponibles":
                                        if (clase.equals("libres")) {
                                            infoGPS = new InfoGPS[json_array.length()];
                                        }
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            String mensaje = json_obj.getString("mensaje");
                                            if (clase.equals("libres")) {
                                                //Se puede aparcar
                                                infoGPS[i] = new InfoGPS();
                                                procesaMensajeValenBisi(infoGPS[i], mensaje);
                                                infoGPS[i].setLatDestino(json_obj.getInt("latDestino") * 1.0 / 1000000);
                                                infoGPS[i].setLongDestino(json_obj.getInt("lonDestino") * 1.0 / 1000000);
                                                infoGPS[i].setDistancia(json_obj.getInt("distancia"));
                                                infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + infoGPS[i].getDireccion());
                                                infoGPS[i].setSnippet(activity.getResources().getString(R.string.plazas_disponibles) + infoGPS[i].getLibres() + ".");
                                                infoGPS[i].setIcon(R.drawable.bici_naranja);
                                            } else {
                                                InfoGPS estacionBB = getEstacion(json_obj.getInt("distancia"));
                                                procesaMensajeValenBisi(estacionBB, mensaje);
                                                // Segunda pasada. Se cargan los datos del marcador
                                                estacionBB.setLatDestino(json_obj.getInt("latDestino") * 1.0 / 1000000);
                                                estacionBB.setLongDestino(json_obj.getInt("lonDestino") * 1.0 / 1000000);
                                                estacionBB.setTitle("(" + estacionBB.getDistancia() + "m) " + estacionBB.getDireccion());
                                                estacionBB.setSnippet(activity.getResources().getString(R.string.bicis_disponibles) + estacionBB.getDisponibles() + ". " + estacionBB.getSnippet());
                                                /*
                                                if (estacionBB.getDisponibles() == 0) {
                                                    estacionBB.setIcon(R.drawable.bici_rojo);
                                                } else {
                                                    if (estacionBB.getDisponibles() > 0 && estacionBB.getLibres() > 0) {
                                                        estacionBB.setIcon(R.drawable.bici_verde);
                                                    } else {
                                                        estacionBB.setIcon(R.drawable.bici_naranja);
                                                    }
                                                }*/
                                                if (estacionBB.getDisponibles() == 0 && estacionBB.getLibres() == 0) {
                                                    estacionBB.setIcon(R.drawable.bici_rojo);
                                                }
                                                if (estacionBB.getDisponibles() > 0 && estacionBB.getLibres() > 0) {
                                                    estacionBB.setIcon(R.drawable.bici_verde);
                                                }
                                                if (estacionBB.getDisponibles() == 0 && estacionBB.getLibres() > 0) {
                                                    estacionBB.setIcon(R.drawable.bici_naranja);
                                                }
                                                if (estacionBB.getDisponibles() > 0 && estacionBB.getLibres()== 0) {
                                                    estacionBB.setIcon(R.drawable.bici_negro);
                                                }

                                            }
                                        }
                                        if (clase.equals("libres")) {
                                            cargaDatosGPS("disponibles");
                                        } else {
                                            bundle = new Bundle();
                                            bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                            bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                            primerFragment.setArguments(bundle);
                                            activity.addFragment(primerFragment, false, "");
                                        }
                                        break;
                                    case "aparcamientos":
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            String mensaje = json_obj.getString("mensaje");
                                            infoGPS[i] = new InfoGPS();
                                            procesaMensajeValenBisi(infoGPS[i], mensaje);
                                            infoGPS[i].setLatDestino(json_obj.getInt("latDestino") * 1.0 / 1000000);
                                            infoGPS[i].setLongDestino(json_obj.getInt("lonDestino") * 1.0 / 1000000);
                                            infoGPS[i].setDistancia(json_obj.getInt("distancia"));
                                            infoGPS[i].setIcon(R.drawable.parking);
                                            infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + infoGPS[i].getDireccion());
                                            infoGPS[i].setSnippet(activity.getResources().getString(R.string.parking_disponible) + infoGPS[i].getLibres());

                                        }
                                        bundle = new Bundle();
                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;
                                    case "taxis":
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            String mensaje = json_obj.getString("mensaje");
                                            infoGPS[i] = new InfoGPS();
                                            infoGPS[i].setDireccion(mensaje);
                                            infoGPS[i].setLatDestino(json_obj.getInt("latDestino") * 1.0 / 1000000);
                                            infoGPS[i].setLongDestino(json_obj.getInt("lonDestino") * 1.0 / 1000000);
                                            infoGPS[i].setDistancia(json_obj.getInt("distancia"));
                                            infoGPS[i].setIcon(R.drawable.taxi);
                                            infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + infoGPS[i].getDireccion());
                                            infoGPS[i].setSnippet("Parada a: " + infoGPS[i].getDistancia() + "m");
                                        }
                                        bundle = new Bundle();
                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;
                                    case "wifi":
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            infoGPS[i] = new InfoGPS();
                                            infoGPS[i].setLatDestino(json_obj.getInt("latDestino") * 1.0 / 1000000);
                                            infoGPS[i].setLongDestino(json_obj.getInt("lonDestino") * 1.0 / 1000000);
                                            infoGPS[i].setDistancia(json_obj.getInt("distancia"));
                                            infoGPS[i].setDireccion(json_obj.getString("mensaje"));
                                            infoGPS[i].setIcon(R.drawable.wifi);
                                            infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + infoGPS[i].getDireccion());
                                            infoGPS[i].setSnippet(activity.getResources().getString(R.string.wifi_a) + infoGPS[i].getDistancia() + "m");

                                        }
                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;
                                    case "trafico":
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            infoGPS[i] = new InfoGPS();
                                            JSONObject json_obj = (JSONObject) json_array.get(i);
                                            //[{"titulo":"TRÁFICO","mensaje":"RUZAFA (Denso)",
                                            infoGPS[i].setTitle(json_obj.getString("mensaje"));
                                            infoGPS[i].setDireccion(json_obj.getString("mensaje"));
                                            infoGPS[i].setSnippet(json_obj.getString("mensaje"));
                                            infoGPS[i].setAvisos(json_obj.getString("mensaje"));
                                            //****************
                                            //infoGPS[i].setAvisos("RUZAFA (Denso) | JACINTO BENAVENTE HACIA PLAZA AMÉRICA (Cortado)");
                                            infoGPS[i].setIcon(R.drawable.trafico);
                                            infoGPS[i].setBbox(json_obj.getString("bbox"));
                                            //****************
                                            //infoGPS[i].setBbox("POLYGON((-0.378372182104639 39.4624357738595,-0.378372182104639 39.4697773968059,-0.370447355947 39.4697773968059,-0.370447355947 39.4624357738595,-0.378372182104639 39.4624357738595)),POLYGON((-0.367340435747141 39.4606770277396,-0.367340435747141 39.4713802470935,-0.357402462620144 39.4713802470935,-0.357402462620144 39.4606770277396,-0.367340435747141 39.4606770277396))");
                                            //mensaje = "PERIS Y VALERO, VALENCIA \\\nGASPAR AGUILAR, VALENCIA";
                                            //infoGPS = procesaMensajeTrafico(mensaje);
                                        }

                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;
                                    default:
                                        //Contenedores
                                        infoGPS = new InfoGPS[json_array.length()];
                                        for (int i = 0; i < json_array.length(); i++) {
                                            JSONObject json_obj2 = (JSONObject) json_array.get(i);
                                            String[] lines = json_obj2.getString("mensaje").split("\\n");
                                            String mensaje = lines[0];
                                            String direccion = lines.length > 1 ? lines[1] : lines[0];
                                            infoGPS[i] = new InfoGPS();
                                            infoGPS[i].setDireccion(direccion);
                                            infoGPS[i].setLatDestino(json_obj2.getInt("latDestino") * 1.0 / 1000000);
                                            infoGPS[i].setLongDestino(json_obj2.getInt("lonDestino") * 1.0 / 1000000);
                                            infoGPS[i].setDistancia(json_obj2.getInt("distancia"));
                                            int idResource = activity.getResources().getIdentifier(clase, "drawable", activity.getPackageName());
                                            infoGPS[i].setIcon(idResource);
                                            infoGPS[i].setTitle("(" + infoGPS[i].getDistancia() + "m) " + mensaje);
                                            String contenedor= getCurrentLocale().equals("en")?contenedores.get(clase):clase;
                                            infoGPS[i].setSnippet(activity.getResources().getString(R.string.contenedor_de) + contenedor);

                                        }
                                        bundle = new Bundle();
                                        bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
                                        bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
                                        primerFragment = new MapFragment();
                                        primerFragment.setArguments(bundle);
                                        activity.addFragment(primerFragment, false, "");
                                        break;
                                }
                            } else {
                                Log.d("***json ERR: ", "NO success" + respuesta);
                                Toast.makeText(activity, activity.getResources().getString(R.string.sin_datos), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(activity, activity.getResources().getString(R.string.sin_datos), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //salida.append("Error: " + error.getMessage());
                        Log.d("***onErrorResponse: ", "Err conexión API");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> cabeceras = new HashMap<String, String>();
                cabeceras.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
                //Autenticación básica para servicio web ayuntamiento Valencia
                String creds = String.format("%s:%s", activity.getString(R.string.aytoApiId), activity.getString(R.string.aytoApiPwd));
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                //Note that you may have to use Base64.NO_WRAP instead of Base64.DEFAULT
                cabeceras.put("Authorization", auth);
                return cabeceras;
            }
        };
        colaPeticiones.add(peticion);
    }

    private void procesaTexto(InfoGPS info, String texto) {
        String[] lines = texto.split("\\n");
        String linea0 = lines[0].equals("null") ? "" : lines[0];
        info.setDireccion(linea0);
        info.setSnippet(lines[1]);
    }

    void procesaMensajeValenBisi(InfoGPS info, String mensaje) {
        String[] lines = mensaje.split("\\n");
        info.setDireccion(lines[0]);
        int numero = extraeUltimoEntero(lines[1]);
        if (lines[1].contains("disponibles")) {
            info.setDisponibles(numero);
        } else {
            info.setLibres(numero);
        }
    }

    InfoGPS[] procesaMensajeTrafico(String mensaje) {
        String[] lines = mensaje.split("\\n");
        InfoGPS[] info = new InfoGPS[lines.length];
        for (int i = 0; i < lines.length; i++) {
            info[i] = new InfoGPS();
            LatLng pos = posicion.getLocationFromAddress(lines[i]);
            if (pos == null) {
                removeElement(info, i);
            } else {
                info[i].setTitle(lines[i]);
                info[i].setLatDestino(pos.latitude);
                info[i].setLongDestino(pos.longitude);
                LatLng miPos = new LatLng(posicion.getMejorLocaliz().getLatitude(), posicion.getMejorLocaliz().getLongitude());
                info[i].setDistancia((Posicion.getDistanceBetween(pos, miPos)).intValue());
                info[i].setDireccion(lines[i]);
                info[i].setSnippet("Tráfico en " + lines[i]);
                info[i].setIcon(R.drawable.trafico);
            }
        }
        return info;

    }

    int extraeUltimoEntero(String cadena) {
        int lastNumberInt = 0;
        Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
        Matcher matcher = lastIntPattern.matcher(cadena);
        if (matcher.find()) {
            String someNumberStr = matcher.group(1);
            lastNumberInt = Integer.parseInt(someNumberStr);
        }
        return lastNumberInt;
    }

    public void removeElement(Object[] arr, int removedIdx) {
        System.arraycopy(arr, removedIdx + 1, arr, removedIdx, arr.length - 1 - removedIdx);
    }

    private InfoGPS getEstacion(int distancia) {
        for (int i = 0; i < infoGPS.length; i++) {
            if (infoGPS[i].getDistancia() == distancia) {
                return infoGPS[i];
            }
        }
        //No está la estación. Se añade una.
        InfoGPS nuevoElemento = new InfoGPS();
        infoGPS = addElement(infoGPS, nuevoElemento);
        return nuevoElemento;
    }

    static InfoGPS[] addElement(InfoGPS[] a, InfoGPS e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }

    String getCurrentLocale() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = activity.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = activity.getResources().getConfiguration().locale;
        }
        return locale.getLanguage();
    }
}

