package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.DB.CodiciComunicazioneTampone;
import Model.DB.Enti;
import Model.Pojo.CodiceComunicazioneTampone;
import Model.Pojo.Ente;

public class CommunicationSwabActivity extends AppCompatActivity {

    LinearLayout boxDettaglioTampone;
    TextView textViewDettaglioTampone;
    TextView textViewEsitoFinaleTampone;
    TextView textViewDataTampone;
    TextView textViewEnteRilascio;
    EditText editTextCodeCommunicationSwab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_swab);
        boxDettaglioTampone = (LinearLayout) findViewById(R.id.boxDettaglioTampone);
        textViewDettaglioTampone = (TextView) findViewById(R.id.textViewDettaglioTampone);
        textViewEsitoFinaleTampone = (TextView) findViewById(R.id.textViewEsitoFinaleTampone);
        textViewDataTampone = (TextView) findViewById(R.id.textViewDataTampone);
        textViewEnteRilascio = (TextView) findViewById(R.id.textViewEnteRilascio);
        editTextCodeCommunicationSwab = (EditText) findViewById(R.id.editTextCodeCommunicationSwab);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onBackButtonPressed(View view) {
        onBackPressed();
    }



    public void onClickCommunicationSwab(View view) {
        // salviamo il codice inserito nell'editText in una stringa che andremo a confrontare con i codici presenti
        // nel database centrale.
        String codiceEditText;
        codiceEditText = editTextCodeCommunicationSwab.getText().toString();


        CodiciComunicazioneTampone.getAllCode(new ClosureList<CodiceComunicazioneTampone>() {
            @Override
            public void closure(List<CodiceComunicazioneTampone> listTamponi) {

                if(listTamponi != null) {

                    int posizioneTamponeUguale = -1;

                    for(int i=0; i<listTamponi.size(); i++) {
                        if(listTamponi.get(i).getId().equals(codiceEditText)) {
                            posizioneTamponeUguale = i;
                        }
                    }

                    if(posizioneTamponeUguale == -1) {
                        textViewDettaglioTampone.setVisibility(View.INVISIBLE);
                        boxDettaglioTampone.setVisibility(View.INVISIBLE);
                        textViewEsitoFinaleTampone.setVisibility(View.INVISIBLE);
                        Toast.makeText(CommunicationSwabActivity.this, "Codice non valido!", Toast.LENGTH_LONG).show();
                    }else{

                        int posizioneTamponeFinale = posizioneTamponeUguale;
                        if(listTamponi.get(posizioneTamponeUguale).getUsato() == false) {
                            // setto il codice come usato, in modo da non essere riutilizzabile.
                            CodiciComunicazioneTampone.updateFields(listTamponi.get(posizioneTamponeUguale).getId(), new ClosureBoolean() {
                                @Override
                                public void closure(boolean isSuccess) {
                                }
                            }, "usato", true);

                            Enti.getAllEnti(new ClosureList<Ente>() {
                                @Override
                                public void closure(List<Ente> list) {
                                    if(list != null) {
                                        Iterator<Ente> it = list.iterator();
                                        while(it.hasNext()) {

                                            Ente e = it.next();
                                            if(e.getId().equals(listTamponi.get(posizioneTamponeFinale).getIdEnteCreatore())) {

                                                if(!e.getNomeDitta().equals("")) {
                                                        textViewEnteRilascio.setText(e.getNomeDitta());
                                                }else{
                                                        textViewEnteRilascio.setText(e.getNomeLP() + " " + e.getCognomeLP());
                                                }


                                            }
                                        }
                                    }
                                }
                            });


                            Calendar calendar = Calendar.getInstance();
                            if(listTamponi.get(posizioneTamponeUguale).getEsitoTampone()) {
                                calendar.setTime(listTamponi.get(posizioneTamponeUguale).getDataCreazione());
                                textViewDataTampone.setText("" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + "  " +
                                        calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

                                textViewEsitoFinaleTampone.setText("Positivo");
                                textViewDettaglioTampone.setVisibility(View.VISIBLE);
                                boxDettaglioTampone.setVisibility(View.VISIBLE);
                                textViewEsitoFinaleTampone.setVisibility(View.VISIBLE);
                                textViewDataTampone.setVisibility(View.VISIBLE);
                                textViewEnteRilascio.setVisibility(View.VISIBLE);

                                // Settare la positività
                            }else{
                                calendar.setTime(listTamponi.get(posizioneTamponeUguale).getDataCreazione());
                                textViewDataTampone.setText("" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + "  " +
                                        calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

                                textViewEsitoFinaleTampone.setText("Negativo");
                                textViewDettaglioTampone.setVisibility(View.VISIBLE);
                                boxDettaglioTampone.setVisibility(View.VISIBLE);
                                textViewEsitoFinaleTampone.setVisibility(View.VISIBLE);
                                textViewDataTampone.setVisibility(View.VISIBLE);
                                textViewEnteRilascio.setVisibility(View.VISIBLE);
                                // Settare la negatività
                            }
                        }else{
                            textViewDettaglioTampone.setVisibility(View.INVISIBLE);
                            boxDettaglioTampone.setVisibility(View.INVISIBLE);
                            textViewEsitoFinaleTampone.setVisibility(View.INVISIBLE);
                            Toast.makeText(CommunicationSwabActivity.this, "Codice già utilizzato!", Toast.LENGTH_LONG).show();
                        }
                    }


                }
            }
        });


    }


}


