package com.viby.playit.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.viby.playit.R;
import com.viby.playit.fragments.MediaControllerFragment;
import com.viby.playit.fragments.MusicListFragment;
import com.viby.playit.models.Song;
import com.viby.playit.service.MediaPlayerService;
import com.viby.playit.service.MediaPlayerService.LocalBinder;
import com.viby.playit.service.IMediaPlayerStateListener;
import com.viby.playit.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MusicListFragment.OnTrackSelected, MediaControllerFragment.IMediaController {
    private MediaPlayerService mService;
    private boolean servicePlaying = false;
    private Intent service;
    private boolean mBound = false;
    private boolean mediaPlayerState = false;

    private List<IMediaPlayerStateListener> mListener = new ArrayList<>();
    private ArrayList<Song> playlist;
    private Handler seekHandler = new Handler();

    private MusicListFragment musicListFragment;
    private MediaControllerFragment mediaControllerFragment;
    private boolean mediaControllerSeekbarTouch = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = ((LocalBinder) service).getService();
            mBound = true;

            if (mService != null) {
                servicePlaying = mService.isPlaying();
                mediaPlayerState = servicePlaying;
                seekBarUpdate();

                if (mService.getSongs() == null)
                    mService.setSongs(playlist);

                if (mListener != null) {
                    onEvent(servicePlaying);

                    if (!playlist.isEmpty()) {
                        Long totalLength = playlist.get(mService.getCurrentSongId()).getLength();
                        updateTotalTimeTextView(totalLength);
                        setMaxProgressBar(playlist.get(mService.getCurrentSongId()).getLength().intValue());
                        setTitle(playlist.get(mService.getCurrentSongId()).getTitle());

                        if (servicePlaying)
                            musicListFragment.nextSong(mService.getCurrentSongId());
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };

    private BroadcastReceiver mMediaPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlayerState = intent.getExtras().getBoolean("is_playing");

            if (mediaPlayerState)
                musicListFragment.nextSong(mService.getCurrentSongId());

            if (mListener != null)
                onEvent(mediaPlayerState);
        }
    };

    private BroadcastReceiver mNextSongStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int nextSongPosition = intent.getExtras().getInt("next_song");
            Long length = playlist.get(nextSongPosition).getLength();

            setMaxProgressBar(length.intValue());
            updateTotalTimeTextView(length);
            mediaControllerFragment.setTitle(playlist.get(nextSongPosition).getTitle());

            if (mediaPlayerState) {
                musicListFragment.nextSong(nextSongPosition);
            }
        }
    };

    private BroadcastReceiver mEndStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean endState = intent.getExtras().getBoolean("end_state");

            if (endState) {
                Long totalLength = playlist.get(mService.getCurrentSongId()).getLength();
                mediaControllerFragment.setTitle(playlist.get(0).getTitle());
                mediaControllerFragment.setCurrentTime("00:00");
                mediaControllerFragment.setCurrentTimeProgressBar(0);
                updateTotalTimeTextView(totalLength);
                setMaxProgressBar(totalLength.intValue());
                musicListFragment.disableNextSong();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("state_start", "true");

        if (!mBound) {
            startForegroundService();
            registerReceiver(mMediaPlayerReceiver, new IntentFilter(Constants.Notify.MEDIAPLAYER_PLAY));
            registerReceiver(mNextSongStateReceiver, new IntentFilter(Constants.Notify.NEXT_SONG));
            registerReceiver(mEndStateReceiver, new IntentFilter(Constants.Notify.END_STATE));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitle("Play It");

        if (savedInstanceState == null) {
            musicListFragment = new MusicListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.fragment_list_view,
                            musicListFragment,
                            "list_fragment"
                    ).commit();

            mediaControllerFragment = new MediaControllerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.media_controller,
                            mediaControllerFragment,
                            "media_controller_fragment"
                    ).commit();

            setServicePlayingListener(new IMediaPlayerStateListener() {
                @Override
                public void onStateChange(boolean listener) {
                    if (listener)
                        seekBarUpdate();
                    else
                        seekHandler.removeCallbacks(run);
                }
            });
        } else {
            musicListFragment = (MusicListFragment) getSupportFragmentManager()
                    .findFragmentByTag("list_fragment");

            mediaControllerFragment = (MediaControllerFragment) getSupportFragmentManager()
                    .findFragmentByTag("media_controller_fragment");
        }

        playlist = musicListFragment.getPlaylist();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            if (!mService.isPlaying()) {
                mService.startAction(Constants.Action.STOPFOREGROUND_ACTION);
            }

            mBound = false;
        }

        unregisterReceiver(mMediaPlayerReceiver);
        unregisterReceiver(mNextSongStateReceiver);
        unregisterReceiver(mEndStateReceiver);
        unbindService(mConnection);
        seekHandler.removeCallbacks(run);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound) {
            if (!mService.isPlaying()) {
                mService.startAction(Constants.Action.STOPFOREGROUND_ACTION);
            }
        }
    }

    public void setServicePlayingListener(IMediaPlayerStateListener listener) {
        mListener.add(listener);
    }

    public void onEvent(boolean b) {
        for (IMediaPlayerStateListener listener : mListener) {
            listener.onStateChange(b);
        }
    }

    @Override
    public void startTrack(int id) {
        if (mBound) {
            if (mService.isPlaying() && mService.getCurrentSongId() == id)
                mService.pauseSong();
            else
                mService.playSong(id, true);

            mediaControllerFragment.setTitle(playlist.get(id).getTitle());
            mediaControllerFragment.setMaxProgressBar(playlist.get(id).getLength().intValue());
            updateTotalTimeTextView(playlist.get(id).getLength());
        }
    }

    @Override
    public void nextTrack() {
        if (mBound) {
            mService.nextSong();
        }
    }

    @Override
    public void play() {
        if (mBound) {
            mService.playSong();
        }
    }

    @Override
    public void pause() {
        if (mBound) {
            mService.pauseSong();
        }
    }

    @Override
    public void previousTrack() {
        if (mBound) {
            mService.prevSong();
        }
    }

    @Override
    public void setRepeatState(int repeatState) {
        if (mBound) {
            mService.setRepeatState(repeatState);
        }
    }

    @Override
    public void activateShuffle() {
        if (mBound) {
            mService.activateShuffle();
        }
    }

    @Override
    public void desactivateShuffle() {
        if (mBound) {
            mService.desactivateShuffle();
        }
    }

    @Override
    public void seekTo(int seek) {
        if (mBound)
            mService.seekTo(seek);
    }

    public void startForegroundService() {
        service = new Intent(this, MediaPlayerService.class);
        service.setAction(Constants.Action.STARTFOREGROUND_ACTION);

        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        startService(service);
        MediaPlayerService.SERVICE_RUNNING = true;
    }

    public void setTitle(String title) {
        mediaControllerFragment.setTitle(title);
    }

    public void setMediaControllerSeekbarTouch(boolean seekbarTouch) {
        mediaControllerSeekbarTouch = seekbarTouch;
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekBarUpdate();
        }
    };

    public void seekBarUpdate() {
        int currentTime = (int) Math.floor(mService.getCurrentTime() / 1000);
        
        mediaControllerFragment.setCurrentTime(
                String.format("%02d", (int) Math.floor(currentTime / 60))
                        + ":"
                        + String.format("%02d", (int) (mService.getCurrentTime() / 1000) % 60)
        );

        if (!mediaControllerSeekbarTouch)
            mediaControllerFragment.setCurrentTimeProgressBar(currentTime);

        seekHandler.postDelayed(run, 1100 - (mService.getCurrentTime() % 1000));
    }

    public void setMaxProgressBar(int length) {
        mediaControllerFragment.setMaxProgressBar(length);
    }

    public void updateTotalTimeTextView(final long length) {
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaControllerFragment.setTotalTime(
                                String.format("%02d", (int) Math.floor(length / 60))
                                + ":"
                                + String.format("%02d", length % 60)
                        );
                    }
                });
            }
        }.start();
    }
}
