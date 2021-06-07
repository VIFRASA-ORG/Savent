package com.vitandreasorino.savent.EventiTab;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.vitandreasorino.savent.R;

public class EventFragment extends Fragment {

    TabLayout upperTabBar;
    View rootView;
    ViewPager2 pager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        upperTabBar = view.findViewById(R.id.upperTabBar);
        pager = (ViewPager2) view.findViewById(R.id.pagerSearchAndNearbyEvents);

        //Setting the custom made adapter
        pager.setAdapter(new CollectionAdapter(this));

        //Attaching the layout mediator
        new TabLayoutMediator(upperTabBar, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.searchEvents);
                        break;
                    case 1:
                        tab.setText(R.string.nearbyEvents);
                }
            }
        }).attach();

        //Handling the on page change event in such a way to disable the scroll left gesture when the map fragment in shown
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                //disabling the gesture in the map view and re-enabling it in the list view.
                if(position == 0) pager.setUserInputEnabled(true);
                else if (position == 1) pager.setUserInputEnabled(false);
            }
        });
    }
}

/**
 * Adepter specifically created to show the two fragment inside the Event Fragment
 */
class CollectionAdapter extends FragmentStateAdapter{

    public CollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:
                return new FragmentSearchEvent();
            case 1:
                return new FragmentMaps();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}