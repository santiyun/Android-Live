package com.tttrtclive.live.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.tttrtclive.live.bean.MyPermissionBean;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by wangzhiguo on 18/3/5.
 */
public class MyPermissionManager {

    private PermissionUtilsInter mPermissionUtilsInter;
    public static final int REQUEST_PERMISSION_CODE = 321;
    public static final int REQUEST_SETTING_CODE = 123;
    private Activity mActivity;

    private AlertDialog mTipDialog;
    private AlertDialog mAskDialog;

    public MyPermissionManager(Activity mActivity, PermissionUtilsInter Inter) {
        this.mActivity = mActivity;
        this.mPermissionUtilsInter = Inter;
    }

    public void clearResource(){
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }

        if (mAskDialog != null) {
            mAskDialog.dismiss();
        }
    }

    /**
     * 开始检查权限
     */
    public boolean checkPermission() {
        List<MyPermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
        List<MyPermissionBean> mNeedApply = new ArrayList<>();
        for (int i = 0; i < mPermissionList.size(); i++) {
            MyPermissionBean permissionBean = mPermissionList.get(i);
            int state = ContextCompat.checkSelfPermission(mActivity, permissionBean.mPermissionName);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (state != PackageManager.PERMISSION_GRANTED) {
                mNeedApply.add(permissionBean);
            }
        }

        if (mNeedApply.size() > 0) {
            // 如果没有授予该权限，就去提示用户请求
            showDialogTipUserRequestPermission(mNeedApply);
            return false;
        }
        return true;
    }

    public boolean onRequestPermissionsResults(final Activity mContext, int requestCode,
                                                      @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<MyPermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
                List<MyPermissionBean> mApplys = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean isGoToSetting = false;
                boolean mIsOk = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        mIsOk = false;
                        boolean b = mContext.shouldShowRequestPermissionRationale(permissions[i]);
                        if (!b) {
                            isGoToSetting = true;
                            sb.append(permissions[i]).append("\n").append("没有被同意").append("\n");
                        } else {
                            for (int j = 0; j < mPermissionList.size(); j++) {
                                if (mPermissionList.get(j).mPermissionName.equals(permissions[i])) {
                                    mApplys.add(mPermissionList.get(j));
                                    break;
                                }
                            }
                        }
                    }
                }

                if (mApplys.size() > 0) {
                    showDialogTipUserRequestPermission(mApplys);
                } else {
                    if (isGoToSetting) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting(sb.toString());
                    }
                }
                return mIsOk;
            }
        }
        return true;
    }

    public boolean onActivityResults(int requestCode) {
        if (requestCode == REQUEST_SETTING_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<MyPermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
                List<MyPermissionBean> mApplys = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean isGoToSetting = false;
                boolean mIsOk = true;
                for (int i = 0; i < mPermissionList.size(); i++) {
                    MyPermissionBean permissionBean = mPermissionList.get(i);
                    // 检查该权限是否已经获取
                    int result = ContextCompat.checkSelfPermission(mActivity, permissionBean.mPermissionName);
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mIsOk = false;
                        boolean b = mActivity.shouldShowRequestPermissionRationale(permissionBean.mPermissionName);
                        if (!b) {
                            isGoToSetting = true;
                            sb.append(permissionBean.mPermissionName).append("没有被同意").append("\n");
                        } else {
                            mApplys.add(permissionBean);
                        }
                    }
                }

                if (mApplys.size() > 0) {
                    showDialogTipUserRequestPermission(mApplys);
                } else {
                    if (isGoToSetting) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting(sb.toString());
                    }
                }
                return mIsOk;
            }
        }
        return true;
    }

    private void showDialogTipUserGoToAppSettting(String message) {
        androidx.appcompat.app.AlertDialog.Builder tipAlertDialog = mPermissionUtilsInter.getTipAppSettingAlertDialog();
        if (tipAlertDialog != null) {
            tipAlertDialog.show();
            return;
        }

        Dialog tipDialog = mPermissionUtilsInter.getTipAppSettingDialog();
        if (tipDialog != null) {
            tipDialog.show();
            return;
        }

        if (mAskDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("权限不可用")
                    .setMessage(message + "\n 请在-应用设置-权限-中,将以上权限打开.")
                    .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转到应用设置界面
                            goToAppSetting();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    }).setCancelable(false);
            mAskDialog = builder.create();
        }

        if (!mActivity.isFinishing() && !mAskDialog.isShowing()) {
            mAskDialog.show();
        } else {
            mAskDialog.dismiss();
        }
    }

    private void showDialogTipUserRequestPermission(final List<MyPermissionBean> mNeedApply) {
        String message;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mNeedApply.size(); i++) {
            MyPermissionBean permissionBean = mNeedApply.get(i);
            sb.append(permissionBean.mPermissionName).append("\n").append(permissionBean.mPermissionReason).append("\n");
        }
        message = sb.toString();
        androidx.appcompat.app.AlertDialog.Builder tipAlertDialog = mPermissionUtilsInter.getTipAlertDialog();
        if (tipAlertDialog != null) {
            tipAlertDialog.show();
            return;
        }

        Dialog tipDialog = mPermissionUtilsInter.getTipDialog();
        if (tipDialog != null) {
            tipDialog.show();
            return;
        }

        if (mTipDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle("权限申请")
                    .setMessage(message)
                    .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startRequestPermission(mActivity, mNeedApply);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    }).setCancelable(false);
            mTipDialog = builder.create();
        }

        if (!mActivity.isFinishing() && !mTipDialog.isShowing()) {
            mTipDialog.show();
        } else {
            mTipDialog.dismiss();
        }
    }

    /**
     * 开始提交请求权限
     */
    private void startRequestPermission(Activity mContext, List<MyPermissionBean> mNeedApply) {
        String[] mTemps = new String[mNeedApply.size()];
        for (int i = 0; i < mNeedApply.size(); i++) {
            MyPermissionBean permissionBean = mNeedApply.get(i);
            mTemps[i] = permissionBean.mPermissionName;
        }
        ActivityCompat.requestPermissions(mContext, mTemps, REQUEST_PERMISSION_CODE);
    }

    /**
     * 跳转到当前应用的设置界面
     *
     */
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
        intent.setData(uri);
        mActivity.startActivityForResult(intent, REQUEST_SETTING_CODE);
    }

    public interface PermissionUtilsInter {

        List<MyPermissionBean> getApplyPermissions();

        androidx.appcompat.app.AlertDialog.Builder getTipAlertDialog();

        Dialog getTipDialog();

        androidx.appcompat.app.AlertDialog.Builder getTipAppSettingAlertDialog();

        Dialog getTipAppSettingDialog();
    }
}


