package Model.Pojo;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'astrazione Partecipazione.
 *
 * La partecipazione è risultato di una relazione N a N tra gli utenti e gli eventi
 * in quanto un utente può partecipare ad uno o più eventi e agli eventi possono
 * partecipare zero o più utenti.
 *
 */
public class Partecipazione {

    @DocumentId
    private String documentId;

    private String idUtente;
    private String idEvento;
    private boolean accettazione;
    private Date dataOra;
    private boolean listaAttesa;



    /**
     * COSTRUTTORI
     */

    public Partecipazione() { }

    public Partecipazione(String idUtente,String idEvento, boolean accettazione, Date dataOra, boolean listaAttesa) {
        this.idUtente = idUtente;
        this.accettazione = accettazione;
        this.dataOra = dataOra;
        this.listaAttesa = listaAttesa;
        this.idEvento = idEvento;
    }



    /**
     * GETTER E SETTER
     */

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public boolean getAccettazione() {
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

    public boolean getListaAttesa() {
        return listaAttesa;
    }

    public void setListaAttesa(boolean listaAttesa) {
        this.listaAttesa = listaAttesa;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
