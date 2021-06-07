package Model.DB;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.Maps.LocationBoundaries;
import Helper.Maps.MapsHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.*;



public class Eventi extends ResultsConverter {

    private static final String EVENTO_COLLECTION = "Eventi";


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

    /** Return the Firebase Task which increases the number of participants of a specific event.
     *
     * @param idEvento
     * @return the Firestore Task.
     */
    public static final Task getIncrementNumberOfPartecipantTask(String idEvento){
        return FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).update("numeroPartecipanti", FieldValue.increment(1));
    }

    /** Return the Firebase Task which decreases the number of participants of a specific event.
     *
     * @param idEvento
     * @return the Firestore Task.
     */
    public static final Task getDecrementNumberOfPartecipantTask(String idEvento){
        return FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).update("numeroPartecipanti", FieldValue.increment(-1));
    }

    /** Return a list of all the event created by the logged-in user.
     *
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getMyPartecipationEvents(ClosureList<Evento> closureList){
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
                                finalList.add(d.toObject(Evento.class));
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
                        closureList.closure(convertResults(task,Evento.class));
                    }else closureList.closure(null);
                }
            });
        }
    }

    /** Add a new event to Firestore. The idEvent is randomly picked and the id inside the pojo object is avoided.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void addNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).add(e).addOnCompleteListener(task -> {
            if (closureBool != null) closureBool.closure(task.isSuccessful());
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

}
