package Model.DB;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.Evento;
import Model.Pojo.Partecipazione;
import Model.Pojo.Utente;

public class Partecipazioni extends ResultsConverter {

    private static final String PARTECIPAZIONE_COLLECTION = "Partecipazioni";
    private static final String EVENTO_COLLECTION = "Eventi";
    private static final String UTENTI_COLLECTION = "Utenti";


    /** Return the participation object of the logged in user to the specified event.
     *
     * @param eventId event id of the participation.
     * @param closureResult return the participation instance of the user to the event if exists, null otherwise.
     */
    public static final void getMyPartecipationAtEvent(String eventId, ClosureResult<Partecipazione> closureResult){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idUtente",AuthHelper.getUserId()).whereEqualTo("idEvento",eventId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        List<Partecipazione> listP = convertResults(task,Partecipazione.class);

                        //If we have only one result
                        if(listP.size() == 1){
                            if(closureResult!=null) closureResult.closure(listP.get(0));
                        }else{
                            if(closureResult!=null) closureResult.closure(null);
                        }
                    }
                }
            });
        }
    }

    /** Add the partecipation to a specific event.
     *  The added user is the logged one.
     *
     * @param idEvento Event whose participation you want to add.
     * @param closureResult return the participation instance created on the server.
     */
    public static final void addMyPartecipationTransaction(String idEvento, ClosureResult<Partecipazione> closureResult){

        //Creating the Partecipazione instance
        Partecipazione p = new Partecipazione();
        p.setDataOra(new Date());
        p.setIdUtente(AuthHelper.getUserId());
        p.setIdEvento(idEvento);

        final DocumentReference eventDocument = FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento);
        final DocumentReference partecipationDocument = FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).document();
        final DocumentReference userDocument = FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId());

        FirestoreHelper.db.runTransaction(transaction -> {
            DocumentSnapshot snapshotEvent = transaction.get(eventDocument);
            DocumentSnapshot snapshotUtente = transaction.get(userDocument);
            Evento e = snapshotEvent.toObject(Evento.class);
            Utente u = snapshotUtente.toObject(Utente.class);

            //Check if the status sanitario is below the maximum
            if(u.getStatusSanitario() <= e.getSogliaAccettazioneStatus()){
                p.setAccettazione(true);

                //If there is free places at the event
                if(e.getNumeroMassimoPartecipanti()-e.getNumeroPartecipanti() > 0){
                    //Increment the number of the partecipant
                    transaction.update(eventDocument,"numeroPartecipanti", FieldValue.increment(1));
                    p.setListaAttesa(false);
                }else{
                    //Since there are no other available places, he will go into the Waiting list
                    transaction.update(eventDocument,"numeroPartecipantiInCoda", FieldValue.increment(1));
                    p.setListaAttesa(true);
                }

            } else {
                p.setAccettazione(false);

                //Since his status sanitario is not compatible, he will go into the Waiting list
                transaction.update(eventDocument,"numeroPartecipantiInCoda", FieldValue.increment(1));
                p.setListaAttesa(true);
            }

            transaction.set(partecipationDocument,p);
            return p;
        }).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(closureResult != null) closureResult.closure(task.getResult());
            }else{
                if(closureResult != null) closureResult.closure(null);
            }
        });
    }

    /** Remove the participation from a specifi event.
     *  The removed participation belogns to the logged in user.
     *
     * @param idEvento event from which you want to remove the participation.
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void removeMyPartecipationTransaction(String idEvento,ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idEvento",idEvento).whereEqualTo("idUtente",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                List<Partecipazione> part = convertResults(task,Partecipazione.class);

                if(part.size() == 1){

                    Partecipazione p = part.get(0);

                    final DocumentReference eventDocument = FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento);
                    final DocumentReference partecipationDocument = FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).document(p.getDocumentId());

                    //Doing a transaction
                    FirestoreHelper.db.runTransaction((Transaction.Function<Void>) transaction -> {

                        //Decrement the counter field
                        if(p.getAccettazione()) transaction.update(eventDocument,"numeroPartecipanti",FieldValue.increment(-1));
                        else transaction.update(eventDocument,"numeroPartecipantiInCoda",FieldValue.increment(-1));

                        transaction.delete(partecipationDocument);

                        return null;
                    }).addOnCompleteListener(task12 -> {
                        if(closureBool != null) closureBool.closure(task12.isSuccessful());
                    });

                }else{
                    if(closureBool != null) closureBool.closure(false);
                }
            });
        }
    }

    /** Return a list of Partecipazione with all the user's participation to events.
     *
     * @param closureList get called with a List of Partecipazione if the task is successful, null otherwise.
     */
    public static final void getMyPartecipations(ClosureList<Partecipazione> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idUtente",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if(closureList != null){
                    if(task.isSuccessful()) closureList.closure(convertResults(task,Partecipazione.class));
                    else closureList.closure(null);
                }
            });
        }
    }
}
