package com.vitandreasorino.savent.Enti;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vitandreasorino.savent.Enti.GenerateCodeTab.GenerateCodeFragment;
import com.vitandreasorino.savent.R;

public class HomeActivityEnte extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ViewPager2 viewPager;

    BottomNavigationView bottomNavigationEnte;
    Class previousFragmentClass = HomeFragmentEnte.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ente);

        bottomNavigationEnte = (BottomNavigationView) findViewById(R.id.bottomNavigationEnte);
        bottomNavigationEnte.setOnNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new HomeFragmentEnteAdapter(this));
        viewPager.setUserInputEnabled(false);

        if(savedInstanceState != null){
            String prevClass = savedInstanceState.getString("previousFragmentClass");
            switch (prevClass){
                case "HomeFragmentEnte":
                    viewPager.setCurrentItem(0);
                    bottomNavigationEnte.setSelectedItemId(R.id.nav_home_ente);
                    break;
                case "GenerateCodeFragment":
                    viewPager.setCurrentItem(1);
                    bottomNavigationEnte.setSelectedItemId(R.id.nav_swab_ente);
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("previousFragmentClass",previousFragmentClass.getSimpleName());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home_ente:
                viewPager.setCurrentItem(0,false);
                previousFragmentClass = HomeFragmentEnte.class;
                return true;
            case R.id.nav_swab_ente:
                viewPager.setCurrentItem(1,false);
                previousFragmentClass = GenerateCodeFragment.class;
                return true;
        }
        return false;
    }

}


/**
 * Adepter specifically created to show the 2 fragment inside the HomeActivityEnte
 */
class HomeFragmentEnteAdapter extends FragmentStateAdapter {

    public HomeFragmentEnteAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:
                return new HomeFragmentEnte();
            case 1:
                return new GenerateCodeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
