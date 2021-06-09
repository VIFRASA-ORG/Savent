package Model.DB;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import Helper.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Pojo.Ente;
import Model.Pojo.Utente;

/**
 * Used to do all the query that involves both Utenti and Enti
 */
public class GenericUser extends ResultsConverter{

    private static final String UTENTI_COLLECTION = "Utenti";
    private static final String ENTI_COLLECTION = "Enti";

    /** Check if the given phone number is already used into another account
     *
     * @param phoneNumber   Phone number of the user to search for.
     * @param closureBool    get called with the true is the phone number is already used, false otherwise.
     */
    public static final void isPhoneNumberAlreadyTaken(String phoneNumber, ClosureBoolean closureBool){
        Task utentiTask = FirestoreHelper.db.collection(UTENTI_COLLECTION).whereEqualTo("numeroDiTelefono",phoneNumber).get();
        Task entiTask = FirestoreHelper.db.collection(ENTI_COLLECTION).whereEqualTo("numeroTelefono",phoneNumber).get();

        Task combinaedTask = Tasks.whenAllComplete(utentiTask,entiTask).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
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
            }
        });
    }

}
