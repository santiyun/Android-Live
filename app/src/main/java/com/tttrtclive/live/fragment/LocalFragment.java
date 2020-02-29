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
            mPixView.requestFocus();
            localPixSpinner.setSelectedIndex(mVideoProfileManager.mVideoProfiles.size());
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
        if (position < mVideoProfileManager.mVideoProfiles.size()) {
            mVideoProfile = mVideoProfileManager.mVideoProfiles.get(position);
            mSetActivity.mLocalVideoProfile = mVideoProfile.videoProfile;
            mPixView.setText(mVideoProfile.width + "x" + mVideoProfile.height);
            mBiteView.setText(mVideoProfile.bRate + "");
            mFrameView.setText(mVideoProfile.fRate + "");
            mPixView.requestFocus();
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
        if (TextUtils.isEmpty(mPixView.getText())) {
            Toast.makeText(getContext(), "自定义视频分辨率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] wh = mPixView.getText().toString().trim().split("x");
        if (wh.length != 2) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        int width, height, birrate, fps;
        try {
            width = Integer.parseInt(wh[0]);
            if (width <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率宽必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            height = Integer.parseInt(wh[1]);
            if (height <= 0) {
                Toast.makeText(getContext(), "自定义视频分辨率高必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (width * height > 1920 * 1080) {
            Toast.makeText(getContext(), "自定义视频分辨率最大值为1920*1080", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(mBiteView.getText())) {
            Toast.makeText(getContext(), "自定义视频码率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            birrate = Integer.parseInt(mBiteView.getText().toString().trim());
            if (birrate <= 0) {
                Toast.makeText(getContext(), "自定义视频码率必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频码率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (birrate > 5000) {
            Toast.makeText(getContext(), "自定义视频码率最大值为5000kbps", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(mFrameView.getText())) {
            Toast.makeText(getContext(), "自定义视频帧率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            fps = Integer.parseInt(mFrameView.getText().toString().trim());
            if (fps <= 0) {
                Toast.makeText(getContext(), "自定义视频帧率必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "自定义视频帧率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fps > 25) {
            Toast.makeText(getContext(), "自定义视频帧率最大值为25", Toast.LENGTH_SHORT).show();
            return false;
        }

        mSetActivity.mLocalWidth = width;
        mSetActivity.mLocalHeight = height;
        mSetActivity.mLocalBitRate = birrate;
        mSetActivity.mLocalFrameRate = fps;

        mSetActivity.mLocalIP = mFrameIP.getText().toString().trim();
        if (!TextUtils.isEmpty(mFramePort.getText())) {
            mSetActivity.mLocalPort = Integer.parseInt(mFramePort.getText().toString().trim());
        } else {
            mSetActivity.mLocalPort = 0;
        }
        return true;
    }
}
