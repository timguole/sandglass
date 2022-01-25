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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public class SandGlassService extends Service {
    private static final String tag = new String("SandGlassService");
    private static final String timestampFile = new String("timestamp.txt");

    private MediaPlayer player = new MediaPlayer();
    private SandGlassBinder sandGlassBinder = new SandGlassBinder();
    private long sandGlassInMilli = 0;

    public SandGlassService() {
   }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(tag, "Call onCreate");
        readTimestamp();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.e(tag, "Call onStartCommand");
        if (intent == null) {
            return START_STICKY;
        }
        String data = intent.getStringExtra("play");
        if (data != null) {
            Log.e(tag, "start and play");
            startBeep();
            String updateUI = intent.getStringExtra("updateUI");
            if (updateUI != null) {
                Log.e(tag, "update ui");
                Intent uiIntent = new Intent();
                uiIntent.setAction(getString(R.string.update_ui));
                sendBroadcast(uiIntent);
            }
        } else {
            Log.e(tag, "start but not play");
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(tag, "Call onBind");
        return sandGlassBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(tag, "Call onDestroy");
        saveTimestamp();
        player.release();
    }

    private void readTimestamp() {
        Log.e(tag, "Call readTimestamp");
        File f = null;
        FileReader fileReader = null;
        String line = null;
        BufferedReader bufferedReader = null;

        try {
         File baseDir = getFilesDir();
            String path = Paths.get(baseDir.toString(), timestampFile).toString();
            Log.e(tag, path);
            f = new File(path);
            f.createNewFile();
            bufferedReader = new BufferedReader(new FileReader(f));
            line = bufferedReader.readLine();
            bufferedReader.close();
        } catch (Exception e) {
            Log.e(tag, e.toString());
            return;
        }

        try {
            if (line == null) {
                Log.e(tag, "No data in file");
                return;
            }
            sandGlassInMilli = Long.parseLong(line);
        } catch (Exception e) {
            Log.e(tag, e.toString());
        }
        Log.e(tag, "read back timestamp from file: " + line);
        Log.e(tag, "timestamp: " + sandGlassInMilli);
    }

    private void saveTimestamp() {
        Log.e(tag, "Call saveTimestamp");
        try {
            File baseDir = getFilesDir();
            String path = Paths.get(baseDir.toString(), timestampFile).toString();
            File f = new File(path);
            f.createNewFile();
            FileWriter fileWriter = new FileWriter(f);
            fileWriter.write(Long.toString(sandGlassInMilli));
            fileWriter.close();
        } catch (Exception e) {
            Log.e(tag, e.toString());
        }
    }

    public void setSandGlass(int minutes) {
        Log.e(tag, "Call setSandGlass: " + minutes);
        if (minutes <= 0) {
            return;
        }
        Calendar calender = Calendar.getInstance();
        long currentRTC = calender.getTimeInMillis();
        sandGlassInMilli = currentRTC + minutes * 60 * 1000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getString(R.string.sand_glass_action));
        intent.setClass(this, SandGlassReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                0);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                sandGlassInMilli,
                pendingIntent);

        // If minute is more than 'vault', set a pre-alarm
        final int vault = 5;
        if (minutes > vault) {
            Log.e(tag, "set preAlarm");
            long preAlarm = sandGlassInMilli - vault * 60 * 1000;
            Intent preIntent = new Intent(
                    getString(R.string.pre_sand_glass_action));
            preIntent.setClass(this, SandGlassReceiver.class);
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    preIntent,
                    0);
            alarmManager.setExactAndAllowWhileIdle(
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

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getString(R.string.sand_glass_action));
        intent.setClass(this, SandGlassReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                0);
        alarmManager.cancel(pendingIntent);

        Intent preIntent = new Intent(
                getString(R.string.pre_sand_glass_action));
        preIntent.setClass(this, SandGlassReceiver.class);
        PendingIntent prePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                preIntent,
                0);
        alarmManager.cancel(prePendingIntent);

        // reset timestamp
        sandGlassInMilli = 0;
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