package com.vitandreasorino.savent.Utenti;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.TechnicalInfoActivity;


public class HomeFragment extends Fragment {

    TextView textTechnicalInfo;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textTechnicalInfo=view.findViewById(R.id.stringTechnicalInfo);
        textTechnicalInfo.setOnClickListener(v -> onClickTechnicalInfo());
    }

    private void onClickTechnicalInfo(){
        Intent viewTechnicalInfo = new Intent(getActivity(), TechnicalInfoActivity.class);
        startActivity(viewTechnicalInfo);
    }

}
