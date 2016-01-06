package com.viby.playit.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viby.playit.R;
import com.viby.playit.activities.MainActivity;
import com.viby.playit.adapters.RecyclerItemClickListener;
import com.viby.playit.adapters.RecyclerViewAdapter;
import com.viby.playit.models.Song;
import com.viby.playit.utils.Constants;
import com.viby.playit.utils.MusicUtil;

import java.util.ArrayList;


public class MusicListFragment extends Fragment {
    private Context mContext;
    private ArrayList<Song> playlist = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager mLayoutManager;

    private OnTrackSelected mCallback;

    private int oldPosition = -1;

    public interface OnTrackSelected {
        void startTrack(int id);
    }

    public MusicListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        try {
            mCallback = (OnTrackSelected) context;
        } catch (ClassCastException classException) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.Permissions.WRITE_EXTERNAL_STORAGE);

        } else {
            MusicUtil.createSongPlaylist(mContext, playlist);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.music_list_fragment, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.music_list_view);
        recyclerViewAdapter = new RecyclerViewAdapter(
                mContext,
                playlist
        );

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        nextSong(recyclerView.findViewHolderForAdapterPosition(position).getLayoutPosition());
                        mCallback.startTrack(position);
                    }
                })
        );

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerViewAdapter);
        
        //recyclerView.addItemDecoration(new DividerItemDecoration(getResources(), DividerItemDecoration.VERTICAL_LIST));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
    }

    public void nextSong(int position) {
        if (recyclerView != null && recyclerView.findViewHolderForAdapterPosition(position) != null) {
            if (oldPosition > -1 && recyclerView.findViewHolderForAdapterPosition(oldPosition) != null) {
                recyclerView.findViewHolderForAdapterPosition(oldPosition).itemView.setSelected(false);
            }

            recyclerView.findViewHolderForAdapterPosition(position).itemView.setSelected(true);
            recyclerViewAdapter.addSelected(position);
            oldPosition = position;
        } else
            recyclerViewAdapter.addSelected(position);
    }

    public void disableNextSong() {
        recyclerViewAdapter.clearSelected();
    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }
 }
