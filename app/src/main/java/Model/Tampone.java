package Model;

import java.util.Date;

public class Tampone {

    private String idUtente;
    private String idEnte;
    private Date data;
    private boolean esito;


    // COSTRUTTORE DELLA CLASSE TAMPONE
    public Tampone() {

    }

    public Tampone(String idUtente, String idEnte, Date data, boolean esito) {
        this.idUtente = idUtente;
        this.idEnte = idEnte;
        this.data = data;
        this.esito = esito;
    }

    // GETTER E SETTER
    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getIdEnte() {
        return idEnte;
    }

    public void setIdEnte(String idEnte) {
        this.idEnte = idEnte;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public boolean isEsito() {
        return esito;
    }

    public void setEsito(boolean esito) {
        this.esito = esito;
    }
}
