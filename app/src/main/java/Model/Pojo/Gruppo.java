package Model.Pojo;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class Gruppo {

    @DocumentId
    private String id;

    private String immagine;
    private String nome;
    private String descrizione;
    private String idAmministratore;

    private List<String> idComponenti;


    // COSTRUTTORI DELLA CLASSE GRUPPO
    public Gruppo() { }

    public Gruppo(String id, String nome, String descrizione, String idAmministratore, List<String> idComponenti) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.idAmministratore = idAmministratore;
        this.idComponenti = idComponenti;
    }

    public Gruppo(String id, String immagine, String nome, String descrizione, String idAmministratore, List<String> idComponenti) {
        this.id = id;
        this.immagine = immagine;
        this.nome = nome;
        this.descrizione = descrizione;
        this.idAmministratore = idAmministratore;
        this.idComponenti = idComponenti;
    }

    // GETTER E SETTER
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getIdAmministratore() {
        return idAmministratore;
    }

    public void setIdAmministratore(String idAmministratore) {
        this.idAmministratore = idAmministratore;
    }

    public List<String> getIdComponenti() {
        return idComponenti;
    }

    public void setIdComponenti(List<String> idComponenti) {
        this.idComponenti = idComponenti;
    }
}
