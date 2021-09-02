package com.vitandreasorino.savent.Enti;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vitandreasorino.savent.R;

public class HomeFragmentEnte extends Fragment {

    public HomeFragmentEnte() { }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Definizione e inizializzazione del layout per questo fragment
        return inflater.inflate(R.layout.fragment_home_ente, container, false);
    }
}