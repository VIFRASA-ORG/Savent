package com.vitandreasorino.savent.Enti.AddSwabTab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.vitandreasorino.savent.R;

public class GenerateCodeFragment extends Fragment {

    public GenerateCodeFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generate_code, container, false);
    }
}
