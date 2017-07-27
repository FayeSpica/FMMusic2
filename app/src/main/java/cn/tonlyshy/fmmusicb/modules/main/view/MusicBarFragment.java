package cn.tonlyshy.fmmusicb.modules.main.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.tonlyshy.fmmusicb.R;
import cn.tonlyshy.fmmusicb.databinding.FragmentMusicBarBinding;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public class MusicBarFragment extends Fragment {
    FragmentMusicBarBinding binding;

    public MusicBarFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_bar, container, true);
        initData();
        initListener();
        return binding.getRoot();
    }

    void initData() {

    }

    void initListener() {

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
