package com.tttrtclive.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.tttrtclive.bean.PermissionBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhiguo on 18/3/5.
 */

public class PermissionUtils {

    private static PermissionUtilsInter mPermissionUtilsInter;
    public static final int REQUEST_PERMISSION_CODE = 321;
    public static final int REQUEST_SETTING_CODE = 123;

    /**
     * 开始检查权限
     *
     * @param mContext
     * @param Inter
     */
    public static boolean checkPermission(Context mContext, PermissionUtilsInter Inter) {
        mPermissionUtilsInter = Inter;
        List<PermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
        List<PermissionBean> mNeedApply = new ArrayList<>();
        for (int i = 0; i < mPermissionList.size(); i++) {
            PermissionBean permissionBean = mPermissionList.get(i);
            int state = ContextCompat.checkSelfPermission(mContext, permissionBean.mPermissionName);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (state != PackageManager.PERMISSION_GRANTED) {
                mNeedApply.add(permissionBean);
            }
        }

        if (mNeedApply.size() > 0) {
            // 如果没有授予该权限，就去提示用户请求
            showDialogTipUserRequestPermission(mContext, mNeedApply);
            return false;
        }
        return true;
    }

    /**
     * 开始提交请求权限
     *
     * @param mContext
     * @param mNeedApply
     */
    public static void startRequestPermission(Activity mContext, List<PermissionBean> mNeedApply) {
        String[] mTemps = new String[mNeedApply.size()];
        for (int i = 0; i < mNeedApply.size(); i++) {
            PermissionBean permissionBean = mNeedApply.get(i);
            mTemps[i] = permissionBean.mPermissionName;
        }
        ActivityCompat.requestPermissions(mContext, mTemps, REQUEST_PERMISSION_CODE);
    }

    public static boolean onRequestPermissionsResults(final Activity mContext, int requestCode,
                                                      @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<PermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
                List<PermissionBean> mApplys = new ArrayList<>();
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
                    showDialogTipUserRequestPermission(mContext, mApplys);
                } else {
                    if (isGoToSetting) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting(mContext, sb.toString());
                    }
                }
                return mIsOk;
            }
        }
        return true;
    }

    public static boolean onActivityResults(final Activity mContext, int requestCode) {
        if (requestCode == REQUEST_SETTING_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<PermissionBean> mPermissionList = mPermissionUtilsInter.getApplyPermissions();
                List<PermissionBean> mApplys = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean isGoToSetting = false;
                boolean mIsOk = true;
                for (int i = 0; i < mPermissionList.size(); i++) {
                    PermissionBean permissionBean = mPermissionList.get(i);
                    // 检查该权限是否已经获取
                    int result = ContextCompat.checkSelfPermission(mContext, permissionBean.mPermissionName);
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mIsOk = false;
                        boolean b = mContext.shouldShowRequestPermissionRationale(permissionBean.mPermissionName);
                        if (!b) {
                            isGoToSetting = true;
                            sb.append(permissionBean.mPermissionName).append("没有被同意").append("\n");
                        } else {
                            mApplys.add(permissionBean);
                        }
                    }
                }

                if (mApplys.size() > 0) {
                    showDialogTipUserRequestPermission(mContext, mApplys);
                } else {
                    if (isGoToSetting) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting(mContext, sb.toString());
                    }
                }
                return mIsOk;
            }
        }
        return true;
    }

    /**
     * 跳转到当前应用的设置界面
     *
     * @param mContext
     */
    private static void goToAppSetting(Activity mContext) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);
        mContext.startActivityForResult(intent, REQUEST_SETTING_CODE);
    }

    private static void showDialogTipUserGoToAppSettting(final Activity mContext, String message) {
        AlertDialog.Builder tipAlertDialog = mPermissionUtilsInter.getTipAppSettingAlertDialog();
        if (tipAlertDialog != null) {
            tipAlertDialog.show();
            return;
        }

        Dialog tipDialog = mPermissionUtilsInter.getTipAppSettingDialog();
        if (tipDialog != null) {
            tipDialog.show();
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("权限不可用")
                .setMessage(message + "\n 请在-应用设置-权限-中,将以上权限打开.")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting(mContext);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.finish();
                    }
                }).setCancelable(false).show();
    }

    private static void showDialogTipUserRequestPermission(final Context mContext, final List<PermissionBean> mNeedApply) {
        String message;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mNeedApply.size(); i++) {
            PermissionBean permissionBean = mNeedApply.get(i);
            sb.append(permissionBean.mPermissionName).append("\n").append(permissionBean.mPermissionReason).append("\n");
        }
        message = sb.toString();
        AlertDialog.Builder tipAlertDialog = mPermissionUtilsInter.getTipAlertDialog();
        if (tipAlertDialog != null) {
            tipAlertDialog.show();
            return;
        }

        Dialog tipDialog = mPermissionUtilsInter.getTipDialog();
        if (tipDialog != null) {
            tipDialog.show();
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle("权限申请")
                .setMessage(message)
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission((Activity) mContext, mNeedApply);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) mContext).finish();
                    }
                }).setCancelable(false).show();
    }

    public interface PermissionUtilsInter {

        List<PermissionBean> getApplyPermissions();

        AlertDialog.Builder getTipAlertDialog();

        Dialog getTipDialog();

        AlertDialog.Builder getTipAppSettingAlertDialog();

        Dialog getTipAppSettingDialog();
    }
}


