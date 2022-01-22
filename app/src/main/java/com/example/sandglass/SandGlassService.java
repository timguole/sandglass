package com.example.sandglass;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SandGlassService extends Service {
    private static final String tag = new String("SandGlassService");
    private MediaPlayer player = new MediaPlayer();
    private SandGlassBinder sandGlassBinder = new SandGlassBinder();

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
            return SandGlassService.this;
        }
    }
}