package cn.tonlyshy.fmmusicb.modules.main.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import cn.tonlyshy.fmmusicb.R;
import cn.tonlyshy.fmmusicb.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements BrowseFragment.FragmentDataHelper {
    ActivityMainBinding binding;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mItemToOpenWhenDrawerCloses = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //initToolBar();
        if (savedInstanceState == null &&//权限申请
                permissionCheck()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, BrowseFragment.newInstance(null))
                    .commit();
        }
    }

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    public boolean permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void initToolBar() {
        mDrawerToggle = new ActionBarDrawerToggle(this, binding.dlContent,
                binding.toolbarContainer.toolbar, R.string.hint_open_drawer, R.string.hint_close_drawer);
        setSupportActionBar(binding.toolbarContainer.toolbar);
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isPlaying) {
        if (item.isPlayable()) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();

            // If the item is playing, pause it, otherwise start it
            if (isPlaying) {
                controls.pause();
            } else {
                controls.playFromMediaId(item.getMediaId(), null);
            }
        } else if (item.isBrowsable()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BrowseFragment.newInstance(item.getMediaId()))
                    .addToBackStack(null)
                    .commit();
        }
    }
}
