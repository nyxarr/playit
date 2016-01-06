package com.viby.playit.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.viby.playit.R;
import com.viby.playit.activities.MainActivity;
import com.viby.playit.service.IMediaPlayerStateListener;
import com.viby.playit.utils.Constants;

public class MediaControllerFragment extends Fragment {
    private Context mContext;

    private ImageView playButton;
    private ImageView nextButton;
    private ImageView previousButton;
    private ImageView shuffle;
    private ImageView repeat;
    private SeekBar musicProgress;
    private TextView currentTime;
    private TextView totalTime;
    private RelativeLayout progressText;
    private TextView title;

    private boolean mediaplayerState = false;
    private int repeatState = 0;
    private int shuffleCount = 0;

    private IMediaController mCallback;

    public MediaControllerFragment() {}

    public interface IMediaController {
        void nextTrack();
        void play();
        void pause();
        void previousTrack();
        void setRepeatState(int repeatState);
        void seekTo(int seek);
        void activateShuffle();
        void desactivateShuffle();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (IMediaController) context;
        } catch (ClassCastException classException) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.music_controller_layout, container, false);

        playButton = (ImageView) rootView.findViewById(R.id.play_button);
        nextButton = (ImageView) rootView.findViewById(R.id.next_button);
        previousButton = (ImageView) rootView.findViewById(R.id.previous_button);
        shuffle = (ImageView) rootView.findViewById(R.id.shuffle_button);
        repeat = (ImageView) rootView.findViewById(R.id.repeat_button);

        title = (TextView) rootView.findViewById(R.id.title_controller);
        musicProgress = (SeekBar) rootView.findViewById(R.id.music_progress);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        progressText = (RelativeLayout) rootView.findViewById(R.id.progress);

        final float yPos = progressText.getY();

        (((MainActivity) getActivity())).setServicePlayingListener(new IMediaPlayerStateListener() {
            @Override
            public void onStateChange(boolean listener) {
                mediaplayerState = listener;
                if (isAdded()) {
                    if (listener) {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_large_orange));
                    } else {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_orange));
                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.nextTrack();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaplayerState) {
                    mCallback.pause();
                } else {
                    mCallback.play();
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.previousTrack();
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleCount == 0) {
                    shuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_shuffle_activated));
                    shuffleCount++;

                    mCallback.activateShuffle();
                } else {
                    shuffle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_shuffle));
                    shuffleCount = 0;

                    mCallback.desactivateShuffle();
                }

            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (repeatState) {
                    case 0:
                        repeat.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_repeat_all));
                        mCallback.setRepeatState(Constants.Repeat.REPEAT_ALL);
                        repeatState++;
                        break;

                    case 1:
                        repeat.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_repeat_same));
                        mCallback.setRepeatState(Constants.Repeat.REPEAT_SAME);
                        repeatState++;
                        break;

                    case 2:
                        repeat.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_repeat));
                        mCallback.setRepeatState(Constants.Repeat.REPEAT_DISABLE);
                        repeatState = 0;
                        break;
                }
            }
        });


        musicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int width = seekBar.getWidth()
                        - seekBar.getPaddingLeft()
                        - seekBar.getPaddingRight();

                if (seekBar.getMax() != 0) {
                    progressText.setX(seekBar.getPaddingLeft()
                                    + (rootView.getMeasuredWidth() / 2) - (seekBar.getMeasuredWidth() / 2)
                                    + width * progress / seekBar.getMax()
                                    - progressText.getMeasuredWidth() / 2
                    );
                }

                ((TextView) progressText.findViewById(R.id.progress_text)).setText(String.format("%02d", progress / 60)
                                + ":"
                                + String.format("%02d", progress % 60)
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((MainActivity) getActivity()).setMediaControllerSeekbarTouch(true);
                progressText
                        .animate()
                        .translationY(yPos + 1)
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                progressText.setVisibility(View.VISIBLE);
                            }
                        });
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((MainActivity) getActivity()).setMediaControllerSeekbarTouch(false);
                float yPos = progressText.getY();
                progressText
                        .animate()
                        .translationY(yPos - 1)
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                progressText.setVisibility(View.GONE);
                            }
                        });

                mCallback.seekTo(seekBar.getProgress());
            }
        });

        return rootView;
    }

    public void setTitle(String title) { this.title.setText(title); }

    public void setCurrentTime(String s) {
        currentTime.setText(s);
    }

    public void setMaxProgressBar(int max) {
        musicProgress.setMax(max);
    }

    public void setCurrentTimeProgressBar(int time) {
        musicProgress.setProgress(time);
    }

    public void setTotalTime(String length) {
        totalTime.setText(length);
    }
}
