package Model.Pojo;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.*;

public class Utente {

    @DocumentId
    private String id;

    @Exclude
    private boolean isProfileImageUploaded;

    private String nome;
    private String cognome;
    private Date dataNascita;
    private String genere;
    private String numeroDiTelefono;
    private String statusSanitario;

    // COSTRUTTORE DELLA CLASSE UTENTE
    public Utente() { }

    public Utente(String id, String nome, String cognome, Date dataNascita, String genere, String statusSanitario,String numeroDiTelefono) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.genere = genere;
        this.statusSanitario = statusSanitario;
        this.numeroDiTelefono = numeroDiTelefono;
        this.isProfileImageUploaded = false;
    }

    // GETTER E SETTTER
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

    public String getNumeroDiTelefono() {
        return numeroDiTelefono;
    }

    public void setNumeroDiTelefono(String numeroDiTelefono) {
        this.numeroDiTelefono = numeroDiTelefono;
    }

    @Exclude
    public boolean isProfileImageUploaded() {
        return isProfileImageUploaded;
    }

    @Exclude
    public void setProfileImageUploaded(boolean profileImageUploaded) {
        isProfileImageUploaded = profileImageUploaded;
    }
}
