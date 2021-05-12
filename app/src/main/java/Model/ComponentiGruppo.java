package Model;

public class ComponentiGruppo {

    private int idGruppo;
    private int idUtente;


    // COSTRUTTORE DELLA CLASSE COMPONENTIGRUPPO
    public ComponentiGruppo() {

    }

    public ComponentiGruppo(int idGruppo, int idUtente) {
        this.idGruppo = idGruppo;
        this.idUtente = idUtente;
    }

    // GETTER E SETTER
    public int getIdGruppo() {
        return idGruppo;
    }

    public void setIdGruppo(int idGruppo) {
        this.idGruppo = idGruppo;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }
}
