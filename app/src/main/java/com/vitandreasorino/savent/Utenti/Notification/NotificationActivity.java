package com.vitandreasorino.savent.Utenti.Notification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Utenti.EventiTab.EventDetailActivity;
import com.vitandreasorino.savent.Utenti.GruppiTab.GroupDetailActivity;
import com.vitandreasorino.savent.Utenti.HomeActivity;
import java.text.SimpleDateFormat;
import java.util.List;

import Helper.NotificationHelper;
import Helper.LocalStorage.SQLiteHelper;
import Model.Pojo.Notification;

public class NotificationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView notificationListView;
    private List<Notification> notifications;
    private NotificationAdapter adapter;
    private AlertDialog.Builder alertDelete;
    boolean fromNotificaton = false;
    SQLiteHelper databaseHelper;

    private SwipeRefreshLayout pullToRefresh;
    private SwipeRefreshLayout emptyResultsPullToRefresh;

    public final static String FROM_NOTIFICATION_INTENT = "FROM_NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        fromNotificaton = getIntent().getBooleanExtra(FROM_NOTIFICATION_INTENT,false);

        notificationListView = findViewById(R.id.notificationListView);
        emptyResultsPullToRefresh = findViewById(R.id.emptyResultsPullToRefresh);
        pullToRefresh = findViewById(R.id.pullToRefresh);

        notificationListView.setEmptyView(emptyResultsPullToRefresh);

        //Setting the alert for the cancellation of a notification
        alertDelete = new  AlertDialog.Builder(this);
        alertDelete.setTitle(R.string.notificationCancellationTitle);
        alertDelete.setMessage(R.string.notificationCancellationMessage);

        //setting the action for the Pull-To-Refresh action
        pullToRefresh.setOnRefreshListener(() -> downloadAllNotification());
        emptyResultsPullToRefresh.setOnRefreshListener( () -> downloadAllNotification());

        downloadAllNotification();
    }

    private void downloadAllNotification(){
        //Download of all the notification
        databaseHelper = new SQLiteHelper(this);
        notifications = databaseHelper.getAllNotificaton(this);

        //Creating the adapter
        adapter = new NotificationAdapter(this, notifications, alertDelete);
        notificationListView.setAdapter(adapter);
        notificationListView.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();

        pullToRefresh.setRefreshing(false);
        emptyResultsPullToRefresh.setRefreshing(false);
    }

    /**
     * Function called when the back button is pressed
     * @param view
     */
    public void onBackButtonPressed(View view){
        /**
         * If this activity is opened on the pression of the notification,
         * nothing else is on the activity stack, so we have to create
         * the HomeActivity
         **/
        if (fromNotificaton){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }else super.onBackPressed();
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Notification selected = notifications.get(position);

        switch (selected.getNotificationType()){
            case NotificationHelper.QUEUE_CLIMBED_NOTIFICATION:
                Intent i = new Intent(this, EventDetailActivity.class);
                i.putExtra(FROM_NOTIFICATION_INTENT,true);
                i.putExtra("eventId",selected.getEventId());
                startActivity(i);
                break;
            case NotificationHelper.NEW_GROUP_NOTIFICATION:
                Intent i1 = new Intent(this, GroupDetailActivity.class);
                i1.putExtra(FROM_NOTIFICATION_INTENT,true);
                i1.putExtra("groupId",selected.getGroupId());
                startActivity(i1);
                break;
        }

        if (!selected.isRead()){
            databaseHelper.readANotification(selected.getId());
            selected.setRead(true);
            adapter.notifyDataSetChanged();
        }
    }
}


class NotificationAdapter extends BaseAdapter{

    GradientDrawable contactRiskGradientBackground = new GradientDrawable();
    private List<Notification> notifications;
    private Context context;
    private AlertDialog.Builder alertDelete;

    public NotificationAdapter(Context context, List<Notification> notifications, AlertDialog.Builder alertDelete) {
        this.notifications = notifications;
        this.context=context;
        this.alertDelete = alertDelete;

        contactRiskGradientBackground.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        contactRiskGradientBackground.setColors( new int[] { Color.argb(150,255,0,0),Color.argb(255,255,204,204) });
        contactRiskGradientBackground.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        contactRiskGradientBackground.setGradientRadius(500.0f);
        contactRiskGradientBackground.setGradientCenter(0.5f, 0.5f);
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
        TextView dateTime = convertView.findViewById(R.id.textViewDateTime);
        ImageView dismissImageView = convertView.findViewById(R.id.dismissImageView);
        ImageView notificationRead = convertView.findViewById(R.id.notificationRead);

        //Adding the on click event to the dismiss button
        dismissImageView.setOnClickListener(v -> {
            //Showing a dialog with the confirmation
            alertDelete.setPositiveButton(R.string.confirmPositive, (dialog, which) -> {
                SQLiteHelper db = new SQLiteHelper(context);
                db.deleteNotification(n.getId());
                notifications.remove(position);
                notifyDataSetChanged();

                Toast.makeText(context,R.string.notificationSuccessfullyCancelled , Toast.LENGTH_SHORT).show();
            });

            alertDelete.setNegativeButton(R.string.confirmNegative, null);
            alertDelete.show();
        });

        titolo.setText(n.getTitle());
        desc.setText(n.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateTime.setText(sdf.format(n.getDate().getTime()));

        if(n.isRead()) notificationRead.setVisibility(View.INVISIBLE);
        else notificationRead.setVisibility(View.VISIBLE);

        if(n.getNotificationType().equals(NotificationHelper.CONTACT_RISK_NOTIFICATION) ) convertView.setBackground(contactRiskGradientBackground);
        else convertView.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }
}