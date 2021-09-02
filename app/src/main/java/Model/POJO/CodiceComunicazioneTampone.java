package Model.POJO;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare il codice creato dagli enti e usato dagli utenti per
 * poter comunicare la loro positività a tutta la piattaforma.
 *
 * L'attrbuto id di questa classe viene fatto generare da Firebase in maniera casuale.
 *
 * Si tratta di codici monouso per comunicare la positivita o negativita di una sola persona.
 * Una volta usato, il flag "usato" sara settato a true su Firebase e il codice non potra più essere usato.
 *
 * Implementa Comparable per poter ordinare le liste di codici in base alla loro data di creazione.
 */
public class CodiceComunicazioneTampone implements Comparable<CodiceComunicazioneTampone>{

    @DocumentId
    private String id;

    private Date dataCreazione;
    private String idEnteCreatore;
    private boolean esitoTampone;
    private boolean usato;



    /**
     * COSTRUTTORI
     */

    public CodiceComunicazioneTampone() {
    }

    public CodiceComunicazioneTampone(String id, Date dataCreazione, String idEnteCreatore, boolean esitoTampone, boolean usato) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.idEnteCreatore = idEnteCreatore;
        this.esitoTampone = esitoTampone;
        this.usato = usato;
    }

    public CodiceComunicazioneTampone(boolean esitoTampone, String idEnteCreatore) {
        this.esitoTampone = esitoTampone;
        this.idEnteCreatore = idEnteCreatore;
        usato = false;
        dataCreazione = new Date();
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

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getIdEnteCreatore() {
        return idEnteCreatore;
    }

    public void setIdEnteCreatore(String idEnteCreatore) {
        this.idEnteCreatore = idEnteCreatore;
    }

    public boolean getEsitoTampone() {
        return esitoTampone;
    }

    public void setEsitoTampone(boolean esitoTampone) {
        this.esitoTampone = esitoTampone;
    }

    public boolean getUsato() {
        return usato;
    }

    public void setUsato(boolean usato) {
        this.usato = usato;
    }



    /**
     * METODI DI SUPPORTO
     */

    /**
     * Formatta la data di creazione del codiceComunicazioneTampone nel tipo "dd/MM/yyyy".
     *
     * @return una stringa con la data formattata come sopra definito, null se la data di nascita non esiste.
     */
    @Exclude
    public String getNeutralData(){
        if(dataCreazione == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dataCreazione);
    }

    /**
     * Implementazione del metodo compareTo per definire la politica di confronto
     * tra due oggetti di classe Contact per poter ordinare delle liste.
     *
     * Il confronto avviene solo sull'attributo "dataCreazione".
     *
     * @param o oggetto con cui comparare.
     * @return comparazione tra le date di creazione.
     */
    @Override
    public int compareTo(CodiceComunicazioneTampone o) {
        return this.dataCreazione.compareTo(o.dataCreazione);
    }
}
