package com.tttrtclive.live.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.wushuangtech.utils.PviewLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    // 系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("MM-dd-HH:mm:ss:SSS", Locale.getDefault());

    /**
     * 保证只有一个CrashHandler实例
     */
    public CrashHandler(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 初始化
     */
    public void init() {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                PviewLog.e(TAG, "error : " + e.getLocalizedMessage());
            }
            // 退出程序
            System.exit(0);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序出现异常,即将退出", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
//        saveCrashInfoToFile(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            PviewLog.e(TAG, "collectDeviceInfo an error occured when collect package info : " + e.getLocalizedMessage());
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                PviewLog.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                PviewLog.e(TAG, "collectDeviceInfo an error occured when collect crash info : " + e.getLocalizedMessage());
            }
        }
    }

    private void saveCrashInfoToFile(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File externalFilesDir = mContext.getExternalFilesDir(null);
                String mSavePathRootDir;
                if (externalFilesDir != null && externalFilesDir.exists()) {
                    mSavePathRootDir = externalFilesDir.getAbsolutePath();
                } else {
                    mSavePathRootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                String path = mSavePathRootDir + File.separator + "TTTRtcEngineCrash" + File.separator;
                PviewLog.e(TAG, "save path : " + path);
                File dir = new File(path);
                if (!dir.exists()) {
                    boolean mkdirs = dir.mkdirs();
                    if (!mkdirs) {
                        return;
                    }
                }

                File desFile = new File(path + fileName);
                if (!desFile.exists()) {
                    boolean newFile = desFile.createNewFile();
                    if (!newFile) {
                        return;
                    }
                }

                FileOutputStream fos = new FileOutputStream(desFile);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        } catch (Exception e) {
            PviewLog.e(TAG, "saveCrashInfoToFile an error occured while writing file... : " + e.getLocalizedMessage());
        }
    }
}