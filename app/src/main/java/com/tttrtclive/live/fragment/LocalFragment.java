package com.tttrtclive.live.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;

import androidx.fragment.app.Fragment;
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

    public boolean getParams() {
        if (mPixView.getText() == null || TextUtils.isEmpty(mPixView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频分辨率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] wh = mPixView.getText().toString().trim().split("x");
        if (wh.length != 2) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            mSetActivity.mLocalWidth = Integer.parseInt(wh[0]);
            if (mSetActivity.mLocalWidth <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率宽必须大于0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            mSetActivity.mLocalHeight = Integer.parseInt(wh[1]);
            if (mSetActivity.mLocalHeight <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率高必须大于0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mBiteView.getText() == null || TextUtils.isEmpty(mBiteView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频码率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        mSetActivity.mLocalBitRate = Integer.parseInt(mBiteView.getText().toString().trim());
        if (mSetActivity.mLocalBitRate <= 0) {
            Toast.makeText(getContext(), "自定义视频码率必须大于0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mFrameView.getText() == null || TextUtils.isEmpty(mFrameView.getText().toString())) {
            Toast.makeText(getContext(), "自定义视频帧率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        mSetActivity.mLocalFrameRate = Integer.parseInt(mFrameView.getText().toString().trim());
        if (mSetActivity.mLocalFrameRate <= 0) {
            Toast.makeText(getContext(), "自定义视频帧率必须大于0", Toast.LENGTH_SHORT).show();
            return false;
        }

        mSetActivity.mLocalIP = mFrameIP.getText().toString().trim();
        if (!TextUtils.isEmpty(mFramePort.getText())) {
            mSetActivity.mLocalPort = Integer.parseInt(mFramePort.getText().toString().trim());
        } else {
            mSetActivity.mLocalPort = 0;
        }
        return true;
    }
}
