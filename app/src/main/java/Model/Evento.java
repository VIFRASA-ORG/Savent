package Model;

import java.util.*;

public class Evento {

    private int id;
    private String locandina;
    private String nome;
    private String descrizione;
    private double longitudine;
    private double latitudine;
    private Date dataOra;
    private Utente utenteCreatore;
    private Gruppo gruppoCreatore;
    private int sogliaAccettazioneStatus;
    private int numeroMassimoPartecipanti;
    private int numeroPartecipanti;


    // COSTRUTTORI DELLA CLASSE EVENTO
    public Evento() {

    }

    public Evento(int id,String locandina, String nome, String descrizione, double longitudine, double latitudine, Date dataOra, Utente utenteCreatore,
                  int sogliaAccettazioneStatus, int numeroMassimoPartecipanti, int numeroPartecipanti) {
        this.id = id;
        this.locandina = locandina;
        this.nome = nome;
        this.descrizione = descrizione;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
        this.dataOra = dataOra;
        this.utenteCreatore = utenteCreatore;
        this.sogliaAccettazioneStatus = sogliaAccettazioneStatus;
        this.numeroMassimoPartecipanti = numeroMassimoPartecipanti;
        this.numeroPartecipanti = numeroPartecipanti;
    }

    public Evento(int id, String locandina, String nome, String descrizione, double longitudine, double latitudine, Date dataOra, Gruppo gruppoCreatore,
                  int sogliaAccettazioneStatus, int numeroMassimoPartecipanti, int numeroPartecipanti) {
        this.id = id;
        this.locandina = locandina;
        this.nome = nome;
        this.descrizione = descrizione;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
        this.dataOra = dataOra;
        this.gruppoCreatore = gruppoCreatore;
        this.sogliaAccettazioneStatus = sogliaAccettazioneStatus;
        this.numeroMassimoPartecipanti = numeroMassimoPartecipanti;
        this.numeroPartecipanti = numeroPartecipanti;
    }

    // GETTER E SETTER
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocandina() {
        return locandina;
    }

    public void setLocandina(String locandina) {
        this.locandina = locandina;
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

    public Utente getUtenteCreatore() {
        return utenteCreatore;
    }

    public void setUtenteCreatore(Utente utenteCreatore) {
        this.utenteCreatore = utenteCreatore;
    }

    public Gruppo getGruppoCreatore() {
        return gruppoCreatore;
    }

    public void setGruppoCreatore(Gruppo gruppoCreatore) {
        this.gruppoCreatore = gruppoCreatore;
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
