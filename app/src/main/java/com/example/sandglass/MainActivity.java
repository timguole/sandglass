package com.example.sandglass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private static final String tag = new String("Sandglass a1");

    private SandGlassService sandGlassService = null;
    private Intent serviceIntent = null;

    private EditText time_input = null;
    private TextView current_alarm = null;
    private Button btn_start = null;
    private Button btn_cancel = null;

    private void initUI() {
        time_input = findViewById(R.id.time_input);
        current_alarm = findViewById(R.id.current_alarm);
        btn_start = findViewById(R.id.btn_start);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "Call onCreate");

        serviceIntent = new Intent(this, SandGlassService.class);
        startService(serviceIntent);

        setContentView(R.layout.activity_main);
        initUI();

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = time_input.getText().toString();
                Toast toast = Toast.makeText(
                        MainActivity.this,
                        R.string.on_invalid_input,
                        Toast.LENGTH_LONG);
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
                if (sandGlassService != null) {
                    sandGlassService.setSandGlass(minutes);
                    showCurrentAlarm();
                    updateUI();
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "无法绑定到后台服务",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(tag, "on btn_cancel clicked");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(tag, "Call onStart");
        boolean bindStatus = bindService(serviceIntent, this, BIND_AUTO_CREATE);
        Log.e(tag, "bind status: " + bindStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(tag, "Call onResume");
       showCurrentAlarm();
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
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
        Log.e(tag, "Call onDestroy");
    }

    private void showCurrentAlarm() {
        Log.e(tag, "Call showCurrentAlarm");
        if (sandGlassService == null) {
            Log.e(tag, "service is null");
            return;
        }

        Calendar now = Calendar.getInstance();
        long currentInMilli = now.getTimeInMillis();
        long alarmInMilli = sandGlassService.getSandGlass();
        boolean alarmIsValid = (currentInMilli < alarmInMilli);
        String t = null;

        Log.e(tag, "now: " + currentInMilli);
        Log.e(tag, "alm: " + alarmInMilli);
        Log.e(tag, "is valid: " + alarmIsValid);

        if (alarmIsValid) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmInMilli);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            t = String.format("%02d:%02d:%02d 响铃", hour, minute, second);
            Log.e(tag, t);
        } else {
            t = new String("没有活跃闹钟");
        }
        current_alarm.setText(t);

    }

    // Only one alarm is allowed. A new alarm can be set only after
    // the current valid alarm is cancelled or there is no valid alarm.
    //
    // If there is no valid alarm, the 'cancel' button is not enabled and
    // other widgets are enabled.
    private void updateUI() {
        if (sandGlassService == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        long currentInMilli = calendar.getTimeInMillis();
        long alarmInMilli = sandGlassService.getSandGlass();
        boolean alarmIsValid = (currentInMilli < alarmInMilli);

        if (alarmIsValid) {
            btn_start.setEnabled(false);
            time_input.setText(new String(""));
            time_input.setEnabled(false);
            btn_cancel.setEnabled(true);
        } else {
            btn_start.setEnabled(true);
            time_input.setEnabled(true);
            btn_cancel.setEnabled(false);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(tag, "Call onServiceConnected");
        sandGlassService = ((SandGlassService.SandGlassBinder)service).getService();
        showCurrentAlarm();
        updateUI();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(tag, "Call onServiceDisconnected");
    }
}