package cn.tonlyshy.fmmusicb.event;

import android.graphics.Bitmap;

/**
 * Created by liaowm5 on 2017/7/28.
 */

public class LoadBitmapSuccessEvent {
    public String mediaId;
    public Bitmap bitmap;

    public LoadBitmapSuccessEvent(String mediaId, Bitmap bitmap) {
        this.mediaId = mediaId;
        this.bitmap = bitmap;
    }
}
