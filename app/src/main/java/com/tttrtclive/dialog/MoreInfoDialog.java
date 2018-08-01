package com.tttrtclive.dialog;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.Helper.TTTRtcEngineHelper;
import com.tttrtclive.LocalConfig;
import com.tttrtclive.R;
import com.tttrtclive.bean.EnterUserInfo;
import com.tttrtclive.ui.MainActivity;
import com.tttrtclive.ui.SplashActivity;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import static com.tttrtclive.LocalConfig.mAudience;
import static com.tttrtclive.LocalConfig.mAuthorSize;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;

/**
 * Created by wangzhiguo on 17/10/11.
 */

public class MoreInfoDialog extends Dialog implements View.OnClickListener {

    private MainActivity mContext;
    private TTTRtcEngine mTTTEngine;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;

    private ImageView mRecordScreenShare;
    private ImageView mRecordScreenBT;
    private ImageView mEarsBack;
    private ImageView mSwitchRole;
    private ImageView mControlShowInfo;
    private DataInfoShowCallback mDataInfoShowCallback;
    private TextView mWatchViewrs;
    private TextView mAuthorViewrs;
    public boolean mIsSwitching = false;

    // 屏幕分享和屏幕录制标志位 两个功能只能开一个
    private boolean mEnableScreenRecord = false;

    public MoreInfoDialog(Context context, int theme, TTTRtcEngine engine, TTTRtcEngineHelper engineHelper) {
        super(context, theme);

        mContext = (MainActivity) context;
        mTTTEngine = engine;
        mTTTRtcEngineHelper = engineHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainly_more_dialog);
        EditText mCdnEdit = findViewById(R.id.mainly_dialog_cdn);

        mRecordScreenShare = findViewById(R.id.mainly_dialog_share);
        mRecordScreenShare.setOnClickListener(this);

        mRecordScreenBT = findViewById(R.id.mainly_dialog_recard);
        mRecordScreenBT.setOnClickListener(this);

        mEarsBack = findViewById(R.id.mainly_dialog_earback);
        mEarsBack.setOnClickListener(this);

        mSwitchRole = findViewById(R.id.mainly_dialog_switch_role);
        mSwitchRole.setOnClickListener(this);

        mControlShowInfo = findViewById(R.id.mainly_dialog_show_info);
        mControlShowInfo.setOnClickListener(this);

        mRecordScreenShare.setSelected(false);
        mRecordScreenBT.setSelected(false);
        mEarsBack.setSelected(false);
        mSwitchRole.setSelected(false);
        mControlShowInfo.setSelected(true);

        if (LocalConfig.mRole != Constants.CLIENT_ROLE_ANCHOR) {
            findViewById(R.id.mainly_dialog_earback_textview).setVisibility(View.GONE);
            mEarsBack.setVisibility(View.GONE);
        }

        if (LocalConfig.mRole != Constants.CLIENT_ROLE_ANCHOR || LocalConfig.mRoomMode == SplashActivity.AUDIO_MODE) {
            findViewById(R.id.mainly_dialog_share_textview).setVisibility(View.GONE);
            mRecordScreenShare.setVisibility(View.GONE);
            findViewById(R.id.mainly_dialog_recard_textview).setVisibility(View.GONE);
            mRecordScreenBT.setVisibility(View.GONE);

            if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                mSwitchRole.setSelected(true);
                mSwitchRole.setImageResource(R.drawable.dakai);
            }
        }

        if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
            findViewById(R.id.mainly_dialog_switch_role_textview).setVisibility(View.GONE);
            mSwitchRole.setVisibility(View.GONE);
        }

        if (LocalConfig.mCurrentAudioRoute != Constants.AUDIO_ROUTE_SPEAKER) {
            mEarsBack.setClickable(true);
        } else {
            mEarsBack.setClickable(false);
        }

        findViewById(R.id.mainly_dialog_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(LocalConfig.mCDNAddress);
            }
        });

        mCdnEdit.setText(LocalConfig.mCDNAddress);
        mCdnEdit.setTextIsSelectable(true);
        mCdnEdit.setSelection(mCdnEdit.length());

        mWatchViewrs = findViewById(R.id.main_btn_viewers);
        mAuthorViewrs = findViewById(R.id.main_btn_secondAnchor);
        setTextViewContent(mWatchViewrs, R.string.main_viewer, String.valueOf(mAudience));
        setTextViewContent(mAuthorViewrs, R.string.main_secondAnchor, String.valueOf(mAuthorSize));
    }

    public void updatePersonNum() {
        setTextViewContent(mWatchViewrs, R.string.main_viewer, String.valueOf(mAudience));
        setTextViewContent(mAuthorViewrs, R.string.main_secondAnchor, String.valueOf(mAuthorSize));
    }

    public void setDataInfoShowCallback(DataInfoShowCallback mDataInfoShowCallback) {
        this.mDataInfoShowCallback = mDataInfoShowCallback;
    }

    private void setTextViewContent(TextView textView, int resourceID, String value) {
        String string = this.getContext().getResources().getString(resourceID);
        String result = String.format(string, value);
        if (textView != null)
            textView.setText(result);
    }

    @Override
    public void show() {
        super.show();
        setTextViewContent(mWatchViewrs, R.string.main_viewer, String.valueOf(mAudience));
        setTextViewContent(mAuthorViewrs, R.string.main_secondAnchor, String.valueOf(mAuthorSize));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainly_dialog_share:
                if (mTTTRtcEngineHelper.mIsRecordering) {
                    Toast.makeText(mContext, "请先关闭屏幕录制！！！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.sys_support_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mTTTRtcEngineHelper.mIsShareRecordering) {
                    mTTTRtcEngineHelper.mIsShareRecordering = false;
                    mTTTEngine.shareScreenRecorder(false);
                    mTTTEngine.stopRecordScreen();
                } else {
                    mTTTEngine.shareScreenRecorder(true);
                    mTTTEngine.tryRecordScreen(mContext);
                    mTTTRtcEngineHelper.mFlagRecord = TTTRtcEngineHelper.RECORD_TYPE_SHARE;
                }
                break;
            case R.id.mainly_dialog_recard:
                if (mTTTRtcEngineHelper.mIsShareRecordering) {
                    Toast.makeText(mContext, "请先关闭屏幕分享！！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.sys_support_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                startScreenRecord();
                break;
            case R.id.mainly_dialog_earback:
                mTTTEngine.enableEarsBack(!v.isSelected());
                break;
            case R.id.mainly_dialog_switch_role:
                if (mIsSwitching) return;
                mIsSwitching = true;
                if (LocalConfig.mRole == CLIENT_ROLE_BROADCASTER) {
                    mContext.removeListData(LocalConfig.mLoginUserID);
                    mContext.switchRole(mContext.getResources().getString(R.string.welcome_audience));
                    mTTTEngine.setClientRole(CLIENT_ROLE_AUDIENCE, null);
                    mAudience++;
                    mAuthorSize--;
                } else {
                    if (mAuthorSize >= 6) return;
                    mContext.addListData(new EnterUserInfo(LocalConfig.mLoginUserID, CLIENT_ROLE_BROADCASTER));
                    mContext.switchRole(mContext.getResources().getString(R.string.welcome_auxiliary));
                    mTTTEngine.setClientRole(CLIENT_ROLE_BROADCASTER, null);
                    mAudience--;
                    mAuthorSize++;
                }
                updatePersonNum();
                break;
            case R.id.mainly_dialog_show_info:
                if (mDataInfoShowCallback != null)
                    mDataInfoShowCallback.showDataInfo(mControlShowInfo.isSelected() ? false : true);
                break;
        }
        ((ImageView) v).setImageResource(v.isSelected() ? R.drawable.guanbi : R.drawable.dakai);
        v.setSelected(!v.isSelected());
    }

    private void startScreenRecord() {
        if (mTTTRtcEngineHelper.mIsRecordering) {
            mTTTRtcEngineHelper.mIsRecordering = false;
            mTTTEngine.stopRecordScreen();
            mContext.mRecordScreen.setVisibility(View.GONE);
            mContext.mRecordScreen.setText("00:00:00");
        } else {
            mTTTRtcEngineHelper.mFlagRecord = TTTRtcEngineHelper.RECORD_TYPE_FILE;
            mTTTEngine.tryRecordScreen(mContext);
        }
    }

    public void audioRouteChange(int audioRoute) {
        if (mEarsBack == null) return;
        if (audioRoute != 0) {
            mEarsBack.setSelected(false);
            mEarsBack.setImageResource(R.drawable.guanbi);
            mTTTEngine.enableEarsBack(false);
            mEarsBack.setClickable(false);
        } else {
            mEarsBack.setClickable(true);
        }
    }

    public void closeShareScreen() {
        mRecordScreenShare.setSelected(false);
        mRecordScreenShare.setImageResource(R.drawable.guanbi);
        mRecordScreenBT.setSelected(false);
        mRecordScreenBT.setImageResource(R.drawable.guanbi);
    }

    public void changeRoleSwitch() {
        if (mSwitchRole == null) {
            return ;
        }
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            mSwitchRole.setSelected(true);
            mSwitchRole.setImageResource(R.drawable.dakai);
        } else {
            mSwitchRole.setSelected(false);
            mSwitchRole.setImageResource(R.drawable.guanbi);
        }
    }
}
