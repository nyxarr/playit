package com.viby.playit.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.viby.playit.R;
import com.viby.playit.activities.MainActivity;
import com.viby.playit.models.Song;
import com.viby.playit.utils.BitmapUtil;
import com.viby.playit.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MediaPlayerService extends Service {

    public static boolean SERVICE_RUNNING = false;

    private static MediaPlayerService sInstance;
    private static ExoPlayer player;
    private static Boolean shuffleActivated = false;
    private NotificationCompat.Builder notification;
    private NotificationManager notificationManager;

    private final IBinder mBinder = new LocalBinder();

    private int currentSongId = 0;
    private int currentTime = 0;
    private ArrayList<Song> mSongs;
    private ArrayList<Integer> shuffleList;

    private int repeatState = 0;

    private ExoPlayer.Listener listener = new ExoPlayer.Listener() {
        private int playWhenReadyStateOnce = 0;

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playWhenReadyStateOnce == 0 && playWhenReady) {
                playWhenReadyStateOnce = 1;
                sendMediaPlayerState(playWhenReady);
            } else if (playWhenReadyStateOnce == 1 && !playWhenReady) {
                playWhenReadyStateOnce = 0;
                sendMediaPlayerState(playWhenReady);
            } else if (playbackState == ExoPlayer.STATE_ENDED) {
                switch (repeatState) {
                    case Constants.Repeat.REPEAT_DISABLE:
                        if (currentSongId == mSongs.size() - 1) {
                            currentSongId = 0;
                            playWhenReadyStateOnce = 0;
                            sendEndPlaylistState(true);
                            sendMediaPlayerState(false);
                        } else {
                            nextSong(currentSongId + 1);
                        }
                        break;

                    case Constants.Repeat.REPEAT_ALL:
                        if (currentSongId == mSongs.size() - 1 && shuffleActivated)
                            shuffle();

                        nextSong(currentSongId + 1);
                        break;

                    case Constants.Repeat.REPEAT_SAME:
                        player.seekTo(0);
                        break;
                }
            }
        }

        @Override
        public void onPlayWhenReadyCommitted() {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        player = ExoPlayer.Factory.newInstance(1, 1000, 2500);
        player.addListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            startAction(action);
        }

        return START_STICKY;
    }

    protected void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.Action.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MediaPlayerService.class);
        previousIntent.setAction(Constants.Action.PREV_ACTION);
        PendingIntent pPreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(Constants.Action.PLAY_ACTION);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction(Constants.Action.PAUSE_ACTION);
        PendingIntent pPauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        Intent nextIntent = new Intent(this, MediaPlayerService.class);
        nextIntent.setAction(Constants.Action.NEXT_ACTION);
        PendingIntent pNextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap icon = BitmapUtil.decodeSampledBitmapFromResource(
                getResources(),
                R.mipmap.ic_launcher,
                128, 128
        );

        NotificationCompat.Action playAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_play,
                "Play",
                pPlayIntent
        ).build();

        NotificationCompat.Action pauseAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_pause,
                "Pause",
                pPauseIntent
        ).build();

        NotificationCompat.Action nextAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_next,
                "Next",
                pPauseIntent
        ).build();

        NotificationCompat.Action previousAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_previous,
                "Previous",
                pPauseIntent
        ).build();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification = ((NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(previousAction)
                .addAction(pauseAction)
                .addAction(nextAction)
        );

        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification.build());

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification.build());
    }

    protected void updateNotification(int id) {
        notification.setContentTitle(mSongs.get(id).getTitle()).build();
        notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification.build());
    }

    public void startAction(String action) {
        if (action != null) {
            if (action.equals(Constants.Action.STARTFOREGROUND_ACTION)) {
                showNotification();
            } else if (action.equals(Constants.Action.PREV_ACTION)) {
                prevSong();
            } else if (action.equals(Constants.Action.NEXT_ACTION)) {
                nextSong();
            } else if (action.equals(Constants.Action.PLAY_ACTION)) {
                playSong(currentSongId, false);
            } else if (action.equals(Constants.Action.PAUSE_ACTION)) {
                pauseSong();
            } else if (action.equals(Constants.Action.STOPFOREGROUND_ACTION)) {
                player.release();
                player = null;
                stopForeground(true);
                stopSelf();
            }
        }
    }

    public void playSong(int id, boolean fromFragmentList) {
        if (currentTime > 0 && currentSongId == id) {
            player.setPlayWhenReady(true);
            return;
        }

        currentSongId = id;
        String path = "";

        if (!shuffleActivated || fromFragmentList) {
            updateNotification(id);
            path = mSongs.get(id).getPath();
        } else {
            updateNotification(shuffleList.get(id));
            path = mSongs.get(shuffleList.get(id)).getPath();
        }

        File musicFile = new File(path);

        if (musicFile.exists()) {
            Uri trackUri = Uri.fromFile(musicFile);

            DefaultUriDataSource uriDataSource = new DefaultUriDataSource(this, "playit/ExoPlayer");

            ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                    trackUri,
                    uriDataSource,
                    new DefaultAllocator(64 * 1024),
                    64 * 1024,
                    new Mp3Extractor()
            );

            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
            player.prepare(audioRenderer);
            player.seekTo(0);
            player.setPlayWhenReady(true);
        } else {
            mSongs.remove(id);
        }
    }

    public void playSong() {
        if (!mSongs.isEmpty())
            playSong(currentSongId, false);
    }

    public void pauseSong() {
        player.setPlayWhenReady(false);
        currentTime = (int) player.getCurrentPosition();
    }

    public boolean isPlaying() {
        if (player != null) {
            try {
                return player.getPlayWhenReady();
            } catch (IllegalStateException e) {
                return false;
            }
        }

        return false;
    }

    public void prevSong() {
        if (player != null) {
            if (player.getCurrentPosition() > 5000) {
                player.seekTo(0);
                player.setPlayWhenReady(true);
            } else {
                int position = currentSongId - 1;
                if (position < 0) position += mSongs.size();
                nextSong(position);
            }
        }
    }

    public void nextSong(int songID) {
        playSong(songID % mSongs.size(), false);

        if (!shuffleActivated) {
            sendNextSongState(songID % mSongs.size());
        } else {
            sendNextSongState(shuffleList.get(songID % shuffleList.size()));
        }
    }

    public void nextSong() {
        nextSong(currentSongId + 1);
    }

    public long getCurrentTime() {
        return player.getCurrentPosition();
    }

    public int getCurrentSongId() {
        return currentSongId;
    }

    public void seekTo(int seek) {
        player.seekTo(seek * 1000);
    }

    public void sendMediaPlayerState(boolean mediaPlayerState) {
        Intent stateIntent = new Intent(Constants.Notify.MEDIAPLAYER_PLAY);
        stateIntent.putExtra("is_playing", mediaPlayerState);
        sendBroadcast(stateIntent);
    }

    public void sendNextSongState(int songPosition) {
        Intent stateIntent = new Intent(Constants.Notify.NEXT_SONG);
        stateIntent.putExtra("next_song", songPosition);
        sendBroadcast(stateIntent);
    }

    public void sendEndPlaylistState(boolean endState) {
        Intent stateIntent = new Intent(Constants.Notify.END_STATE);
        stateIntent.putExtra("end_state", endState);
        sendBroadcast(stateIntent);
    }

    public void activateShuffle() {
        if (shuffleList == null) {
            shuffleActivated = true;
            shuffleList = new ArrayList<>();

            for (int i = 0; i < mSongs.size(); i++) {
                shuffleList.add(i);
            }

            Collections.shuffle(shuffleList);
            nextSong(0);
        }
    }

    public void shuffle() {
        if (shuffleList != null) {
            Collections.shuffle(shuffleList);
        }
    }

    public void desactivateShuffle() {
        currentSongId = shuffleList.get(currentSongId);
        shuffleActivated = false;
        shuffleList.clear();
        shuffleList = null;
    }

    public void setRepeatState(int repeatState) {
        this.repeatState = repeatState;
    }

    public void setSongs(ArrayList<Song> songs) {
        mSongs = songs;
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (player != null && !player.getPlayWhenReady()) {
            player.release();
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (shuffleActivated) {
            shuffleActivated = false;
            shuffleList.clear();
            shuffleList = null;
        }

        if (player != null)
            player.release();
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return sInstance;
        }
    }
}
