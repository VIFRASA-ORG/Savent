package Model.Pojo;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.*;

public class Utente {

    public static final String MALE = "male";
    public static final String FEMALE = "female";
    public static final String UNDEFINED = "undefined";

    @DocumentId
    private String id;

    private boolean isProfileImageUploaded;

    @Exclude
    private Bitmap profileImageBitmap = null;
    @Exclude
    private Uri profileImageUri = null;

    private String nome;
    private String cognome;
    private Date dataNascita;
    private String genere;
    private String numeroDiTelefono;
    private int statusSanitario;

    // COSTRUTTORE DELLA CLASSE UTENTE
    public Utente() {
        this.statusSanitario = 0;
        this.isProfileImageUploaded = false;
    }

    public Utente(String id, String nome, String cognome, Date dataNascita, String genere, int statusSanitario,String numeroDiTelefono) {
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

    public int getStatusSanitario() {
        return statusSanitario;
    }

    public void setStatusSanitario(int statusSanitario) {
        this.statusSanitario = statusSanitario;
    }

    public String getNumeroDiTelefono() {
        return numeroDiTelefono;
    }

    public void setNumeroDiTelefono(String numeroDiTelefono) {
        this.numeroDiTelefono = numeroDiTelefono;
    }

    public boolean getIsProfileImageUploaded() {
        return isProfileImageUploaded;
    }

    public void setIsProfileImageUploaded(boolean isProfileImageUploaded) {
        this.isProfileImageUploaded = isProfileImageUploaded;
    }

    @Exclude
    public Bitmap getProfileImageBitmap() {
        return profileImageBitmap;
    }

    @Exclude
    public void setProfileImageBitmap(Bitmap profileImageBitmap) {
        this.profileImageBitmap = profileImageBitmap;
    }
  
    /**
     * Return the event data and time formatted as following:
     * dd/MM/yyyy HH:mm
     *
     * @return a string with the formatted data
     */
    public String getNeutralData(){
        if(dataNascita == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dataNascita);
    }

    @Exclude
    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    @Exclude
    public void setProfileImageUri(Uri profileImageUri) {
        this.profileImageUri = profileImageUri;
    }
}
