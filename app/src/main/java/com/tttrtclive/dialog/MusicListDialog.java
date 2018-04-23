package com.tttrtclive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.tttrtclive.R;
import com.tttrtclive.bean.Song;
import com.tttrtclive.utils.MusicUtils;

import java.util.List;

/**
 * Created by wangzhiguo on 17/11/7.
 */

public class MusicListDialog extends Dialog {

    private boolean mIsPlaying;
    private MusicListOnClickListener mMusicListOnClickListener;

    public MusicListDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainly_music_dialog);
        List<Song> musicData = MusicUtils.getMusicData(getContext());
        int num;
        if (musicData.size() > 6) {
            num = 6;
        } else {
            num = musicData.size();
        }
        for (int i = 0; i < num; i++) {
            View rootView = null;
            switch (i) {
                case 0:
                    rootView = findViewById(R.id.music_dialog_item_one);
                    break;
                case 1:
                    rootView = findViewById(R.id.music_dialog_item_two);
                    break;
                case 2:
                    rootView = findViewById(R.id.music_dialog_item_three);
                    break;
                case 3:
                    rootView = findViewById(R.id.music_dialog_item_four);
                    break;
                case 4:
                    rootView = findViewById(R.id.music_dialog_item_five);
                    break;
                case 5:
                    rootView = findViewById(R.id.music_dialog_item_six);
                    break;
            }
            setMusicNameAndListener(rootView, musicData.get(i));
        }
    }

    private void setMusicNameAndListener(View rootView, Song song) {
        rootView.setVisibility(View.VISIBLE);
        TextView mNameTV = (TextView) rootView.findViewById(R.id.music_block_name);
        mNameTV.setText(song.song);
        MyMusicItemOnClickListener mMyMusicItemOnClickListener = new MyMusicItemOnClickListener(song.path);
        rootView.setOnClickListener(mMyMusicItemOnClickListener);
    }

    public void setMusicListOnClickListener(MusicListOnClickListener mMusicListOnClickListener) {
        this.mMusicListOnClickListener = mMusicListOnClickListener;
    }

    public synchronized void setPlaying(boolean mIsPlaying) {
        this.mIsPlaying = mIsPlaying;
    }

    public synchronized boolean isPlaying() {
        return mIsPlaying;
    }

    class MyMusicItemOnClickListener implements View.OnClickListener {

        private String mMusicName;

        MyMusicItemOnClickListener(String mMusicName) {
            this.mMusicName = mMusicName;
        }

        @Override
        public void onClick(View v) {
            if (mMusicListOnClickListener != null) {
                if (mIsPlaying) {
                    mMusicListOnClickListener.stopAudioMixing();
                }

                mMusicListOnClickListener.startAudioMixing
                        (mMusicName, false, false, 1);
                setPlaying(true);
                mMusicListOnClickListener.showCannelMusicPlayingBT();
            }
        }
    }

    public interface MusicListOnClickListener {

        void startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle);

        void stopAudioMixing();

        void showCannelMusicPlayingBT();
    }
}
