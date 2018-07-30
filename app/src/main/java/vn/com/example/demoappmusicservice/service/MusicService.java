package vn.com.example.demoappmusicservice.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.com.example.demoappmusicservice.R;
import vn.com.example.demoappmusicservice.model.Song;
import vn.com.example.demoappmusicservice.view.MainActivity;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int ID_NOTIFICATION = 1;
    private List<Song> mSongs;
    private TaskListener mTaskListener;

    private MusicIBinder iBinder = new MusicIBinder();
    private int mCurrentPosition = 0;
    private MediaPlayer mMediaPlayer;
    private boolean mIsLoaded = false;
    private boolean mIsClientPlay = false;
    private Handler mHandler = new Handler();
    private Runnable mRunnableCurrentTime = new Runnable() {
        @Override
        public void run() {
            mTaskListener.onCurrentTime(mMediaPlayer.getCurrentPosition());
            if (isAudioPlaying()) {
                mHandler.postDelayed(this, 50);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mSongs = new ArrayList<>();
        new MusicAsynctask().execute(Environment.getExternalStorageDirectory());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handlerButtonNotification(intent);
        return START_STICKY;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isAudioPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
    }

    public void playMusicPosition(int position) {
        mIsClientPlay = true;
        mCurrentPosition = position;
        initMusic(mSongs.get(position));
    }

    private void initMusic(Song song) {
        initMediaPlayer();
        Uri uri = Uri.parse(song.getmFilePath().toString());
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlerButtonNotification(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case ACTION_PLAY:
                pauseMusic();
                break;
            case ACTION_PAUSE:
                continuesMusic();
                break;
            case ACTION_NEXT:
                nextMusic();
                break;
            case ACTION_PREVIOUS:
                previousMusic();
                break;
        }
    }

    //create and handler event notification
    private void updateNotification(String action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //main intent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseStartIntent = new Intent(this, MusicService.class);
        Intent nextIntent = new Intent(this, MusicService.class);
        Intent previousIntent = new Intent(this, MusicService.class);

        pauseStartIntent.setAction(action);
        nextIntent.setAction(ACTION_NEXT);
        previousIntent.setAction(ACTION_PREVIOUS);

//        pendingIntent handler button
        PendingIntent pauseStartPendingIntent = PendingIntent.getService(this, 0,
                pauseStartIntent, 0);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_start);
        remoteViews.setOnClickPendingIntent(R.id.button_start, pauseStartPendingIntent);
        if (action.equals(ACTION_PAUSE)) {
            remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_pause);
            remoteViews.setOnClickPendingIntent(R.id.button_pause, pauseStartPendingIntent);
        }
        //set data, event
        remoteViews.setImageViewResource(R.id.image_avatar, R.drawable.ic_launcher_background);
        remoteViews.setTextViewText(R.id.text_song_name, mSongs.get(mCurrentPosition).getName());
        remoteViews.setOnClickPendingIntent(R.id.button_previous, previousPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.button_next, nextPendingIntent);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(1)
                .build();

        startForeground(ID_NOTIFICATION, notification);
    }

    private void initMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        postName();
        postTime();
        if (mIsClientPlay) {
            mMediaPlayer.start();
            postStartMusic();
            updateNotification(ACTION_PLAY);
        }
    }

    private void postStartMusic() {
        mTaskListener.startMusicButton();
    }

    private void postTime() {
        mTaskListener.postTime(mMediaPlayer.getDuration());
        mHandler.postDelayed(mRunnableCurrentTime, 50);
    }

    private void postName() {
        mTaskListener.postName(mSongs.get(mCurrentPosition).getName());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCurrentPosition < mSongs.size() - 1) {
            mCurrentPosition++;
        } else {
            mCurrentPosition = 0;
        }
        initMusic(mSongs.get(mCurrentPosition));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mMediaPlayer.reset();
        return false;
    }

    public boolean isAudioPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying()) ? true : false;
    }

    //pause music
    public void pauseMusic() {
        if (isAudioPlaying()) {
            mMediaPlayer.pause();
        }
        mIsClientPlay = true;
        updateNotification(ACTION_PAUSE);
        postPauseMusic();
    }

    private void postPauseMusic() {
        mTaskListener.pauseMusicButton();
    }

    //continues music
    public void continuesMusic() {
        if (!isAudioPlaying()) {
            mMediaPlayer.start();
            postTime();
            postStartMusic();
        }
        mIsClientPlay = true;
        updateNotification(ACTION_PLAY);
    }

    //next Song
    public void nextMusic() {
        mIsClientPlay = true;
        if (mCurrentPosition < mSongs.size() - 1) {
            mCurrentPosition++;
        } else {
            mCurrentPosition = 0;
        }
        initMusic(mSongs.get(mCurrentPosition));
    }

    // previousMusic
    public void previousMusic() {
        mIsClientPlay = true;
        if (mMediaPlayer.getCurrentPosition() > 5000) {
            initMusic(mSongs.get(mCurrentPosition));
            return;
        }
        if (mCurrentPosition > 0) {
            mCurrentPosition--;
        } else {
            mCurrentPosition = mSongs.size() - 1;
        }
        initMusic(mSongs.get(mCurrentPosition));
    }

    //seekto progess
    public void seekToProgess(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    //next turn on service
    public void setData() {
        if (mIsLoaded) {
            mTaskListener.onCommitLoad(mSongs);
            postTime();
            postName();
            if (isAudioPlaying()) {
                postStartMusic();
                return;
            }
            postPauseMusic();
        }
    }

    public class MusicIBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public class MusicAsynctask extends AsyncTask<File, File, Void> {

        private static final String DOTMP3 = ".mp3";

        @Override
        protected Void doInBackground(File... files) {
            getFile(files[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(File... values) {
            super.onProgressUpdate(values);
            Song song = new Song();
            song.setName(values[0].getName());
            song.setmFilePath(values[0]);
            song.setmAuthor(values[0].getParent());
            mSongs.add(song);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mIsLoaded = true;
            mTaskListener.onCommitLoad(mSongs);
            initMusic(mSongs.get(mCurrentPosition));
        }

        private void getFile(File dir) {
            File listFile[] = dir.listFiles();
            if (listFile == null || listFile.length <= 0) {
                return;
            }
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    getFile(listFile[i]);
                } else if (listFile[i].getName().endsWith(DOTMP3)) {
                    publishProgress(listFile[i]);
                }
            }
        }
    }

    //khởi tạo service interface trong MainActivity
    public void initService(MainActivity activity) {
        mTaskListener = activity;
    }
}
