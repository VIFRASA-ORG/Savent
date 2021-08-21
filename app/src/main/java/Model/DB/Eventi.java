package Model.DB;
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
import Model.Pojo.*;



public class Eventi extends ResultsConverter {

    private static final String EVENTO_COLLECTION = "Eventi";
    public static final String NOME_FIELD = "nome";
    public static final String DESCRIZIONE_FIELD = "descrizione";
    public static final String MAX_PARTECIPANTI_FIELD = "numeroMassimoPartecipanti";
    public static final String STATUS_SOGLIA_FIELD = "sogliaAccettazioneStatus";
    public static final String LATITUDINE_FIELD = "latitudine";
    public static final String LONGITUDINE_FIELD = "longitudine";
    public static final String DATA_ORA_FIELD = "dataOra";
    public static final String UTENTE_CREATORE_FIELD = "idUtenteCreatore";
    public static final String GRUPPO_CREATORE_FIELD = "idGruppoCreatore";


    /** Update the information of the GROUP.
     *
     * @param eventId the id of the event
     * @param closureBool get called with true if the task is successful, false otherwise.
     * @param firstField the name of the first field to update
     * @param firstValue tha new value of the first field
     * @param otherFieldAndValues an array of object with other field and values.
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
     * Add a listener to all the updates from the server to the selected event.
     *
     * @param eventId id of the event whose changes you want to listen.
     * @param activity the context of the owner activity
     * @param closureResult event that will be invoked every time an update is found. it will give the event object if found, null otherwise.
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
     * Return all the event in the map visible region.
     *
     * @param region the map Visible region.
     * @param closureList closure for the results.
     */
    public static final void getEventInRegion(VisibleRegion region, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){

            //Compute all the boundaries.
            double maxLatitude = MapsHelper.maxCoordinate(region.farLeft.latitude,region.farRight.latitude,region.nearRight.latitude,region.nearLeft.latitude);
            double minLatitude = MapsHelper.minCoordinate(region.farLeft.latitude,region.farRight.latitude,region.nearRight.latitude,region.nearLeft.latitude);

            double maxLongitude = MapsHelper.maxCoordinate(region.farLeft.longitude,region.farRight.longitude,region.nearRight.longitude,region.nearLeft.longitude);
            double minLongitude = MapsHelper.minCoordinate(region.farLeft.longitude,region.farRight.longitude,region.nearRight.longitude,region.nearLeft.longitude);

            Task latitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("latitudine",maxLatitude)
                    .whereGreaterThan("latitudine",minLatitude)
                    .get();

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

                        //If the event is in both list means that is in both the latitude and longitude constraint.
                        //intersection among the two list || and logic
                        listFirstQuery.retainAll(listSecondQuery);

                        closureList.closure(listFirstQuery);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Return all the event in the nearby of a location
     *
     * @param refPosition reference location
     * @param radius tha maximum distance, in all direction, from the reference location.
     * @param closureList closure for the results.
     */
    public static final void getNearbyEvent(LatLng refPosition, int radius, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){

            //compute the boundaries for the query
            LocationBoundaries boundaries = MapsHelper.calcBoundaries(refPosition,radius);

            Task latitudeTask = FirestoreHelper.db.collection(EVENTO_COLLECTION)
                    .whereLessThan("latitudine",boundaries.upperLatitude)
                    .whereGreaterThan("latitudine",boundaries.lowerLatitude)
                    .get();

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

                        //If the event is in both list means that is in both the latitude and longitude constraint.
                        //intersection among the two list || and logic
                        listFirstQuery.retainAll(listSecondQuery);

                        closureList.closure(listFirstQuery);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /** Return a list of all the event in which the user participate
     *
     * @param closureList the parameter list is null in case the task is not successful.
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

    /** Return a specific event
     *
     * @param idEvento
     * @param closureRes the parameter is null in case the task is not successful.
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

    /** Return a list of event created by a specific group
     *
     * @param groupId
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getAllGroupEvents(String groupId, ClosureList<Evento> closureList){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).whereEqualTo("idGruppoCreatore",groupId).get().addOnCompleteListener(task -> {
            if (closureList != null){
                if(task.isSuccessful()){
                    closureList.closure(convertResults(task,Evento.class));
                }else closureList.closure(null);
            }
        });
    }

    /** Return a list with all events created by the logged in user
     *
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getMyEvent(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(EVENTO_COLLECTION).whereEqualTo("idUtenteCreatore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if (closureList != null){
                    if(task.isSuccessful()){
                        List<Evento> l = convertResults(task,Evento.class);

                        //Sorting by name, impossible to do into the query on different field
                        Collections.sort(l);
                        closureList.closure(l);
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Return a list with all the event created by all group given as parameter
     *
     * @param adminGroupIdList the list of all the group id.
     * @param closureList invoked with the list of the event in case of success, null otherwise.
     */
    private static final void getAllEventCreatedByMyAdminGroup(List<String> adminGroupIdList, ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            if(adminGroupIdList.size() == 0){
                if(closureList != null) closureList.closure(null);
                return;
            }

            if(adminGroupIdList.size() > 10){
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();
                int numbersOfChucks = adminGroupIdList.size() / 10;

                //Adding the chunked query to the list
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
     * Return a list with all the events created by the user and created from all groups in which the user is a member.
     *
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getAllMyEvents(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            List<Evento> listEventi = new ArrayList<>();

            //Downloading all the event created by the logged in user
            getMyEvent(list -> {

                //Adding them to the final list
                if(list != null) listEventi.addAll(list);

                //Downloading all the group of which the user is the admin or member.
                Gruppi.getAllMyGroups(listOfGroup -> {

                    //If the list is null
                    //return only the one downloaded since now
                    if(listOfGroup == null){
                        if(closureList != null) closureList.closure(listEventi);
                        return;
                    }

                    //Extrapolating only the id of the group to perform the query with the in operator
                    List<String> adminGroupIdList = new ArrayList<>();
                    for(Gruppo group : listOfGroup ) adminGroupIdList.add(group.getId());

                    getAllEventCreatedByMyAdminGroup(adminGroupIdList, newEvents -> {
                        if(newEvents != null) listEventi.addAll(newEvents);

                        if(closureList != null) closureList.closure(listEventi);
                    });

                });

            });
        }
    }

    /** Add a new event to Firestore. The idEvent is randomly picked and the id inside the pojo object is avoided.
     *
     * @param e event to add to Firestore
     * @param closureResult get called with the event id created if the task is successful, false otherwise
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

    /** Add a new event to Firestore. The idEvent on Firestore is the one inside the pojo object and it is not randomly picked.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void setNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(e.getId()).set(e).addOnCompleteListener(task -> {
            if (closureBool != null) closureBool.closure(task.isSuccessful());
        });
    }

    /** Return a list of all the event on Firestore.
     *
     * @param closureList ClosureList of Event type
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

    /** Upload the event image to the Firestore Storage. It is placed inside a directory named after the event id.
     * It is replaced if already present.
     * The image is placed inside the following path: Eventi/\idEvento\/locandina
     *
     * The user must be logged in.
     *
     * @param file  file to upload
     * @param idEvento  event whose image you want to change
     * @param closureBool   get called with true if the task is successful, false otherwise.
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

    /** Download the event image from the Firestore Storage.
     *
     * User must be logged;
     *
     * @param idEvento  event whose image you want to download.
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadEventImage(String idEvento, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

    /** Download the user image from Firebase Storage.
     *
     * User must be logged-in.
     *
     * @param closureResult get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadEventImage(String idEvento, ClosureResult<File> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImage(finalChildName,closureResult);
    }

    /** Download the user image from Firebase Storage.
     *
     * User must be logged-in.
     *
     * @param closureResult get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadEventImageUri(String idEvento, ClosureResult<Uri> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImageUri(finalChildName,closureResult);
    }


    /** Delete event.
     *
     * @param idEvento   id of the event to delete.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void deleteEvent(String idEvento, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).delete().addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

}
