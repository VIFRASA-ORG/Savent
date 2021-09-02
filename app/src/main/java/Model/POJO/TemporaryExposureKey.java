package Model.POJO;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'entità TEK o Temporary Exposure Key.
 *
 * Gli oggetti di questa classe sono codici generati in maniera random dal server Firebase
 * i cui id rappresentano un dispositivo in maniera univoca nel sistema.
 *
 * L'id viene memorizzato nelle tabelle TemporaryExposureKeys, ContattiAvvenuti.COLUMN_NAME_CODICI nel database SQLite locale
 * e nelle Collections TemporaryExposureKeys e Positivi su Firebase.
 */
public class TemporaryExposureKey {

    @DocumentId
    private String id;
    private Date data;



    /**
     * COSTRUTTORI
     */
    public TemporaryExposureKey() { }

    public TemporaryExposureKey(String id, Date data) {
        this.id = id;
        this.data = data;
    }



    /**
     * GETTER E SETTER
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
