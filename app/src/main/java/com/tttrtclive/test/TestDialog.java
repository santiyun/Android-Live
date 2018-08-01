package com.tttrtclive.test;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tttrtclive.LocalConfig;
import com.tttrtclive.R;
import com.tttrtclive.utils.SharedPreferencesUtil;

/**
 * Created by Administrator on 2017-10-11.
 */
public class TestDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private EditText mPushUrl;
    private EditText mPushUrl2;
    private EditText mPort;
    private EditText mIP;
    private String mRoomID;
    public boolean mIsHightVoice = false;

    public TestDialog(@NonNull Context context, String mRoomID) {
        super(context, R.style.NoBackGroundDialog);
        mContext = context;
        this.mRoomID = mRoomID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_dialog_layout);

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = dm.widthPixels;
            getWindow().setAttributes(lp);
        }

        mPushUrl = findViewById(R.id.push_url);
        mPushUrl2 = findViewById(R.id.push_url2);
        mIP = findViewById(R.id.ip);
        mPort = findViewById(R.id.port);
        RadioGroup group = findViewById(R.id.audio_selector_ly);
        RadioButton normal = findViewById(R.id.audio_selector_normal);
        RadioButton high = findViewById(R.id.audio_selector_high);

        Object audioHighQuality = SharedPreferencesUtil.getParam(getContext(), "audioHighQuality", true);
        if (audioHighQuality != null) {
            mIsHightVoice = (boolean) audioHighQuality;
        }

        if (mIsHightVoice) {
            high.setChecked(true);
        } else {
            normal.setChecked(true);
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.audio_selector_high) {
                    mIsHightVoice = true;
                    SharedPreferencesUtil.setParam(getContext() , "audioHighQuality" , true);
                } else {
                    mIsHightVoice = false;
                    SharedPreferencesUtil.setParam(getContext() , "audioHighQuality" , false);
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(this);

        LocalConfig.mExtraPushUrl = "rtmp://39.106.11.15:11935/live/ss";

        setServerParams();
    }

    public void setRoomID(String mRoomID) {
        this.mRoomID = mRoomID;
    }

    public void setServerParams() {
        if (mPort != null && !TextUtils.isEmpty(LocalConfig.mIP)) {
            mIP.setText(LocalConfig.mIP);
        }

        if (mPort != null && LocalConfig.mPort != -1) {
            mPort.setText(String.valueOf(LocalConfig.mPort));
        }

//        if (!TextUtils.isEmpty(LocalConfig.mPushUrl)) {
//            mPushUrl.setText(LocalConfig.mPushUrl);
//        } else {

        if (mPushUrl != null) {
            if (!TextUtils.isEmpty(mRoomID)) {
                String target = LocalConfig.mPushUrlPrefix + mRoomID;
                mPushUrl.setText(target);
            } else {
                mPushUrl.setText(LocalConfig.mPushUrlPrefix);
            }
        }

        if (LocalConfig.mExtraPushUrl != null)
            mPushUrl2.setText(LocalConfig.mExtraPushUrl);

//        }
    }

    public void onOKButtonClick() {
        Editable mPushUrlText = mPushUrl.getText();
        if (!TextUtils.isEmpty(mPushUrlText)) {
            LocalConfig.mPushUrl = mPushUrlText.toString();
        } else {
            LocalConfig.mPushUrl = "";
        }

        Editable mPushUrlText2 = mPushUrl2.getText();
        if (!TextUtils.isEmpty(mPushUrlText2)) {
            LocalConfig.mExtraPushUrl = mPushUrlText2.toString();
        } else {
            LocalConfig.mExtraPushUrl = null;
        }

        Editable mIPText = mIP.getText();
        if (!TextUtils.isEmpty(mIPText)) {
            LocalConfig.mIP = mIPText.toString();
        } else {
            LocalConfig.mIP = "";
        }

        Editable mPortText = mPort.getText();
        if (!TextUtils.isEmpty(mPortText)) {
            LocalConfig.mPort = Integer.valueOf(mPortText.toString());
        } else {
            LocalConfig.mPort = -1;
        }
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        onOKButtonClick();
    }
}
