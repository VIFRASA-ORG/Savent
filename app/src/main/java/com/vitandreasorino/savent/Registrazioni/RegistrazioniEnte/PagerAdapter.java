package com.vitandreasorino.savent.Registrazioni.RegistrazioniEnte;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    final int contatorePagina = 2;
    private String[] titoloTab = new String[] {"Freelance","Company"};

    public PagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }


    /**
     * Metodo utilizzato per lo switch da un fragment all'altro.
     *
     * @param position dell'item selezionato da parte dell'utente
     * @return il fragment corrispondente alla posizione selezionata, altrimenti ritorna null.
     */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FreelanceFragment();
            case 1:
                return new CompanyFragment();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return contatorePagina;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titoloTab[position];
    }


}
