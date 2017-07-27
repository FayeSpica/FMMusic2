package cn.tonlyshy.fmmusicb.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by liaowm5 on 2017/7/28.
 */

public class ImageLoader {
    public interface FetchListener {
        void onError(int position, Uri artUrl, Exception e);

        void onFetched(int position, Uri artUrl, Bitmap bitmap);
    }

    public static void fetchLocal(Context context, final int position, final Uri artUrl, final FetchListener listener) {

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void[] objects) {
                Bitmap bitmap = null;

                InputStream is = null;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                    is = context.getContentResolver().openInputStream(artUrl);
                    if (null != is) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "doInBackground: fetchLocal artUrl=" + artUrl);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    listener.onError(position, artUrl, new IllegalArgumentException("got null bitmap"));
                } else {
                    listener.onFetched(position, artUrl,
                            bitmap);
                }
            }
        }.execute();
    }
}
