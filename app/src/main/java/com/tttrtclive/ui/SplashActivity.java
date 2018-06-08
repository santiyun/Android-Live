package com.tttrtclive.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.Helper.TTTRtcEngineHelper;
import com.tttrtclive.LocalConfig;
import com.tttrtclive.LocalConstans;
import com.tttrtclive.R;
import com.tttrtclive.bean.JniObjs;
import com.tttrtclive.bean.PermissionBean;
import com.tttrtclive.bean.ResolutionManager;
import com.tttrtclive.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.utils.MyLog;
import com.tttrtclive.utils.PermissionUtils;
import com.tttrtclive.utils.SharedPreferencesUtil;
import com.wushuangtech.library.Constants;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;
import java.util.List;

import static com.tttrtclive.LocalConfig.mAudience;
import static com.tttrtclive.LocalConfig.mAuthorSize;
import static com.tttrtclive.LocalConfig.mLoginRoomID;
import static com.tttrtclive.LocalConfig.mPullUrlPrefix;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_FAILED;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;

public class SplashActivity extends BaseActivity {

    private int mRole = -1;
    private Dialog mDialog;
    private boolean mIsLoging;
    private EditText mRoomIDET;
    private RadioButton mHostBT, mAuthorBT, mAudienceBT;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ArrayList<PermissionBean> mPermissionList = new ArrayList<>();
        mPermissionList.add(new PermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.permission_write_external_storage)));
        mPermissionList.add(new PermissionBean(Manifest.permission.CAMERA, getResources().getString(R.string.permission_camera)));
        mPermissionList.add(new PermissionBean(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.permission_record_audio)));
        mPermissionList.add(new PermissionBean(Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_read_phone_state)));
        boolean isOk = PermissionUtils.checkPermission(this, new PermissionUtils.PermissionUtilsInter() {
            @Override
            public List<PermissionBean> getApplyPermissions() {
                return mPermissionList;
            }

            @Override
            public AlertDialog.Builder getTipAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipDialog() {
                return null;
            }

            @Override
            public AlertDialog.Builder getTipAppSettingAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipAppSettingDialog() {
                return null;
            }
        });

        if (isOk) {
            init();
        }

    }

    private void init() {
        initView();
        mTTTRtcEngineHelper = new TTTRtcEngineHelper();
        // 读取保存的数据
        long roomID = (long) SharedPreferencesUtil.getParam(this, "RoomID", 0L);
        // 设置保存的数据
        if (roomID != 0) {
            String s = String.valueOf(roomID);
            mRoomIDET.setText(s);
            mRoomIDET.setSelection(s.length());
        }

        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
    }

    private void initView() {
        mAuthorBT = findViewById(R.id.vice);
        mAudienceBT = findViewById(R.id.audience);
        mRoomIDET = findViewById(R.id.room_id);
        mHostBT = findViewById(R.id.host);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);

        ResolutionManager resolutionManager = new ResolutionManager();
        ArrayList<String> resolutionList = resolutionManager.getList();
        Spinner sp = findViewById(R.id.resolution);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.resolution, resolutionList);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int currentResolution = resolutionManager.getIndex(resolutionList.get(position));
                mTTTEngine.setVideoProfile(currentResolution, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d("SplashActivity onDestroy....");
        TTTRtcEngine.destroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void onClickRoleButton(View v) {
        mHostBT.setChecked(false);
        mAuthorBT.setChecked(false);
        mAudienceBT.setChecked(false);

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.host:
                mRole = CLIENT_ROLE_ANCHOR;
                break;
            case R.id.vice:
                mRole = CLIENT_ROLE_BROADCASTER;
                break;
            case R.id.audience:
                mRole = CLIENT_ROLE_AUDIENCE;
                break;
        }
    }


    public void onClickEnterButton(View v) {
        boolean checkResult = mTTTRtcEngineHelper.splashCheckSetting(this, mRoomIDET.getText().toString());
        if (!checkResult) {
            return;
        }

        // 重置直播房间内的参数
        mAudience = 0;
        mAuthorSize = 0;

        // 设置频道属性
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        // 启用视频模式
        mTTTEngine.enableVideo();
        mTTTEngine.setHighQualityAudioParameters(true);
        // 设置进入直播房间的角色
        if (mRole == -1) {
            if (mHostBT.isChecked()) {
                mRole = CLIENT_ROLE_ANCHOR;
            } else if (mAuthorBT.isChecked()) {
                mRole = CLIENT_ROLE_BROADCASTER;
            } else if (mAudienceBT.isChecked()) {
                mRole = CLIENT_ROLE_AUDIENCE;
            }
        }
        LocalConfig.mRole = mRole;
        mTTTEngine.setClientRole(LocalConfig.mRole, null);
        // 拉流地址
        LocalConfig.mCDNAddress = mPullUrlPrefix + mLoginRoomID;
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", LocalConfig.mLoginRoomID);
        if (mIsLoging) {
            return;
        }
        mIsLoging = true;
        showWaittingDialog();
        new Thread(() -> {
            PviewLog.testPrint("joinChannel" , "begin");
            int result = mTTTEngine.joinChannel("", String.valueOf(LocalConfig.mLoginRoomID), LocalConfig.mLoginUserID);
            MyLog.d("joinChannel result : " + result +
                    " | Room ID : " + LocalConfig.mLoginRoomID + " | User ID : " + LocalConfig.mLoginUserID);
        }).start();

    }

    private void showWaittingDialog() {
        if (mDialog == null) {
            mDialog = ProgressDialog.show(this, "", "正在进入房间...");
        } else {
            mDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isOk = PermissionUtils.onRequestPermissionsResults(this, requestCode, permissions, grantResults);
        if (isOk) {
            init();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean isOk = PermissionUtils.onActivityResults(this, requestCode);
        if (isOk) {
            init();
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(
                        MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        mIsLoging = false;
                        //界面跳转
                        startActivity(new Intent(mContext, MainActivity.class));
                        PviewLog.testPrint("joinChannel" , "end");
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mIsLoging = false;
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        final int errorType = mJniObjs.mErrorType;
                        runOnUiThread(() -> {
                            MyLog.d("onReceive CALL_BACK_ON_ERROR errorType : " + errorType);
                            if (errorType == ERROR_ENTER_ROOM_TIMEOUT) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_FAILED) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_unconnect), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_VERIFY_FAILED) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_BAD_VERSION) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_noroom), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        }
    }

}
