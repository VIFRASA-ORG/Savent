package com.vitandreasorino.savent.Utenti.EventiTabFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vitandreasorino.savent.R;

import Helper.AuthHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureResult;
import Model.DB.Eventi;
import Model.DB.Gruppi;
import Model.DB.Partecipazioni;
import Model.DB.Utenti;
import Model.Pojo.Evento;
import Model.Pojo.Utente;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Model that contains all the information shown in the interface
    Evento eventModel;
    Utente userModel;

    TextView titleTextView;
    TextView descriptionTextView;
    TextView dateTime;
    ImageView eventLocandinaImageView;
    TextView availablePlacesTextView;
    TextView queueTextView;
    TextView creatorTextView;

    Button buttonPartecipate;
    Button buttonLeave;
    TextView messageBox;

    MapView mapView;
    GoogleMap map;

    PageMode pageMode = PageMode.JOIN_EVENT;
    



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

        //Adding the listener for updates
        Eventi.addDocumentListener(eventModel.getId(),this,closureResult -> {
            if(closureResult != null){
                eventModel = closureResult;
                refreshData();
            }
        });

        //Downloading the user info
        Utenti.getUser(AuthHelper.getUserId(),utente -> {
            userModel = utente;
        });

        refreshData();
    }

    private void refreshData(){
        //Insert all model information in the view
        titleTextView.setText(eventModel.getNome());
        descriptionTextView.setText(eventModel.getDescrizione());
        dateTime.setText(eventModel.getNeutralData());

        //Computing the available places and the number of participant in the queue
        int maxPartecipation = eventModel.getNumeroMassimoPartecipanti();
        int actualPartecipation = eventModel.getNumeroPartecipanti();

        availablePlacesTextView.setText((actualPartecipation >= maxPartecipation) ? "0" : (maxPartecipation-actualPartecipation) + "");
        queueTextView.setText(eventModel.getNumeroPartecipantiInCoda() + "");

        //We have to download the image all over again because the intent allow you to pass just file under the size of 1mb
        Eventi.downloadEventImage(eventModel.getId(), (ClosureBitmap) result -> {
            eventModel.setImageBitmap(result);
            eventLocandinaImageView.setImageBitmap(result);
        });

        //Download the url
        Eventi.downloadEventImageUri(eventModel.getId(), new ClosureResult<Uri>() {
            @Override
            public void closure(Uri result) {
                if(result != null){
                    eventModel.setImageUri(result);
                }
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

        //Locate the map to the correct position
        addMarkerToPosition(new LatLng(eventModel.getLatitudine(),eventModel.getLongitudine()));

        //Enable or disable the correct button inside the view
        participationManager();
    }

    /**
     * Method that perform all the controls and enable or disable the correct component
     * based on the user and the participation to the event.
     */
    private void participationManager(){

        //trying to download the partecipation instance to this event
        Partecipazioni.getMyPartecipationAtEvent(eventModel.getId(), partecipazione -> {
            if(partecipazione != null){
                //User has already joined the event

                if(partecipazione.getAccettazione()){
                    if(partecipazione.getListaAttesa()){
                        //Accepted and in the queue
                        setPageMode(PageMode.LEAVE_EVENT_FROM_QUEUE);
                    }else{
                        //Accepted but not in queue
                        setPageMode(PageMode.LEAVE_EVENT_FROM_PARTICIPATION);
                    }
                }else{
                    if(partecipazione.getListaAttesa()){
                        //Not accepted but in queue
                        setPageMode(PageMode.LEAVE_EVENT_FROM_QUEUE_DUE_STATUS);
                    }
                }
            }else{
                //User is not joining the event yet
                setPageMode(PageMode.JOIN_EVENT);
            }
        });
    }

    /**
     * Enable the correct component based to the mode given.
     *
     * @param newPageMode the new mode.
     */
    private void setPageMode(PageMode newPageMode){
        switch (newPageMode){
            case JOIN_EVENT:
                //Not joined the event yet
                buttonLeave.setVisibility(View.GONE);
                buttonPartecipate.setVisibility(View.VISIBLE);
                buttonPartecipate.setEnabled(true);
                messageBox.setVisibility(View.GONE);
                break;
            case LEAVE_EVENT_FROM_QUEUE:
                //Joined but in queue
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.queueMessage);
                break;
            case LEAVE_EVENT_FROM_PARTICIPATION:
                //Joined and not in queue
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.participationMessage);
                break;

            case LEAVE_EVENT_FROM_QUEUE_DUE_STATUS:
                //Joined but in queue due to health minimun requirement not meet.
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.messageStatus);
                break;
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

        buttonPartecipate = findViewById(R.id.buttonPartecipate);
        buttonLeave = findViewById(R.id.buttonLeaveEvent);
        messageBox = findViewById(R.id.messageBox);
    }

    /**
     * On click event of the back button
     * @param view
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

    /**
     * On click method for the join event button.
     *
     * @param view
     */
    public void onParticipateButtonPressed(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yesValue, joinEventListener)
                .setNegativeButton("No", joinEventListener)
                .setTitle(R.string.areUSure);

        if(userModel == null || eventModel == null) return;

        //Determine the message to insert into the dialog
        if(userModel.getStatusSanitario() <= eventModel.getSogliaAccettazioneStatus()){
            //The health status is OK
            if(eventModel.getNumeroPartecipanti() < eventModel.getNumeroMassimoPartecipanti()){
                // Health OK, available Places OK
                builder.setMessage(R.string.popUpJoinHealthOkPlacesOk);
            }else{
                // Health OK, available places NOT OK
                builder.setMessage(R.string.popUpJoinHealthOkPlacesNO);
            }
        }else{
            //The health status is NOT OK
            //The message is the same for both the cases
            builder.setMessage(R.string.popUpJoinHealthNO);
        }

        builder.show();
    }

    /**
     * On click event for the leave button.
     *
     * @param view
     */
    public void onLeaveButtonPressed(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yesValue, leaveEventListener)
                .setNegativeButton("No", leaveEventListener)
                .setTitle(R.string.areUSure);
        builder.setMessage(R.string.youSureLeavingEvent).show();
    }

    public void onShareButtonClick(View view){
//        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        String shareBody = "Here is the share content body";
//        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }






    /*

        DialogInterface.OnClickListener
        IT IS USED TO MANAGE THE OncClick ON THE TWO BUTTON IN THE DIALOG
        TO JOIN AND LEAVE AN EVENT.

     */
    DialogInterface.OnClickListener leaveEventListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Partecipazioni.removeMyPartecipationTransaction(eventModel.getId(), closureBool -> {
                        if(closureBool) Toast.makeText(getApplicationContext(),R.string.eventAbandonedCorrectly, Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getApplicationContext(),R.string.errorAbandoningEvent, Toast.LENGTH_SHORT).show();
                    });
                    break;

                default:
                    break;
            }
        }
    };

    DialogInterface.OnClickListener joinEventListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Partecipazioni.addMyPartecipationTransaction(eventModel.getId(), part ->{
                        if(part != null) Toast.makeText(getApplicationContext(),R.string.participationCorrectlySent, Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getApplicationContext(),R.string.errorJoiningEvent, Toast.LENGTH_SHORT).show();
                    });
                    break;
                default:
                    break;
            }
        }
    };







    /*

        OVERRIDE OF SOME METHOD IN THE OnMapReadyCallback INTERFACE

     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(false);

        if(eventModel != null){
            addMarkerToPosition(new LatLng(eventModel.getLatitudine(),eventModel.getLongitudine()));
        }
    }

    private void addMarkerToPosition(LatLng latLng){
        if (map == null) return;
        map.clear();

        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_icon))
                .title(eventModel.getNome()));
        moveToLocation(latLng);
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






    /*

        ENUM USED TO DEFINE THE STATUS OF THE PAGE

     */

    private enum PageMode{
        JOIN_EVENT,
        LEAVE_EVENT_FROM_QUEUE,
        LEAVE_EVENT_FROM_PARTICIPATION,
        LEAVE_EVENT_FROM_QUEUE_DUE_STATUS;
    }

}