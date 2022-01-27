package com.example.sandglass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MainActivity extends AppCompatActivity
        implements ServiceConnection, View.OnClickListener {
    private static final String tag = "SandGlass a1";
    private SandGlassService sandGlassService = null;
    private Intent serviceIntent = null;
    private EditText time_input = null;
    private TextView current_alarm = null;
    private Button btn_start = null;
    private Button btn_cancel = null;
    private final UIReceiver uiReceiver = new UIReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "Call onCreate");

        serviceIntent = new Intent(this, SandGlassService.class);
        startService(serviceIntent);
        boolean bindStatus = bindService(serviceIntent, this, BIND_AUTO_CREATE);
        Log.e(tag, "bind status: " + bindStatus);

        setContentView(R.layout.activity_main);
        initUI();
        btn_start.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(tag, "Call onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(tag, "Call onResume, register receiver");
        IntentFilter filter =new IntentFilter(
                getString(R.string.update_ui));
        filter.addAction(getString(R.string.update_ui));
        registerReceiver(uiReceiver, filter);
        updateUI();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(tag, "Call onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(tag, "Call onPause, unregister receiver");
        unregisterReceiver(uiReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(tag, "Call onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        stopService(serviceIntent);
        Log.e(tag, "Call onDestroy");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(tag, "Call onServiceConnected");
        sandGlassService = ((SandGlassService.SandGlassBinder)service).getService();
        updateUI();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(tag, "Call onServiceDisconnected");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                onStartClicked(view);
                break;
            case R.id.btn_cancel:
                onCancelClicked(view);
                break;
        }
    }

    private void initUI() {
        time_input = findViewById(R.id.time_input);
        current_alarm = findViewById(R.id.current_alarm);
        btn_start = findViewById(R.id.btn_start);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    private void onStartClicked(View view) {
        String s = time_input.getText().toString();
        Toast toast = Toast.makeText(
                this,
                R.string.on_invalid_input,
                Toast.LENGTH_LONG);
        long minutes;

        try {
            minutes = Long.parseLong(s);
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
            updateUI();
        } else {
            Toast.makeText(
                    this,
                    R.string.service_not_available,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onCancelClicked(View view) {
        Log.e(tag, "on btn_cancel clicked");
        if (sandGlassService == null) {
            Toast.makeText(
                    this,
                    R.string.service_not_available,
                    Toast.LENGTH_LONG).show();
            return;
        }
        sandGlassService.cancelSandGlass();
        updateUI();
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
        Calendar now = Calendar.getInstance();
        long currentInMilli = now.getTimeInMillis();
        long alarmInMilli = sandGlassService.getSandGlass();
        boolean alarmIsValid = (currentInMilli < alarmInMilli);
        String t;

        if (alarmIsValid) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmInMilli);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            t = String.format("%02d:%02d:%02d 响铃", hour, minute, second);
            Log.e(tag, t);
            btn_start.setEnabled(false);
            time_input.setText("");
            time_input.setEnabled(false);
            btn_cancel.setEnabled(true);
        } else {
            btn_start.setEnabled(true);
            time_input.setEnabled(true);
            btn_cancel.setEnabled(false);
            t = getString(R.string.no_active_alarm);
        }
        current_alarm.setText(t);
    }

    public class UIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(tag, "Call onReceive");
            updateUI();
        }
    }
}