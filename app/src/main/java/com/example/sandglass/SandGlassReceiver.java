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

public class SandGlassReceiver extends BroadcastReceiver {
    private static final String tag = new String("SandGlassReceiver");
    private SandGlassService sandGlassService = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "Call onReceive");
        Log.e(tag, "intent action: " + intent.getAction());
        
        // start service to play sound
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, SandGlassService.class);
        serviceIntent.putExtra("play", "true");
        if (intent.getAction().equals(
                context.getString(R.string.sand_glass_action))) {
            serviceIntent.putExtra("updateUI", "true");
        }
        context.startService(serviceIntent);
    }
}