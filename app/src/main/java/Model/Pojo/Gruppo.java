package Model.Pojo;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;

public class Gruppo implements Serializable {

    @DocumentId
    private String id;

    @Exclude
    private Uri immagine;

    @Exclude
    private Bitmap immagineBitmap;

    private String nome;
    private String descrizione;
    private String idAmministratore;
    private boolean isImmagineUploaded;
    private List<String> idComponenti;


    // COSTRUTTORI DELLA CLASSE GRUPPO
    public Gruppo() { }

    public Gruppo(String id, String nome, String descrizione, String idAmministratore, List<String> idComponenti) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.idAmministratore = idAmministratore;
        this.isImmagineUploaded = false;
        this.idComponenti = idComponenti;
    }

    public Gruppo(String id, Uri immagine, String nome, String descrizione, String idAmministratore, List<String> idComponenti) {
        this.id = id;
        this.immagine = immagine;
        this.nome = nome;
        this.descrizione = descrizione;
        this.idAmministratore = idAmministratore;
        this.isImmagineUploaded = true;
        this.idComponenti = idComponenti;
    }

    // GETTER E SETTER
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude public Uri getImmagine() {
        return immagine;
    }

    @Exclude public void setImmagine(Uri immagine) {
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

    public boolean isImmagineUploaded() {
        return isImmagineUploaded;
    }

    public void setImmagineUploaded(boolean immagineUploaded) {
        isImmagineUploaded = immagineUploaded;
    }

    @Exclude
    public Bitmap getImmagineBitmap() {
        return immagineBitmap;
    }

    @Exclude
    public void setImmagineBitmap(Bitmap immagineBitmap) {
        this.immagineBitmap = immagineBitmap;
    }
}
