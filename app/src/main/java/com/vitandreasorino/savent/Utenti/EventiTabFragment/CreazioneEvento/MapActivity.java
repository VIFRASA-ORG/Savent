package com.vitandreasorino.savent.Utenti.EventiTabFragment.CreazioneEvento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vitandreasorino.savent.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    GoogleMap map;

    private double latitudine = 0;
    private double longitudine = 0;
    private boolean isNotNull;

    MarkerOptions markerOptions = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.mapViewEvent);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;

        isNotNull = getIntent().getBooleanExtra("isNotNull", false);
        latitudine = getIntent().getDoubleExtra("latitudine",0);
        longitudine = getIntent().getDoubleExtra("longitudine",0);
        MarkerOptions marker = new MarkerOptions();
        LatLng oggetto = new LatLng(latitudine,longitudine);
        marker.position(oggetto);
        map.clear();

        if(isNotNull) {
            map.addMarker(marker);
        }



        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                // Quando clicca sulla mappa
                // Inizializza il Marker
                markerOptions = new MarkerOptions();

                // Setto la posizione del Marker
                markerOptions.position(latLng);

                // Setto il titolo del Marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Rimozione di tutti i Marker
                googleMap.clear();

                // Animazione dello zoom del Marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                // Aggiungo il Marker sulla mappa
                googleMap.addMarker(markerOptions);

                latitudine = latLng.latitude;
                longitudine = latLng.longitude;


            }
        });
    }


    public void onLocation(View view) {

        Intent intent = new Intent();

        intent.putExtra("isNull", markerOptions == null);
        intent.putExtra("latitudine", latitudine);
        intent.putExtra("longitudine", longitudine);
        setResult(RESULT_OK, intent);
        finish();

    }


    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }



}