package com.vitandreasorino.savent.Utenti.Notification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Utenti.HomeActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Model.Pojo.Evento;
import Model.Pojo.Notification;

public class NotificationActivity extends AppCompatActivity {

    private ListView notificationListView;
    private List<Notification> notifications;
    private NotificationAdapter adapter;
    boolean fromNotificaton = false;

    public final static String FROM_NOTIFICATION_INTENT = "FROM_NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        fromNotificaton = getIntent().getBooleanExtra(FROM_NOTIFICATION_INTENT,false);

        notificationListView = findViewById(R.id.notificationListView);
        notificationListView.setEmptyView(findViewById(R.id.emptyResults));

        notifications = new ArrayList<>();
        notifications.add(new Notification("","Notifica 1", "Descrizione 1",true));
        notifications.add(new Notification("","Notifica 2", "Descrizione 2",true));
        notifications.add(new Notification("","Notifica 2", "Descrizione 2"));
        notifications.add(new Notification("","Notifica 3", "Descrizione 3"));
        notifications.add(new Notification("","Notifica 3", "Descrizione 3"));
        notifications.add(new Notification("","Notifica 1", "Descrizione 1"));
        notifications.add(new Notification("","Notifica 2", "Descrizione 2"));
        notifications.add(new Notification("","Notifica 3", "Descrizione 3"));
        notifications.add(new Notification("","Notifica 1", "Descrizione 1"));
        notifications.add(new Notification("","Notifica 1", "Descrizione 1"));
        notifications.add(new Notification("","Notifica 2", "Descrizione 2"));
        notifications.add(new Notification("","Notifica 3", "Descrizione 3"));
        notifications.add(new Notification("","Notifica 1", "Descrizione 1"));
        notifications.add(new Notification("","Notifica 2", "Descrizione 2"));
        notifications.add(new Notification("","Notifica 3", "Descrizione 3"));


        System.out.println(Calendar.getInstance().getTimeInMillis());

        adapter = new NotificationAdapter(this, notifications);
        notificationListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    public void onBackButtonPressed(View view){
        if (fromNotificaton){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }else super.onBackPressed();
        finish();
    }

}


class NotificationAdapter extends BaseAdapter{

    private List<Notification> notifications;
    private Context context;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.notifications = notifications;
        this.context=context;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.notification_list_row, null);
        Notification n = (Notification) getItem(position);

        TextView titolo = convertView.findViewById(R.id.textViewTitolo);
        TextView desc = convertView.findViewById(R.id.textViewDescrizione);
        ConstraintLayout back = convertView.findViewById(R.id.backgroundLayout);

        titolo.setText(n.getTitle());
        desc.setText(n.getDescription());

        if(n.isRead()) back.setBackgroundColor(Color.rgb(224,222,255));
        else back.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }
}