package Model;

import java.util.Date;

public class Tampone {

    private int idUtente;
    private int idEnte;
    private Date data;
    private boolean esito;


    // COSTRUTTORE DELLA CLASSE TAMPONE
    public Tampone() {

    }

    public Tampone(int idUtente, int idEnte, Date data, boolean esito) {
        this.idUtente = idUtente;
        this.idEnte = idEnte;
        this.data = data;
        this.esito = esito;
    }

    // GETTER E SETTER
    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public int getIdEnte() {
        return idEnte;
    }

    public void setIdEnte(int idEnte) {
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
