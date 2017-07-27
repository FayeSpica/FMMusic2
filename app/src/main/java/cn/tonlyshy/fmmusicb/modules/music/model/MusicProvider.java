package cn.tonlyshy.fmmusicb.modules.music.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedHashMap;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public class MusicProvider {
    private Context mContext;
    private final LinkedHashMap<String, MediaMetadataCompat> mMusicListById;

    public MusicProvider(Context mContext) {
        mMusicListById = new LinkedHashMap<>();
        this.mContext = mContext;
    }

    private enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    /**
     * Callback used by MusicService.
     */
    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public Iterable<MediaMetadataCompat> getAllMusics() {
        if (mCurrentState != State.INITIALIZED || mMusicListById.isEmpty()) {
            return Collections.emptyList();
        }
        for (MediaMetadataCompat m : mMusicListById.values()) {
            Log.d(TAG, String.format("title = [%s] media Id = [%s] uri = [%s]", m.getDescription().getTitle(), m.getDescription().getMediaId(), m.getDescription().getMediaUri()));
        }
        return mMusicListById.values();
    }

    public MediaMetadataCompat getMusic(String musicId) {
        Log.d(TAG, "mMusicListById.containsKey(musicId) =  " + mMusicListById.containsKey(musicId));
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId) : null;
    }

    public synchronized void updateMusic(String musicId, MediaMetadataCompat metadata) {
        MediaMetadataCompat track = mMusicListById.get(musicId);
        if (track != null) {
            mMusicListById.put(musicId, metadata);
        }
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        Log.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            // Already initialized, so call back immediately.
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                getMusicInfo(cursor);
                cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                getMusicInfo(cursor);
                mCurrentState = State.INITIALIZED;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not retrieve music list", e);
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    protected void getMusicInfo(Cursor cursor) {
        int c = 0;
        while (cursor.moveToNext()) {
            c++;
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            String musicId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, Uri.parse("file://" + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))).toString());
            builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse("content://media/external/audio/albumart/" + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))).toString());
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse("content://media/external/audio/albumart/" + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))).toString());
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
            builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)));
            mMusicListById.put(musicId, builder.build());
            Log.d("hint", String.format("title = [%s] uri = [%s]", (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))), Uri.parse("http://fs.open.kugou.com/ccd4cc2fe46a54aabb91c8d397d1e330/597a2338/G032/M02/0C/08/wIYBAFWiT4CAMsYbAEX5khnQiiY227.mp3").toString()));
        }
    }
}
