package com.tttrtclive.Helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.LocalConfig;
import com.tttrtclive.R;
import com.tttrtclive.bean.DisplayDevice;
import com.tttrtclive.bean.EnterUserInfo;
import com.tttrtclive.bean.VideoViewObj;
import com.tttrtclive.ui.MainActivity;
import com.tttrtclive.ui.SplashActivity;
import com.tttrtclive.utils.DensityUtils;
import com.tttrtclive.utils.MyLog;
import com.wushuangtech.bean.LocalAudioStats;
import com.wushuangtech.bean.LocalVideoStats;
import com.wushuangtech.bean.RemoteAudioStats;
import com.wushuangtech.bean.RemoteVideoStats;
import com.wushuangtech.bean.ScreenRecordConfig;
import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.library.Constants;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangzhiguo on 17/11/21.
 */

public class TTTRtcEngineHelper {

    public static final int AUTHOR_MAX_NUM = 6;
    public static final int VOLUME_MAX_NUM = 9;

    public static final int RECORD_TYPE_FILE = 10;
    public static final int RECORD_TYPE_SHARE = 11;

    private MainActivity mActivity;
    private TTTRtcEngine mTTTEngine;
    private int mScreenWidth;
    private int mScreenHeight;

    public LongSparseArray<Long> mRemoteUserLastVideoData;
    public LongSparseArray<Long> mRemoteUserLastAudioData;

    private ImageView mRemoteDialogBT;
    private View mRemoteDialog;
    private AlertDialog.Builder mErrorExitDialog;

    private int mSurfaceWidth;
    private int mSurfaceHeight;
    public int mFlagRecord;
    public boolean mIsRecordering;
    public boolean mIsShareRecordering;

    public TTTRtcEngineHelper() {
    }

    public TTTRtcEngineHelper(MainActivity mActivity) {
        this.mActivity = mActivity;
        //获取屏幕的宽和高
        int[] screenData = DensityUtils.getScreenData(mActivity);
        mScreenWidth = screenData[0];
        mScreenHeight = screenData[1];
        //获取SDK实例对象
        mTTTEngine = TTTRtcEngine.getInstance();

        mRemoteUserLastVideoData = new LongSparseArray<>();
        mRemoteUserLastAudioData = new LongSparseArray<>();

        initErrorExitDialog();
    }


    public boolean splashCheckSetting(Context mContext, String mRoomID) {
        if (TextUtils.isEmpty(mRoomID)) {
            Toast.makeText(mContext, "房间ID为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern p = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");
        String roomID = mRoomID.trim();
        Matcher matcher = p.matcher(roomID);
        if (matcher.matches()) {
            Toast.makeText(mContext, "房间ID输入不合法", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            LocalConfig.mLoginRoomID = Integer.valueOf(roomID);
        } catch (Exception e) {
            Toast.makeText(mContext, "房间ID输入不合法", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-12-6 17:19:33<br/>
     * Description: 初始化6路小窗口的视频布局
     *
     * @param mLocalSeiList the m local sei list
     */
    public void initRemoteLayout(ArrayList<VideoViewObj> mLocalSeiList) {
        LinearLayout remotely = mActivity.findViewById(R.id.main_remotely_parent);
        int[] screenData = DensityUtils.getScreenData(mActivity);
        mSurfaceWidth = screenData[0] / 3;
        mSurfaceHeight = mSurfaceWidth * 4 / 3;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) remotely.getLayoutParams();
        params.height = mSurfaceHeight * 2;
        remotely.setLayoutParams(params);

        for (int i = 0; i < AUTHOR_MAX_NUM; i++) {
            ViewGroup superRoot = null;
            final VideoViewObj obj = new VideoViewObj();
            switch (i) {
                case 0:
                    superRoot = mActivity.findViewById(R.id.main_remotely1);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg1);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang1);
                    break;
                case 1:
                    superRoot = mActivity.findViewById(R.id.main_remotely2);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg2);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang2);
                    break;
                case 2:
                    superRoot = mActivity.findViewById(R.id.main_remotely3);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg3);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang3);
                    break;
                case 3:
                    superRoot = mActivity.findViewById(R.id.main_remotely4);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg4);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang4);
                    break;
                case 4:
                    superRoot = mActivity.findViewById(R.id.main_remotely5);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg5);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang5);
                    break;
                case 5:
                    superRoot = mActivity.findViewById(R.id.main_remotely6);
                    obj.mRootBG = mActivity.findViewById(R.id.main_remotely_bg6);
                    obj.mRootHead = mActivity.findViewById(R.id.main_remotely_touxiang6);
                    break;
            }

            if (superRoot == null) {
                break;
            }
            obj.mIndex = i;
            obj.mRoot = superRoot;
            obj.mContentRoot = superRoot.findViewById(R.id.videoly_contents);
            obj.mContentRoot.setVisibility(View.INVISIBLE);
            obj.mSpeakImage = superRoot.findViewById(R.id.speak_image);
            obj.mReserveCamera = superRoot.findViewById(R.id.videoly_btn_camera);
            obj.mRemoteUserID = superRoot.findViewById(R.id.videoly_remote_userid);
            obj.mMuteVoiceBT = superRoot.findViewById(R.id.videoly_dialog_mute);
            ImageView videoDialogMore = obj.mContentRoot.findViewById(R.id.videoly_more);
            if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
                videoDialogMore.setVisibility(View.VISIBLE);
            } else {
                videoDialogMore.setVisibility(View.INVISIBLE);
            }
            // 远端视频对话框点击按钮
            videoDialogMore.setOnClickListener(v -> {
                if (mRemoteDialog != null && mRemoteDialog.getVisibility() == View.VISIBLE) {
                    mRemoteDialog.setVisibility(View.INVISIBLE);
                    mRemoteDialogBT = null;
                }

                mRemoteDialog = obj.mContentRoot.findViewById(R.id.videoly_dialog);
                if (mRemoteDialog.getVisibility() != View.VISIBLE) {
                    mRemoteDialog.setVisibility(View.VISIBLE);
                    videoDialogMore.setImageResource(R.drawable.videoly_dialog_more_up);
                    mRemoteDialogBT = videoDialogMore;
                } else {
                    mRemoteDialog.setVisibility(View.INVISIBLE);
                    mRemoteDialogBT = null;
                    videoDialogMore.setImageResource(R.drawable.videoly_dialog_more_down);
                }
            });

            // 关闭按钮
            obj.mContentRoot.findViewById(R.id.videoly_dialog_close).setOnClickListener(v -> {
                mRemoteDialog.setVisibility(View.INVISIBLE);
                videoDialogMore.setImageResource(R.drawable.videoly_dialog_more_down);
                mTTTEngine.kickChannelUser(obj.mBindUid);
            });

            // 禁言按钮
            obj.mMuteVoiceBT = obj.mContentRoot.findViewById(R.id.videoly_dialog_mute);
            obj.mMuteVoiceBT.setOnClickListener(v -> {
                if (obj.mIsMuteRemote) {
                    mTTTEngine.muteRemoteSpeaking((int) obj.mBindUid, false);
                    obj.mIsMuteRemote = false;
                    obj.mMuteVoiceBT.setText(mActivity.getResources().getString(R.string.remote_window_ban));
                } else {
                    mTTTEngine.muteRemoteSpeaking((int) obj.mBindUid, true);
                    obj.mIsMuteRemote = true;
                    obj.mMuteVoiceBT.setText(mActivity.getResources().getString(R.string.remote_window_cancel_ban));
                }

                mRemoteDialog.setVisibility(View.INVISIBLE);
                videoDialogMore.setImageResource(R.drawable.videoly_dialog_more_down);
            });

            obj.mReserveCamera.setOnClickListener(v -> mTTTEngine.switchCamera());

            obj.mSpeakImage.setOnClickListener(v -> {
                if (obj.mIsRemoteDisableAudio)
                    return;
                speakMuteClick(obj);
            });
            mLocalSeiList.add(obj);
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-6 16:44:36<br/>
     * Description: 调整远端小窗口的显示与隐藏
     *
     * @param isVisibile the is visibile
     * @param info       the info
     */
    public void adJustRemoteViewDisplay(boolean isVisibile, EnterUserInfo info) {
        SurfaceView mSurfaceView = null;
        long id = info.getId();
        if (isVisibile) {
            if (LocalConfig.mRole != Constants.CLIENT_ROLE_ANCHOR && info.getRole() != Constants.CLIENT_ROLE_ANCHOR) {
                if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
                    boolean checkRes = checkVideoExist(info.mShowIndex);
                    if (checkRes) {
                        return;
                    }
                } else {
                    boolean result = checkEmptyLocation(info);
                    if (result) {
                        return;
                    }
                }
            } else {
                boolean checkRes = checkVideoExist(info.getId());
                if (checkRes) {
                    return;
                }
            }

            if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
                mSurfaceView = mTTTEngine.CreateRendererView(mActivity);
                if (id == LocalConfig.mLoginUserID) {
                    mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN,
                            mSurfaceView), mActivity.getRequestedOrientation());
                } else {
                    mTTTEngine.setupRemoteVideo(new VideoCanvas(info.getId(), Constants.
                            RENDER_MODE_HIDDEN, mSurfaceView));
                }
            }

            if (info.getRole() == Constants.CLIENT_ROLE_ANCHOR) {
                if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE && mSurfaceView != null) {
                    mSurfaceView.setZOrderMediaOverlay(false);
                    mActivity.mFullScreenShowView.addView(mSurfaceView, 0);
                }
            } else {
                VideoViewObj obj;
                if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
                    obj = getRemoteViewParentLayout();
                } else {
                    obj = getRemoteViewParentLayout(info);
                }
                if (obj != null) {
                    obj.mMuteVoiceBT.setText(mActivity.getResources().getString(R.string.remote_window_ban));
                    if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
                        ViewGroup mRemoteChildLayout = obj.mRoot;
                        mSurfaceView.setZOrderMediaOverlay(true);
                        mRemoteChildLayout.addView(mSurfaceView, 0);
                        obj.mRootBG.setVisibility(View.INVISIBLE);
                    }
                    obj.mContentRoot.setVisibility(View.VISIBLE);
                    obj.mBindUid = info.getId();
                    obj.mRemoteUserID.setText(String.valueOf(info.getId()));
                    if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
                        if (id == LocalConfig.mLoginUserID) {
                            obj.mReserveCamera.setVisibility(View.VISIBLE);
                            obj.mReserveCamera.setImageResource(R.drawable.mainly_btn_camera_selector);
                        } else {
                            obj.mReserveCamera.setVisibility(View.INVISIBLE);
                        }
                    }

                    Iterator<Long> iterator = mActivity.mMutedAudioUserID.iterator();
                    while (iterator.hasNext()) {
                        Long next = iterator.next();
                        if (next == obj.mBindUid) {
                            PviewLog.i("OnRemoteAudioMuted init it .... 1" + next);
                            obj.mIsMuteRemote = true;
                            obj.mSpeakImage.setImageResource(R.drawable.jinyan);
                            mActivity.mMutedAudioUserID.remove(obj.mBindUid);
                            break;
                        }
                    }

                    iterator = mActivity.mMutedSpeakUserID.iterator();
                    while (iterator.hasNext()) {
                        Long next = iterator.next();
                        if (next == obj.mBindUid) {
                            PviewLog.i("zhxtext OnRemoteAudioMuted init it .... 2" + next);
                            obj.mIsRemoteDisableAudio = true;
                            obj.mSpeakImage.setImageResource(R.drawable.jinyan);
                            mActivity.mMutedSpeakUserID.remove(obj.mBindUid);
                            break;
                        }
                    }

                    /*if (obj.mBindUid == LocalConfig.mLoginUserID && !obj.mIsRemoteDisableAudio) {
                        if (mActivity.mIsHeadset) {
                            obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_selector);
                        }
                    }*/


                    mActivity.mShowingDevices.put(info.getId(), new DisplayDevice(obj, info));
                }

                MyLog.d("adJustRemoteViewDisplay" , "add user video : " + info.getId()
                        + " | VideoViewObj : " + obj);
            }
        } else {
            if (info.getRole() == Constants.CLIENT_ROLE_ANCHOR) {
                mActivity.mFullScreenShowView.removeViewAt(0);
            } else {
                removeUserByView(info.getId());
            }
        }

        if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR && LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
            Log.d("zhxtest", "fa song sei!!!: ");
            VideoCompositingLayout layout = new VideoCompositingLayout();
            layout.regions = buildRemoteLayoutLocation();
            mTTTEngine.setVideoCompositingLayout(layout);
        }
    }

    public void removeErrorIndexView(TreeSet<EnterUserInfo> mInfos) {
        for (EnterUserInfo value : mInfos) {
            for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
                VideoViewObj videoCusSei = mActivity.mLocalSeiList.get(i);
                if (videoCusSei.mIsUsing && videoCusSei.mBindUid == value.getId()) {
                    if (value.mShowIndex != videoCusSei.mIndex) {
                        adJustRemoteViewDisplay(false, value);
                        break;
                    }
                }
            }
        }
    }

    public void speakMuteClick(VideoViewObj obj) {
        if (obj.mBindUid == LocalConfig.mLoginUserID) {
            if (obj.mIsMuteRemote) {
                obj.mIsMuteRemote = false;
                LocalConfig.mLocalMuteAuido = false;
                mTTTEngine.muteLocalAudioStream(false);
                if (mActivity.mIsHeadset) {
                    obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_selector);
                } else {
                    obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_selector);
                }
            } else {
                obj.mIsMuteRemote = true;
                LocalConfig.mLocalMuteAuido = true;
                mTTTEngine.muteLocalAudioStream(true);
                if (mActivity.mIsHeadset) {
                    obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_muted_headset_selector);
                } else {
                    obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_mute_speaker_selector);
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-11-21 17:57:22<br/>
     * Description: 处理小窗口对话框的显示管理
     */
    public void handlerRemoteDialogVisibile(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mRemoteDialog != null && mRemoteDialog.getVisibility() == View.VISIBLE) {
                if (!inRangeOfView(mRemoteDialog, ev)) {
                    mRemoteDialog.setVisibility(View.INVISIBLE);
                    if (mRemoteDialogBT != null) {
                        mRemoteDialogBT.setImageResource(R.drawable.videoly_dialog_more_down);
                    }
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-11-21 17:37:01<br/>
     * Description: 若自己为主播角色，需要将副播的显示位置发送给服务器.
     */
    public VideoCompositingLayout.Region[] buildRemoteLayoutLocation() {
        List<VideoCompositingLayout.Region> tempList = new ArrayList<>();
        for (int i = 0; i < LocalConfig.mUserEnterOrder.size(); i++) {
            long userID = LocalConfig.mUserEnterOrder.get(i);
            DisplayDevice value = mActivity.mShowingDevices.get(userID);
            if (value != null) {
                SurfaceView mRemoteSurfaceView = null;
                int childCount = value.getDisplayView().mRoot.getChildCount();
                for (int j = 0; j < childCount; j++) {
                    if (value.getDisplayView().mRoot.getChildAt(j) instanceof SurfaceView) {
                        mRemoteSurfaceView = (SurfaceView) value.getDisplayView().mRoot.getChildAt(j);
                        break;
                    }
                }
                if (mRemoteSurfaceView != null) {
                    int[] location = new int[2];
                    mRemoteSurfaceView.getLocationOnScreen(location);
                    VideoCompositingLayout.Region mRegion = new VideoCompositingLayout.Region();
                    mRegion.mUserID = userID;
                    mRegion.x = location[0] * 1.0f / mScreenWidth;
                    mRegion.y = location[1] * 1.0f / mScreenHeight;
                    mRegion.width = mSurfaceWidth * 1.0f / mScreenWidth;
                    mRegion.height = (mSurfaceHeight * 1.0f / mScreenHeight) * 0.998f;
                    mRegion.zOrder = 1;
                    tempList.add(mRegion);
                }
            }
        }

        VideoCompositingLayout.Region mRegion = new VideoCompositingLayout.Region();
        mRegion.mUserID = LocalConfig.mLoginUserID;
        mRegion.x = 0;
        mRegion.y = 0;
        mRegion.width = 1;
        mRegion.height = 1;
        mRegion.zOrder = 0;
        tempList.add(mRegion);

        VideoCompositingLayout.Region[] mRegions = new VideoCompositingLayout.Region[tempList.size()];
        for (int k = 0; k < tempList.size(); k++) {
            VideoCompositingLayout.Region region = tempList.get(k);
            mRegions[k] = region;
        }
        return mRegions;
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-12-1 15:21:15<br/>
     * Description: 根据远端用户的视频接收状态回调，处理DEMO界面中下行视频数据图标
     *
     * @param mRemoteVideoStats the m remote video stats
     */
    public void remoteVideoStatus(RemoteVideoStats mRemoteVideoStats) {
        long tempUid = mRemoteVideoStats.getUid();
        if (tempUid == LocalConfig.mBroadcasterID) {
            mActivity.setTextViewContent(mActivity.mVideoSpeedShow, R.string.videoly_videodown, String.valueOf(mRemoteVideoStats.getReceivedBitrate()));
        } else {
            Set<Map.Entry<Long, DisplayDevice>> entries = mActivity.mShowingDevices.entrySet();
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                long userID = next.getKey();
                if (tempUid == userID) {
                    DisplayDevice value = next.getValue();
                    View mVideoDownSpeedShow = value.getDisplayView().mContentRoot.findViewById(R.id.videoly_video_down);
                    mActivity.setTextViewContent((TextView) mVideoDownSpeedShow, R.string.videoly_videodown,
                            String.valueOf(mRemoteVideoStats.getReceivedBitrate()));
                    break;
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Description: 根据远端用户的音频接收状态回调，处理DEMO界面中下行音频数据图标
     *
     * @param mRemoteAudioStats the m remote audio stats
     */
    public void remoteAudioStatus(RemoteAudioStats mRemoteAudioStats) {
        long tempAudioUid = mRemoteAudioStats.getUid();
        if (tempAudioUid == LocalConfig.mBroadcasterID) {
            mActivity.setTextViewContent(mActivity.mAudioSpeedShow, R.string.videoly_audiodown, String.valueOf(mRemoteAudioStats.getReceivedBitrate()));
        } else {
            Set<Map.Entry<Long, DisplayDevice>> entries = mActivity.mShowingDevices.entrySet();
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                long userID = next.getKey();
                if (tempAudioUid == userID) {
                    DisplayDevice valsue = next.getValue();
                    View mAudioDownSpeedShow = valsue.getDisplayView().mContentRoot.findViewById(R.id.videoly_audio_down);
                    mActivity.setTextViewContent((TextView) mAudioDownSpeedShow, R.string.videoly_audiodown, String.valueOf(mRemoteAudioStats.getReceivedBitrate()));
                    break;
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Description: 根据本地的视频发送状态回调，处理DEMO界面中上行视频数据图标
     *
     * @param mLocalVideoStats the m local video stats
     */
    public void localVideoStatus(LocalVideoStats mLocalVideoStats) {
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
            mActivity.setTextViewContent(mActivity.mVideoSpeedShow, R.string.main_videoups, String.valueOf(mLocalVideoStats.getSentBitrate()));
        } else {
            Set<Map.Entry<Long, DisplayDevice>> entries = mActivity.mShowingDevices.entrySet();
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                long userID = next.getKey();
                if (LocalConfig.mLoginUserID == userID) {
                    DisplayDevice valsue = next.getValue();
                    View mAudioDownSpeedShow = valsue.getDisplayView().mContentRoot.findViewById(R.id.videoly_video_down);
                    mActivity.setTextViewContent((TextView) mAudioDownSpeedShow, R.string.main_videoups, String.valueOf(mLocalVideoStats.getSentBitrate()));
                    break;
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Description: 根据本地的音频发送状态回调，处理DEMO界面中上行音频数据图标
     *
     * @param mLocalAudioStats the m local audio stats
     */
    public void LocalAudioStatus(LocalAudioStats mLocalAudioStats) {
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
            mActivity.setTextViewContent(mActivity.mAudioSpeedShow, R.string.main_audioup, String.valueOf(mLocalAudioStats.getSentBitrate()));
        } else {
            Set<Map.Entry<Long, DisplayDevice>> entries = mActivity.mShowingDevices.entrySet();
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                long userID = next.getKey();
                if (LocalConfig.mLoginUserID == userID) {
                    DisplayDevice valsue = next.getValue();
                    View mAudioDownSpeedShow = valsue.getDisplayView().mContentRoot.findViewById(R.id.videoly_audio_down);
                    mActivity.setTextViewContent((TextView) mAudioDownSpeedShow, R.string.main_audioup, String.valueOf(mLocalAudioStats.getSentBitrate()));
                    break;
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Description: 根据音量上报回调，处理DEMO界面中音量图标的变化
     *
     * @param volumeUserID
     * @param volumeLevel
     */
    public void audioVolumeIndication(long volumeUserID, int volumeLevel, boolean mIsMuteRemote) {
        if (volumeUserID == LocalConfig.mBroadcasterID) {
            if (mIsMuteRemote) {
                return;
            }

            if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
                if (mActivity.mIsHeadset) {
                    if (volumeLevel >= 0 && volumeLevel <= 3) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_selector);
                    } else if (volumeLevel > 3 && volumeLevel <= 6) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_middle_selector);
                    } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_big_selector);
                    }
                } else {
                    if (volumeLevel >= 0 && volumeLevel <= 3) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_selector);
                    } else if (volumeLevel > 3 && volumeLevel <= 6) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_middle_selector);
                    } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                        mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_big_selector);
                    }
                }
            } else {
                if (volumeLevel >= 0 && volumeLevel <= 3) {
                    mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_selector);
                } else if (volumeLevel > 3 && volumeLevel <= 6) {
                    mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_middle_selector);
                } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                    mActivity.mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_big_selector);
                }
            }
        } else {
            for (final VideoViewObj obj : mActivity.mLocalSeiList) {
                if (obj.mBindUid == volumeUserID && obj.mSpeakImage != null) {
                    if (obj.mIsMuteRemote || obj.mIsRemoteDisableAudio) {
                        return;
                    }

                    if (obj.mBindUid == LocalConfig.mLoginUserID) {
                        if (mActivity.mIsHeadset) {
                            if (volumeLevel >= 0 && volumeLevel <= 3) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_selector);
                            } else if (volumeLevel > 3 && volumeLevel <= 6) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_middle_selector);
                            } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_big_selector);
                            }
                        } else {
                            if (volumeLevel >= 0 && volumeLevel <= 3) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_selector);
                            } else if (volumeLevel > 3 && volumeLevel <= 6) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_middle_selector);
                            } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                                obj.mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_big_selector);
                            }
                        }
                    } else {
                        if (volumeLevel == 0) {
                            obj.mSpeakImage.setImageResource(R.drawable.audio_xiao);
                        } else if (volumeLevel > 0 && volumeLevel <= 3) {
                            obj.mSpeakImage.setImageResource(R.drawable.audio_xiao);
                        } else if (volumeLevel > 3 && volumeLevel <= 6) {
                            obj.mSpeakImage.setImageResource(R.drawable.audio_zhong);
                        } else if (volumeLevel > 6 && volumeLevel <= VOLUME_MAX_NUM) {
                            obj.mSpeakImage.setImageResource(R.drawable.audio_da);
                        }
                    }
                    break;
                }
            }
        }
    }

    public void realStartCapture(Intent data) {
        int widthPixels = mActivity.getResources().getDisplayMetrics().widthPixels;
        int heightPixels = mActivity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth;
        int mRecordBitRate;
        if (1280 * 720 > widthPixels * heightPixels) {
            targetWidth = 640;
            mRecordBitRate = 400 * 1000;
        } else {
            targetWidth = 1280;
            mRecordBitRate = 1130 * 1000;
        }
        float rate;
        if (widthPixels > heightPixels) {
            rate = (float) widthPixels / (float) heightPixels;
        } else {
            rate = (float) heightPixels / (float) widthPixels;
        }
        MyLog.d("record screen width height rate : " + rate);
        int targetHeight = (int) (targetWidth / rate);
        MyLog.d("record screen targetHeight : " + targetHeight);
        int temp = targetHeight % 8;
        MyLog.d("record screen targetHeight % 8 : " + temp);
        if (temp != 0) {
            targetHeight = targetHeight - temp;
        }

        if (targetHeight > heightPixels) {
            targetHeight = heightPixels;
        }
        MyLog.d("record screen target result width : " + targetWidth + " | height : " + targetHeight);
        int mScreenRecordWidth;
        int mScreenRecordHeight;
        if (TTTRtcEngineHelper.isTabletDevice(mActivity)) {
            if (targetWidth < targetHeight) {
                mScreenRecordWidth = targetHeight;
                mScreenRecordHeight = targetWidth;
            } else {
                mScreenRecordWidth = targetWidth;
                mScreenRecordHeight = targetHeight;
            }
        } else {
            if (targetWidth > targetHeight) {
                mScreenRecordWidth = targetHeight;
                mScreenRecordHeight = targetWidth;
            } else {
                mScreenRecordWidth = targetWidth;
                mScreenRecordHeight = targetHeight;
            }
        }
        MyLog.d("record screen mScreenRecordWidth : " + mScreenRecordWidth +
                " | mScreenRecordHeight : " + mScreenRecordHeight);
        ScreenRecordConfig mConfig = new ScreenRecordConfig(
                mScreenRecordWidth, mScreenRecordHeight,
                mRecordBitRate, 15, 96 * 1000);
        mTTTEngine.startScreenRecorder(data, mConfig);
        if (mFlagRecord == RECORD_TYPE_FILE) {
            mIsRecordering = true;
            mActivity.mRecordScreen.setVisibility(View.VISIBLE);
            mFlagRecord = 0;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else if (mFlagRecord == RECORD_TYPE_SHARE) {
            mIsShareRecordering = true;
            mFlagRecord = 0;
        }
    }

    //时间计数器，最多只能到99小时，如需要更大小时数需要改改方法
    public String showTimeCount(long time) {
        if (time >= 360000) {
            return "00:00:00";
        }
        String timeCount;
        long hourc = time / 3600;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length() - 2, hour.length());

        long minuec = (time - hourc * 3600) / (60);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length() - 2, minue.length());

        long secc = time - hourc * 3600 - minuec * 60;
        String sec = "0" + secc;
        sec = sec.substring(sec.length() - 2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

    /**
     * Checks if the device is a tablet or a phone
     *
     * @param activityContext The Activity Context.
     * @return Returns true if the device is a Tablet
     */
    public static boolean isTabletDevice(Context activityContext) {
        int smallestScreenWidthDp = activityContext.getResources().getConfiguration().smallestScreenWidthDp;
        if (smallestScreenWidthDp >= 600) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-11-21 18:08:37<br/>
     * Description: 显示因错误的回调而退出的对话框
     *
     * @param message the message 错误的原因
     */
    public void showErrorExitDialog(String message) {
        Log.w("wzg", "showErrorExitDialog message " + message);
        if (!TextUtils.isEmpty(message)) {
            mErrorExitDialog.setMessage("退出原因: " + message);//设置显示的内容
            mErrorExitDialog.show();
        }
    }

    private void initErrorExitDialog() {
        if (mErrorExitDialog == null) {
            //添加确定按钮
            mErrorExitDialog = new AlertDialog.Builder(mActivity)
                    .setTitle("退出房间提示")//设置对话框标题
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialog, which) -> {//确定按钮的响应事件
                        mActivity.exitRoom();
                    });
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-6 16:45:00<br/>
     * Description: 创建一个新的远端小视频的布局窗口
     *
     * @return the list
     */
    private VideoViewObj getRemoteViewParentLayout() {
        for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
            VideoViewObj videoCusSei = mActivity.mLocalSeiList.get(i);
            if (!videoCusSei.mIsUsing) {
                videoCusSei.mIsUsing = true;
                return videoCusSei;
            }
        }
        return null;
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-6-6 16:45:00<br/>
     * Description: 创建一个新的远端小视频的布局窗口
     *
     * @return the list
     */
    private VideoViewObj getRemoteViewParentLayout(EnterUserInfo info) {
        for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
            VideoViewObj videoCusSei = mActivity.mLocalSeiList.get(i);
            if (videoCusSei.mIndex == info.mShowIndex) {
                videoCusSei.mIsUsing = true;
                return videoCusSei;
            }
        }
        MyLog.d("adJustRemoteViewDisplay" , "getRemoteViewParentLayout failed! not find VideoViewObj : " + info.getId());
        return null;
    }

    private boolean checkVideoExist(int showIndex) {
        VideoViewObj videoViewObj = mActivity.mLocalSeiList.get(showIndex);
        return videoViewObj.mIsUsing;
    }

    private boolean checkVideoExist(long uid) {
        for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
            VideoViewObj videoCusSei = mActivity.mLocalSeiList.get(i);
            if (videoCusSei.mIsUsing && videoCusSei.mBindUid == uid) {
                return true;
            }
        }
        MyLog.d("adJustRemoteViewDisplay" , "checkVideoExist failed! not find VideoViewObj : " + uid);
        return false;
    }

    private boolean checkEmptyLocation(EnterUserInfo info) {
        for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
            VideoViewObj videoViewObj = mActivity.mLocalSeiList.get(i);
            if (!videoViewObj.mIsUsing) {
                info.mShowIndex = i;
                return false;
            }
        }
        return true;
    }

    public void removeUserByView(long uid) {
        for (Map.Entry<Long, DisplayDevice> next : mActivity.mShowingDevices.entrySet()) {
            final DisplayDevice value = next.getValue();
            MyLog.d("adJustRemoteViewDisplay" , "removeUserByView VideoViewObj : " + value.getDisplayView().mIsUsing
                    + " | uid : " + value.getDisplayView().mBindUid);
        }

        for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
            MyLog.d("adJustRemoteViewDisplay" , "removeUserByView VideoViewObj : " + mActivity.mLocalSeiList.get(i).mIsUsing
                    + " | uid : " + mActivity.mLocalSeiList.get(i).mBindUid);
        }
        DisplayDevice mRemoteDisplayDevice = mActivity.mShowingDevices.remove(uid);
        if (mRemoteDisplayDevice != null) {
            VideoViewObj videoCusSei = null;
            for (int i = 0; i < mActivity.mLocalSeiList.size(); i++) {
                videoCusSei = mActivity.mLocalSeiList.get(i);
                if (videoCusSei.mBindUid == uid) {
                    videoCusSei.clearData();
                    break;
                }
            }

            if (videoCusSei != null) {
                if (LocalConfig.mRoomMode == SplashActivity.VIDEO_MODE) {
                    SurfaceView childAt = (SurfaceView) videoCusSei.mRoot.getChildAt(0);
                    videoCusSei.mRoot.removeView(childAt);
                    videoCusSei.mReserveCamera.setVisibility(View.INVISIBLE);
                }
                videoCusSei.mContentRoot.setVisibility(View.INVISIBLE);
                videoCusSei.mRootBG.setVisibility(View.VISIBLE);
            }
        }
    }

    private static int formatedSpeedKbps(long bytes, long elapsed_milli) {
        long kb = bytes / 1024;
        return (int) (kb * 1000 / elapsed_milli);
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }
}
