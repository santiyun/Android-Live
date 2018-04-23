package com.tttrtclive.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.tttrtclive.R;
import com.wushuangtech.wstechapi.internal.TTTRtcUnity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends AppCompatActivity {

    TTTRtcUnity tttRtcUnity;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mImageView = findViewById(R.id.imageView);

        tttRtcUnity = TTTRtcUnity.create(this, "test900572e02867fab8131651339518");
        tttRtcUnity.enableVideo();
//        tttRtcUnity.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);
        tttRtcUnity.joinChannel("", "112233", 1122);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tttRtcUnity.getDeviceBuffer(1122);
//                        mImageView.setImageBitmap(convertToBitmap(tttRtcUnity.getDeviceBuffer(1122)));
                    }
                });
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 5000, 3000);
    }


    private Bitmap convertToBitmap(byte[] ia) {
        int mWidth = tttRtcUnity.getDeviceWidth(1122);
        int mHeight = tttRtcUnity.getDeviceHeight(1122);
        byte[] iat = new byte[mWidth * mHeight * 4];
        for (int i = 0; i < mHeight; i++) {
            System.arraycopy(ia, i * mWidth, iat, (mHeight - i - 1) * mWidth, mWidth);
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(iat));
        File file = new File("/sdcard/PNG/test.png");
        if(file.exists()){
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

}
