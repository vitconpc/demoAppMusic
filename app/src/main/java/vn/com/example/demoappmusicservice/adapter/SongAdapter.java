package vn.com.example.demoappmusicservice.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import vn.com.example.demoappmusicservice.R;
import vn.com.example.demoappmusicservice.model.Song;
import vn.com.example.demoappmusicservice.service.AdapterListener;
import vn.com.example.demoappmusicservice.view.MainActivity;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MusicViewHolder> {
    private Context mContext;
    private List<Song> mSongs;
    private AdapterListener mAdapterListener;

    public SongAdapter(Context context, List<Song> songs) {
        this.mContext = context;
        this.mSongs = songs;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Song currentSong = mSongs.get(position);
        if (!currentSong.getName().equals("")) {
            holder.mTextSongName.setText(currentSong.getName());
        }
        if (!currentSong.getmAuthor().equals("")) {
            holder.mTextAuthorName.setText(currentSong.getmAuthor());
        }
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void initListener(MainActivity activity) {
        mAdapterListener = activity;
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextSongName;
        private TextView mTextAuthorName;

        public MusicViewHolder(View itemView) {
            super(itemView);
            mTextSongName = itemView.findViewById(R.id.text_song_name);
            mTextAuthorName = itemView.findViewById(R.id.text_author_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapterListener.clickItem(getAdapterPosition());
                }
            });
        }
    }
}
