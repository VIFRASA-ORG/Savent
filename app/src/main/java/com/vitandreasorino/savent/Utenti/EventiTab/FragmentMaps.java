package com.vitandreasorino.savent.Utenti.EventiTab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Helper.AnimationHelper;
import Model.DAO.Eventi;
import Model.LogDebug;
import Model.POJO.Evento;

public class FragmentMaps extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnInfoWindowClickListener {

    LinearLayout searchButtonLinearLayout;
    private boolean firstTimeRenderingTheMap = true;
    private FloatingActionButton buttonMyPosition;

    MapView mapView;
    GoogleMap map;

    private boolean isLocationGranted = false;
    Location lastKnownLocation = null;

    //La locazione di default è a Roma
    LatLng defaultLocation = new LatLng(41.9027835,12.4963655);
    FusedLocationProviderClient fusedLocationProviderClient;

    //Elenco di tutti i marker effettivamente resi nella mappa
    List<Marker> markerList = new ArrayList<>();

    //Elenco di tutti gli eventi effettivamente nella mappa
    List<Evento> eventList = new ArrayList<>();

    private final int MAP_CAMERA_ZOOM = 10;
    private final int MAP_NEARBY_RADIUS = 20000;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Impostazione dell'evento per il pulsante utilizzato per la ricerca nella zona
        searchButtonLinearLayout = view.findViewById(R.id.searchInThisZoneLinearLayout);
        buttonMyPosition = view.findViewById(R.id.buttonMyPosition);
        buttonMyPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        searchButtonLinearLayout.setOnClickListener(v -> {
            searchInThisZoneOnClick(v);
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapView = (MapView) view.findViewById(R.id.mapViewPrincipal);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    /**
     * Richiedi il permesso di localizzazione, se non già concesso.
     */
    private void getLocationPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            isLocationGranted = true;
        }
    }


    /**
     * Controllo che il permesso di ACCESS_FINE_LOCATION sia garantito
     * @param requestCode codice di richiesta per controllare se il permesso è stato concesso
     * @param permissions permessi da controllare
     * @param grantResults 0 se il permesso è GRANTED, -1 se il permesso è DENIED.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isLocationGranted = true;
            }
        }

        updateLocationUi();
        getDeviceLocation();
    }

    /**
     * On click event per il bottone "searchButtonLinearLayout"
     * @param view
     */
    public void searchInThisZoneOnClick(View view){

        //Ottieni la regione visibile sulla mappa, ovvero la porzione rettangolare della mappa mostrata all'utente
        VisibleRegion r = map.getProjection().getVisibleRegion();
        removeAllMarker();

        //Download del nuovo evento presente nell'attuale regione visibile.
        Eventi.getEventInRegion(r, closureList ->{
            for(Evento e : closureList){
                markerList.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(e.getLatitudine(),e.getLongitudine()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon))
                        .title(e.getNome())));
                eventList.add(e);
            }

            //dissolvenza(fadeOut) del pulsante dopo l'aggiornamento del marker
            AnimationHelper.fadeOut(searchButtonLinearLayout,500);
        });
    }

    /**
     * Rimozione di tutti i marker presenti sulla mappa.
     */
    private void removeAllMarker(){
        for(Marker m : markerList){
            m.remove();
        }
        markerList.clear();
        eventList.clear();
    }

    /**
     * Mostra sulla mappa tutti gli eventi presenti in un raggio specificato come parametro a partire da un punto sulla mappa
     * @param refLocation punto di partenza sulla mappa
     * @param radius raggio dell'area
     */
    private void showAllNearbyEvents(LatLng refLocation, int radius){
        //Download di tutti gli eventi nelle vicinanze
        Eventi.getNearbyEvent(refLocation,radius, closureList -> {
            for(Evento e : closureList){
                markerList.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(e.getLatitudine(),e.getLongitudine()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon))
                        .title(e.getNome())));
                eventList.add(e);
            }
        });
    }

    /**
     * Sposta la fotocamera della mappa nella posizione predefinita
     */
    private void moveToDefaultLocation(){
        map.animateCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, MAP_CAMERA_ZOOM));
    }

    /**
     * Metodo che cerca di ottenere la posizione del dispositivo e, se lo trova,
     * sposta la telecamera della mappa.
     */
    private void getDeviceLocation(){
        /*
         * Ottieni la posizione migliore e più recente del dispositivo, che può essere nulla in rari
         * casi quando una posizione non è disponibile.
         */
        try {
            if (isLocationGranted) {
                @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Imposta la posizione della fotocamera sulla mappa sulla posizione corrente del dispositivo.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), MAP_CAMERA_ZOOM));

                                showAllNearbyEvents(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),20000);

                            }
                        } else {
                            //Se l'attività non ha esito positivo, sposta la telecamera nella posizione predefinita.
                            moveToDefaultLocation();

                            //Scarica tutti gli eventi nelle vicinanze della posizione predefinita.
                            showAllNearbyEvents(defaultLocation,MAP_NEARBY_RADIUS);
                            map.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                });
            }else{
                //Se l'attività non ha esito positivo, sposta la telecamera nella posizione predefinita.
                moveToDefaultLocation();

                //Scarica tutti gli eventi nelle vicinanze della posizione predefinita.
                showAllNearbyEvents(defaultLocation,MAP_NEARBY_RADIUS);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Aggiorna il componente dell'interfaccia utente della mappa con il nuovo parametro
     */
    @SuppressLint({"LongLogTag", "MissingPermission"})
    private void updateLocationUi(){
        if(map == null) return;
        else{
            try{
               if(isLocationGranted){
                   map.setMyLocationEnabled(true);
                   map.getUiSettings().setMyLocationButtonEnabled(true);
               }else{
                   map.setMyLocationEnabled(false);
                   map.getUiSettings().setMyLocationButtonEnabled(false);
                   lastKnownLocation = null;
               }
            } catch (SecurityException e){
                Log.e(LogDebug.MAPS_LOCATION_PERMISSION,e.getMessage());
            }
        }
    }




    /*
        OVERRIDE DI ALCUNI METODI NELL'INTERFACCIA OnMapReadyCallback
    */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraIdleListener(this);
        map.setOnInfoWindowClickListener(this);

        //Get the location permission when the map is ready
        getLocationPermission();

        updateLocationUi();
        getDeviceLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



     /*
        OVERRIDE DEL METODO NELL'INTERFACCIA OnCameraIdleListener
        Si attiva quando la telecamera smette di muoversi
     */

    /**
     * Mostra il pulsante searchInThisArea quando la telecamera si ferma.
     */
    @Override
    public void onCameraIdle() {
        if(!firstTimeRenderingTheMap){
            if(searchButtonLinearLayout.getVisibility() == View.INVISIBLE) AnimationHelper.fadeIn(searchButtonLinearLayout,500);
        }else firstTimeRenderingTheMap = false;
    }



     /*
        OVERRIDE DEL METODO NELL'INTERFACCIA OnInfoWindowClickListener
        Viene attivato quando si fa clic sulla finestra Info di un marker
     */
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        int pos = markerList.indexOf(marker);
        if( pos >= 0){
            Intent i = new Intent(getContext(), EventDetailActivity.class);
            i.putExtra("eventObj",eventList.get(pos));
            startActivity(i);
        }
    }
}