package com.vitandreasorino.savent.Utenti.EventiTab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.vitandreasorino.savent.Utenti.Notification.NotificationActivity;

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

    private boolean fromCreation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        //Inflate all the component
        inflateAll();

        //Setting-up the mapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fromCreation = getIntent().getBooleanExtra("Creation",false);

        //Checking if we are arriving here from the notification
        boolean fromNotification = getIntent().getBooleanExtra(NotificationActivity.FROM_NOTIFICATION_INTENT,false);
        if(fromNotification){
            String eventId = getIntent().getStringExtra("eventId");

            Eventi.getEvent(eventId, evento -> {
                eventModel = evento;
                setAllEventData();
            });
        }else{
            //Deserializing the object from the intent
            eventModel = (Evento) getIntent().getSerializableExtra("eventObj");
            setAllEventData();
        }

    }

    /**
     * Set the listener for the event and download the logged in user info
     */
    private void setAllEventData(){
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

        int quant = maxPartecipation-actualPartecipation;
        availablePlacesTextView.setText(getResources().getQuantityString(R.plurals.availablePlaces,quant,quant));
        queueTextView.setText(getResources().getQuantityString(R.plurals.queuePlaces,eventModel.getNumeroPartecipantiInCoda(),eventModel.getNumeroPartecipantiInCoda()));

        if(eventModel.getIsImageUploaded()){
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
        }

        //Download the user name of the creator or of the group
        if(!eventModel.getIdUtenteCreatore().isEmpty()){
            Utenti.getNameSurnameOfUser(eventModel.getIdUtenteCreatore(), closureResult -> {
                if(closureResult != null) creatorTextView.setText(closureResult);
            });
        }else if (!eventModel.getIdGruppoCreatore().isEmpty()){
            Gruppi.getGroupName(eventModel.getIdGruppoCreatore(), pair -> {
                if(pair != null) creatorTextView.setText(pair.first);
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

        if(fromCreation){
            Intent i = new Intent("UpdateEvent");
            i.putExtra("Updated", true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }

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

    /**
     * Attraverso il pulsante di condivisione, ci permette di condividere le info dell'evento con applicazioni
     * esterne come WhatsApp, Telegram ecc
     * @param view
     */
    public void onShareButtonClick(View view){

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,       getString(R.string.titleEventPartecipation) + "\n" +
                getString(R.string.shareNameEventPartecipation) + ": " + eventModel.getNome() + "\n" +
                getString(R.string.descriptionEventPartecipation) + ": " + eventModel.getDescrizione() + "\n" +
                getString(R.string.positionEventPartecipation) + "\n" +
                getString(R.string.latitudineEventPartecipation) + ": " + eventModel.getLatitudine() + "\n" +
                getString(R.string.longitudineEventPartecipation) + ": " + eventModel.getLongitudine() + "\n" +
                getString(R.string.dataEventPartecipation) + ": " + eventModel.getDataOra() + "\n");

        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share to");
        startActivity(shareIntent);
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
                        if(closureBool){
                            Toast.makeText(getApplicationContext(),R.string.eventAbandonedCorrectly, Toast.LENGTH_SHORT).show();
                            Intent i = new Intent("UpdateListPartecipations");
                            i.putExtra("UpdatedListPartecipations", true);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                        }
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