package Model.DAO;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import Helper.FirebaseStorage.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.POJO.Ente;
import Model.POJO.Utente;

/**
 * Used to do all the query that involves both Utenti and Enti
 */
/**
 * Classe DAO (Data Access Object) che fornisce alcuni metodi
 * comuni tra gli Enti e gli Utenti memorizzati su Firestore.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe Utente o Ente.
 */
public class GenericUser extends ResultsConverter{

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    private static final String UTENTI_COLLECTION = "Utenti";
    private static final String ENTI_COLLECTION = "Enti";

    /**
     * Metodo che controlla se il numero di telefono passato come parametro è gia utilizzato
     * da un altro account, che esso sia un Ente o un Utente.
     *
     * @param phoneNumber numero di telefono da cercare.
     * @param closureBool invocato con true se il numero di telefono è gia utilizzato, false altrimenti.
     */
    public static final void isPhoneNumberAlreadyTaken(String phoneNumber, ClosureBoolean closureBool){
        Task utentiTask = FirestoreHelper.db.collection(UTENTI_COLLECTION).whereEqualTo("numeroDiTelefono",phoneNumber).get();
        Task entiTask = FirestoreHelper.db.collection(ENTI_COLLECTION).whereEqualTo("numeroTelefono",phoneNumber).get();

        Task combinaedTask = Tasks.whenAllComplete(utentiTask,entiTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<Utente> l = convertResults((Task<QuerySnapshot>) task.getResult().get(0),Utente.class);
                List<Ente> e = convertResults((Task<QuerySnapshot>) task.getResult().get(1),Ente.class);

                if(e.size() == 0 && l.size() == 0){
                    if(closureBool!=null) closureBool.closure(false);
                }else{
                    if(closureBool!=null) closureBool.closure(true);
                }

            }else{
                if(closureBool!=null) closureBool.closure(true);
            }
        });
    }

}
