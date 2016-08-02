package com.testapi.adans.aplicationtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void buildGoogleApiClient() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),   Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) this.getSystemService(this.getApplicationContext().LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(final Location location) {

        // Add a marker in Sydney and move the camera
        if ( location != null ) {
            final LatLng currentLocal = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(currentLocal).title("You are here!!").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocal));



            //sends to webservice
            new AsyncTask<String, Integer, String>(){

                @Override
                protected String doInBackground(String... params) {
                    // should be a singleton
                    OkHttpClient client = new OkHttpClient();


                    JSONObject velocidadeTempoLocalizacaoObj = new JSONObject();
                    try {
                        velocidadeTempoLocalizacaoObj.put("idLog",04);
                        velocidadeTempoLocalizacaoObj.put("cnpjEmpresaTransporte","51194232000169");
                        velocidadeTempoLocalizacaoObj.put("placaVeiculo","AAA0000");
                        velocidadeTempoLocalizacaoObj.put("velocidadeAtual",location.getSpeed());
                        velocidadeTempoLocalizacaoObj.put("distanciaPercorrida",200);
                        velocidadeTempoLocalizacaoObj.put("situacaoIgnicaoMotor",1);
                        velocidadeTempoLocalizacaoObj.put("situacaoPortaVeiculo","1");
                        velocidadeTempoLocalizacaoObj.put("latitude", location.getLatitude());
                        velocidadeTempoLocalizacaoObj.put("longitude",location.getLongitude());
                        velocidadeTempoLocalizacaoObj.put("pdop",location.getAccuracy());
                        velocidadeTempoLocalizacaoObj.put("dataHoraEvento","2016-01-01T09:00:00");
                        velocidadeTempoLocalizacaoObj.put("imei", "123456789012345678");
                    } catch (JSONException e) {
                    }


                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), velocidadeTempoLocalizacaoObj.toString() );

                    Request request = new Request.Builder()
                            .header("Authorization", "cb47e90c-9255-4809-a82d-eea7b45881bc")
                            .addHeader("Content-Type","application/json")
                             .post(body)
                            .url("http://appservices.antt.gov.br:8956/antt/monitriip/rest/InserirLogVelocidadeTempoLocalizacao")
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        return response.body().string();
                    } catch (IOException e) {
                        return "Erro" + e.getMessage();
                    }
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                    Log.e("Inserido na base",result);

                }
            }.execute();

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
