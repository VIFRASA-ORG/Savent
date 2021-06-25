package com.vitandreasorino.savent.Utenti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.Utenti.AccountTab.AccountFragment;
import com.vitandreasorino.savent.Utenti.EventiTab.EventFragment;
import com.vitandreasorino.savent.Utenti.GruppiTab.GroupFragment;
import com.vitandreasorino.savent.R;

import Helper.AnimationHelper;
import Model.DB.Utenti;


public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    HealthStatus actualHealtStatus = HealthStatus.NOT_DEFINED_YET;

    Toolbar topBar;
    LinearLayout imageAndStatusContainer;

    ImageView statusLogoSmall;
    ImageView statusLogoBig;
    TextView textStatusHomeBig;
    TextView textStatusHomeSmall;

    Class previousFragmentClass = null;
    TopBarConfiguration previousConfiguration = TopBarConfiguration.BIG;

    private HomeFragment homeFragment = new HomeFragment();
    private GroupFragment groupFragment = new GroupFragment();
    private AccountFragment accountFragment = new AccountFragment();
    private EventFragment eventFragment = new EventFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        topBar = (Toolbar) findViewById(R.id.toolbarStatusHome);
        imageAndStatusContainer = (LinearLayout) findViewById(R.id.imageAndStatusLayoutContainer);

        statusLogoSmall = findViewById(R.id.logoStatusHomeSmall);
        statusLogoBig = findViewById(R.id.logoStatusHomeBig);
        textStatusHomeSmall = findViewById(R.id.textStatusHomeSmall);
        textStatusHomeBig = findViewById(R.id.textStatusHomeBig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) statusLogoSmall.setAlpha(0f);

        if(savedInstanceState == null) loadFragment(new HomeFragment());
        else{
            boolean prevS = savedInstanceState.getBoolean("previousConfiguration_BIG");
            if(prevS == true) previousConfiguration = TopBarConfiguration.BIG;
            else previousConfiguration = TopBarConfiguration.SMALL;
            reloadConfiguration();
        }

        //Setting the listener to the logged in user.
        Utenti.addDocumentListener(this, newUser -> {
            setHealthStatusInView(newUser.getStatusSanitario());
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("previousConfiguration_BIG",previousConfiguration == TopBarConfiguration.BIG);
    }

    /**
     * Set the user interface based on the logged in user health status.
     *
     * @param healthStatus the logged in user halth status.
     */
    @SuppressLint("ResourceType")
    private void setHealthStatusInView(int healthStatus){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(healthStatus <= 33 && actualHealtStatus != HealthStatus.GREEN) {
                //Green
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.green_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.greenStatusText, Color.GREEN);
                actualHealtStatus = HealthStatus.GREEN;
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealtStatus != HealthStatus.YELLOW){
                //Yellow
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.yellow_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.yelloStatusText,Color.YELLOW);
                actualHealtStatus = HealthStatus.YELLOW;
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealtStatus != HealthStatus.RED){
                //Red
                if(previousConfiguration == TopBarConfiguration.SMALL) AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                else statusLogoSmall.setImageResource(R.drawable.red_status_icon);

                AnimationHelper.switchImageWithFadeAnimations(statusLogoBig,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeBig,R.string.redStatusText,Color.RED);
                actualHealtStatus = HealthStatus.RED;
            }
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(healthStatus <= 33 && actualHealtStatus != HealthStatus.GREEN) {
                //Green
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.green_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.greenStatusText,Color.GREEN);
                actualHealtStatus = HealthStatus.GREEN;
            }else if(healthStatus > 33 && healthStatus <= 66 && actualHealtStatus != HealthStatus.YELLOW){
                //Yellow
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.yellow_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.yelloStatusText,Color.YELLOW);
                actualHealtStatus = HealthStatus.YELLOW;
            }else if(healthStatus > 66 && healthStatus <= 100 && actualHealtStatus != HealthStatus.RED){
                //Red
                AnimationHelper.switchImageWithFadeAnimations(statusLogoSmall,R.drawable.red_status_icon);
                AnimationHelper.switchTextWithFadeAnimation(textStatusHomeSmall,R.string.redStatusText,Color.RED);
                actualHealtStatus = HealthStatus.RED;
            }
        }
    }

    /**
     * invoked when a configuration change happen, that is when the bundle is not null.
     */
    private void reloadConfiguration(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(previousConfiguration == TopBarConfiguration.BIG){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),0);
                AnimationHelper.fadeIn(imageAndStatusContainer,0);
                AnimationHelper.fadeOut(statusLogoSmall,0);
            }else if(previousConfiguration == TopBarConfiguration.SMALL){
                AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),0);
                AnimationHelper.fadeOut(imageAndStatusContainer,0);
                AnimationHelper.fadeIn(statusLogoSmall,0);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()){
            case R.id.nav_home:
                selectedFragment = homeFragment;
                moveTopBar(TopBarConfiguration.BIG,HomeFragment.class);
                break;
            case R.id.nav_group:
                selectedFragment = groupFragment;
                moveTopBar(TopBarConfiguration.SMALL,GroupFragment.class);
                break;
            case R.id.nav_event:
                selectedFragment = eventFragment;
                moveTopBar(TopBarConfiguration.SMALL,EventFragment.class);
                break;
            case R.id.nav_account:
                selectedFragment = accountFragment;
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

    /**
     * Function that execute the animation between the BIG top bar and the SMALL top bar configuration.
     *
     * @param configuration the new configuration to set.
     * @param selectedFragmentClass the new destination fragment. Used to avoid reloading the fragment when the shown fragment button is pressed.
     */
    private void moveTopBar(TopBarConfiguration configuration,Class selectedFragmentClass){

        //Do this only in portrait mode
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && previousFragmentClass != selectedFragmentClass && previousConfiguration != configuration){
            switch (configuration){
                case BIG:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(200f,this),400);
                    AnimationHelper.fadeIn(imageAndStatusContainer,400);
                    AnimationHelper.fadeOut(statusLogoSmall,400);
                    previousConfiguration = TopBarConfiguration.BIG;
                    break;
                case SMALL:
                    AnimationHelper.slideView(topBar,topBar.getHeight(),AnimationHelper.dpToPx(55f,this),400);
                    AnimationHelper.fadeOut(imageAndStatusContainer,200);
                    AnimationHelper.fadeIn(statusLogoSmall,200);
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





    /*

        ENUM DECLARATION

     */

    /**
     * Used to explicit the usr interface status based on the user Health status.
     */
    private enum HealthStatus{
        GREEN,
        YELLOW,
        RED,
        NOT_DEFINED_YET;
    }

    /**
     * Used to define the user interface status based on the top bar configuration.
     */
    private enum TopBarConfiguration{
        BIG,
        SMALL
    }
    
}