package cn.tonlyshy.fmmusicb.modules.main.presenter;

import java.util.List;

import cn.tonlyshy.fmmusic.music.PBMusic;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public interface IMusicLoad {
    abstract class Presenter {
        public abstract void loadMusicFormLocal(int offset, int limit);
    }

    interface LocalMusicView {
        void onLoadLocalMusicStart();

        void onLoadLocalMusicSuccess(List<PBMusic> mList);

        void onLoadLocalMusicFail();
    }

}
