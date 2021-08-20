package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.vitandreasorino.savent.Enti.HomeActivityEnte;
import com.vitandreasorino.savent.Utenti.BluetoothLEServices.GattServerCrawlerService;
import com.vitandreasorino.savent.Utenti.BluetoothLEServices.GattServerService;
import com.vitandreasorino.savent.Utenti.HomeActivity;

import Helper.AuthHelper;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG_LOG = SplashActivity.class.getName();

    /*MIN_WAIT_INTERVAL: costante che rappresenta l'intervallo min da attendere per il passaggio all'attività successiva
      MAX_WAIT_INTERVAL: costante che rappresenta l'intervallo max in cui il passaggio avverrà in automatico
    */
    private static final long MIN_WAIT_INTERVAL = 500L;
    private static final long MAX_WAIT_INTERVAL = 1000L;
    private static final int GO_AHEAD_WHAT = 1;

    /**
     * mStartTime: variabile che rappresenta l'istante della prima visualizzazione
     * mIsDone: variabile che indica l'informazione relativa al fatto che il passaggio all'attività seguente sia già stata realizzata.
     */
    private long mStartTime;
    private boolean mIsDone;

    /**
     * Handler: oggetto in grado di elaborare dei comandi a seguito della ricezione di un messaggio all'interno di un
     * particolare thread.
     *
     */
    private Handler mHandler = new Handler() {

        @Override
        /**
         * override del metodo handleMessage(), responsabile della ricezione ed elaborazione dei messaggi
         * what: proprietà dell'oggetto di tipo Message, che individua il tipo di operazione da seguire
         */
        public void handleMessage(Message msg){
            switch (msg.what){
                case GO_AHEAD_WHAT:
                    long enlapsedTime = SystemClock.uptimeMillis() - mStartTime;
                    if (enlapsedTime >= MIN_WAIT_INTERVAL && !mIsDone){
                        mIsDone = true;
                        goAhead();
                    }
                    break;
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * uptimeMillis(): metodo statico della classe SystemClock, fa riferimento al tempo attuale
         * obtainMessage(): metodo che creaa un messaggio attraverso GO_AHEAD_WHAT
         * GO_AHEAD_WHAT: costante di tipo int che caratterizza il tipo di messaggio e quindi l'operazione
         * sendMessageAtTime: metodo che invia un messaggio in un preciso istante
         */
        mStartTime = SystemClock.uptimeMillis();
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage, mStartTime + MAX_WAIT_INTERVAL);
        Log.d(TAG_LOG, "Handler message sent!");

    }

    /**
     * goAhead(): metodo per passare all'attivivò successiva
     * finish(): elimina l'attività dallo stack in modo tale da non poter ritornare alla schermate precedente con il tasto back.
     *
     */
    private void goAhead(){

        //If not logged-in, go to the login activity.
        if(!AuthHelper.isLoggedIn()){
            Intent intent = new Intent(this, LogSingInActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            //If logged-in, check what type of user is logged-in.
            AuthHelper.getLoggedUserType(closureRes -> {
                Intent intent;
                switch (closureRes){
                    case Utente:
                        intent = new Intent(this, HomeActivity.class);

                        //Starting the Gatt Server service
                        startService(new Intent(getBaseContext(), GattServerService.class));
                        startService(new Intent(getBaseContext(), GattServerCrawlerService.class));

                        break;
                    case Ente:
                        intent = new Intent(this, HomeActivityEnte.class);
                        break;
                    case None:
                    default:
                        intent = new Intent(this, LogSingInActivity.class);
                        AuthHelper.logOut();
                        break;
                }
                startActivity(intent);
                finish();
            });

        }
    }

}