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

/**
 * Activity inerente alla gestione del dettaglio del singolo evento.
 */
public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Model che contiene tutte le informazioni mostrate nell'interfaccia
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

        //riferimenti a tutti i componenti dell'interfaccia
        inflateAll();

        //Impostazione di mapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fromCreation = getIntent().getBooleanExtra("Creation",false);

        //Controllo se stiamo arrivando qui dalla notifica (notification)
        boolean fromNotification = getIntent().getBooleanExtra(NotificationActivity.FROM_NOTIFICATION_INTENT,false);
        if(fromNotification){
            String eventId = getIntent().getStringExtra("eventId");

            //lettura dell'evento
            Eventi.getEvent(eventId, evento -> {
                eventModel = evento;
                setAllEventData();
            });
        }else{
            //Deserializzazione dell'oggetto per l'intent
            eventModel = (Evento) getIntent().getSerializableExtra("eventObj");
            setAllEventData();
        }

    }

    /**
     * Imposta il listener per l'evento e scarica le informazioni dell'utente connesso
     */
    private void setAllEventData(){
        //Aggiungo il listener per gli aggiornamenti su firestore
        Eventi.addDocumentListener(eventModel.getId(),this,closureResult -> {
            if(closureResult != null){
                eventModel = closureResult;
                refreshData();
            }
        });

        //Scaricare le informazioni dell'utente
        Utenti.getUser(AuthHelper.getUserId(),utente -> {
            userModel = utente;
        });

        refreshData();
    }

    private void refreshData(){
        //Inserire tutte le informazioni sul model nella vista
        titleTextView.setText(eventModel.getNome());
        descriptionTextView.setText(eventModel.getDescrizione());
        dateTime.setText(eventModel.getNeutralData());

        //Calcolo dei posti disponibili e del numero dei partecipanti in coda
        int maxPartecipation = eventModel.getNumeroMassimoPartecipanti();
        int actualPartecipation = eventModel.getNumeroPartecipanti();

        int quant = maxPartecipation-actualPartecipation;
        availablePlacesTextView.setText(getResources().getQuantityString(R.plurals.availablePlaces,quant,quant));
        queueTextView.setText(getResources().getQuantityString(R.plurals.queuePlaces,eventModel.getNumeroPartecipantiInCoda(),eventModel.getNumeroPartecipantiInCoda()));

        //controllo dell'image del evento
        if(eventModel.getIsImageUploaded()){
            //Dobbiamo scaricare di nuovo l'immagine perché l'intent ti consente di passare solo file di dimensioni inferiori a 1mb
            Eventi.downloadEventImage(eventModel.getId(), (ClosureBitmap) result -> {
                eventModel.setImageBitmap(result);
                eventLocandinaImageView.setImageBitmap(result);
            });

            //Scarica l'URL
            Eventi.downloadEventImageUri(eventModel.getId(), new ClosureResult<Uri>() {
                @Override
                public void closure(Uri result) {
                    if(result != null){
                        eventModel.setImageUri(result);
                    }
                }
            });
        }

        //Scarica il nome utente del creatore o del gruppo
        if(!eventModel.getIdUtenteCreatore().isEmpty()){
            Utenti.getNameSurnameOfUser(eventModel.getIdUtenteCreatore(), closureResult -> {
                if(closureResult != null) creatorTextView.setText(closureResult);
            });
        }else if (!eventModel.getIdGruppoCreatore().isEmpty()){
            Gruppi.getGroupName(eventModel.getIdGruppoCreatore(), pair -> {
                if(pair != null) creatorTextView.setText(pair.first);
            });
        }

        //Trova la mappa nella posizione corretta
        addMarkerToPosition(new LatLng(eventModel.getLatitudine(),eventModel.getLongitudine()));

        //Abilita o disabilita il pulsante corretto all'interno della vista
        participationManager();
    }

    /**
     * Metodo che esegue tutti i controlli e attiva o disattiva il componente corretto
     * in base all'utente e alla partecipazione all'evento.
     */
    private void participationManager(){

        //prova a scaricare l'istanza di partecipazione a questo evento
        Partecipazioni.getMyPartecipationAtEvent(eventModel.getId(), partecipazione -> {
            if(partecipazione != null){
                //L'utente ha già aderito all'evento

                if(partecipazione.getAccettazione()){
                    if(partecipazione.getListaAttesa()){
                        //Accettato e in coda
                        setPageMode(PageMode.LEAVE_EVENT_FROM_QUEUE);
                    }else{
                        //Accettato ma non in coda
                        setPageMode(PageMode.LEAVE_EVENT_FROM_PARTICIPATION);
                    }
                }else{
                    if(partecipazione.getListaAttesa()){
                        //Non accettato ma in coda
                        setPageMode(PageMode.LEAVE_EVENT_FROM_QUEUE_DUE_STATUS);
                    }
                }
            }else{
                //L'utente non si è ancora unito all'evento
                setPageMode(PageMode.JOIN_EVENT);
            }
        });
    }

    /**
     * Abilita i componenti corretti in base alla modalità data.
     *
     * @param newPageMode la nuova modalità.
     */
    private void setPageMode(PageMode newPageMode){
        switch (newPageMode){
            case JOIN_EVENT:
                //Non ho ancora partecipato all'evento
                buttonLeave.setVisibility(View.GONE);
                buttonPartecipate.setVisibility(View.VISIBLE);
                buttonPartecipate.setEnabled(true);
                messageBox.setVisibility(View.GONE);
                break;
            case LEAVE_EVENT_FROM_QUEUE:
                //Iscritto ma in coda
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.queueMessage);
                break;
            case LEAVE_EVENT_FROM_PARTICIPATION:
                //Iscritto e non in coda
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.participationMessage);
                break;

            case LEAVE_EVENT_FROM_QUEUE_DUE_STATUS:
                //Iscritto ma in coda a causa del requisito minimo di salute non soddisfatto.
                buttonLeave.setVisibility(View.VISIBLE);
                buttonPartecipate.setVisibility(View.GONE);
                buttonLeave.setEnabled(true);

                messageBox.setVisibility(View.VISIBLE);
                messageBox.setText(R.string.messageStatus);
                break;
        }
    }

    /**
     * Metodo utilizzato per ottenere tutti i riferimenti dell'interfaccia dal file xml
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
     * pulsante "back" che permette di tornare all'activity precedente.
     * Tale pulsante manda anche messaggi in broadcast locale per aggiornare gli eventi
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
     * Metodo per il click al pulsante di partecipazione all'evento.
     * Controlla se ci sono posti disponibili e se viene soddisfatto lo stato di salute.
     * @param view
     */
    public void onParticipateButtonPressed(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yesValue, joinEventListener)
                .setNegativeButton("No", joinEventListener)
                .setTitle(R.string.areUSure);

        if(userModel == null || eventModel == null) return;

        // Determina il messaggio da inserire nella finestra di dialog
        if(userModel.getStatusSanitario() <= eventModel.getSogliaAccettazioneStatus()){
            //Lo stato di salute è OK
            if(eventModel.getNumeroPartecipanti() < eventModel.getNumeroMassimoPartecipanti()){
                // Lo stato di salute è OK, e ci sono Posti disponibili
                builder.setMessage(R.string.popUpJoinHealthOkPlacesOk);
            }else{
                // Lo stato di salute è OK, ma non ci sono Posti disponibili
                builder.setMessage(R.string.popUpJoinHealthOkPlacesNO);
            }
        }else{
            //Lo stato di salute NON è OK
            //Il messaggio è lo stesso per entrambi i casi
            builder.setMessage(R.string.popUpJoinHealthNO);
        }

        builder.show();
    }

    /**
     * Pulsante abbandona evento
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

    /**
     * DialogInterface.OnClickListener
     * SI USA PER GESTIRE L'OnClick SUI DUE PULSANTI NELLA DIALOG PER PARTECIPARE E LASCIARE UN EVENTO.
     */
    DialogInterface.OnClickListener leaveEventListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Abbandona evento da un evento specifico.
                    Partecipazioni.removeMyPartecipationTransaction(eventModel.getId(), closureBool -> {
                        if(closureBool){
                            Toast.makeText(getApplicationContext(),R.string.eventAbandonedCorrectly, Toast.LENGTH_SHORT).show();
                            //messaggio in broadcast per l'aggiornamento della lista delle partecipazioni
                            Intent i = new Intent("UpdateListPartecipations");
                            i.putExtra("UpdatedListPartecipations", true);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                        }
                        //abbandono non riuscito
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
                    //Partecipazione all'evento specifico.
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

    /**
     * OVERRIDE DI ALCUNI METODI NELL'INTERFACCIA OnMapReadyCallback
     * @param googleMap
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

    /**
     * aggiunta del marker nella posizione indicata dalla latitudine e longitudine indicata con rispettivo nome e icona dell'evento
     * @param latLng
     */
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
     * Sposta la camera della mappa nella posizione indicata
     * @param location posizione in cui deve essere spostata la camera
     */

    private void moveToLocation(LatLng location) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));
        // Ingrandisci (Zoom in), animando la camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Rimpicciolisci (Zoom out) per ingrandire il livello 10, animando con una durata di 2 secondi.
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


    /**
     * ENUM UTILIZZATO PER DEFINIRE LO STATO DELLA PAGINA
     */
    private enum PageMode{
        JOIN_EVENT,
        LEAVE_EVENT_FROM_QUEUE,
        LEAVE_EVENT_FROM_PARTICIPATION,
        LEAVE_EVENT_FROM_QUEUE_DUE_STATUS;
    }

}