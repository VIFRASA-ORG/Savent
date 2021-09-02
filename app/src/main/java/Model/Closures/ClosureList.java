package Model.Closures;

import java.util.List;

/**
 * Interfaccia creata per essere utilizzata come lambda nelle query definite nelle classi DAO.
 *
 * Closure o chiusura creata per gestire i valori di ritorno delle query nello stesso contesto
 * dell'activity chiamante.
 *
 * In particolare questa chiusura permette di gestire come valore di ritorno
 * una Lista per evitare l'utilizzo dell'interfaccia OnCompleteListener<QuerySnapshot>
 * e per effettuare la conversione da QuerySnapshot a Lista di oggetti di tipo E
 * direttamente nel blocco dell'OnCompleteListener<QuerySnapshot>.
 */
public interface ClosureList<E>{
    public void closure(List<E> list);
}