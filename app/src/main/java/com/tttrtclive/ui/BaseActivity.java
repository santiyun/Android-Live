package com.tttrtclive.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.jaeger.library.StatusBarUtil;
import com.wushuangtech.wstechapi.TTTRtcEngine;

/**
 * Created by wangzhiguo on 17/10/12.
 */

public class BaseActivity extends Activity {

    protected TTTRtcEngine mTTTEngine;
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //状态栏透明
        StatusBarUtil.setTranslucent(this);
        //获取上下文
        mContext = this;
        //获取SDK实例对象
        mTTTEngine = TTTRtcEngine.getInstance();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
