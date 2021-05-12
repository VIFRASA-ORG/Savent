package Model;

public class Gruppo {

    private int id;
    private String immagine;
    private String nome;
    private String descrizione;
    private Utente amministratore;


    // COSTRUTTORI DELLA CLASSE GRUPPO
    public Gruppo() {

    }

    public Gruppo(int id, String nome, String descrizione, Utente amministratore) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.amministratore = amministratore;
    }

    public Gruppo(int id, String immagine, String nome, String descrizione, Utente amministratore) {
        this.id = id;
        this.immagine = immagine;
        this.nome = nome;
        this.descrizione = descrizione;
        this.amministratore = amministratore;
    }

    // GETTER E SETTER
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

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Utente getAmministratore() {
        return amministratore;
    }

    public void setAmministratore(Utente amministratore) {
        this.amministratore = amministratore;
    }
}
