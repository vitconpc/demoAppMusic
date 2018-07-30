package vn.com.example.demoappmusicservice.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import vn.com.example.demoappmusicservice.R;
import vn.com.example.demoappmusicservice.adapter.SongAdapter;
import vn.com.example.demoappmusicservice.model.Song;
import vn.com.example.demoappmusicservice.service.AdapterListener;
import vn.com.example.demoappmusicservice.service.MusicService;
import vn.com.example.demoappmusicservice.service.TaskListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        TaskListener, AdapterListener {

    private static final int REQUEST_CODE = 100;
    private static final String ACTION_BIND_SERVICE = "ACTION_BIND_SERVICE";
    private RecyclerView mRecyclerMusic;
    private SeekBar mSbMusic;
    private ImageButton mButtonPrevious;
    private ImageButton mButtonPause;
    private ImageButton mButtonNext;
    private TextView mTextSongName;
    private TextView mTextTotalTime;
    private TextView mTextCurrentTime;
    private List<Song> mSongs;
    private SongAdapter mSongAdapter;
    private boolean mIsBound;
    private MusicService mMusicService;
    private ServiceConnection mServiceConnection;
    private SimpleDateFormat mDateFormat;
    private boolean mTrackingSeekBar = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        checkpermission();
        initView();
        turnOnService();
        setEvent();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkpermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            turnOnService();
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    private void setEvent() {
        mButtonPrevious.setOnClickListener(this);
        mButtonPause.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mSbMusic.setOnSeekBarChangeListener(this);
    }


    private void initView() {
        mDateFormat = new SimpleDateFormat(getString(R.string.date_time));
        mIsBound = false;
        mRecyclerMusic = findViewById(R.id.recycler_music);
        mTextSongName = findViewById(R.id.text_song_name);
        mTextTotalTime = findViewById(R.id.text_total_time);
        mTextCurrentTime = findViewById(R.id.text_curent_time);
        mTextSongName.setVisibility(View.INVISIBLE);
        mSbMusic = findViewById(R.id.seekbar_music);
        mButtonNext = findViewById(R.id.button_next);
        mButtonPause = findViewById(R.id.button_pause);
        mButtonPrevious = findViewById(R.id.button_privious);
        mSongs = new ArrayList<>();
        mSongAdapter = new SongAdapter(MainActivity.this, mSongs);
        mSongAdapter.initListener(MainActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerMusic.setLayoutManager(linearLayoutManager);
        mRecyclerMusic.setHasFixedSize(true);
        mRecyclerMusic.setAdapter(mSongAdapter);

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                mMusicService = ((MusicService.MusicIBinder) binder).getService();
                mMusicService.initService(MainActivity.this);
                mMusicService.setData();
                mIsBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    @Override
    public void onClick(View v) {
        if (mIsBound) {
            switch (v.getId()) {
                case R.id.button_pause:
                    if (mMusicService.isAudioPlaying()) {
                        mMusicService.pauseMusic();
                    } else {
                        mMusicService.continuesMusic();
                    }
                    break;
                case R.id.button_next:
                    mMusicService.nextMusic();
                    break;
                case R.id.button_privious:
                    mMusicService.previousMusic();
                    break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingSeekBar = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingSeekBar = false;
        mMusicService.seekToProgess(seekBar.getProgress());
        if (!mMusicService.isAudioPlaying()) {
            mMusicService.continuesMusic();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            turnOnService();
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    //connected service
    private void turnOnService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(ACTION_BIND_SERVICE);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //load data
    @Override
    public void onCommitLoad(List<Song> songs) {
        mSongs.addAll(songs);
        mSongAdapter.notifyDataSetChanged();
    }

    //set song name
    @Override
    public void postName(String name) {
        mTextSongName.setVisibility(View.VISIBLE);
        mTextSongName.setText(name);
    }

    //    set total time
    @Override
    public void postTime(int total) {
        mSbMusic.setMax(total);
        mTextTotalTime.setText(mDateFormat.format(total));
    }

    //set current time
    @Override
    public void onCurrentTime(int currentPosition) {
        mTextCurrentTime.setText(mDateFormat.format(currentPosition));
        if (!mTrackingSeekBar) {
            mSbMusic.setProgress(currentPosition);
        }
    }

    @Override
    public void pauseMusicButton() {
        mButtonPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);
    }

    @Override
    public void startMusicButton() {
        mButtonPause.setImageResource(R.drawable.ic_pause_black_48dp);
    }

    @Override
    public void clickItem(int position) {
        mMusicService.playMusicPosition(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mMusicService.isAudioPlaying()) {
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(ACTION_BIND_SERVICE);
            stopService(intent);
        }
    }
}
