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
        holder.textViewDuration.setText(getDuration(mList.get(position).getDuration()));
        if (mList.get(position).isSelect()) {
            holder.imageViewPlay.setImageResource(R.drawable.pause_icon);
        } else {
            holder.imageViewPlay.setImageResource(R.drawable.play_icon);
        }
    }

    /**
     * Get duration
     */
    private String getDuration(String duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm", Locale.US);
        try {
            Date dt = sdf.parse(duration);
            sdf = new SimpleDateFormat("HH:mm", Locale.US);
            //System.out.println(sdf.format(dt));
            return sdf.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mList.size();
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
            });
        }
    }
}
