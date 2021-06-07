package com.vitandreasorino.savent.EventiTab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vitandreasorino.savent.R;

import Model.DB.Eventi;
import Model.DB.Gruppi;
import Model.DB.Utenti;
import Model.Pojo.Evento;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Model that contains all the information shown in the interface
    Evento eventModel;

    TextView titleTextView;
    TextView descriptionTextView;
    TextView dateTime;
    ImageView eventLocandinaImageView;
    TextView availablePlacesTextView;
    TextView queueTextView;
    TextView creatorTextView;

    MapView mapView;
    GoogleMap map;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        //Deserializing the object from the intent
        eventModel = (Evento) getIntent().getSerializableExtra("eventObj");

        //Inflate all the component
        inflateAll();

        //Setting-up the mapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Insert all model information in the view
        titleTextView.setText(eventModel.getNome());
        descriptionTextView.setText(eventModel.getDescrizione());
        dateTime.setText(eventModel.getDataOra().toString());

        //Computing the available places and the number of participant in the queue
        int maxPartecipation = eventModel.getNumeroMassimoPartecipanti();
        int actualPartecipation = eventModel.getNumeroPartecipanti();
        int queuePartecipant = (actualPartecipation > maxPartecipation) ? Math.abs(maxPartecipation - actualPartecipation) : 0;

        availablePlacesTextView.setText((actualPartecipation >= maxPartecipation) ? "0" : (maxPartecipation-actualPartecipation) + "");
        queueTextView.setText(queuePartecipant+"");

        //We have to download the image all over again because the intent allow you to pass just file under the size of 1mb
        Eventi.downloadEventImage(eventModel.getId(), bitmap -> {
            if(bitmap != null){
                eventLocandinaImageView.setImageBitmap(bitmap);
                eventModel.setImageBitmap(bitmap);
            }
        });

        //Download the user name of the creator or of the group
        if(!eventModel.getIdUtenteCreatore().isEmpty()){
            Utenti.getNameSurnameOfUser(eventModel.getIdUtenteCreatore(), closureResult -> {
                if(closureResult != null) creatorTextView.setText(closureResult);
            });
        }else if (!eventModel.getIdGruppoCreatore().isEmpty()){
            Gruppi.getGroupName(eventModel.getIdGruppoCreatore(), closureResult -> {
                if(closureResult != null) creatorTextView.setText(closureResult);
            });
        }
    }

    /**
     * Method used to gett all the interface reference from the xml file
     */
    private void inflateAll(){
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionContentTextView);
        dateTime = findViewById(R.id.dateTextView);
        mapView = (MapView) findViewById(R.id.mapView);
        eventLocandinaImageView = findViewById(R.id.imageViewProfile);
        availablePlacesTextView = findViewById(R.id.availablePlacesContentTextView);
        queueTextView = findViewById(R.id.queueContentTextView);
        creatorTextView = findViewById(R.id.creatorContentTextView);
    }

    /**
     * On click event of the back button
     * @param view
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }








    /*

        OVERRIDE OF SOME METHOD IN THE OnMapReadyCallback INTERFACE

     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(false);

        LatLng eventPlace = new LatLng(eventModel.getLatitudine(),eventModel.getLongitudine());

        map.addMarker(new MarkerOptions()
                .position(eventPlace)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon))
                .title(eventModel.getNome()));
        moveToLocation(eventPlace);
    }

    /**
     * Move the map camera to the location given
     * @param location  location to which tha camera is to be moved
     */
    private void moveToLocation(LatLng location) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
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
}