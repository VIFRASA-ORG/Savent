package Model.POJO;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'entità Evento.
 *
 * Implementa Serializable per permettere il passaggio di un oggetto tra intent espliciti.
 * È stato reso "imageBitmap" transient per evitare di dover utilizzare Parcelable al posto di Serializable.
 * In questa maniera l'immagine bitmap non viene serializzata e quindi passata tra gli intent espliciti.
 *
 * Implementa Comparable per poter ordinare delle liste di oggetti di classe Evento.
 */
public class Evento implements Serializable, Comparable<Evento> {

    @DocumentId
    private String id;

    private String nome;
    private String descrizione;
    private double longitudine;
    private double latitudine;
    private Date dataOra;
    private String idUtenteCreatore;
    private String idGruppoCreatore;
    private int sogliaAccettazioneStatus;
    private int numeroMassimoPartecipanti;
    private int numeroPartecipanti;
    private int numeroPartecipantiInCoda;

    @Exclude
    private transient Bitmap imageBitmap;

    @Exclude
    private transient Uri imageUri;
    private boolean isImageUploaded;



    /**
     * COSTRUTTORI
     */
    public Evento() {
        nome = "";
        descrizione = "";
        longitudine = 0;
        latitudine = 0;
        dataOra = new Date();
        idUtenteCreatore = "";
        idGruppoCreatore = "";
        sogliaAccettazioneStatus = 0;
        numeroMassimoPartecipanti = 0;
        numeroPartecipanti = 0;
        numeroPartecipantiInCoda = 0;
        isImageUploaded = false;
    }

    public Evento(String id, String nome, String descrizione, double longitudine, double latitudine, Date dataOra, String idUtenteCreatore,
                  int sogliaAccettazioneStatus, int numeroMassimoPartecipanti, int numeroPartecipanti,int numeroPartecipantiInCoda, boolean isImageUploaded) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
        this.dataOra = dataOra;
        this.idUtenteCreatore = idUtenteCreatore;
        this.sogliaAccettazioneStatus = sogliaAccettazioneStatus;
        this.numeroMassimoPartecipanti = numeroMassimoPartecipanti;
        this.numeroPartecipanti = numeroPartecipanti;
        this.isImageUploaded = isImageUploaded;
        this.numeroPartecipantiInCoda = numeroPartecipantiInCoda;
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

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public Date getDataOra() {
        return dataOra;
    }

    public void setDataOra(Date dataOra) {
        this.dataOra = dataOra;
    }

    public String getIdUtenteCreatore() {
        return idUtenteCreatore;
    }

    public void setUtenteCreatore(String idUtenteCreatore) {
        this.idUtenteCreatore = idUtenteCreatore;
    }

    public String getIdGruppoCreatore() {
        return idGruppoCreatore;
    }

    public void setGruppoCreatore(String idGruppoCreatore) {
        this.idGruppoCreatore = idGruppoCreatore;
    }

    public int getSogliaAccettazioneStatus() {
        return sogliaAccettazioneStatus;
    }

    public void setSogliaAccettazioneStatus(int sogliaAccettazioneStatus) {
        this.sogliaAccettazioneStatus = sogliaAccettazioneStatus;
    }

    public int getNumeroMassimoPartecipanti() {
        return numeroMassimoPartecipanti;
    }

    public void setNumeroMassimoPartecipanti(int numeroMassimoPartecipanti) {
        this.numeroMassimoPartecipanti = numeroMassimoPartecipanti;
    }

    public int getNumeroPartecipanti() {
        return numeroPartecipanti;
    }

    public void setNumeroPartecipanti(int numeroPartecipanti) {
        this.numeroPartecipanti = numeroPartecipanti;
    }

    public boolean getIsImageUploaded() {
        return isImageUploaded;
    }

    public void setImageUploaded(boolean imageUploaded) {
        isImageUploaded = imageUploaded;
    }

    public int getNumeroPartecipantiInCoda() {
        return numeroPartecipantiInCoda;
    }

    public void setNumeroPartecipantiInCoda(int numeroPartecipantiInCoda) {
        this.numeroPartecipantiInCoda = numeroPartecipantiInCoda;
    }



    /**
     * GETTER E SETTER ESCLUSI SU FIREBASE IN QUANTO
     * MEMORIZZATI DIVERSAMENTE DI UNA SEMPLICE SCRITTURA.
     */

    @Exclude
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    @Exclude
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @Exclude
    public Uri getImageUri() {
        return imageUri;
    }

    @Exclude
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }



    /**
     * METODI DI SUPPORTO
     */

    /**
     * Formatta la data di svolgimento dell'evento  nel tipo "dd/MM/yyyy HH:mm".
     *
     * @return una stringa con la data formattata come sopra definito, null se la data di nascita non esiste.
     */
    @Exclude
    public String getNeutralData(){
        if(dataOra == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(dataOra);
    }

    /**
     * Implementazione del metodo equals per definire l'ugualianza tra due oggetti di classe Ente.
     * Due oggetti sono uguali se i loro id sono gli stessi.
     *
     * @param o oggetto con cui fare il confronto.
     * @return true se sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    /**
     * Calcola l'ash dell'oggetto.
     * L'hash è calcolato solamento sul campo id.
     *
     * @return l'hash dell'id dell'oggetto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Implementazione del metodo compareTo per definire la politica di confronto
     * tra due oggetti di classe evento per poter ordinare delle liste.
     *
     * Il confronto avviene solo sull'attributo "nome".
     *
     * @param o oggetto con cui comparare.
     * @return comparazione lessicografica tra i nomi dei due eventi.
     */
    @Override
    public int compareTo(Evento o) {
        return this.nome.compareTo(o.nome);
    }
}
