package myplayer.com.myplayer.activity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import myplayer.com.myplayer.R;
import myplayer.com.myplayer.adapter.PlayListAdapter;
import myplayer.com.myplayer.databinding.ActivityMainBinding;
import myplayer.com.myplayer.listener.ServiceCallbacks;
import myplayer.com.myplayer.model.Audio;
import myplayer.com.myplayer.service.MediaPlayerService;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {

    private MediaPlayerService player;
    boolean serviceBound = false;
    private ArrayList<Audio> audioList;
    private PlayListAdapter playListAdapter;
    private ActivityMainBinding mBinding;
    private final int PLAY = 1;
    private final int PAUSE = 2;
    private int currentSong = 0;
    private final int DELAY_TIME = 500;
    private boolean isPauseResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setActivity(this);
        loadAudio();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @Override
    public void onComplete() {
        serviceBound = false;
        changePlayPauseIcon(PLAY);
    }

    /**
     * Binding to the AudioPlayer Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            player.setCallbacks(MainActivity.this);

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    /**
     * Click on play & pause button
     */
    public void onClickPlay() {
        if (player != null) {
            if (player.mediaPlayer.isPlaying()) {
                player.pauseMedia();
                changePlayPauseIcon(PLAY);
                isPauseResume = !isPauseResume;
            } else {
                if (isPauseResume) {
                    player.resumeMedia();
                    changePlayPauseIcon(PAUSE);
                    isPauseResume = !isPauseResume;
                } else {
                    playSelectSong();
                }
            }
        }

    }

    /**
     * Click on previous or next button
     */
    public void onClickPreviousNext(int which) {
        switch (which) {
            case 1://previous
                final int previous = previousSong();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDestroyed()) {
                            playAudio(audioList.get(previous).getData());
                        }
                    }
                }, DELAY_TIME);
                break;
            case 2://next
                final int next = nextSong();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDestroyed()) {
                            playAudio(audioList.get(next).getData());
                        }
                    }
                }, DELAY_TIME);
                break;
        }
    }

    /**
     * Play audio
     */
    private void playAudio(final String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            changePlayPauseIcon(PAUSE);
            setSongTitle();
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }

    /**
     * Load audio
     */
    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist, duration));
            }
            cursor.close();
        }
        setAdapter();
    }

    /**
     * Set adapter
     */
    private void setAdapter() {
        if (audioList != null && audioList.size() > 0) {
            mBinding.linearLayoutBottom.setVisibility(View.VISIBLE);
            mBinding.textViewNoSong.setVisibility(View.GONE);

            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            playListAdapter = new PlayListAdapter(MainActivity.this, audioList);
            mBinding.recyclerView.setAdapter(playListAdapter);

            setBottomUI();
        } else {
            mBinding.linearLayoutBottom.setVisibility(View.GONE);
            mBinding.textViewNoSong.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set bottom UI
     */
    private void setBottomUI() {
        if (audioList == null || audioList.size() == 0) {
            return;
        }
        setSongTitle();
    }

    /**
     * Set song title
     */
    private void setSongTitle() {
        if (audioList != null && audioList.size() > 0) {
            mBinding.textViewName.setText(audioList
                    .get(getCurrentSong()).getTitle());
        }
    }

    /**
     * Change play pause icon
     */
    private void changePlayPauseIcon(int which) {
        switch (which) {
            case 1://play
                mBinding.imageViewPlay.setImageResource(R.drawable.play_circle_icon);
                break;
            case 2://pause
                mBinding.imageViewPlay.setImageResource(R.drawable.pause_circle_icon);
                break;
        }
    }

    /**
     * Get current song position
     */
    private int getCurrentSong() {
        return currentSong;
    }

    /**
     * Get current song position
     */
    public void setCurrentSong(int position) {
        changePlayPauseIcon(PLAY);
        serviceBound = false;
        currentSong = position;
    }

    /**
     * Get previous song position
     */
    private int previousSong() {
        changePlayPauseIcon(PLAY);
        serviceBound = false;
        player.stopMedia();
        if (currentSong > 0) {
            currentSong--;
        }
        return currentSong;
    }

    /**
     * Get next song position
     */
    private int nextSong() {
        changePlayPauseIcon(PLAY);
        serviceBound = false;
        player.stopMedia();
        int size = audioList.size();
        if (currentSong < size) {
            currentSong++;
        } else {
            currentSong = 0;
        }
        return currentSong;
    }

    /**
     * Play select song from list
     */
    public void playSelectSong() {
        isPauseResume = false;
        if (player != null) {
            player.stopMedia();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    playAudio(audioList.get(getCurrentSong()).getData());
                }
            }
        }, DELAY_TIME);
    }

}
