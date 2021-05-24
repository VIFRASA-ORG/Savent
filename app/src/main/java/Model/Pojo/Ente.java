package Model.Pojo;

import android.net.Uri;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class Ente {

    @DocumentId
    private String id;

    @Exclude
    private Uri certificatoPIVA;

    @Exclude
    private Uri visuraCamerale;

    private String partitaIva;
    private String nomeDitta;
    private String domicilioFiscaleDitta;
    private String nomeLP;
    private String cognomeLP;
    private String codiceFiscaleLP;
    private String residenzaLP;
    private String numeroIscrizioneAlboLP;
    private String numeroTelefono;
    private boolean abilitazione;


    // COSTRUTTORI DELLA CLASSE ENTE
    public Ente() { }

    public Ente(String nomeLP, String cognomeLP, String codiceFiscaleLP, String residenzaLP, String numeroIscrizioneAlboLP, String numeroTelefono, boolean abilitazione) {
        this.nomeLP = nomeLP;
        this.cognomeLP = cognomeLP;
        this.codiceFiscaleLP = codiceFiscaleLP;
        this.residenzaLP = residenzaLP;
        this.numeroIscrizioneAlboLP = numeroIscrizioneAlboLP;
        this.numeroTelefono = numeroTelefono;
        this.abilitazione = abilitazione;
    }

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
    }

    // GETTER E SETTER
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

    public boolean isAbilitazione() {
        return abilitazione;
    }

    public void setAbilitazione(boolean abilitazione) {
        this.abilitazione = abilitazione;
    }
}
