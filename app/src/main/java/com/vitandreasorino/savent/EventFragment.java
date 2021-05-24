package com.vitandreasorino.savent;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
/*
    Toolbar toolbarStatus = (Toolbar) getActivity().findViewById(R.id.toolbarStatusHome);
            toolbarStatus.setVisibility(View.INVISIBLE);*/

        return inflater.inflate(R.layout.fragment_event, container, false);
    }
}
