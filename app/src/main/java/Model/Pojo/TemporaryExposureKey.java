package Model.Pojo;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

/**
 * Pojo class used to manage the TEK abstraction.
 */
public class TemporaryExposureKey {

    @DocumentId
    private String id;
    private Date data;

    public TemporaryExposureKey() { }

    // COSTRUTTORI DELLA CLASSE CODICEIDENTIFICATIVO
    public TemporaryExposureKey(String id, Date data) {
        this.id = id;
        this.data = data;
    }

    // GETTERS AND SETTERS
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
