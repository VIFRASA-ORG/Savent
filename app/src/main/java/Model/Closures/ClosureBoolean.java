package Model.Closures;

/**
 * Interfaccia creata per essere utilizzata come lambda nelle query definite nelle classi DAO.
 *
 * Closure o chiusura creata per gestire i valori di ritorno delle query nello stesso contesto
 * dell'activity chiamante.
 *
 * In particolare questa chiusura permette di gestire come valore di ritorno
 * un booleano per evitare l'utilizzo dell'interfaccia OnCompleteListener<>
 * o delle interfacce AddOnSuccessListener o AddOnCanceledListener dei task di Firestore.
 */
public interface ClosureBoolean {
    public void closure(boolean isSuccess);
}
