package com.example.sandglass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import com.example.sandglass.SandGlassService;

public class SandGlassReceiver extends BroadcastReceiver implements ServiceConnection {
    private static final String tag = new String("SandGlassReceiver");
    private SandGlassService sandGlassService = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "Call onReceive");
        
        // start service to play sound
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, SandGlassService.class);
        serviceIntent.putExtra("play", "true");
        context.startService(serviceIntent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(tag, "Call onServiceConnected");
        sandGlassService = ((SandGlassService.SandGlassBinder)service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(tag, "Call onServiceDisconnected");
    }
}