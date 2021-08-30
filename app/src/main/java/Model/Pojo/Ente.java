package Model.Pojo;

import android.net.Uri;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'entità Ente.
 *
 * L'Ente è un altro tipo di utente dell'applicazione con permessi e funzionalità
 * totalmente diverse dall'utente.
 *
 * Esistono due tipi di enti: Ditta o libero professionista.
 * I due tipi di Enti fanno uso di attributi diversi, i restanti vengono lasciati vuoti.
 */
public class Ente {

    @DocumentId
    private String id;

    private String partitaIva;
    private String nomeDitta;
    private String domicilioFiscaleDitta;
    private String nomeLP;
    private String cognomeLP;
    private String codiceFiscaleLP;
    private String residenzaLP;
    private String numeroIscrizioneAlboLP;
    private String numeroTelefono;
    private String sedeDitta;
    private boolean abilitazione;

    private boolean isCertificatoPIVAUploaded;
    private boolean isVisuraCameraleUploaded;

    @Exclude
    private Uri certificatoPIVA;

    @Exclude
    private Uri visuraCamerale;



    /**
     * COSTRUTTORI
     */

    public Ente() {
        isCertificatoPIVAUploaded = false;
        isVisuraCameraleUploaded = false;
        certificatoPIVA = null;
        visuraCamerale = null;
    }

    //Costruttore Ente: Libero professionista
    public Ente(String nomeLP, String cognomeLP, String codiceFiscaleLP, String residenzaLP, String numeroIscrizioneAlboLP, String numeroTelefono,String partitaIva, Uri certificatoPIVA, boolean abilitazione) {
        this.nomeLP = nomeLP;
        this.cognomeLP = cognomeLP;
        this.codiceFiscaleLP = codiceFiscaleLP;
        this.residenzaLP = residenzaLP;
        this.numeroIscrizioneAlboLP = numeroIscrizioneAlboLP;
        this.numeroTelefono = numeroTelefono;
        this.certificatoPIVA = certificatoPIVA;
        this.partitaIva = partitaIva;
        this.abilitazione = abilitazione;
        isCertificatoPIVAUploaded = false;
        isVisuraCameraleUploaded = false;
        this.certificatoPIVA = null;
        this.visuraCamerale = null;
    }

    //Costruttore Ente: Ditta
    public Ente(String id, String partitaIva, Uri certificatoPIVA, Uri visuraCamerale, String nomeDitta, String domicilioFiscaleDitta,
                String numeroTelefono, boolean abilitazione) {
        this.id = id;
        this.partitaIva = partitaIva;
        this.certificatoPIVA = certificatoPIVA;
        this.visuraCamerale = visuraCamerale;
        this.nomeDitta = nomeDitta;
        this.domicilioFiscaleDitta = domicilioFiscaleDitta;
        this.numeroTelefono = numeroTelefono;
        this.abilitazione = abilitazione;
        isCertificatoPIVAUploaded = false;
        isVisuraCameraleUploaded = false;
        this.certificatoPIVA = null;
        this.visuraCamerale = null;
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

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getNomeDitta() {
        return nomeDitta;
    }

    public void setNomeDitta(String nomeDitta) {
        this.nomeDitta = nomeDitta;
    }

    public String getDomicilioFiscaleDitta() {
        return domicilioFiscaleDitta;
    }

    public void setDomicilioFiscaleDitta(String domicilioFiscaleDitta) {
        this.domicilioFiscaleDitta = domicilioFiscaleDitta;
    }

    public String getNomeLP() {
        return nomeLP;
    }

    public void setNomeLP(String nomeLP) {
        this.nomeLP = nomeLP;
    }

    public String getCognomeLP() {
        return cognomeLP;
    }

    public void setCognomeLP(String cognomeLP) {
        this.cognomeLP = cognomeLP;
    }

    public String getCodiceFiscaleLP() {
        return codiceFiscaleLP;
    }

    public void setCodiceFiscaleLP(String codiceFiscaleLP) {
        this.codiceFiscaleLP = codiceFiscaleLP;
    }

    public String getResidenzaLP() {
        return residenzaLP;
    }

    public void setResidenzaLP(String residenzaLP) {
        this.residenzaLP = residenzaLP;
    }

    public String getNumeroIscrizioneAlboLP() {
        return numeroIscrizioneAlboLP;
    }

    public void setNumeroIscrizioneAlboLP(String numeroIscrizioneAlboLP) {
        this.numeroIscrizioneAlboLP = numeroIscrizioneAlboLP;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public boolean getAbilitazione() {
        return abilitazione;
    }

    public void setAbilitazione(boolean abilitazione) {
        this.abilitazione = abilitazione;
    }

    public boolean getIsCertificatoPIVAUploaded() {
        return isCertificatoPIVAUploaded;
    }

    public void setIsCertificatoPIVAUploaded(boolean isCertificatoPIVAUploaded) {
        this.isCertificatoPIVAUploaded = isCertificatoPIVAUploaded;
    }

    public boolean getIsVisuraCameraleUploaded() {
        return isVisuraCameraleUploaded;
    }

    public void setIsVisuraCameraleUploaded(boolean isVisuraCameraleUploaded) {
        this.isVisuraCameraleUploaded = isVisuraCameraleUploaded;
    }

    public String getSedeDitta() {
        return sedeDitta;
    }

    public void setSedeDitta(String sedeDitta) {
        this.sedeDitta = sedeDitta;
    }



    /**
     * GETTER E SETTER ESCLUSI SU FIREBASE IN QUANTO
     * MEMORIZZATI DIVERSAMENTE DI UNA SEMPLICE SCRITTURA.
     */

    @Exclude public Uri getCertificatoPIVA() {
        return certificatoPIVA;
    }

    @Exclude public void setCertificatoPIVA(Uri certificatoPIVA) {
        this.certificatoPIVA = certificatoPIVA;
    }

    @Exclude public Uri getVisuraCamerale() {
        return visuraCamerale;
    }

    @Exclude public void setVisuraCamerale(Uri visuraCamerale) {
        this.visuraCamerale = visuraCamerale;
    }
}
