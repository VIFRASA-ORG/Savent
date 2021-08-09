package com.vitandreasorino.savent.Enti;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.Enti.AddSwabTab.GenerateCodeFragment;
import com.vitandreasorino.savent.R;

public class HomeActivityEnte extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private HomeFragmentEnte homeFragmentEnte = new HomeFragmentEnte();
    private GenerateCodeFragment generateCodeFragmentEnte = new GenerateCodeFragment();

    BottomNavigationView bottomNavigationEnte;
    Class previousFragmentClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ente);

        bottomNavigationEnte = (BottomNavigationView) findViewById(R.id.bottomNavigationEnte);
        bottomNavigationEnte.setOnNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {
            loadFragment(homeFragmentEnte);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()){
            case R.id.nav_home_ente:
                selectedFragment = homeFragmentEnte;
                break;
            case R.id.nav_swab_ente:
                selectedFragment = generateCodeFragmentEnte;
                break;

        }
        previousFragmentClass = selectedFragment.getClass();
        return loadFragment(selectedFragment);
    }


    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_ente, fragment)
                    .commit();
            return true;
        }
        return false;
    }





}
