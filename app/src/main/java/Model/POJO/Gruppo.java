package Model.POJO;

import android.graphics.Bitmap;
import android.net.Uri;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'entità Gruppo.
 *
 * Implementa Serializable per permettere il passaggio di un oggetto tra intent espliciti.
 * È stato reso "immagineBitmap" transient per evitare di dover utilizzare Parcelable al posto di Serializable.
 *
 * In questa maniera l'immagine bitmap non viene serializzata e quindi passata tra gli intent espliciti.
 */
public class Gruppo implements Serializable {

    @DocumentId
    private String id;

    private String nome;
    private String descrizione;
    private String idAmministratore;
    private boolean isImmagineUploaded;
    private List<String> idComponenti;

    @Exclude
    private Uri immagine;

    @Exclude
    private transient Bitmap immagineBitmap;



    /**
     * COSTRUTTORI
     */
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



    /**
     * GETTER E SETTER
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean getIsImmagineUploaded() {
        return isImmagineUploaded;
    }

    public void setIsImmagineUploaded(boolean isImmagineUploaded) {
        this.isImmagineUploaded = isImmagineUploaded;
    }



    /**
     * GETTER E SETTER ESCLUSI SU FIREBASE IN QUANTO
     * MEMORIZZATI DIVERSAMENTE DI UNA SEMPLICE SCRITTURA.
     */

    @Exclude
    public Bitmap getImmagineBitmap() {
        return immagineBitmap;
    }

    @Exclude
    public void setImmagineBitmap(Bitmap immagineBitmap) {
        this.immagineBitmap = immagineBitmap;
    }

    @Exclude public Uri getImmagine() {
        return immagine;
    }

    @Exclude public void setImmagine(Uri immagine) {
        this.immagine = immagine;
    }
}
