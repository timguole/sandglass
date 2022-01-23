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

    private static final String sg_action = new String("com.example.sandglass.action.SANDGLASS");
    private static final String presg_action = new String("com.example.sandglass.action.PRESANDGLASS");

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
                        "分钟数必须是大于零的整数",
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
                    disableUIOnSet();
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

        long timestamp = sandGlassService.getSandGlass();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String t = String.format("%02d:%02d:%02d响铃", hour, minute, second);
        current_alarm.setText(t);
        Log.e(tag, t);
    }

    private void disableUIOnSet() {
        btn_start.setClickable(false);
        time_input.setText(new String(""));
        time_input.setEnabled(false);
    }

    private void enableUIOnCancel() {
        btn_start.setClickable(true);
        time_input.setEnabled(true);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(tag, "Call onServiceConnected");
        sandGlassService = ((SandGlassService.SandGlassBinder)service).getService();
        showCurrentAlarm();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(tag, "Call onServiceDisconnected");
    }
}