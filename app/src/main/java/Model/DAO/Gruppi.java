package Model.DAO;

import android.net.Uri;
import android.util.Pair;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.FirebaseStorage.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.Gruppo;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti i Gruppi
 * memorizzati su firestore.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO Gruppo.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe Gruppo.
 */
public class Gruppi extends ResultsConverter{

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    private static final String GRUPPO_COLLECTION = "Gruppi";

    /**
     * CONSTANTI CHE INDICANO I NOMI DEI CAMPI SU FIREBASE
     */
    public static final String NOME_FIELD = "nome";
    public static final String DESCRIZIONE_FIELD = "descrizione";
    public static final String COMPONENTI_FIELD = "idComponenti";



    /**
     * Metodo che permette di modificare uno o più campi del gruppo con id passato come parametro.
     *
     * @param groupId id del gruppo di cui si vole cambiare i valori.
     * @param closureBool invocato con true se l'esecuzione va a buon fine, false altrimenti.
     * @param firstField il nome del primo campo da aggiornare
     * @param firstValue nuovo valore da inserire nel primo campo sopra citato.
     * @param otherFieldAndValues array di oggetti con altri campi e valori da sostituire.
     */
    public static final void updateFields(String groupId,ClosureBoolean closureBool, String firstField, Object firstValue, Object... otherFieldAndValues ){
        FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(groupId).update(firstField,firstValue,otherFieldAndValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /**
     * Metodo che restituisce il nome del gruppo il cui id è dato come parametro.
     *
     * @param groupId id del gruppo di cui si vuole sapere il nome.
     * @param closureRes invocato con un Pair<String, Boolean> contenente il nome del gruppo e una flag indicante se l'utente loggato è admin del gruppo se il task va a buon fine, null altrimenti.
     */
    public static final void getGroupName(String groupId, ClosureResult<Pair<String,Boolean>> closureRes){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(groupId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if (closureRes != null){
                        Gruppo g = task.getResult().toObject(Gruppo.class);
                        if(g != null)
                            closureRes.closure(new Pair<>(g.getNome(), (g.getIdAmministratore().equals(AuthHelper.getUserId())) ? true : false ));
                        else
                            closureRes.closure(null);
                    }
                }else {
                    if(closureRes != null) closureRes.closure(null);
                }
            });
        }
    }

    /**
     * Metodo che permette di rimuovere un partecipante dal gruppo.
     *
     * @param idUser id dell'utente da rimuovere.
     * @param idGroup id del gruppo a cui si vuole rimuovere un partecipante.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void removeUserFromGroup(String idUser, String idGroup, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGroup).update("idComponenti", FieldValue.arrayRemove(idUser)).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Metodo che permette la cancellazione di un gruppo il cui id viene dato come parametro.
     *
     * @param idGroup id del gruppo da cancellare.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void deleteGroup(String idGroup, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGroup).delete().addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Metodo che permette di aggiungere un nuovo utente ad un gruppo.
     *
     * @param idNewUser id dell'utente da aggiungere.
     * @param idGroup id del gruppo in cui si vuole aggiungere il nuovo utente.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void addUserToGroup(String idNewUser, String idGroup, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGroup).update("idComponenti", FieldValue.arrayUnion(idNewUser)).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Metodo che restituisce una lista di gruppi di cui l'utente è membro.
     *
     * @param closureList invocato con una lista di gruppi se il task va a buon fine, null altrimenti.
     */
    public static final void getAllMyGroups(ClosureList<Gruppo> closureList){
        if(AuthHelper.isLoggedIn()){
            Task secondTask = FirestoreHelper.db.collection(GRUPPO_COLLECTION).whereArrayContains("idComponenti",AuthHelper.getUserId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        if(closureList != null) closureList.closure(convertResults(task,Gruppo.class));
                    }else{
                        if(closureList != null) closureList.closure(null);
                    }
                }
            });
        }
    }

    /**
     * Metodo che resituisce la lista di gruppi di cui l'utente è amministratore.
     *
     * @param closureList invocato con una lista di gruppi se il task va a buon fine, null altrimenti.
     */
    public static final void getAdministrationGroups(ClosureList<Gruppo> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).whereEqualTo("idAmministratore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if (closureList != null){
                    if(task.isSuccessful() ){
                        closureList.closure(convertResults(task,Gruppo.class));
                    }else closureList.closure(null);
                }
            });
        }
    }

    /**
     * Metodo usato per creare un nuovo gruppo in Firestore nella tabella Gruppi.
     * L'id del gruppo viene creato randomicamente da Firebase e quello presente nell'oggetto passato come
     * parametro viene ignorato.
     *
     * @param g oggetto Gruppo da creare su firebase.
     * @param closureRes invocato con l'id del gruppo appena creato se la creazione va a buon fine, null altrimenti.
     */
    public static final void addNewGroup(Gruppo g, ClosureResult<String> closureRes){

        FirestoreHelper.db.collection(GRUPPO_COLLECTION).add(g).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String groupId = task.getResult().getId();
                if(g.getImmagine()!= null) {
                    uploadGroupImage(g.getImmagine(),groupId,isSuccessful -> {
                        if(isSuccessful){
                            if (closureRes != null) closureRes.closure(groupId);
                        }else{
                            if (closureRes != null) closureRes.closure(null);
                        }

                    });
                }else{
                    if (closureRes != null) closureRes.closure(groupId);
                }
            }else{
                if (closureRes != null) closureRes.closure(null);
            }

        });
    }

    /**
     * Metodo che restituisce il gruppo con lo specifico id passato come parametro.
     *
     * @param idGruppo id del gruppo che si vuole ottenere.
     * @param closureRes invocato con l'oggetto che si è richiesto se trovato, null altrimenti.
     */
    public static final void getGroup(String idGruppo, ClosureResult<Gruppo> closureRes){
        FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGruppo).get().addOnCompleteListener(task -> {
            if(closureRes!= null){
                if(task.isSuccessful()){
                    closureRes.closure(task.getResult().toObject(Gruppo.class));
                }else closureRes.closure(null);
            }
        });
    }

    /**
     * Metodo che permette di caricare l'immagine del gruppo con id passato come parametro su Firestore Storage.
     * L'immagine viene salvata in una directory chiamata con lo stesso id del gruppo
     * e viene sostituita se gia esistente.
     *
     * L'immagine viene messa nella seguente direcotry: Gruppi/\idGruppo\/immagineProfilo
     *
     * @param file uri dell'immagine da caricare.
     * @param idGruppo id del gruppo a cui associare l'immagine.
     * @param closureBool invocato con true se l'upload va a buon fine, false altrimenti.
     */
    public static final void uploadGroupImage(Uri file, String idGruppo, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = "Gruppi/" + idGruppo + "/immagineProfilo";
        StorageHelper.uploadImage(file, finalChildName, new ClosureBoolean() {
            @Override
            public void closure(boolean isSuccess) {
                if(!isSuccess){
                    if(closureBool != null) closureBool.closure(false);
                } else {
                    FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGruppo).update("isImmagineUploaded", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(closureBool != null) closureBool.closure(task.isSuccessful());
                        }
                    });
                }
            }
        });
    }

    /**
     * Metodo che permette di scaricare l'immagine del gruppo con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param idGruppo id del gruppo di cui si vuole scaricare l'immagine.
     * @param closureBitmap invocato con una bitmap se il download va a buon fine, null altrimenti.
     */
    public static final void downloadGroupImage(String idGruppo, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = GRUPPO_COLLECTION + "/" + idGruppo + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

}
