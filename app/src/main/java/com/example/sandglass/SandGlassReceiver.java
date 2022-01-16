package com.example.sandglass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class SandGlassReceiver extends BroadcastReceiver {
    private static final String tag = new String("SandGlassReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(tag, "Call onReceive");

        AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        int oldAudioMode = audioManager.getMode();
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        try {
            AssetManager am = context.getAssets();
            MediaPlayer player = new MediaPlayer();

            AssetFileDescriptor afd = am.openFd("beep.wav");
            player.reset();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    Log.e(tag, "Current mode: " + audioManager.getMode());
                    Log.e(tag, "Recover old audio mode: " + oldAudioMode);
                    audioManager.setMode(oldAudioMode);
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
}