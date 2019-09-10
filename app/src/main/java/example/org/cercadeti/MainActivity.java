package example.org.cercadeti;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import example.org.cercadeti.api.APIAyuntamiento;
import example.org.cercadeti.api.APICiudades;
import example.org.cercadeti.fragments.ContenedoresFragment;
import example.org.cercadeti.fragments.InicioFragment;
import example.org.cercadeti.gps.Posicion;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 1234;
    private Posicion posicion;
    private String claseConsulta = "";
    private boolean posicionManual = false;
    public static LatLng PosCentroValencia = new LatLng(39.469791, -0.376968);
    public static double RadioValencia = 5500; //asignamos 5500m. de radio desde Ayto
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, "Sin el permiso localización no puedo mostrar tu posición actual.", SOLICITUD_PERMISO_LOCALIZACION, this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            posicion = Posicion.getInstance(this);
            posicion.activarProveedores();
            if (posicion.getMejorLocaliz() == null) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        //MapFragment primerFragment = new MapFragment();
        //getFragmentManager().beginTransaction().add(R.id.fr_contenedor, primerFragment)
        //        .commit();
        if (!isOnline()) {
            Toast.makeText(this, getResources().getString(R.string.sinInternet), Toast.LENGTH_LONG).show();
        }
        if (posicion == null || !posicion.isGPSEnable()) {
            Toast.makeText(this, getResources().getString(R.string.sinGPS), Toast.LENGTH_LONG).show();
        }
        InicioFragment primerFragment = new InicioFragment();
        addFragment(primerFragment, false, "");
        /*
        if (posicion.getMejorLocaliz() == null) {
            finishAffinity();
            System.exit(0);
        } else {
            Bundle bundle = new Bundle();
            bundle.putDouble("latitud", posicion.getMejorLocaliz().getLatitude());
            bundle.putDouble("longitud", posicion.getMejorLocaliz().getLongitude());
            primerFragment.setArguments(bundle);
            addFragment(primerFragment, false, "");
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (posicion == null) {
            posicion = Posicion.getInstance(this);
        }
        if (posicion != null) {
            posicion.activarProveedores();
        }
        actualizaPosicion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (posicion == null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            posicion.removeUpdates();
            Log.d("***Debug ", "onPause removeUpdates");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            addFragment(new InicioFragment(), false, "");
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        compruebaPosicionInicial();
        claseConsulta = "";
        if (!isOnline()) {
            Toast.makeText(this, getResources().getString(R.string.sinInternet), Toast.LENGTH_LONG).show();
            return true;
        }
        if (!posicion.isGPSEnable() && getPosicionManual() == null) {
            Toast.makeText(this, getResources().getString(R.string.sinGPS), Toast.LENGTH_LONG).show();
            return true;
        }
        int id = item.getItemId();

        if (id == R.id.bus) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "bus";
                apiAyuntamiento.cargaDatosGPS("bus");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.metro) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "metro";
                apiAyuntamiento.cargaDatosGPS("metro");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.bicis) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "libres";
                apiAyuntamiento.cargaDatosGPS("libres");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.parking) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "aparcamientos";
                apiAyuntamiento.cargaDatosGPS("aparcamientos");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_wifi) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "wifi";
                apiAyuntamiento.cargaDatosGPS("wifi");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_trafico) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "trafico";
                apiAyuntamiento.cargaDatosGPS("trafico");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_taxis) {
            APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
            try {
                claseConsulta = "taxis";
                apiAyuntamiento.cargaDatosGPS("taxis");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_ciudades) {
            APICiudades apiCiudades = new APICiudades(this);
            apiCiudades.consultaTiempo(null);

        } else if (id == R.id.nav_contenedores) {
            claseConsulta = "contenedores";
            addFragment(new ContenedoresFragment(), false, "");

        } else if (id == R.id.nav_acercade) {
            addFragment(new InicioFragment(), false, "");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void solicitarPermiso(final String permiso, String justificacion, final int requestCode, final Activity actividad) {
        Log.d("***Debug ", "solicitarPermiso " + permiso);
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)) {
            Log.d("***Debug ", "solicitarPermisoRAT " + permiso);
            new android.app.AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
                        }
                    }).show();
        } else {
            Log.d("***Debug ", "solicitarPermisoELSE " + permiso);
            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("***onRequestPermission ", "onRequestPermissionsResult ");
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
            Log.d("***Debug ", "onRequestPermissionsResultIN_LOCAT");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("***Debug ", "onRequestPermissionsResultOK");
                if (posicion == null) {
                    posicion = Posicion.getInstance(this);
                }
                posicion.updatePosicion();
                //adaptador.notifyDataSetChanged();
            }
        }
    }

    public void onPosicionChanged() {
        cargaDatosGPS();
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.fr_contenedor, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    public boolean isOnline() {
        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;
        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    public void actualizaPosicion() {
        posicion.ultimaLocalizacion();
    }

    public void setPosicion(LatLng latLng, boolean isManual) {
        Posicion.setLocalizacionManual(latLng);
        posicionManual = isManual;
    }

    public void resetPosicion() {
        Posicion.setLocalizacionManual(null);
        actualizaPosicion();
        posicionManual = false;
    }

    public LatLng getPosicionManual() {
        return Posicion.getLocalizacionManual();
    }

    public Location getLocalizacion() {
        return posicion.getMejorLocaliz();
    }

    public void cargaDatosGPS() {
        if (claseConsulta.equals("")) return;
        APIAyuntamiento apiAyuntamiento = new APIAyuntamiento(this);
        try {
            apiAyuntamiento.cargaDatosGPS(claseConsulta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGPSEnabled() {
        if (posicion == null) return false;
        if (posicion.isGPSEnable()) {
            return true;
        }
        return false;
    }

    public boolean isValencia(LatLng latLng) {
        LatLng latLngUbicacion;
        if (latLng == null) {
            latLngUbicacion = posicion.getMejorLocalizLatLng();
        } else {
            latLngUbicacion = latLng;
        }
        if (Posicion.getDistanceBetween(latLngUbicacion, PosCentroValencia) > RadioValencia) {
            Toast.makeText(this, getResources().getString(R.string.msg_uso_Valencia), Toast.LENGTH_LONG).show();
            setPosCentroValencia();
            return false;
        }
        return true;
    }

    public boolean isPosicionManual() {
        return posicionManual;
    }

    public void setClaseConsulta(String claseConsulta) {
        this.claseConsulta = claseConsulta;
    }

    public void setPosCentroValencia() {
        setPosicion(PosCentroValencia, false);
    }

    public void compruebaPosicionInicial() {
        // Comprobar si hay una posición que no coincide con la actual y no se ha establecido a mano
        if (!isPosicionManual() && getPosicionManual() != null && isValencia(posicion.ultimaLocalizacionGPS())) {
            Posicion.setLocalizacionManual(null);
            posicion.ultimaLocalizacion();
        }
        if (getPosicionManual() == null && !isValencia(posicion.ultimaLocalizacionGPS())) {
            setPosCentroValencia();
        }
    }

    public void compruebaPosicionInicial(Location loc) {
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        // Comprobar si hay una posición que no coincide con la actual y no se ha establecido a mano
        if (!isPosicionManual() && getPosicionManual() != null && isValencia(latLng)) {
            Posicion.setLocalizacionManual(null);
            //posicion.ultimaLocalizacion();
        }
        if (getPosicionManual() == null && !isValencia(latLng)) {
            setPosCentroValencia();
        }
    }
}
