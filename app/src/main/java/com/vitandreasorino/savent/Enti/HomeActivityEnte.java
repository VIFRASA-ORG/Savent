package com.vitandreasorino.savent.Enti;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.Enti.GenerateCodeTab.GenerateCodeFragment;
import com.vitandreasorino.savent.LogSingInActivity;
import com.vitandreasorino.savent.R;

import Helper.AuthHelper;


public class HomeActivityEnte extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ViewPager2 viewPager;

    BottomNavigationView bottomNavigationEnte;
    Class previousFragmentClass = HomeFragmentEnte.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ente);

        bottomNavigationEnte = (BottomNavigationView) findViewById(R.id.bottomNavigationEnte);
        bottomNavigationEnte.setOnNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new HomeFragmentEnteAdapter(this));
        viewPager.setUserInputEnabled(false);

        if (savedInstanceState != null) {
            String prevClass = savedInstanceState.getString("previousFragmentClass");
            switch (prevClass) {
                //Nel caso in cui si clicchi sulla home dell'ente
                case "HomeFragmentEnte":
                    viewPager.setCurrentItem(0);
                    bottomNavigationEnte.setSelectedItemId(R.id.nav_home_ente);
                    break;
                //Nel caso in cui si clicchi sulla sezione dedicata alla generazione dei codici
                case "GenerateCodeFragment":
                    viewPager.setCurrentItem(1);
                    bottomNavigationEnte.setSelectedItemId(R.id.nav_generate_code_ente);
                    break;
            }
        }
    }

    /**
     * Metodo che viene chiamato per recuperare lo stato per istanza da un'attività prima di essere uccisa,
     * in modo che lo stato possa essere ripristinato successivamente.
     *
     * @param outState l'ultimo stato dell'attività
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("previousFragmentClass", previousFragmentClass.getSimpleName());
    }

    /**
     * Metodo richiamato quando viene selezionato un elemento nel menu di navigazione.
     *
     * @param item : l'elemento selezionato
     * @return true per visualizzare l'elemento come elemento selezionato, false se non viene selezionato niente.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            //Nel caso in cui si clicca sull'home dell'ente
            case R.id.nav_home_ente:
                viewPager.setCurrentItem(0, false);
                previousFragmentClass = HomeFragmentEnte.class;
                return true;
            //Nel caso in cui si seleziona sulla parte dedicata alla generazione dei codici
            case R.id.nav_generate_code_ente:
                viewPager.setCurrentItem(1, false);
                previousFragmentClass = GenerateCodeFragment.class;
                return true;
        }
        return false;
    }

    /**
     * Metodo che viene richiamato per effettuare il logout dalla home dell'ente
     * @param view : la nuova vista da visualizzare (ovveroLogSingInActivity)
     */
    public void onClickLogoutEnte(View view) {
        //Si crea un alertDialog per confermare il logout dall'home ente
        AlertDialog.Builder alertLogout = new AlertDialog.Builder(HomeActivityEnte.this);
        alertLogout.setTitle(R.string.stringConfirmLogout);
        alertLogout.setMessage(R.string.stringDialogLogout);

        // Nel caso di risposta positiva nel dialog
        alertLogout.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Si effettua il logout richiamando l'apposito metodo
                AuthHelper.logOutEnte();
                Toast.makeText(HomeActivityEnte.this, R.string.stringLogoutDone, Toast.LENGTH_SHORT).show();

                //Si ritorna alla schermata iniziale
                Intent intentLogin = new Intent(HomeActivityEnte.this, LogSingInActivity.class);
                intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the task all the previous Activity.
                startActivity(intentLogin);

            }
        });
        // Nel caso di risposta negativa nel dialog
        alertLogout.setNegativeButton(R.string.confirmNegative, null);
        //Mostra il dialog
        alertLogout.show();

    }
}

/**
 * Si imposta l'adapter per creare in modo specifico e mostrare i due fragment presenti
 * nell'Home activity ente.
 */
class HomeFragmentEnteAdapter extends FragmentStateAdapter {

    public HomeFragmentEnteAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Metodo per la creazione dei fragment relativi all'home ente
     * @param position : valore intero indicante la posizione in cui dovrà essere creato il fragment
     * @return un nuovo fragment, o niente (caso di default).
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new HomeFragmentEnte();
            case 1:
                return new GenerateCodeFragment();
            default:
                return null;
        }
    }

    /**
     * Metodo che restituisce il numero degli item, ovvero dei fragment da creare.
     * @return il numero dei fragment da generare.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
