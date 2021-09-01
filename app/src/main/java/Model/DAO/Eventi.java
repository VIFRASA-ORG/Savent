package Model.DAO;

import android.app.Activity;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.Maps.LocationBoundaries;
import Helper.Maps.MapsHelper;
import Helper.FirebaseStorage.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.*;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti gli Eventi
 * memorizzati su firestore.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO Evento.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe Evento.
 */
public class Eventi extends ResultsConverter {

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    private static final String EVENTO_COLLECTION = "Eventi";

    /**
     * CONSTANTI CHE INDICANO I NOMI DEI CAMPI SU FIREBASE
     */
    public static final String NOME_FIELD = "nome";
    public static final String DESCRIZIONE_FIELD = "descrizione";
    public static final String MAX_PARTECIPANTI_FIELD = "numeroMassimoPartecipanti";
    public static final String STATUS_SOGLIA_FIELD = "sogliaAccettazioneStatus";
    public static final String LATITUDINE_FIELD = "latitudine";
    public static final String LONGITUDINE_FIELD = "longitudine";
    public static final String DATA_ORA_FIELD = "dataOra";
    public static final String UTENTE_CREATORE_FIELD = "idUtenteCreatore";
    public static final String GRUPPO_CREATORE_FIELD = "idGruppoCreatore";


    /**
     * Metodo che permette di modificare uno o più campi dell'evento con id passato come parametro.
     *
     * @param eventId id dell'evento di cui si vole cambiare i valori.
     * @param closureBool invocato con true se l'esecuzione va a buon fine, false altrimenti.
     * @param firstField il nome del primo campo da aggiornare
     * @param firstValue nuovo valore da inserire nel primo campo sopra citato.
     * @param otherFieldAndValues array di oggetti con altri campi e valori da sostituire.
     */
    public static final void updateFields(String eventId,ClosureBoolean closureBool, String firstField, Object firstValue, Object... otherFieldAndValues ){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(eventId).update(firstField,firstValue,otherFieldAndValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /**
     * Metodo che aggiunge un lister sul documento Evento il cui id viene passato come parametro.
     *
     * @param eventId id dell'evento di cui ci si vuole mettere in ascolto per i cambiamenti.
     * @param activity il contesto dell'apllicazione o dell'activity.
     * @param closureResult invocato con il nuovo oggetto aggiornato tutte le volte che si verifica un aggiornamento, null altrimenti.
     */
    public static final void addDocumentListener(String eventId, Activity activity, ClosureResult<Evento> closureResult){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(eventId).addSnapshotListener(activity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    if(closureResult != null) closureResult.closure(null);
                }

                if (value != null && value.exists()) {
                    if(closureResult != null) closureResult.closure(value.toObject(Evento.class));
                } else {
                    if(closureResult != null) closureResult.closure(null);
                }
            }
        });
    }

    /**
     *  Metodo che ricerca tutti gli eventi nella regione di mappa visibile.
     *
     * @param region la regione di mappa visibile.
     * @param closureList invocato con la lista di Eventi se il task va a buon fine , null altrimenti.
     */
    public static final void getEventInRegion(VisibleRegion region, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){

            //Calcolo i confini in cui cercare
            double maxLatitude = MapsHelper.maxCoordinate(region.farLeft.latitude,region.farRight.latitude,region.nearRight.latitude,region.nearLeft.latitude);
            double minLatitude = MapsHelper.minCoordinate(region.farLeft.latitude,region.farRight.latitude,region.nearRight.latitude,region.nearLeft.latitude);

            double maxLongitude = MapsHelper.maxCoordinate(region.farLeft.longitude,region.farRight.longitude,region.nearRight.longitude,region.nearLeft.longitude);
            double minLongitude = MapsHelper.minCoordinate(region.farLeft.longitude,region.farRight.longitude,region.nearRight.longitude,region.nearLeft.longitude);

            //Cerco tutti gli eventi all'interno della fascia latitudinale
            Task latitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("latitudine",maxLatitude)
                    .whereGreaterThan("latitudine",minLatitude)
                    .get();

            //Cerco tutti gli eventi all'interno della fascia longitudinale
            Task longitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("longitudine",maxLongitude)
                    .whereGreaterThan("longitudine",minLongitude)
                    .get();

            Task combinedTask = Tasks.whenAllComplete(latitudeTask,longitudeTask).addOnCompleteListener( task -> {
                if(closureList != null){
                    if(task.isSuccessful()){
                        List<Evento> listFirstQuery = new ArrayList<Evento>();
                        List<Evento> listSecondQuery = new ArrayList<Evento>();

                        listFirstQuery.addAll(convertResults((Task<QuerySnapshot>) task.getResult().get(0),Evento.class));
                        listSecondQuery.addAll(convertResults((Task<QuerySnapshot>) task.getResult().get(1),Evento.class));

                        //Se l'evento è in tutte e due le lista, vuol dire che soddisfa sia i constraint di latitudine che di longitudine
                        //Effettuo l'intersezione tra le due liste. logica AND.
                        listFirstQuery.retainAll(listSecondQuery);

                        closureList.closure(listFirstQuery);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Metodo che ricerca tutti gli eventi nelle vicinanze di una determinata posizione data come parametro.
     *
     * @param refPosition posizione di riferimento.
     * @param radius la distanza massima in cui cercare, in tutte le direzioni, dalla posizione di riferimento.
     * @param closureList invocato con la lista di Eventi se il task va a buon fine , null altrimenti.
     */
    public static final void getNearbyEvent(LatLng refPosition, int radius, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){

            //Calcolo i confini in cui cercare
            LocationBoundaries boundaries = MapsHelper.calcBoundaries(refPosition,radius);

            //Cerco tutti gli eventi all'interno della fascia latitudinale
            Task latitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("latitudine",boundaries.upperLatitude)
                    .whereGreaterThan("latitudine",boundaries.lowerLatitude)
                    .get();

            //Cerco tutti gli eventi all'interno della fascia longitudinale
            Task longitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("longitudine",boundaries.rightLongitude)
                    .whereGreaterThan("longitudine",boundaries.leftLongitude)
                    .get();

            Task combinedTask = Tasks.whenAllComplete(latitudeTask,longitudeTask).addOnCompleteListener( task -> {
                if(closureList != null){
                    if(task.isSuccessful()){
                        List<Evento> listFirstQuery = new ArrayList<Evento>();
                        List<Evento> listSecondQuery = new ArrayList<Evento>();

                        listFirstQuery.addAll(convertResults((Task<QuerySnapshot>) task.getResult().get(0),Evento.class));
                        listSecondQuery.addAll(convertResults((Task<QuerySnapshot>) task.getResult().get(1),Evento.class));

                        //Se l'evento è in tutte e due le lista, vuol dire che soddisfa sia i constraint di latitudine che di longitudine
                        //Effettuo l'intersezione tra le due liste. logica AND.
                        listFirstQuery.retainAll(listSecondQuery);

                        closureList.closure(listFirstQuery);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Metodo che cerca tutti gli eventi a cui l'utente loggato partecipa e li restituisce.
     *
     * @param closureList invocato con la lista di Eventi se il task va a buon fine , null altrimenti.
     */
    public static final void getMyParticipationEvents(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            Partecipazioni.getMyPartecipations(list -> {
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();

                for (Partecipazione p : list) {
                    Task t = FirestoreHelper.db.collection(EVENTO_COLLECTION).document(p.getIdEvento()).get();
                    taskList.add(t);
                }

                Task combinedTasks = Tasks.whenAllComplete(taskList).addOnCompleteListener(task -> {
                    if(closureList != null){
                        if(task.isSuccessful()){
                            List<Evento> finalList = new ArrayList<Evento>();
                            for(Task<?> t : task.getResult()){

                                DocumentSnapshot d = (DocumentSnapshot) t.getResult();
                                Evento ev = d.toObject(Evento.class);
                                if(ev != null) finalList.add(ev);
                            }
                            closureList.closure(finalList);
                        }else closureList.closure(null);
                    }
                });
            });
        }
    }

    /**
     * Metodo che ritorna l'oggetto di uno specifico evento identificato dall'id dato come parametro.
     *
     * @param idEvento id dell'evento da cercare.
     * @param closureRes invocato con l'oggetto Evento se trovato, null altrimenti.
     */
    public static final void getEvent(String idEvento, ClosureResult<Evento> closureRes){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Evento e = task.getResult().toObject(Evento.class);
                if (closureRes != null) closureRes.closure(e);
            }else{
                if (closureRes != null) closureRes.closure(null);
            }
        });

    }

    /**
     * Metodo che ritorna una lista con tutti gli eventi creati dall'utente loggato.
     *
     * @param closureList invocato con la lista se il task va a buon fine, null altrimenti
     */
    public static final void getMyEvent(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(EVENTO_COLLECTION).whereEqualTo("idUtenteCreatore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if (closureList != null){
                    if(task.isSuccessful()){
                        List<Evento> l = convertResults(task,Evento.class);

                        //Ordino la lista per nome in quanto non è possibile farlo direttamente nella query
                        Collections.sort(l);
                        closureList.closure(l);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Metodo che ritorna una lista di eventi creati dai gruppi i cui id sono dati come parametro.
     *
     * @param adminGroupIdList lista di id di gruppi di cui si vogliono conoscere gli eventi creati.
     * @param closureList invocato con la lista se il task va a buon fine, null altrimenti
     */
    private static final void getAllEventCreatedByMyAdminGroup(List<String> adminGroupIdList, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            if(adminGroupIdList.size() == 0){
                if(closureList != null) closureList.closure(null);
                return;
            }

            //La query IN supporta una lista di massimo 10 elementi
            //Quindi bisogna dividere la lista di id di gruppi in chuck di lunghezza massima 10
            if(adminGroupIdList.size() > 10){
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();
                int numbersOfChucks = adminGroupIdList.size() / 10;

                //Aggiungo la query del singolo chuck alla lista di task
                for(int i=0; i <= numbersOfChucks; i++){
                    Task t;
                    if(i == numbersOfChucks) t = FirestoreHelper.db.collection(EVENTO_COLLECTION).whereIn("idGruppoCreatore",adminGroupIdList.subList(10*i,adminGroupIdList.size())).get();
                    else t = FirestoreHelper.db.collection(EVENTO_COLLECTION).whereIn("idGruppoCreatore",adminGroupIdList.subList(10*i,10*i+10)).get();
                    taskList.add(t);
                }

                Task combinedTask = Tasks.whenAllComplete(taskList).addOnCompleteListener(task -> {
                    if(closureList != null){
                        if(task.isSuccessful()){
                            List<Evento> finalList = new ArrayList<>();
                            for(Task<?> t : task.getResult()){
                                finalList.addAll(convertResults((Task<QuerySnapshot>) t,Evento.class));
                            }
                            closureList.closure(finalList);
                        }else closureList.closure(null);
                    }
                });

            }else{
                FirestoreHelper.db.collection(EVENTO_COLLECTION).whereIn("idGruppoCreatore",adminGroupIdList).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(closureList != null) closureList.closure(convertResults(task,Evento.class));
                    }else{
                        if(closureList != null) closureList.closure(null);
                    }
                });
            }
        }
    }

    /**
     * Metodo che ritorna tutti gli eventi creati dall'utente o dai gruppi di cui l'utente è membro.
     *
     * @param closureList invocato con la lista se il task va a buon fine, null altrimenti
     */
    public static final void getAllMyEvents(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            List<Evento> listEventi = new ArrayList<>();

            //Downloading all the event created by the logged in user
            //Scarico tutti gli eventi creati dall'utente.
            getMyEvent(list -> {

                //Li aggiungo alla lista finale
                if(list != null) listEventi.addAll(list);

                //Downloading all the group of which the user is the admin or member.
                //Scarico tutti i gruppi di cui l'utente è membro.
                Gruppi.getAllMyGroups(listOfGroup -> {

                    //Se la lista è vuota, ritorno solo quelli fino ad ora scaricati
                    if(listOfGroup == null){
                        if(closureList != null) closureList.closure(listEventi);
                        return;
                    }

                    //Estraggo solo gli id dei gruppi per eseguire la query con l'operatore IN.
                    List<String> adminGroupIdList = new ArrayList<>();
                    for(Gruppo group : listOfGroup ) adminGroupIdList.add(group.getId());

                    //Richiamo la query per ricevere tutti gli eventi creati dai gruppi dell'utente loggato.
                    getAllEventCreatedByMyAdminGroup(adminGroupIdList, newEvents -> {
                        if(newEvents != null) listEventi.addAll(newEvents);

                        if(closureList != null) closureList.closure(listEventi);
                    });

                });

            });
        }
    }

    /**
     * Metodo che crea un nuovo evento su Firestore.
     * L'id dell'evento viene generato automaticamente da Firebase e quello presente nell'oggeto viene ignorato.
     *
     * @param e evento da aggiungere su Firebase.
     * @param closureResult invocato con l'id dell'evento appena creato se il task va a buon fine, null altrimenti.
     */
    public static final void addNewEvent(Evento e, ClosureResult<String> closureResult){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).add(e).addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                if (closureResult != null) closureResult.closure(task.getResult().getId());
            }else{
                if (closureResult != null) closureResult.closure(null);
            }

        });
    }

    /**
     * Metodo che ritorna una lista di tutti gli eventi presneti su Firebase.
     *
     * @param closureList invocato con la lista se il task va a buon fine, null altrimenti
     */
    public static final void getAllEvent(ClosureList<Evento> closureList){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).orderBy("nome").get().addOnCompleteListener(task -> {
            if(closureList != null){
                if(task.isSuccessful() ){
                    List<Evento> events = new ArrayList<>();
                    closureList.closure(convertResults(task,Evento.class));
                }else closureList.closure(null);
            }
        });
    }

    /**
     * Metodo che permette di caricare la locandina dell'evento con id passato come parametro su Firestore Storage.
     * L'immagine viene salvata in una directory chiamata con lo stesso id dell'evento
     * e viene sostituita se gia esistente.
     *
     * L'immagine viene messa nella seguente direcotry: Eventi/\idEvento\/locandina
     *
     * @param file uri dell'immagine da caricare.
     * @param idEvento id dell'evento a cui associare l'immagine.
     * @param closureBool invocato con true se l'upload va a buon fine, false altrimenti.
     */
    public static final void uploadEventImage(Uri file, String idEvento, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = "Eventi/"+idEvento+"/locandina";
        StorageHelper.uploadImage(file, finalChildName, new ClosureBoolean() {
            @Override
            public void closure(boolean isSuccess) {
                FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).update("isImageUploaded", true).addOnCompleteListener(task -> {
                    if (closureBool != null) closureBool.closure(task.isSuccessful());
                });
            }
        });
    }

    /**
     * Metodo che permette di scaricare la locandina dell'evento con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param idEvento id dell'evento di cui si vuole scaricare la locandina.
     * @param closureBitmap invocato con una bitmap e se il download va a buon fine, null altrimenti.
     */
    public static final void downloadEventImage(String idEvento, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

    /**
     * Metodo che permette di scaricare la locandina di un evento con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param idEvento id dell'evento di cui si vuole scaricare la locandina.
     * @param closureResult invocato con un temp file nella chache se il download va a buon fine, null altrimenti.
     */
    public static final void downloadEventImage(String idEvento, ClosureResult<File> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImage(finalChildName,closureResult);
    }

    /**
     * Metodo che permette di scaricare la locandina di un vento con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param idEvento id dell'evento di cui si vuole scaricare la locandina.
     * @param closureResult invocato con un URI se il download va a buon fine, null altrimenti.
     */
    public static final void downloadEventImageUri(String idEvento, ClosureResult<Uri> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImageUri(finalChildName,closureResult);
    }

    /**
     * Metodo che permette la cancellazione di un evento il cui id viene dato come parametro.
     *
     * @param idEvento id dell'evento da cancellare.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void deleteEvent(String idEvento, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).delete().addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }
}
