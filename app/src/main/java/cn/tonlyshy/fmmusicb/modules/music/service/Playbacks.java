//package cn.tonlyshy.fmmusicb.modules.music.service;
//
//import android.content.Context;
//import android.media.AudioManager;
//import android.media.MediaMetadata;
//import android.media.MediaPlayer;
//import android.media.session.PlaybackState;
//import android.net.wifi.WifiManager;
//import android.os.PowerManager;
//import android.support.v4.media.MediaMetadataCompat;
//import android.support.v4.media.session.MediaSessionCompat;
//import android.support.v4.media.session.PlaybackStateCompat;
//import android.text.TextUtils;
//import android.util.Log;
//
//import java.io.IOException;
//
//import cn.tonlyshy.fmmusicb.modules.music.model.MusicProvider;
//
//import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
//
///**
// * Created by liaowm5 on 2017/7/27.
// */
//
//public class Playback implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
//
//    interface Callback {
//        void onCompletion();
//
//        void onPlaybackStatusChanged(int state);
//
//        void onError(String errorMsg);
//    }
//
//
//    // The volume we set the media player to when we lose audio focus, but are
//    // allowed to reduce the volume instead of stopping playback.
//    public static final float VOLUME_DUCK = 0.2f;
//    // The volume we set the media player when we have audio focus.
//    public static final float VOLUME_NORMAL = 1.0f;
//
//    // we don't have audio focus, and can't duck (play at a low volume)
//    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
//    // we don't have focus, but can duck (play at a low volume)
//    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
//    // we have full audio focus
//    private static final int AUDIO_FOCUSED = 2;
//
//    // Type of audio focus we have:
//    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
//
//    private final MusicService mService;
//    private final MusicProvider mMusicProvider;
//
//    private AudioManager mAudioManager;
//    private MediaPlayer mMediaPlayer;
//
//    private boolean mPlayOnFocusGain;
//    private volatile int mCurrentPosition;
//    private volatile String mCurrentMediaId;
//
//    //playing state
//    private int mState = PlaybackState.STATE_NONE;
//    private final WifiManager.WifiLock mWifiLock;
//    private Callback mCallback;
//
//
//    public Playback(MusicService mService, MusicProvider mMusicProvider) {
//        Context context = mService.getApplicationContext();
//        this.mService = mService;
//        this.mMusicProvider = mMusicProvider;
//        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//
//        // Create the Wifi lock (this does not acquire the lock, this just creates it).
//        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
//                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sample_lock");
//    }
//
//    public void play(MediaSessionCompat.QueueItem item) {
//        getAudioFocus();
//        String mediaId = item.getDescription().getMediaId();
//        boolean mediaChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
//        if (mediaChanged) {
//            mCurrentPosition = 0;
//            mCurrentMediaId = mediaId;
//        }
//
//        if (mState == PlaybackState.STATE_PAUSED
//                && !mediaChanged
//                && mMediaPlayer != null) {
//            configMediaPlayerState();
//        } else {
//            mState = PlaybackState.STATE_STOPPED;
//            relaxResources(false);
//            MediaMetadataCompat track = mMusicProvider.getMusic(item.getDescription().getMediaId());
//
//            String source = track.getString(MediaMetadata.METADATA_KEY_MEDIA_URI);
//
//            try {
//                if (mMediaPlayer == null) {
//                    mMediaPlayer = new MediaPlayer();
//
//                    // Make sure the media player will acquire a wake-lock while
//                    // playing. If we don't do that, the CPU might go to sleep while the
//                    // song is playing, causing playback to stop.
//                    mMediaPlayer.setWakeMode(mService.getApplicationContext(),
//                            PowerManager.PARTIAL_WAKE_LOCK);
//
//                    // we want the media player to notify us when it's ready preparing,
//                    // and when it's done playing:
//                    mMediaPlayer.setOnPreparedListener(this);
//                    mMediaPlayer.setOnCompletionListener(this);
//                    mMediaPlayer.setOnErrorListener(this);
//                    mMediaPlayer.setOnSeekCompleteListener(this);
//                } else {
//                    mMediaPlayer.reset();
//                }
//
//                mState = PlaybackState.STATE_BUFFERING;
//
//                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mMediaPlayer.setDataSource(source);
//
//                mMediaPlayer.prepareAsync();
//
//                mWifiLock.acquire();
//                if (mCallback != null) {
//                    mCallback.onPlaybackStatusChanged(mState);
//                }
//            } catch (IOException e) {
//                if (mCallback != null)
//                    mCallback.onError(e.getMessage());
//            }
//        }
//    }
//
//    public void pause() {
//        if (mState == PlaybackState.STATE_PLAYING) {
//            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//                mMediaPlayer.pause();
//                mCurrentPosition = mMediaPlayer.getCurrentPosition();
//            }
//            relaxResources(false);
//        }
//
//        mState = PlaybackState.STATE_PAUSED;
//        if (mCallback != null)
//            mCallback.onPlaybackStatusChanged(mState);
//    }
//
//    public void stop() {
//        mState = PlaybackState.STATE_STOPPED;
//        if (mCallback != null)
//            mCallback.onPlaybackStatusChanged(mState);
//        mCurrentPosition = getCurrentStreamPosition();
//        giveUpAudioFocus();
//        relaxResources(true);
//        if (mWifiLock.isHeld()) {
//            mWifiLock.release();
//        }
//    }
//
//    public void seekTo(int position) {
//        Log.d(TAG, "seekTo called with " + position);
//
//        if (mMediaPlayer != null) {
//            mCurrentPosition = position;
//        } else {
//            if (mMediaPlayer.isPlaying()) {
//                mState = PlaybackState.STATE_BUFFERING;
//            }
//            mMediaPlayer.seekTo(position);
//            if (mCallback != null)
//                mCallback.onPlaybackStatusChanged(mState);
//        }
//    }
//
//    /**
//     * Try to get the system audio focus.
//     */
//    private void getAudioFocus() {
//        Log.d(TAG, "tryToGetAudioFocus");
//        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN);
//        mAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
//                ? AUDIO_FOCUSED : AUDIO_NO_FOCUS_NO_DUCK;
//        Log.d(TAG, "tryToGetAudioFocus result = " + result);
//    }
//
//    /**
//     * Give up the audio focus.
//     */
//    private void giveUpAudioFocus() {
//        Log.d(TAG, "giveUpAudioFocus");
//        if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
//        }
//    }
//
//
////    private void configMediaPlayerState() {
////        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
////            if (mState == PlaybackState.STATE_PLAYING) {
////                pause();
////            }
////        } else {
////            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
////                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
////            } else {
////                if (mMediaPlayer != null) {
////                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
////                }
////            }
////
////            if (mPlayOnFocusGain) {
////                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
////                    if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
////                        mMediaPlayer.start();
////                        mState = PlaybackState.STATE_PLAYING;
////                    } else {
////                        mMediaPlayer.seekTo(mCurrentPosition);
////                        mState = PlaybackState.STATE_BUFFERING;
////                    }
////                }
////                mPlayOnFocusGain = true;
////            }
////        }
////
////        if (mCallback != null) {
////            mCallback.onPlaybackStatusChanged(mState);
////        }
////    }
//private void configMediaPlayerState() {
//    Log.d(TAG, "configMediaPlayerState. mAudioFocus=" + mAudioFocus);
//    if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
//        // If we don't have audio focus and can't duck, we have to pause,
//        if (mState == PlaybackStateCompat.STATE_PLAYING) {
//            pause();
//        }
//    } else {  // we have audio focus:
//        if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
//            mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // we'll be relatively quiet
//        } else {
//            if (mMediaPlayer != null) {
//                mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
//            } // else do something for remote client.
//        }
//        // If we were playing when we lost focus, we need to resume playing.
//        if (mPlayOnFocusGain) {
//            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//                Log.d(TAG, "configMediaPlayerState startMediaPlayer. seeking to "
//                        + mCurrentPosition);
//                if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
//                    mMediaPlayer.start();
//                    mState = PlaybackStateCompat.STATE_PLAYING;
//                } else {
//                    mMediaPlayer.seekTo(mCurrentPosition);
//                    mState = PlaybackStateCompat.STATE_BUFFERING;
//                }
//            }
//            mPlayOnFocusGain = false;
//        }
//    }
//    if (mCallback != null) {
//        mCallback.onPlaybackStatusChanged(mState);
//    }
//}
//
//    public boolean isConnected() {
//        return true;
//    }
//
//    public int getCurrentStreamPosition() {
//        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : mCurrentPosition;
//    }
//
//    public int getState() {
//        return mState;
//    }
//
//    public boolean isPlaying() {
//        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
//    }
//
//    public void setmCallback(Callback mCallback) {
//        this.mCallback = mCallback;
//    }
//
//
//    //AudioManager listener
//
//    @Override
//    public void onPrepared(MediaPlayer mediaPlayer) {
//        Log.d(TAG, "onPrepared from MediaPlayer");
//        configMediaPlayerState();
//    }
//
//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        Log.d(TAG, "onAudioFocusChange from MediaPlayer");
//        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//            // We have gained focus:
//            mAudioFocus = AUDIO_FOCUSED;
//
//        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
//                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
//                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//            // We have lost focus. If we can duck (low playback volume), we can keep playing.
//            // Otherwise, we need to pause the playback.
//            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
//            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;
//
//            // If we are playing, we need to reset media player by calling configMediaPlayerState
//            // with mAudioFocus properly set.
//            if (mState == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
//                // If we don't have audio focus and can't duck, we save the information that
//                // we were playing, so that we can resume playback once we get the focus back.
//                mPlayOnFocusGain = true;
//            }
//        } else {
//            Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: " + focusChange);
//        }
//        configMediaPlayerState();
//    }
//
//
//    @Override
//    public void onSeekComplete(MediaPlayer mediaPlayer) {
//        Log.d(TAG, "onSeekComplete from MediaPlayer");
//        mCurrentPosition = mediaPlayer.getCurrentPosition();
//        if (mState == PlaybackStateCompat.STATE_BUFFERING) {
//            mMediaPlayer.start();
//            mState = PlaybackStateCompat.STATE_PLAYING;
//        }
//        if (mCallback != null) {
//            mCallback.onPlaybackStatusChanged(mState);
//        }
//    }
//
//    @Override
//    public void onCompletion(MediaPlayer mediaPlayer) {
//        Log.d(TAG, "onCompletion from MediaPlayer");
//        if (mCallback != null)
//            mCallback.onCompletion();
//    }
//
//    @Override
//    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//        if (mCallback != null) {
//            mCallback.onError("MediaPlayer error " + i + " (" + i1 + ")");
//        }
//        return true;
//    }
//
//    private void relaxResources(boolean releaseMediaPlayer) {
//        mService.stopForeground(true);
//
//        // stop and release the Media Player, if it's available
//        if (releaseMediaPlayer && mMediaPlayer != null) {
//            mMediaPlayer.reset();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
//
//        if (mWifiLock.isHeld()) {
//            mWifiLock.release();
//        }
//    }
//}
