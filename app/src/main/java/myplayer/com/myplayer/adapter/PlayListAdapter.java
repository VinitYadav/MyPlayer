package myplayer.com.myplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import myplayer.com.myplayer.R;
import myplayer.com.myplayer.model.Audio;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private ArrayList<Audio> mList = new ArrayList<>();
    private Activity mActivity;

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
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewPlay;
        private TextView textViewName;
        private TextView textViewTitle;

        ViewHolder(View view) {
            super(view);
            imageViewPlay = view.findViewById(R.id.imageViewPlay);
            textViewName = view.findViewById(R.id.textViewName);
            textViewTitle = view.findViewById(R.id.textViewTitle);
        }
    }
}
