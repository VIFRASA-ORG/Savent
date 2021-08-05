package Model.Pojo;

import java.util.Date;

public class Notification {

    private String title;
    private String description;
    private Date date;
    private boolean isRead = false;
    private String idEvento;
    private String nomeEvento;


    public Notification() {}

    public Notification(String name, String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Notification(String name, String title, String description,Date date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public Notification(String name, String title, String description,boolean isRead) {
        this.title = title;
        this.description = description;
        this.isRead = isRead;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public String getNomeEvento() {
        return nomeEvento;
    }

    public void setNomeEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
