package com.vitandreasorino.savent.Utenti.EventiTab;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.vitandreasorino.savent.Utenti.EventiTab.CreazioneEvento.NewEvent;
import com.vitandreasorino.savent.R;

public class EventFragment extends Fragment {

    TabLayout upperTabBar;
    View rootView;
    ViewPager2 pager;
    Button buttonCreateEvent, buttonMyPartecipations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonCreateEvent = view.findViewById(R.id.buttonCreaEvento);
        buttonMyPartecipations = view.findViewById(R.id.buttonMyPartecipations);
        upperTabBar = view.findViewById(R.id.upperTabBar);
        pager = (ViewPager2) view.findViewById(R.id.pagerSearchAndNearbyEvents);
        pager.setSaveEnabled(false);

        //Impostazione dell'adattatore personalizzato
        pager.setAdapter(new CollectionAdapter(this));

        //Attacco il mediatore di layout
        new TabLayoutMediator(upperTabBar, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.searchEvents);
                        break;
                    case 1:
                        tab.setText(R.string.myEvents);
                        break;
                    case 2:
                        tab.setText(R.string.nearbyEvents);
                        break;
                }
            }
        }).attach();

        //Gestire l'evento di cambio pagina in modo tale da disabilitare il gesto di scorrimento a sinistra quando viene mostrato il frammento di mappa
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                //disabilitazione del gesto nella visualizzazione mappa e riabilitazione nella visualizzazione elenco.
                if(position == 0 || position == 1) pager.setUserInputEnabled(true);
                else if (position == 2) pager.setUserInputEnabled(false);
            }
        });

        buttonCreateEvent.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), NewEvent.class);
            startActivity(i);
        });

        buttonMyPartecipations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MyEventPartecipations.class);
                startActivity(i);
            }
        });
    }
}

/**
 * Adapter creato appositamente per mostrare i due frammenti all'interno del Frammento Evento
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
                return new FragmentSearchEvent(FragmentSearchEvent.SearchEventType.ALL_EVENTS);
            case 1:
                return new FragmentSearchEvent(FragmentSearchEvent.SearchEventType.MY_EVENTS);
            case 2:
                return new FragmentMaps();

            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}