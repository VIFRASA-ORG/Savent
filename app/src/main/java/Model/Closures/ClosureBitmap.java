package Model.Closures;

import android.graphics.Bitmap;

/**
 * Interfaccia creata per essere utilizzata come lambda nelle query definite nelle classi DAO.
 *
 * Closure o chiusura creata per gestire i valori di ritorno delle query nello stesso contesto
 * dell'activity chiamante.
 *
 * In particolare questa chiusura permette di gestire come valore di ritorno
 * una bitmap per evitare l'utilizzo dell'interfaccia OnCompleteListener<byte[]>
 * e per effettuare la conversione da byte[] a bitmap direttamente nel blocco
 * dell'OnCompleteListener<byte[]>.
 */
public interface ClosureBitmap {
    public void closure(Bitmap bitmap);
}
