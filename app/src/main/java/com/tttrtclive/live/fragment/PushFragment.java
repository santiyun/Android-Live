package com.tttrtclive.live.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;

import so.library.SoSpinner;

@SuppressLint("ValidFragment")
public class PushFragment extends Fragment implements SoSpinner.OnItemSelectedListener{

    private VideoProfileManager mVideoProfileManager = new VideoProfileManager();
    private EditText mPixView, mBiteView, mFrameView;
    private VideoProfileManager.VideoProfile mVideoProfile;
    private SetActivity mSetActivity;
    private static PushFragment sf;

    public static PushFragment getInstance() {
        if (sf == null)
            sf = new PushFragment();
        return sf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSetActivity = (SetActivity) getActivity();
        View v = inflater.inflate(R.layout.push_set, null);

        mPixView = v.findViewById(R.id.push_pix_rate);
        mBiteView = v.findViewById(R.id.push_bite_rate);
        mFrameView = v.findViewById(R.id.push_frame_rate);

        SoSpinner pushPixSpinner = v.findViewById(R.id.push_pix_spinner);
        SoSpinner pushEncodeSpinner = v.findViewById(R.id.push_encode_spinner);
        SoSpinner pushAudioSpinner = v.findViewById(R.id.push_audio_spinner);

        pushPixSpinner.setOnItemSelectedListener(this);
        pushEncodeSpinner.setOnItemSelectedListener(this);
        pushAudioSpinner.setOnItemSelectedListener(this);

        if (mSetActivity.mPushVideoProfile != 0) {
            mVideoProfile = mVideoProfileManager.getVideoProfile(mSetActivity.mPushVideoProfile);
            pushPixSpinner.setSelectedIndex(mVideoProfileManager.mVideoProfiles.indexOf(mVideoProfile));
        } else {
            mPixView.setText(mSetActivity.mPushWidth + "x" + mSetActivity.mPushHeight);
            mBiteView.setText(mSetActivity.mPushBitRate + "");
            mFrameView.setText(mSetActivity.mPushFrameRate + "");

            pushPixSpinner.setSelectedIndex(5);
        }

        pushEncodeSpinner.setSelectedIndex(mSetActivity.mEncodeType);
        pushAudioSpinner.setSelectedIndex(mSetActivity.mAudioSRate);

        return v;
    }

    @Override
    public void onItemSelected(View parent, int position) {
        switch (parent.getId()) {
            case R.id.push_pix_spinner:
                if (position != 5) {
                    mVideoProfile = mVideoProfileManager.mVideoProfiles.get(position);
                    mSetActivity.mPushVideoProfile = mVideoProfile.videoProfile;
                    mPixView.setText(mVideoProfile.width + "x" + mVideoProfile.height);
                    mBiteView.setText(mVideoProfile.bRate + "");
                    mFrameView.setText(mVideoProfile.fRate + "");

                    mPixView.setEnabled(false);
                    mBiteView.setEnabled(false);
                    mFrameView.setEnabled(false);
                } else {
                    mSetActivity.mPushVideoProfile = 0;

                    mPixView.setEnabled(true);
                    mBiteView.setEnabled(true);
                    mFrameView.setEnabled(true);
                }
                break;
            case R.id.push_encode_spinner:
                mSetActivity.mEncodeType = position;
                break;
            case R.id.push_audio_spinner:
                mSetActivity.mAudioSRate = position;
                mSetActivity.mChannels = position + 1;
                break;
        }
    }

    public void getParams() {
        String[] wh = mPixView.getText().toString().trim().split("x");
        mSetActivity.mPushWidth = Integer.parseInt(wh[0]);
        mSetActivity.mPushHeight = Integer.parseInt(wh[1]);
        mSetActivity.mPushBitRate = Integer.parseInt(mBiteView.getText().toString().trim());
        mSetActivity.mPushFrameRate = Integer.parseInt(mFrameView.getText().toString().trim());
    }

}