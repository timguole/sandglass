package com.example.sandglass;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity  {
    private static final String tag = new String("Sandglass a1");

    private static final String sg_action = new String("com.example.sandglass.action.SANDGLASS");

    private EditText time_input = null;
    private TextView time_remain = null;
    private Button btn_start = null;
    private Button btn_cancel = null;

    private void initUI() {
        time_input = findViewById(R.id.time_input);
        time_remain = findViewById(R.id.time_remain);
        btn_start = findViewById(R.id.btn_start);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "Call onCreate");

        setContentView(R.layout.activity_main);
        initUI();
        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = time_input.getText().toString();
                Toast toast = Toast.makeText(MainActivity.this, "分钟数必须大于零", Toast.LENGTH_LONG);
                int minutes = 0;

                try {
                    minutes = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    Log.e(tag, "Input is not a number: \"" + s + "\"");
                    toast.show();
                    return;
                }

                if (minutes <= 0) {
                    toast.show();
                    return;
                }
                Calendar c = Calendar.getInstance();
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent i = new Intent(sg_action);
                i.setClass(MainActivity.this, SandGlassReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, i, 0);
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + 60000, pi);
                Log.e(tag, "time stamp: " + c.getTimeInMillis());
                Log.e(tag, "Setup alarm done");
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(tag, "send broadcast");
                Intent i = new Intent(sg_action);
                i.setClass(MainActivity.this, SandGlassReceiver.class);
                sendBroadcast(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(tag, "Call onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(tag, "Call onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(tag, "Call onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(tag, "Call onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(tag, "Call onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(tag, "Call onDestroy");
    }
}