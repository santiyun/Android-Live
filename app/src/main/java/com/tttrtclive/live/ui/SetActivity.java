package com.tttrtclive.live.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.tttrtclive.live.R;
import com.tttrtclive.live.fragment.LocalFragment;
import com.tttrtclive.live.fragment.PushFragment;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;

public class SetActivity extends BaseActivity {

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles = {"本地设置", "推流设置"};
    private SegmentTabLayout mTabLayout_1;

    /*-------------------------------配置参数---------------------------------*/
    public int mLocalVideoProfile = Constants.VIDEO_PROFILE_DEFAULT;
    public int mPushVideoProfile = Constants.VIDEO_PROFILE_DEFAULT;
    public boolean mUseHQAudio = false;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    public int mEncodeType = 0;//0:H.264  1:H.265
    public int mAudioSRate = 0;// 0:48000 1:44100
    public int mChannels = 1;
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Intent intent = getIntent();
        mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
        mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
        mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
        mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
        mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
        mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
        mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
        mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
        mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
        mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
        mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
        mEncodeType = intent.getIntExtra("EDT", mEncodeType);
        mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);

        mFragments.add(LocalFragment.getInstance());
        mFragments.add(PushFragment.getInstance());

        mTabLayout_1 = findViewById(R.id.tl_1);

        mTabLayout_1.setTabData(mTitles);
        tl_1();
    }


    private void tl_1() {
        final ViewPager vp_3 = findViewById(R.id.vp_2);
        vp_3.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mTabLayout_1.setTabData(mTitles);
        mTabLayout_1.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp_3.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vp_3.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout_1.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_3.setCurrentItem(0);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

    public void onExitButtonClick(View v) {
        exit();
    }

    @Override
    public void onBackPressed() {
        exit();
        super.onBackPressed();
    }

    public void onOkButtonClick(View v) {
        TTTRtcEngine.getInstance().setVideoMixerParams(mPushBitRate, mPushFrameRate, mPushWidth, mPushHeight);
        TTTRtcEngine.getInstance().setAudioMixerParams(mAudioSRate, mAudioSRate == 0 ? 48000 : 44100, mChannels);
        if (mLocalVideoProfile != 0) {
            TTTRtcEngine.getInstance().setVideoProfile(mLocalVideoProfile, true);
        } else {
            LocalFragment.getInstance().getParams();
            PushFragment.getInstance().getParams();
            TTTRtcEngine.getInstance().setVideoProfile(mLocalWidth, mLocalHeight, mLocalBitRate, mLocalFrameRate);
        }
        TTTRtcEngine.getInstance().setHighQualityAudioParameters(mUseHQAudio);
        exit();
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("LVP", mLocalVideoProfile);
        intent.putExtra("PVP", mPushVideoProfile);
        intent.putExtra("LWIDTH", mLocalWidth);
        intent.putExtra("LHEIGHT", mLocalHeight);
        intent.putExtra("LBRATE", mLocalBitRate);
        intent.putExtra("LFRATE", mLocalFrameRate);
        intent.putExtra("PWIDTH", mPushWidth);
        intent.putExtra("PHEIGHT", mPushHeight);
        intent.putExtra("PBRATE", mPushBitRate);
        intent.putExtra("PFRATE", mPushFrameRate);
        intent.putExtra("HQA", mUseHQAudio);
        intent.putExtra("EDT", mEncodeType);
        intent.putExtra("ASR", mAudioSRate);
        setResult(1, intent);
        finish();
    }
}
