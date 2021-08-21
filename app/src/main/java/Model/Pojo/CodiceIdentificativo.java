package Model.Pojo;


import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class CodiceIdentificativo {

    @DocumentId
    private String id;
    private Date data;


    // COSTRUTTORI DELLA CLASSE CODICEIDENTIFICATIVO
    public CodiceIdentificativo(String id, Date data) {
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
