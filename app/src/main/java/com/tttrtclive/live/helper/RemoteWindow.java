package com.tttrtclive.live.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tttrtclive.live.R;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import androidx.constraintlayout.widget.ConstraintLayout;

public class RemoteWindow extends RelativeLayout {

    private TTTRtcEngine mTTTEngine;
    private Context mContext;

    public long mId = -1;
    public long mLocalUid;
    private boolean mIsMuted;
    private boolean mLocalIsMuted = false;

    private ImageView mSpeakImage;
    private ImageView mCameraImage;
    private TextView mIdView;
    private TextView mAudioBitrate;
    private TextView mVideoBitrate;
    private ConstraintLayout mVideoLayout;
    public int mIndex;

    public RemoteWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        View v = LayoutInflater.from(context).inflate(R.layout.audio_remote_window, this, true);
        mSpeakImage = v.findViewById(R.id.speakimage);
        mCameraImage = v.findViewById(R.id.remote_btn_switch_camera);
        mIdView = v.findViewById(R.id.id);
        mAudioBitrate = v.findViewById(R.id.audiorate);
        mVideoBitrate = v.findViewById(R.id.videorate);
        mVideoLayout = v.findViewById(R.id.video_layout);

        mTTTEngine = TTTRtcEngine.getInstance();
    }

    public void show(long id) {
        show(-1, id, 0);
    }

    public void show(long localId, long id, int oritation) {
        mId = id;
        mLocalUid = localId;
        mIdView.setText("" + mId);
        mSpeakImage.setVisibility(View.VISIBLE);
        mIdView.setVisibility(View.VISIBLE);
        mAudioBitrate.setVisibility(View.VISIBLE);
        mVideoBitrate.setVisibility(View.VISIBLE);

        if (localId == id) {
            mSpeakImage.setOnClickListener(v -> {
                if (localId == id) {
                    mLocalIsMuted = !mLocalIsMuted;
                    mSpeakImage.setImageResource(mLocalIsMuted ? R.drawable.mainly_btn_mute_speaker_selector : R.drawable.mainly_btn_speaker_selector);
                    mTTTEngine.muteLocalAudioStream(mLocalIsMuted);
                }
            });
            mCameraImage.setVisibility(View.VISIBLE);
            mCameraImage.setOnClickListener(v -> {
                if (localId == id) {
                    mTTTEngine.switchCamera();
                }
            });
        }

        SurfaceView surfaceView;
        if (localId == id) {
            surfaceView = TTTRtcEngine.CreateRendererSurfaceView(mContext);
            surfaceView.setZOrderMediaOverlay(false);
            mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN, surfaceView), oritation);
        } else {
            surfaceView = TTTRtcEngine.CreateRendererView(mContext);
            surfaceView.setZOrderMediaOverlay(true);
            mTTTEngine.setupRemoteVideo(new VideoCanvas(mId, Constants.RENDER_MODE_HIDDEN, surfaceView));
        }
        mVideoLayout.addView(surfaceView);
        mSpeakImage.setImageResource(mIsMuted ? R.drawable.jinyan : R.drawable.mainly_btn_speaker_selector);
    }

    public void hide() {
        mId = -1;
        mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_selector);
        mSpeakImage.setVisibility(View.INVISIBLE);
        mCameraImage.setVisibility(View.INVISIBLE);
        mIdView.setVisibility(View.INVISIBLE);
        mAudioBitrate.setVisibility(View.INVISIBLE);
        mVideoBitrate.setVisibility(View.INVISIBLE);
        mVideoLayout.removeAllViews();
    }

    public void mute(boolean mute) {
        mIsMuted = mute;
        mSpeakImage.setImageResource(mute ? R.drawable.jinyan : R.drawable.mainly_btn_speaker_selector);
    }

    public void updateAudioBitrate(String bitrate) {
        mAudioBitrate.setText(bitrate);
    }

    public void updateVideoBitrate(String bitrate) {
        mVideoBitrate.setText(bitrate);
    }

    public void updateSpeakState(int volumeLevel) {
        if (mIsMuted) return;

        if (volumeLevel >= 0 && volumeLevel <= 3) {
            mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_selector);
        } else if (volumeLevel > 3 && volumeLevel <= 6) {
            mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_middle_selector);
        } else if (volumeLevel > 6 && volumeLevel <= 9) {
            mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_big_selector);
        }
    }

}
