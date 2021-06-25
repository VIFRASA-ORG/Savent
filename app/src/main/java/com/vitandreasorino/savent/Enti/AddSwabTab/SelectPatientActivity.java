package com.vitandreasorino.savent.Enti.AddSwabTab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitandreasorino.savent.R;

import java.util.Calendar;

public class SelectPatientActivity extends AppCompatActivity {

    private TextView textBirthdayPatient;
    private ImageView imageBirthdayPatient;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_patient);
        textBirthdayPatient = (TextView) findViewById(R.id.textBirthdayPatient);
        imageBirthdayPatient = (ImageView) findViewById(R.id.imageBirthdayPatient);
        imageBirthdayPatient.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Calendar cal1 = (Calendar) cal.clone();

                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(SelectPatientActivity.this,
                        dateSetListener, year, month, day);

                dialog.getDatePicker().setMinDate(cal1.getTime().getTime());
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                String date = dayOfMonth + "/" + (month+1) + "/" + year;

                Calendar calendario = Calendar.getInstance();
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.YEAR, year);

                textBirthdayPatient.setText(date);

            }
        };

    }
    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }
}