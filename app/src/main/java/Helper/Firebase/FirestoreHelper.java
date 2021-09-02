package Helper.Firebase;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Classe Helper contenente il riferimento al database Firestore
 * che è il punto di inizio per tutte le operazioni su Firestore.
 */
public class FirestoreHelper {
    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();
}
