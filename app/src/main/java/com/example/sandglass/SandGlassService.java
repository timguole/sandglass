package com.example.sandglass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

public class SandGlassService extends Service {
    private static final String tag = new String("SandGlassService");
    private static final String sg_action = new String("com.example.sandglass.action.SANDGLASS");
    private static final String presg_action = new String("com.example.sandglass.action.PRESANDGLASS");

    private MediaPlayer player = new MediaPlayer();
    private SandGlassBinder sandGlassBinder = new SandGlassBinder();
    private long sandGlassInMilli = 0;

    public SandGlassService() {
   }

    @Override
    public void onCreate() {
        Log.e(tag, "Call onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.e(tag, "Call onStartCommand");
        if (intent == null) {
            return START_NOT_STICKY;
        }
        String data = intent.getStringExtra("play");
        if (data != null) {
            startBeep();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(tag, "Call onDestroy");
        player.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(tag, "Call onBind");
        return sandGlassBinder;
    }

    public void setSandGlass(int minutes) {
        Log.e(tag, "Call setSandGlass: " + minutes);
        if (minutes <= 0) {
            return;
        }
        Calendar calender = Calendar.getInstance();
        long currentRTC = calender.getTimeInMillis();
        long finalAlarm = currentRTC + minutes * 60 * 1000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(sg_action);
        intent.setClass(this, SandGlassReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                0);
        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                finalAlarm,
                pendingIntent);

        // If minute is more than 'vault', set a pre-alarm
        final int vault = 1;
        if (minutes > vault) {
            Log.e(tag, "set preAlarm");
            long preAlarm = finalAlarm - vault * 60 * 1000;
            Intent preIntent = new Intent(presg_action);
            preIntent.setClass(this, SandGlassReceiver.class);
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    preIntent,
                    0);
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    preAlarm,
                    prePendingIntent);
        }
        Log.e(tag, "Setup alarm done");
    }

    public long getSandGlass() {
        Log.e(tag, "Call getSandGlass");
        return sandGlassInMilli;
    }

    public void cancelSandGlass() {
        Log.e(tag, "Call cancelSandGlass");
    }

    public void startBeep() {
        Log.e(tag, "Call startBeep");
        try {
            AssetManager am = getAssets();

            AssetFileDescriptor afd = am.openFd("beep.wav");
            player.reset();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    Log.e(tag, "Call onCompletion");
                }
            });
            player.setDataSource(afd);
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e(tag, "Failed to play sound");
            Log.e(tag, e.toString());
        }
    }

    public void stopBeep() {
        Log.e(tag, "Call stopBeep");
        player.stop();
    }

    public class SandGlassBinder extends Binder {
        public SandGlassService getService() {
            Log.e(tag, "Call getService");
            // This is a local service, so return service object directly.
            return SandGlassService.this;
        }
    }
}