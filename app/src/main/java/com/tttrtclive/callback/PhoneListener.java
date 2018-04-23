package com.tttrtclive.callback;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.tttrtclive.bean.JniObjs;
import com.tttrtclive.utils.MyLog;

import static com.tttrtclive.LocalConstans.CALL_BACK_ON_PHONE_LISTENER_COME;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_PHONE_LISTENER_IDLE;

public class PhoneListener extends PhoneStateListener {
    private Context mContext;

    public PhoneListener(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        try {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   //来电
                    MyLog.d("来电话了 : " + incomingNumber);

                    JniObjs mJniObjs = new JniObjs();
                    mJniObjs.mJniType = CALL_BACK_ON_PHONE_LISTENER_COME;

                    Intent i = new Intent();
                    i.setAction(MyTTTRtcEngineEventHandler.TAG);
                    i.putExtra(MyTTTRtcEngineEventHandler.MSG_TAG, mJniObjs);
                    mContext.sendBroadcast(i);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:  //挂掉电话
                    MyLog.d("挂掉电话 : " + incomingNumber);

                    JniObjs mJniObjs1 = new JniObjs();
                    mJniObjs1.mJniType = CALL_BACK_ON_PHONE_LISTENER_IDLE;

                    Intent i1 = new Intent();
                    i1.setAction(MyTTTRtcEngineEventHandler.TAG);
                    i1.putExtra(MyTTTRtcEngineEventHandler.MSG_TAG, mJniObjs1);
                    mContext.sendBroadcast(i1);
                    break;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}