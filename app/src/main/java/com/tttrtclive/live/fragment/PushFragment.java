package com.tttrtclive.live.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.VideoProfileManager;
import com.tttrtclive.live.ui.SetActivity;
import com.tttrtclive.live.utils.MyLog;

import androidx.fragment.app.Fragment;

public class PushFragment extends Fragment implements Spinner.OnItemSelectedListener {

    private VideoProfileManager mVideoProfileManager = new VideoProfileManager();
    private EditText mPixView, mBiteView, mFrameView;
    private VideoProfileManager.VideoProfile mVideoProfile;
    private SetActivity mSetActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSetActivity = (SetActivity) getActivity();
        View v = inflater.inflate(R.layout.child_set_push, null);

        mPixView = v.findViewById(R.id.push_pix_rate);
        mBiteView = v.findViewById(R.id.push_bite_rate);
        mFrameView = v.findViewById(R.id.push_frame_rate);

        Spinner pushPixSpinner = v.findViewById(R.id.push_pix_spinner);
        Spinner pushEncodeSpinner = v.findViewById(R.id.push_encode_spinner);
        Spinner pushAudioSpinner = v.findViewById(R.id.push_audio_spinner);

        pushPixSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mSetActivity, R.array.SoVideoItems, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pushPixSpinner.setAdapter(adapter);

        pushEncodeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(mSetActivity, R.array.SoEncodeItems, R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pushEncodeSpinner.setAdapter(adapter2);

        pushAudioSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(mSetActivity, R.array.SoAudioItems, R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pushAudioSpinner.setAdapter(adapter3);

        if (mSetActivity.mPushVideoProfile != 0) {
            mVideoProfile = mVideoProfileManager.getVideoProfile(mSetActivity.mPushVideoProfile);
            pushPixSpinner.setSelection(mVideoProfileManager.mVideoProfiles.indexOf(mVideoProfile));
        } else {
            mPixView.setText(mSetActivity.mPushWidth + "x" + mSetActivity.mPushHeight);
            mBiteView.setText(mSetActivity.mPushBitRate + "");
            mFrameView.setText(mSetActivity.mPushFrameRate + "");
            mPixView.requestFocus();
            pushPixSpinner.setSelection(mVideoProfileManager.mVideoProfiles.size());
        }

        pushEncodeSpinner.setSelection(mSetActivity.mEncodeType);
        pushAudioSpinner.setSelection(mSetActivity.mAudioSRate);
        MyLog.d("PushFragment onCreateView invoked!");
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.d("PushFragment onDestroy invoked!");
        mSetActivity = null;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.push_pix_spinner:
                if (position < mVideoProfileManager.mVideoProfiles.size()) {
                    mVideoProfile = mVideoProfileManager.mVideoProfiles.get(position);
                    mSetActivity.mPushVideoProfile = mVideoProfile.videoProfile;
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
                    mSetActivity.mPushVideoProfile = 0;
                    mPixView.setEnabled(true);
                    mBiteView.setEnabled(true);
                    mFrameView.setEnabled(true);
                    mPixView.setTextColor(Color.parseColor("#FF666666"));
                    mBiteView.setTextColor(Color.parseColor("#FF666666"));
                    mFrameView.setTextColor(Color.parseColor("#FF666666"));
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

    public boolean getParams() {
        if (TextUtils.isEmpty(mPixView.getText())) {
            Toast.makeText(mSetActivity, "自定义推流分辨率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] wh = mPixView.getText().toString().trim().split("x");
        if (wh.length != 2) {
            Toast.makeText(mSetActivity, "自定义推流分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        int width, height, birrate, fps;
        try {
            width = Integer.parseInt(wh[0]);
            if (width <= 0) {
                Toast.makeText(mSetActivity, "自定义推流分辨率宽必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(mSetActivity, "自定义推流分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            height = Integer.parseInt(wh[1]);
            if (height <= 0) {
                Toast.makeText(mSetActivity, "自定义推流分辨率高必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(mSetActivity, "自定义推流分辨率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (width * height > 1920 * 1080) {
            Toast.makeText(mSetActivity, "自定义推流分辨率最大值为1920*1080", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(mBiteView.getText())) {
            Toast.makeText(mSetActivity, "自定义推流码率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            birrate = Integer.parseInt(mBiteView.getText().toString().trim());
            if (birrate <= 0) {
                Toast.makeText(mSetActivity, "自定义推流码率必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(mSetActivity, "自定义推流码率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (birrate > 5000) {
            Toast.makeText(mSetActivity, "自定义推流码率最大值为5000kbps", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(mFrameView.getText())) {
            Toast.makeText(mSetActivity, "自定义推流帧率不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            fps = Integer.parseInt(mFrameView.getText().toString().trim());
            if (fps <= 0) {
                Toast.makeText(mSetActivity, "自定义推流帧率必须大于0，输入正确参数", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(mSetActivity, "自定义推流帧率格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fps > 30) {
            Toast.makeText(mSetActivity, "自定义推流帧率最大值为30", Toast.LENGTH_SHORT).show();
            return false;
        }

        mSetActivity.mPushWidth = width;
        mSetActivity.mPushHeight = height;
        mSetActivity.mPushBitRate = birrate;
        mSetActivity.mPushFrameRate = fps;
        return true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}