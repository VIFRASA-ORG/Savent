package Model;

public class ComponentiGruppo {

    private String idGruppo;
    private String idUtente;


    // COSTRUTTORE DELLA CLASSE COMPONENTIGRUPPO
    public ComponentiGruppo() {

    }

    public ComponentiGruppo(String idGruppo, String idUtente) {
        this.idGruppo = idGruppo;
        this.idUtente = idUtente;
    }

    // GETTER E SETTER
    public String getIdGruppo() {
        return idGruppo;
    }

    public void setIdGruppo(String idGruppo) {
        this.idGruppo = idGruppo;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }
}
