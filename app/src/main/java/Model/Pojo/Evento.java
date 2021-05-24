package Model.Pojo;

import com.google.firebase.firestore.DocumentId;

import java.util.*;

public class Evento {

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

    // COSTRUTTORI DELLA CLASSE EVENTO
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
    }

    /**
     * Constructor for an event made by a normal user
     *
     * @param id
     * @param nome
     * @param descrizione
     * @param longitudine
     * @param latitudine
     * @param dataOra
     * @param idUtenteCreatore
     * @param sogliaAccettazioneStatus
     * @param numeroMassimoPartecipanti
     * @param numeroPartecipanti
     */
    public Evento(String id, String nome, String descrizione, double longitudine, double latitudine, Date dataOra, String idUtenteCreatore,
                  int sogliaAccettazioneStatus, int numeroMassimoPartecipanti, int numeroPartecipanti) {
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
    }


    // GETTER E SETTER
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
}
