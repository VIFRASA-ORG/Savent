package Model.DAO;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che deve essere implementata da tutte le classi DAO
 * per convertire direttamente i risultati dei task nella relativa classe POJO.
 */
public abstract class ResultsConverter {

    protected static <E> List<E> convertResults(Task<QuerySnapshot> task, Class<E> refClass){
        ArrayList<E> list = new ArrayList<>();
        for(QueryDocumentSnapshot document : task.getResult()){
            E e = document.toObject(refClass);
            list.add(e);
        }
        return list;
    }

}
