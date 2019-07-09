package com.nikhil.kalendarviewexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nikhil.kalendarview.KalendarView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    KalendarView kalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kalendarView = findViewById(R.id.kalendarView);
        kalendarView.setEventHandler(new KalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {

            }

            @Override
            public void onDayClicked(Date date) {

            }
        });

    }
}
