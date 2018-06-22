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
    private final int PLAY = 2;
    private final int PAUSE = 2;
    public int currentSong = 0;

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
        playAudio(audioList.get(currentSong).getData());
    }

    /**
     * Click on previous or next button
     */
    public void onClickPreviousNext(int which) {
        switch (which) {
            case 1://previous
                if (currentSong > 0) {
                    currentSong--;
                    playAudio(audioList.get(currentSong).getData());
                }
                break;
            case 2://next
                int size = audioList.size();
                if (currentSong < size) {
                    currentSong++;
                    playAudio(audioList.get(currentSong).getData());
                }
                break;
        }
    }

    /**
     * Play audio
     */
    private void playAudio(String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            changePlayPauseIcon(PAUSE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            try {
                player.mediaPlayer.setDataSource(media);
                player.playMedia();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        mBinding.textViewName.setText(audioList.get(0).getTitle());
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

    @Override
    public void doSomething() {
        serviceBound = false;
    }


}
