package Model.Pojo;

import java.util.Date;

public class Partecipazione {

    private String idUtente;
    private boolean accettazione;
    private Date dataOra;
    private boolean listaAttesa;


    // COSTRUTTORE DELLA CLASSE PARTECIPAZIONE
    public Partecipazione() { }

    public Partecipazione(String idUtente, boolean accettazione, Date dataOra, boolean listaAttesa) {
        this.idUtente = idUtente;
        this.accettazione = accettazione;
        this.dataOra = dataOra;
        this.listaAttesa = listaAttesa;
    }

    // GETTER E SETTER
    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public boolean isAccettazione() {
        return accettazione;
    }

    public void setAccettazione(boolean accettazione) {
        this.accettazione = accettazione;
    }

    public Date getDataOra() {
        return dataOra;
    }

    public void setDataOra(Date dataOra) {
        this.dataOra = dataOra;
    }

    public boolean isListaAttesa() {
        return listaAttesa;
    }

    public void setListaAttesa(boolean listaAttesa) {
        this.listaAttesa = listaAttesa;
    }
}
