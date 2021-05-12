package Model;

import java.util.*;

public class Utente {

    private int id;
    private String immagine;
    private String nome;
    private String cognome;
    private Date dataNascita;
    private String genere;
    private String statusSanitario;

    // COSTRUTTORE DELLA CLASSE UTENTE
    public Utente() {

    }

    public Utente(int id, String nome, String cognome, Date dataNascita, String genere, String statusSanitario) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.genere = genere;
        this.statusSanitario = statusSanitario;
    }

    public Utente(int id, String immagine, String nome, String cognome, Date dataNascita, String genere, String statusSanitario) {
        this.id = id;
        this.immagine = immagine;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.genere = genere;
        this.statusSanitario = statusSanitario;
    }

    // GETTER E SETTTER
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public Date getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(Date dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public String getStatusSanitario() {
        return statusSanitario;
    }

    public void setStatusSanitario(String statusSanitario) {
        this.statusSanitario = statusSanitario;
    }
}
