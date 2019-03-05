package com.tttrtclive.live.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;

import so.library.SoSpinner;

@SuppressLint("ValidFragment")
public class LocalFragment extends Fragment implements SoSpinner.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private VideoProfileManager mVideoProfileManager = new VideoProfileManager();
    private EditText mPixView, mBiteView, mFrameView, mFrameIP, mFramePort;
    private VideoProfileManager.VideoProfile mVideoProfile;
    private SetActivity mSetActivity;
    private static LocalFragment sf;

    public static LocalFragment getInstance() {
        if (sf == null)
            sf = new LocalFragment();
        return sf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSetActivity = (SetActivity) getActivity();
        View v = inflater.inflate(R.layout.child_set_local, null);

        mPixView = v.findViewById(R.id.local_pix_view);
        mBiteView = v.findViewById(R.id.local_bite_rate);
        mFrameView = v.findViewById(R.id.local_frame_rate);
        mFrameIP = v.findViewById(R.id.local_frame_ip);
        mFramePort = v.findViewById(R.id.local_frame_port);
        View mLocalIp = v.findViewById(R.id.local_ip_tv);
        View mLocalPort = v.findViewById(R.id.local_port_tv);
        if (LocalConfig.VERSION_FLAG == LocalConstans.VERSION_WHITE) {
            mFrameIP.setVisibility(View.VISIBLE);
            mFramePort.setVisibility(View.VISIBLE);
            mLocalIp.setVisibility(View.VISIBLE);
            mLocalPort.setVisibility(View.VISIBLE);
        } else {
            mFrameIP.setVisibility(View.INVISIBLE);
            mFramePort.setVisibility(View.INVISIBLE);
            mLocalIp.setVisibility(View.INVISIBLE);
            mLocalPort.setVisibility(View.INVISIBLE);
        }
//        mLocalIp.setVisibility(View.VISIBLE);
//        mLocalPort.setVisibility(View.VISIBLE);
//        mFrameIP.setVisibility(View.VISIBLE);
//        mFramePort.setVisibility(View.VISIBLE);

        SoSpinner localPixSpinner = v.findViewById(R.id.local_pix_spinner);
        localPixSpinner.setOnItemSelectedListener(this);

        ((Switch) v.findViewById(R.id.local_audio_switch)).setOnCheckedChangeListener(this);

        if (mSetActivity.mLocalVideoProfile != 0) {
            mVideoProfile = mVideoProfileManager.getVideoProfile(mSetActivity.mLocalVideoProfile);
            localPixSpinner.setSelectedIndex(mVideoProfileManager.mVideoProfiles.indexOf(mVideoProfile));
        } else {
            mPixView.setText(mSetActivity.mLocalWidth + "x" + mSetActivity.mLocalHeight);
            mBiteView.setText(mSetActivity.mLocalBitRate + "");
            mFrameView.setText(mSetActivity.mLocalFrameRate + "");

            localPixSpinner.setSelectedIndex(5);
        }

        ((Switch) v.findViewById(R.id.local_audio_switch)).setChecked(mSetActivity.mUseHQAudio);

        mFrameIP.setText(mSetActivity.mLocalIP);
        if (mSetActivity.mLocalPort != 0) {
            mFramePort.setText(String.valueOf(mSetActivity.mLocalPort));
        }
        return v;
    }

    @Override
    public void onItemSelected(View parent, int position) {
        if (position != 5) {
            mVideoProfile = mVideoProfileManager.mVideoProfiles.get(position);
            mSetActivity.mLocalVideoProfile = mVideoProfile.videoProfile;
            mPixView.setText(mVideoProfile.width + "x" + mVideoProfile.height);
            mBiteView.setText(mVideoProfile.bRate + "");
            mFrameView.setText(mVideoProfile.fRate + "");

            mPixView.setEnabled(false);
            mBiteView.setEnabled(false);
            mFrameView.setEnabled(false);
            mPixView.setTextColor(Color.parseColor("#FF999999"));
            mBiteView.setTextColor(Color.parseColor("#FF999999"));
            mFrameView.setTextColor(Color.parseColor("#FF999999"));
        } else {
            mSetActivity.mLocalVideoProfile = 0;

            mPixView.setEnabled(true);
            mBiteView.setEnabled(true);
            mFrameView.setEnabled(true);
            mPixView.setTextColor(Color.parseColor("#FF666666"));
            mBiteView.setTextColor(Color.parseColor("#FF666666"));
            mFrameView.setTextColor(Color.parseColor("#FF666666"));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((SetActivity) getActivity()).mUseHQAudio = isChecked;
    }

    public void getParams() {
        String[] wh = mPixView.getText().toString().trim().split("x");
        mSetActivity.mLocalWidth = Integer.parseInt(wh[0]);
        mSetActivity.mLocalHeight = Integer.parseInt(wh[1]);
        mSetActivity.mLocalBitRate = Integer.parseInt(mBiteView.getText().toString().trim());
        mSetActivity.mLocalFrameRate = Integer.parseInt(mFrameView.getText().toString().trim());
        mSetActivity.mLocalIP = mFrameIP.getText().toString().trim();
        if (!TextUtils.isEmpty(mFramePort.getText())) {
            mSetActivity.mLocalPort = Integer.parseInt(mFramePort.getText().toString().trim());
        } else {
            mSetActivity.mLocalPort = 0;
        }
    }

}