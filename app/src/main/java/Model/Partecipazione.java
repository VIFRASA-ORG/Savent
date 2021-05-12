package Model;

import java.util.Date;

public class Partecipazione {

    private String idEvento;
    private String idUtente;
    private boolean accettazione;
    private Date dataOra;
    private boolean listaAttesa;


    // COSTRUTTORE DELLA CLASSE PARTECIPAZIONE
    public Partecipazione() {

    }

    public Partecipazione(String idEvento, String idUtente, boolean accettazione, Date dataOra, boolean listaAttesa) {
        this.idEvento = idEvento;
        this.idUtente = idUtente;
        this.accettazione = accettazione;
        this.dataOra = dataOra;
        this.listaAttesa = listaAttesa;
    }

    // GETTER E SETTER
    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

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
