package cn.tonlyshy.fmmusicb.modules.main.presenter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import cn.tonlyshy.fmmusic.music.PBMusic;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public class MusicListPresenter extends IMusicLoad.Presenter {
    IMusicLoad.LocalMusicView mView;
    Context mContext;
    List<PBMusic> mList;

    public MusicListPresenter(Context mContext, IMusicLoad.LocalMusicView mView) {
        this.mView = mView;
        this.mContext = mContext;
        mList = new ArrayList<>();
    }

    @Override
    public void loadMusicFormLocal(int offset, int limit) {
        mView.onLoadLocalMusicStart();
        try {
            mList = new ArrayList<>();
            Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            getMusicList(mList, cursor);
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "duration > 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            getMusicList(mList, cursor);
            mView.onLoadLocalMusicSuccess(mList);
        } catch (Exception e) {
            e.printStackTrace();
            mView.onLoadLocalMusicFail();
        }
    }

    protected void getMusicList(List<PBMusic> mList, Cursor cursor) {
        while (cursor.moveToNext()) {
            PBMusic.Builder builder = PBMusic.newBuilder();
            builder.setId(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)))
                    .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                    .setDuration(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))
                    .setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                    .setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                    .setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                    .setAlbumUri(String.format("content://media/external/audio/albumart/%s", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
            mList.add(builder.build());
        }
    }
}
