package com.github.filipebezerra.awesomeplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

// Issue: http://stackoverflow.com/questions/20087804/should-have-subtitle-controller-already-set-mediaplayer-error-android
// Docs: http://developer.android.com/reference/android/media/MediaPlayer.html
// Guide: http://developer.android.com/guide/topics/media/mediaplayer.html
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private StreamSubscriber mStreamSubscriber = new StreamSubscriber();

    private List<Track> mTrackList;
    private TrackAdapter mTrackAdapter;

    @Bind(R.id.track_list_view) ListView mTrackListView;
    @Bind(R.id.root_layout) CoordinatorLayout mRootLayout;
    @Bind(R.id.selected_track_image) ImageView mSelectedTrackImage;
    @Bind(R.id.selected_track_title) TextView mSelectedTrackTitle;
    @Bind(R.id.selected_track_toolbar) Toolbar mSeletedTrackToolbar;
    @Bind(R.id.buffering) TextView mBufferingSelectedTrack;

    private MaterialDialog mProgressDialog;
    private MediaPlayer mMediaPlayer;

    private MenuItem mPlayerControl;
    private boolean mIsMediaPlayerPreparing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        mSeletedTrackToolbar.inflateMenu(R.menu.menu_main);
        mPlayerControl = mSeletedTrackToolbar.getMenu().findItem(R.id.action_media_player);
        mSeletedTrackToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_media_player) {
                    togglePlayPause();
                }
                return true;
            }
        });

        mTrackList = new ArrayList<>();
        mTrackAdapter = new TrackAdapter(this, mTrackList);
        mTrackListView.setAdapter(mTrackAdapter);

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Track track = mTrackList.get(position);

                if (mSeletedTrackToolbar.getVisibility() == View.GONE) {
                    mSeletedTrackToolbar.setVisibility(View.VISIBLE);
                }

                mSelectedTrackTitle.setText(track.getTitle());
                Picasso.with(MainActivity.this)
                        .load(track.getArtworkUrl())
                        .placeholder(R.drawable.ic_image_white)
                        .error(R.drawable.ic_broken_image_white)
                        .fit()
                        .tag(track.getId())
                        .into(mSelectedTrackImage);

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                mMediaPlayer.reset();

                try {
                    mMediaPlayer.setDataSource(
                            track.getStreamUrl() + "?client_id=" + Config.CLIENT_ID);
                    mMediaPlayer.prepareAsync();
                    mIsMediaPlayerPreparing = true;
                    mPlayerControl.setIcon(R.drawable.ic_hourglass_empty);
                } catch (IllegalStateException e) {
                    Snackbar.make(mRootLayout, "Must be called in Idle state.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                } catch (IOException e) {
                    Timber.e(e, "Error trying to set the stream url");
                    Snackbar.make(mRootLayout, "Error playing the music.", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Timber.d("Media is ready to play");
                mIsMediaPlayerPreparing = false;
                togglePlayPause();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Timber.d("Media has been reached during playback");
                togglePlayPause();
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Timber.d("Error with code %d and extra %d", what, extra);
                mMediaPlayer.reset();
                return true;
            }
        });

        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Timber.d("Buffer update to %d", percent);
            }
        });

        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Timber.d("Buffer info/warning is available with code %d and extra %d", what, extra);

                switch (what) {
                    case MEDIA_INFO_BUFFERING_START:
                        mPlayerControl.setIcon(R.drawable.ic_hourglass_empty);
                        mBufferingSelectedTrack.setVisibility(View.VISIBLE);
                        break;
                    case MEDIA_INFO_BUFFERING_END:
                        if (mMediaPlayer.isPlaying()) {
                            mPlayerControl.setIcon(R.drawable.ic_pause);
                        } else {
                            mPlayerControl.setIcon(R.drawable.ic_play);
                        }
                        mBufferingSelectedTrack.setVisibility(View.GONE);
                        break;
                    case 703: //MediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH
                        mBufferingSelectedTrack.setText(getString(R.string.buffering, extra));
                        break;
                }
                return true;
            }
        });

        Timber.tag(TAG);
    }

    private void togglePlayPause() {
        if (mIsMediaPlayerPreparing) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            Timber.d("Media will pause");
            mMediaPlayer.pause();
            mPlayerControl.setIcon(R.drawable.ic_play);
        } else {
            Timber.d("Media will start playing");
            mMediaPlayer.start();
            mPlayerControl.setIcon(R.drawable.ic_pause);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestTracks();
    }

    private void requestTracks() {
        mProgressDialog = new MaterialDialog.Builder(this)
                .content("Loading tracks, please wait...")
                .progress(true, 0)
                .show();

        RetrofitController.streamService(this)
                .getRecentTracks(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
                        .format(new Date()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mStreamSubscriber);
    }

    private class StreamSubscriber extends Subscriber<List<Track>> {
        @Override
        public void onCompleted() {
            Timber.d("Completed requesting tracks");
            dismissProgressDialog();
            Snackbar.make(mRootLayout, "Good, all tracks loaded!", Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Error requesting tracks");
            dismissProgressDialog();
            Snackbar.make(mRootLayout, "Sorry, we got a fail!", Snackbar.LENGTH_SHORT)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestTracks();
                        }
                    })
                    .show();
        }

        @Override
        public void onNext(List<Track> tracks) {
            Timber.d("Next requesting tracks: %d", tracks.size());
            loadTracks(tracks);
        }
    }

    private void loadTracks(List<Track> tracks) {
        mTrackList.clear();
        mTrackList.addAll(tracks);
        mTrackAdapter.notifyDataSetChanged();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mStreamSubscriber != null) {
            if (!mStreamSubscriber.isUnsubscribed()) {
                mStreamSubscriber.unsubscribe();
            }
        }
    }
}
