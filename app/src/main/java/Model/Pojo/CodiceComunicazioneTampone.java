package Model.Pojo;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CodiceComunicazioneTampone implements Comparable<CodiceComunicazioneTampone>{

    @DocumentId
    private String id;

    private Date dataCreazione;
    private String idEnteCreatore;
    private boolean esitoTampone;
    private boolean usato;

    public CodiceComunicazioneTampone() {
    }

    public CodiceComunicazioneTampone(String id, Date dataCreazione, String idEnteCreatore, boolean esitoTampone, boolean usato) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.idEnteCreatore = idEnteCreatore;
        this.esitoTampone = esitoTampone;
        this.usato = usato;
    }

    public CodiceComunicazioneTampone(boolean esitoTampone, String idEnteCreatore) {
        this.esitoTampone = esitoTampone;
        this.idEnteCreatore = idEnteCreatore;
        usato = false;
        dataCreazione = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getIdEnteCreatore() {
        return idEnteCreatore;
    }

    public void setIdEnteCreatore(String idEnteCreatore) {
        this.idEnteCreatore = idEnteCreatore;
    }

    public boolean getEsitoTampone() {
        return esitoTampone;
    }

    public void setEsitoTampone(boolean esitoTampone) {
        this.esitoTampone = esitoTampone;
    }

    public boolean getUsato() {
        return usato;
    }

    public void setUsato(boolean usato) {
        this.usato = usato;
    }

    @Override
    public int compareTo(CodiceComunicazioneTampone o) {
        return this.dataCreazione.compareTo(o.dataCreazione);
    }

    @Exclude
    public String getNeutralData(){
        if(dataCreazione == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dataCreazione);
    }
}
