package org.androidtown.mobile_term;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class RecordService extends Service {
    //MediaRecorder mediaRecorder;
    MediaRecorder recorder;
    String recordpath = ""; //임시저장
    String path = ""; //최종본 저장
    String nowFile = "";
    NotificationManager notifManager;
    NotificationCompat.Builder mBuilder;
    int State = 0;
    int Pause_state = 0;
    /*날짜 셋팅*/
    long mNow;
    Date mDate;
    RemoteViews remoteViews;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyMMdd-HH:mm");

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(CommandActions.RECORD)) {
            record();
            State = 0;
            showCustomNotification();
        } else if (intent.getAction().equals(CommandActions.PAUSE)) {
            pause();
            State = 1;
            showCustomNotification();
        } else if (intent.getAction().equals(CommandActions.CLOSERECORD)) {
            remoteViews.setBoolean(R.id.btn_record, "setEnabled", false);
            remoteViews.setBoolean(R.id.btn_pause, "setEnabled", false);
            remoteViews.setBoolean(R.id.btn_close, "setEnabled", false);
            stopSelf();
            notifManager.cancelAll();
        }
        return START_STICKY;
    }

    public void record() {
        recordpath = FileList.recordpath;
        String number = Integer.toString(FileList.filecounter);
        recordpath = recordpath + number + ".mp4";
        FileList.outputFileList.add(recordpath);
        FileList.filecounter++;

        if (recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//마이크 사용
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//파일 확장자 설정
            recorder.setOutputFile(recordpath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 인코더 설정

            try {
                recorder.prepare();
            } catch (IOException e) {
            } catch (IllegalStateException il) {

            }
            //Toast.makeText(this, "Record Service가 시작되었습니다.", Toast.LENGTH_LONG).show();
            recorder.start();//녹음 시작
        }

        Pause_state = 0;
    }

    public void pause() {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        Pause_state = 1;
    }

    public void stop() { //여기에 합치는거 들어가야함
        Toast.makeText(this, "Record Service가 중지되었습니다.", Toast.LENGTH_LONG).show();
        FileList.filecounter = 0;
        if (Pause_state == 1) {
            ;
        } else {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        try {
            append(FileList.outputFileList);
        } catch (IOException e) {
            ;
        }
        Intent myIntent = new Intent(this, FolderPicker.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        startActivity(myIntent);
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent agr0) {
        return null;
    }

    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    private void showCustomNotification() {
        mBuilder = createNotification();

        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_record);
        remoteViews.setTextViewText(R.id.txt_title, "녹음중");
        if (State == 0) {//record 버튼 안눌리게
            remoteViews.setImageViewResource(R.id.btn_record, R.drawable.stoprecord);
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.pause);
            remoteViews.setBoolean(R.id.btn_record, "setEnabled", false);
            remoteViews.setBoolean(R.id.btn_pause, "setEnabled", true);
        } else if (State == 1) { //일시정지 버튼 안눌리게
            remoteViews.setImageViewResource(R.id.btn_pause, R.drawable.stoppause);
            remoteViews.setImageViewResource(R.id.btn_record, R.drawable.record);
            remoteViews.setBoolean(R.id.btn_pause, "setEnabled", false);
            remoteViews.setBoolean(R.id.btn_record, "setEnabled", true);
        }

        Intent actionTogglePlay = new Intent(CommandActions.RECORD);
        Intent actionPause = new Intent(CommandActions.PAUSE);
        Intent actionClose = new Intent(CommandActions.CLOSERECORD);

        PendingIntent togglePlay;
        PendingIntent Pause;

        if (Build.VERSION.SDK_INT >= 26) {
            togglePlay = PendingIntent.getForegroundService(this, 0, actionTogglePlay, FLAG_CANCEL_CURRENT);
            Pause = PendingIntent.getForegroundService(this, 0, actionPause, FLAG_CANCEL_CURRENT);
        } else {
            togglePlay = PendingIntent.getService(this, 0, actionTogglePlay, FLAG_CANCEL_CURRENT);
            Pause = PendingIntent.getService(this, 0, actionPause, FLAG_CANCEL_CURRENT);
        }


        PendingIntent close = PendingIntent.getService(this, 0, actionClose, FLAG_CANCEL_CURRENT);

        mBuilder.setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_ALL);

        remoteViews.setOnClickPendingIntent(R.id.btn_record, togglePlay);
        remoteViews.setOnClickPendingIntent(R.id.btn_pause, Pause);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, close);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(1, mBuilder.build());
        startForeground(1, mBuilder.build());
    }

    private NotificationCompat.Builder createNotification() {
        String channelId = "channel";
        String channelName = "Channel Name";
        notifManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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

    public void append(List<String> list) throws IOException {
        Movie[] inMovies;
        inMovies = new Movie[list.size()];
        try {
            for (int i = 0; i < list.size(); i++) {
                inMovies[i] = MovieCreator.build(list.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);
        FileChannel fc = null;

        try {
            fc = new FileOutputStream(new File(FileList.recordpath
                    + "[" + getTime() + "] " + FileList.FolderName + ".mp4")).getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.writeContainer(fc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            File fl = new File(list.get(i));
            fl.delete();
        }
        FileList.outputFileList.clear();
    }
}
