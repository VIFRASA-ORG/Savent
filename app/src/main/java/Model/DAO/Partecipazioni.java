package Model.DAO;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import java.util.Date;
import java.util.List;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.Evento;
import Model.POJO.Partecipazione;
import Model.POJO.Utente;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti le partecipazioni
 * degli utenti agli eventi.
 *
 * La partecipazione è risultato di una relazione N a N tra gli utenti e gli eventi
 * in quanto un utente può partecipare ad uno o più eventi e agli eventi possono
 * partecipare zero o più utenti.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO Partecipazione.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe Partecipazione.
 */
public class Partecipazioni extends ResultsConverter {

    /**
     * NOMI DELLE COLLECTION SU FIREBASE USATE DALLA CLASSE
     */
    private static final String PARTECIPAZIONE_COLLECTION = "Partecipazioni";
    private static final String EVENTO_COLLECTION = "Eventi";
    private static final String UTENTI_COLLECTION = "Utenti";


    /**
     * Metodo che ritorna la partecipazione ad uno specifico evento, se esiste, per l'utente loggato.
     *
     * @param eventId id dell'evento a cui partecipa.
     * @param closureResult invocato con l'oggetto Partecipazione trovato, null altrimenti.
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

    /**
     * Metodo che permette all'utente loggato di partecipare ad un evento.
     * Tramite una transaction, verrà creato l'oggetto Partecipazione, verranno modificati
     * i campi dell'evento e verra controllato lo status sanitario dell'utente per capire
     * se inserirlo in coda o accettare direttamente la partecipazione.
     *
     * @param idEvento id dell'evento a cui partecipare.
     * @param closureResult invocato con l'oggetto Partecipazione appena creato se il task ha avuto successo, null altrimenti.
     */
    public static final void addMyPartecipationTransaction(String idEvento, ClosureResult<Partecipazione> closureResult){

        //Creazione dell'oggetto Partecipazione da caricare
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

            //Controllo che lo status sanitario sia sotto il valore soglia dell'evento
            if(u.getStatusSanitario() <= e.getSogliaAccettazioneStatus()){
                p.setAccettazione(true);

                //Controllo se ci sono posti disponibili
                if(e.getNumeroMassimoPartecipanti()-e.getNumeroPartecipanti() > 0){
                    //Incremento il numero di partecipanti all'evento.
                    transaction.update(eventDocument,"numeroPartecipanti", FieldValue.increment(1));
                    p.setListaAttesa(false);
                }else{
                    //Dato che non ci sono posti disponibili, l'utente andra nella lista di attesa.
                    transaction.update(eventDocument,"numeroPartecipantiInCoda", FieldValue.increment(1));
                    p.setListaAttesa(true);
                }

            } else {
                p.setAccettazione(false);

                //Dato che lo status sanitario dell'utente non è compatibile, andra nella lista d'attesa.
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

    /**
     * Metodo che rimuove la partecipazione da uno specifico evento passato come parametro.
     * Esegue la transaction per assicurarsi che tutti i campi vengano aggiornati e che
     * la partecipazione sia cancellata.
     *
     * L'id dell'utente creatore della partecipazione è l'id dell'utente loggato.
     *
     * @param idEvento id dell'evento da cui rimuovere la partecipazione.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void removeMyPartecipationTransaction(String idEvento,ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idEvento",idEvento).whereEqualTo("idUtente",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                List<Partecipazione> part = convertResults(task,Partecipazione.class);

                if(part.size() == 1){

                    Partecipazione p = part.get(0);

                    final DocumentReference eventDocument = FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento);
                    final DocumentReference partecipationDocument = FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).document(p.getDocumentId());

                    //Eseguo una transaction
                    FirestoreHelper.db.runTransaction((Transaction.Function<Void>) transaction -> {

                        //Decremento il field counter.
                        if(p.getAccettazione() && !p.getListaAttesa()) transaction.update(eventDocument,"numeroPartecipanti",FieldValue.increment(-1));
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

    /**
     * Metodo che ritorna una lista di oggetti di tipo Partecipazione che indicano
     * tutte le partecipazioni dell'utente loggato a degli eventi.
     *
     * @param closureList invocato con la lista delle partecipazioni trovate se il task va a buon fine, null altrimenti.
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
