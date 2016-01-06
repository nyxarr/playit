package com.viby.playit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viby.playit.R;
import com.viby.playit.models.Song;
import com.viby.playit.widget.TypeFacedWidget;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Song> musicList;
    private LayoutInflater inflater;
    private ArrayList<Integer> selected = new ArrayList<>();

    public RecyclerViewAdapter(Context context, List<Song> list) {
        inflater = LayoutInflater.from(context);
        musicList = list;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_music_list, parent, false);
        ViewHolder vh = new ViewHolder(itemView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = musicList.get(position).getTitle();
        long length = musicList.get(position).getLength();
        String album = musicList.get(position).getAlbum();
        String artist = musicList.get(position).getArtist();

        String trackLength = String.format("%02d", (int) Math.floor(length / 60))
                + ":"
                + String.format("%02d", length % 60);

        holder.title.setText(title);
        holder.length.setText(trackLength);
        holder.albumArtist.setText(album + " - " + artist);

        if (!selected.contains(position)) {
            holder.itemView.setSelected(false);
        } else {
            holder.itemView.setSelected(true);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void addSelected(int position) {
        if (!selected.isEmpty()) {
            notifyItemChanged(selected.get(0));
            selected.clear();
        }

        selected.add(position);
    }

    public void clearSelected() {
        if (!selected.isEmpty()) {
            notifyItemChanged(selected.get(0));
            selected.clear();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout layout;
        public TextView title;
        public TextView length;
        public TextView album;
        public TextView albumArtist;
        public ImageView iconPlayPause;

        public ViewHolder(View itemView) {
            super(itemView);

            layout = (RelativeLayout) itemView.findViewById(R.id.layout_item_list);
            iconPlayPause = (ImageView) itemView.findViewById(R.id.icon_list);
            title = (TextView) itemView.findViewById(R.id.music_title);
            length = (TextView) itemView.findViewById(R.id.music_length);
            albumArtist = (TextView) itemView.findViewById(R.id.music_album_artist);
        }
    }
}
