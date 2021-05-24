package com.vitandreasorino.savent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(savedInstanceState == null) loadFragment(new HomeFragment());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()){
            case R.id.nav_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.nav_group:
                selectedFragment = new GroupFragment();
                break;
            case R.id.nav_event:
                selectedFragment = new EventFragment();
                break;
            case R.id.nav_account:
                selectedFragment = new AccountFragment();
                break;
        }
        return loadFragment(selectedFragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}