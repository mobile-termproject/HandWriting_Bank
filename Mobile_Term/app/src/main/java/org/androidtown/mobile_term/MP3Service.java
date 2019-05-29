package org.androidtown.mobile_term;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MP3Service extends Service {

    FileInputStream fis = null;
    MediaPlayer mediaPlayer;
    int state = 0;
    String path = "";
    String filename = "";
    NotificationManager notifManager;
    NotificationCompat.Builder mBuilder;
    Uri playparser;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSelf();
                notifManager.cancelAll();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            path = FileList.servicepath;
            filename = FileList.servicename;

            File mp3file = new File(path);
            if (intent.getAction().equals(CommandActions.TOGGLE_PLAY) && mp3file.exists()) {
                state = 1;
                new Thread(mRun).start();
                showCustomNotification();
            } else if (intent.getAction().equals(CommandActions.MP4_PLAY) && mp3file.exists()) {
                state = 1;
                new Thread(tRun).start();
                showCustomNotification();
            } else if (intent.getAction().equals(CommandActions.PLAY)) {
                state = 0;
                if(mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                else
                    mediaPlayer.start();
                showCustomNotification();
            } else if (intent.getAction().equals(CommandActions.CLOSE)) {
                notifManager.cancelAll();
                stopSelf();
            }

        }
        return START_STICKY;
    }

    Runnable mRun = new Runnable() {

        @Override
        public void run() {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable tRun = new Runnable() {

        @Override
        public void run() {
            try {
                fis = new FileInputStream(path);
                mediaPlayer.setDataSource(fis.getFD());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent agr0) {
        return null;
    }

    private void showCustomNotification(){
        mBuilder = createNotification();

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification_player);
        remoteViews.setTextViewText(R.id.txt_title,filename);
        if (state == 1)
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);
        else if(!mediaPlayer.isPlaying())
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
        else
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);

        Intent actionTogglePlay = new Intent(CommandActions.PLAY);
        Intent actionClose = new Intent(CommandActions.CLOSE);
        PendingIntent togglePlay = PendingIntent.getService(this, 0, actionTogglePlay, FLAG_CANCEL_CURRENT);
        PendingIntent close = PendingIntent.getService(this, 0, actionClose, FLAG_CANCEL_CURRENT);

        mBuilder.setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_ALL);

        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, close);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(1,mBuilder.build());
    }

    private NotificationCompat.Builder createNotification() {
        String channelId = "channel";
        String channelName = "Channel Name";
        notifManager
                = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);
        return builder;
    }
}
