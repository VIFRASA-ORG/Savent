package com.vitandreasorino.savent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.EventiTab.EventFragment;

import Helper.AnimationHelper;


public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    Toolbar topBar;
    LinearLayout imageAndStatusContainer;
    ImageView statusLogoTopLeft;

    Class previousFragmentClass = null;
    TopBarConfiguration previousConfiguration = TopBarConfiguration.BIG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        topBar = (Toolbar) findViewById(R.id.toolbarStatusHome);
        imageAndStatusContainer = (LinearLayout) findViewById(R.id.imageAndStatusLayoutContainer);
        statusLogoTopLeft = (ImageView) findViewById(R.id.logoStatusHome1);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) statusLogoTopLeft.setAlpha(0f);

        if(savedInstanceState == null) loadFragment(new HomeFragment());
        else{
            boolean prevS = savedInstanceState.getBoolean("previousConfiguration_BIG");
            if(prevS == true) previousConfiguration = TopBarConfiguration.BIG;
            else previousConfiguration = TopBarConfiguration.SMALL;
            reloadConfiguration();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("previousConfiguration_BIG",previousConfiguration == TopBarConfiguration.BIG);
    }

    private void reloadConfiguration(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(previousConfiguration == TopBarConfiguration.BIG){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),0);
                AnimationHelper.fadeIn(imageAndStatusContainer,0);
                AnimationHelper.fadeOut(statusLogoTopLeft,0);
            }else if(previousConfiguration == TopBarConfiguration.SMALL){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),0);
                AnimationHelper.fadeOut(imageAndStatusContainer,0);
                AnimationHelper.fadeIn(statusLogoTopLeft,0);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()){
            case R.id.nav_home:
                selectedFragment = new HomeFragment();
                moveTopBar(TopBarConfiguration.BIG,HomeFragment.class);
                break;
            case R.id.nav_group:
                selectedFragment = new GroupFragment();
                moveTopBar(TopBarConfiguration.SMALL,GroupFragment.class);
                break;
            case R.id.nav_event:
                selectedFragment = new EventFragment();
                moveTopBar(TopBarConfiguration.SMALL,EventFragment.class);
                break;
            case R.id.nav_account:
                selectedFragment = new AccountFragment();
                moveTopBar(TopBarConfiguration.SMALL,AccountFragment.class);
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
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }


    private void moveTopBar(TopBarConfiguration configuration,Class selectedFragmentClass){

        //Do this only in portrait mode
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && previousFragmentClass != selectedFragmentClass && previousConfiguration != configuration){
            switch (configuration){
                case BIG:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),400);
                    AnimationHelper.fadeIn(imageAndStatusContainer,400);
                    AnimationHelper.fadeOut(statusLogoTopLeft,400);
                    previousConfiguration = TopBarConfiguration.BIG;
                    break;
                case SMALL:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),400);
                    AnimationHelper.fadeOut(imageAndStatusContainer,200);
                    AnimationHelper.fadeIn(statusLogoTopLeft,200);
                    previousConfiguration = TopBarConfiguration.SMALL;
                    break;
            }
        }

        //if landscape update only the previousConfiguration
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            switch (configuration){
                case BIG:
                    previousConfiguration = TopBarConfiguration.BIG;
                    break;
                case SMALL:
                    previousConfiguration = TopBarConfiguration.SMALL;
                    break;
            }
        }
    }

    private enum TopBarConfiguration{
        BIG,
        SMALL
    }
}