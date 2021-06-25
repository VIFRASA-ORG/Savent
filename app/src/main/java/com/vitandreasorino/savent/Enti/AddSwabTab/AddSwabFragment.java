package com.vitandreasorino.savent.Enti.AddSwabTab;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vitandreasorino.savent.R;


public class AddSwabFragment extends Fragment {
    Button buttonSelectPatient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_swab, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonSelectPatient = view.findViewById(R.id. buttonSelectPatient);

        /**
         * metodo che richiama la successiva classe abbinata allo scopo di selezionare il paziente che si vuole cercare per
         * poterne aggiornare la condizione di salute cliccando il tasto "Seleziona Paziente"
         */
        buttonSelectPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectPatient = new Intent(getActivity(), SelectPatientActivity.class);
                startActivity(selectPatient);
            }
        });
    }

}