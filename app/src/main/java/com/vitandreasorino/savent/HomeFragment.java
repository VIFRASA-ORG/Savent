package com.vitandreasorino.savent;


import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import Helper.AuthHelper;


public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
        Toolbar toolbarStatus = (Toolbar) getActivity().findViewById(R.id.toolbarStatusHome);
        toolbarStatus.setVisibility(View.VISIBLE); */

        return inflater.inflate(R.layout.fragment_home, container, false);
    }


}
