package Model.Closures;

/**
 * Interfaccia creata per essere utilizzata come lambda nelle query definite nelle classi DAO.
 *
 * Closure o chiusura creata per gestire i valori di ritorno delle query nello stesso contesto
 * dell'activity chiamante.
 *
 * In particolare questa chiusura permette di gestire come valore di ritorno
 * una tipo Generics E per evitare l'utilizzo dell'interfaccia OnCompleteListener<>
 * e per effettuare la conversione direttamente al tipo Generic
 * direttamente nel blocco dell'OnCompleteListener<>.
 */
public interface ClosureResult<E> {
    void closure(E result);
}
