package com.vitandreasorino.savent.Registrazioni.RegistrazioniEnte;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.vitandreasorino.savent.R;

public class RegisterEnteActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ente);

        ViewPager vp = findViewById(R.id.viewPager);
        PagerAdapter page = new PagerAdapter(getSupportFragmentManager());
        vp.setAdapter(page);

        TabLayout tabLayoutRegistrazione = findViewById(R.id.tabLayoutRegistrazione);
        tabLayoutRegistrazione.setupWithViewPager(vp);

        tabLayoutRegistrazione.getTabAt(0).setIcon(R.drawable.ic_baseline_person_24)
                .setText(R.string.freelance);
        tabLayoutRegistrazione.getTabAt(1).setIcon(R.drawable.ic_baseline_home_work_24)
                .setText(R.string.company);
    }
}