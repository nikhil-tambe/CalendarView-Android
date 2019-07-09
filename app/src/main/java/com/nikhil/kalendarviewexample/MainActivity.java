package com.nikhil.kalendarviewexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.nikhil.kalendarview.KalendarView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "nnn_MainActivity";
    //
    KalendarView kalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kalendarView = findViewById(R.id.kalendarView);
        /*kalendarView.setAllowHighlight(true);
        kalendarView.setDateFormat("dd MMM yyyy");
        kalendarView.setHighLightResource(R.drawable.round_arrow_left_black_48);
        kalendarView.updateCalendar();*/

        kalendarView.setEventHandler(new KalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {
                Log.d(TAG, "onDayLongPress: " + date.toString());
            }

            @Override
            public void onDayClicked(Date date) {
                Log.d(TAG, "onDayClicked: " + date.toString());
            }
        });

    }
}
