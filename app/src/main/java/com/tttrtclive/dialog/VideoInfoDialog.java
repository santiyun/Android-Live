package com.tttrtclive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tttrtclive.R;
import com.wushuangtech.wstechapi.model.Size;

import static com.wushuangtech.library.Constants.VIDEO_PROFILE_1080P;
import static com.wushuangtech.library.Constants.VIDEO_PROFILE_240P;
import static com.wushuangtech.library.Constants.VIDEO_PROFILE_480P;

/**
 * Created by wangzhiguo on 17/10/23.
 */

public class VideoInfoDialog extends Dialog {

    private RadioButton r320x240, r640x480, r1280x720;
    private RadioButton fps10, fps15, fps30;
    private RadioButton h264, h265;
    private RadioButton s48k, d44k;
    public RadioButton okButton;
    public int mWidth = -1;
    public int mHeight = -1;
    public int mCodingFormat = 1;
    public int mFrameRate = 15;
    public int mBitRate = 500 * 1000;
    public int mAudioBitRate = 64;
    public int mSamplingRate = 48000;
    public int mChannels = 1;

    public VideoInfoDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.sp_videoinfo_dialog);

        r320x240 = findViewById(R.id.r320_240);
        r640x480 = findViewById(R.id.r640_480);
        r1280x720 = findViewById(R.id.r1280_720);

        fps10 = findViewById(R.id.fps10);
        fps15 = findViewById(R.id.fps15);
        fps30 = findViewById(R.id.fps30);

        h264 = findViewById(R.id.H264);
        h265 = findViewById(R.id.H265);

        s48k = findViewById(R.id.s48k);
        d44k = findViewById(R.id.d44k);

        okButton = findViewById(R.id.okButton);

        r320x240.setOnClickListener(onSelectResolutionClick);
        r640x480.setOnClickListener(onSelectResolutionClick);
        r1280x720.setOnClickListener(onSelectResolutionClick);

        fps10.setOnClickListener(onSelectFrameRateClick);
        fps15.setOnClickListener(onSelectFrameRateClick);
        fps30.setOnClickListener(onSelectFrameRateClick);

        h264.setOnClickListener(onSelectCodingFormatClick);
        h265.setOnClickListener(onSelectCodingFormatClick);

        s48k.setOnClickListener(onSelectSamplingRateClick);
        d44k.setOnClickListener(onSelectSamplingRateClick);

        mWidth = 480;
        mHeight = 640;
    }

    public View.OnClickListener onSelectResolutionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            r320x240.setChecked(false);
            r640x480.setChecked(false);
            r1280x720.setChecked(false);
            ((RadioButton)v).setChecked(true);
            switch (v.getId()) {
                case R.id.r320_240:
                    mWidth = 240;
                    mHeight = 320;
                    mBitRate = 200;
                    break;
                case R.id.r640_480:
                    mWidth = 480;
                    mHeight = 640;
                    mBitRate = 500;
                    break;
                case R.id.r1280_720:
                    mWidth = 720;
                    mHeight = 1280;
                    mBitRate = 1130;
                    break;
            }
        }
    };

    public View.OnClickListener onSelectFrameRateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fps10.setChecked(false);
            fps15.setChecked(false);
            fps30.setChecked(false);
            ((RadioButton)v).setChecked(true);
            switch (v.getId()) {
                case R.id.fps10:
                    mFrameRate = 10;
                    break;
                case R.id.fps15:
                    mFrameRate = 15;
                    break;
                case R.id.fps30:
                    mFrameRate = 30;
                    break;
            }
        }
    };

    public View.OnClickListener onSelectCodingFormatClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            h264.setChecked(false);
            h265.setChecked(false);
            ((RadioButton)v).setChecked(true);
            switch (v.getId()) {
                case R.id.H264:
                    mCodingFormat = 1;
                    break;
                case R.id.H265:
                    mCodingFormat = 0;
                    break;
            }
        }
    };

    public View.OnClickListener onSelectSamplingRateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            s48k.setChecked(false);
            d44k.setChecked(false);
            ((RadioButton)v).setChecked(true);
            switch (v.getId()) {
                case R.id.s48k:
                    mSamplingRate = 48000;
                    mChannels = 1;
                    break;
                case R.id.d44k:
                    mSamplingRate = 44100;
                    mChannels = 2;
                    break;
            }
        }
    };

}
