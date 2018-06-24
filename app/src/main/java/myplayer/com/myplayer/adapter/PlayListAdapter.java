package myplayer.com.myplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import myplayer.com.myplayer.R;
import myplayer.com.myplayer.activity.MainActivity;
import myplayer.com.myplayer.model.Audio;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private ArrayList<Audio> mList;
    private Activity mActivity;
    private int selected = 0;

    public PlayListAdapter(Activity activity, ArrayList<Audio> list) {
        this.mList = list;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.play_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewName.setText(mList.get(position).getTitle());
        holder.textViewTitle.setText(mList.get(position).getArtist());
        long duration = Long.parseLong(mList.get(position).getDuration());
        holder.textViewDuration.
                setText(getDuration(mList.get(position).getDuration()));
        if (mList.get(position).isSelect()) {
            holder.imageViewPlay.setImageResource(R.drawable.pause_icon);
        } else {
            holder.imageViewPlay.setImageResource(R.drawable.play_icon);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * Set play pause icon on current playing song
     */
    public void setPlayPause(boolean flag) {
        if (flag) {//pause
            mList.get(selected).setSelect(false);
        } else {//resume
            mList.get(selected).setSelect(true);
        }
        notifyItemChanged(selected);
    }

    /**
     * Get duration
     */
    private String getDuration(String duration) {
        int temp = Integer.parseInt(duration);
        String time = String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(temp),
                TimeUnit.MILLISECONDS.toSeconds(temp) -
                        TimeUnit.MINUTES.toSeconds
                                (TimeUnit.MILLISECONDS.toMinutes(temp))
        );
        return time;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewPlay;
        private TextView textViewName;
        private TextView textViewTitle;
        private TextView textViewDuration;

        ViewHolder(View view) {
            super(view);
            imageViewPlay = view.findViewById(R.id.imageViewPlay);
            textViewName = view.findViewById(R.id.textViewName);
            textViewTitle = view.findViewById(R.id.textViewTitle);
            textViewDuration = view.findViewById(R.id.textViewDuration);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selected == getAdapterPosition()) {
                        ((MainActivity) mActivity).onClickPlay();
                    } else {
                        if (mList.get(getAdapterPosition()).isSelect()) {
                            mList.get(getAdapterPosition()).setSelect(false);
                        } else {
                            mList.get(getAdapterPosition()).setSelect(true);
                        }
                        if (mList.get(selected).isSelect()) {
                            mList.get(selected).setSelect(false);
                        } else {
                            mList.get(selected).setSelect(true);
                        }
                        notifyItemChanged(selected);
                        notifyItemChanged(getAdapterPosition());
                        selected = getAdapterPosition();
                        ((MainActivity) mActivity).setCurrentSong(getAdapterPosition());
                        ((MainActivity) mActivity).playSelectSong();
                    }
                }
            });
        }
    }
}
