package Model.DB;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class used by all the DB class.
 * Contains the method convertResult to convert all the result to a specific type.
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
