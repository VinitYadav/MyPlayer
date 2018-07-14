package myplayer.com.myplayer.listener;

import android.media.MediaPlayer;

public interface ServiceCallbacks {
    void onComplete();
    void onDuration(int duration);
}
