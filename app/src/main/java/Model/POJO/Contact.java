package Model.POJO;

import java.util.Objects;

/**
 * Classe POJO (Plain Old Java Object), classe ordinaria
 * utilizzata per rappresentare l'entità dei Contatti presenti nella rubrica del telefono.
 * Si memorizzano solo i dati necessari.
 *
 * Implementa Comparable per poter ordinare le liste di contatti trovati sul telefono.
 */
public class Contact implements Comparable<Contact>{

    private String name;
    private String number;
    private boolean isChecked = false;

    //Id associato al contatto nella rubrica del telefono
    private String id;

    //Id dell'utente firebase associato a questo conatto, se esiste.
    private String documentId;



    /**
     * COSTRUTTORI
     */
    public Contact() {}

    public Contact(String numeroDiTelefono){
        this.number = numeroDiTelefono;
    }



    /**
     * GETTER E SETTER
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }



    /**
     * METODI DI SUPPORTO
     */

    /**
     * Implementazione del metodo equals per definire l'ugualianza tra due oggetti di classe Contact.
     * Due oggetti sono uguali se i loro numeri di telefono sono gli stessi.
     *
     * @param o oggetto con cui fare il confronto.
     * @return true se sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact that = (Contact) o;
        return Objects.equals(number, that.number);
    }

    /**
     * Calcola l'hash dell'oggetto.
     * L'hash è calcolato solamento sul campo "number".
     *
     * @return l'hash del numero dell'oggetto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    /**
     * Implementazione del metodo compareTo per definire la politica di confronto
     * tra due oggetti di classe Contact per poter ordinare delle liste.
     *
     * Il confronto avviene solo sull'attributo "number".
     *
     * @param o oggetto con cui comparare.
     * @return comparazione lessicografica tra i numeri dei due contatti.
     */
    @Override
    public int compareTo(Contact o) {
        return this.number.compareTo(o.getNumber());
    }
}
