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
import Model.DB.Eventi;
import Model.LogDebug;
import Model.Pojo.Evento;

public class FragmentMaps extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnInfoWindowClickListener {

    LinearLayout searchButtonLinearLayout;
    private boolean firstTimeRenderingTheMap = true;
    private FloatingActionButton buttonMyPosition;

    MapView mapView;
    GoogleMap map;

    private boolean isLocationGranted = false;
    Location lastKnownLocation = null;

    //The default location is in Rome
    LatLng defaultLocation = new LatLng(41.9027835,12.4963655);
    FusedLocationProviderClient fusedLocationProviderClient;

    //List of all the marker actually rendered in the map
    List<Marker> markerList = new ArrayList<>();
    //List of all the event actually rendered in the map
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

        //Setting up the event for the button used to search in the zone
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
     * Ask for the location permission, if not already granted.
     */
    private void getLocationPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            isLocationGranted = true;
        }
    }

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
     * On click event for the "searchButtonLinearLayout" button.
     *
     * @param view
     */
    public void searchInThisZoneOnClick(View view){

        //Get the visible region on the map, that is the rectangular portion of the map shown to the user
        VisibleRegion r = map.getProjection().getVisibleRegion();
        removeAllMarker();

        //Downloading the new event present in the actual visible region.
        Eventi.getEventInRegion(r, closureList ->{
            for(Evento e : closureList){
                markerList.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(e.getLatitudine(),e.getLongitudine()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon))
                        .title(e.getNome())));
                eventList.add(e);
            }

            //fading out the button after updating the marker
            AnimationHelper.fadeOut(searchButtonLinearLayout,500);
        });
    }

    /**
     * Remove all the marker in the map.
     */
    private void removeAllMarker(){
        for(Marker m : markerList){
            m.remove();
        }
        markerList.clear();
        eventList.clear();
    }

    /**
     * Show on the map, all events present in a radius specified as a parameter starting from a point on the map
     * @param refLocation starting point on the map
     * @param radius radius of the area
     */
    private void showAllNearbyEvents(LatLng refLocation, int radius){
        //Downloading all the nearby event
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
     * Move the map camera to the default location
     */
    private void moveToDefaultLocation(){
        map.animateCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, MAP_CAMERA_ZOOM));
    }

    /**
     * Method that try to get the device location and, if it is found,
     * it moves the map camera over.
     */
    private void getDeviceLocation(){
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (isLocationGranted) {
                @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), MAP_CAMERA_ZOOM));

                                showAllNearbyEvents(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()),20000);

                            }
                        } else {
                            //If the task is not successful, move the camera to the defualt location.
                            moveToDefaultLocation();

                            //Download all the event in the nearby of the default location.
                            showAllNearbyEvents(defaultLocation,MAP_NEARBY_RADIUS);
                            map.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                });
            }else{
                //If the task is not successful, move the camera to the defualt location.
                moveToDefaultLocation();

                //Download all the event in the nearby of the default location.
                showAllNearbyEvents(defaultLocation,MAP_NEARBY_RADIUS);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Update the map ui component with the new parameter
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

        OVERRIDE OF SOME METHOD IN THE OnMapReadyCallback INTERFACE

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

        OVERRIDE OF THE METHOD IN THE OnCameraIdleListener INTERFACE
        It is triggered when the camera stop moving

     */

    /**
     * Show the searchInThisArea button when the camera stop moving.
     */
    @Override
    public void onCameraIdle() {
        if(!firstTimeRenderingTheMap){
            if(searchButtonLinearLayout.getVisibility() == View.INVISIBLE) AnimationHelper.fadeIn(searchButtonLinearLayout,500);
        }else firstTimeRenderingTheMap = false;
    }



    /*

        OVERRIDE OF THE METHOD IN THE OnInfoWindowClickListener INTERFACE
        It is triggered when you click on the InfoWindow of a a marker

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