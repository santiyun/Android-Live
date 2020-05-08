package com.tttrtclive.live.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.tttrtclive.live.R;
import com.tttrtclive.live.fragment.LocalFragment;
import com.tttrtclive.live.fragment.PushFragment;
import com.tttrtclive.live.utils.DensityUtils;
import com.wushuangtech.library.Constants;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import static com.tttrtclive.live.ui.SplashActivity.ACTIVITY_SETTING;

public class SetActivity extends BaseActivity {

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    private String[] mTitles;
    private TabLayout mTabLayout_1;

    /*-------------------------------配置参数---------------------------------*/
    public int mLocalVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    public int mPushVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    public boolean mUseHQAudio = false;
    public String mLocalIP;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate, mLocalPort;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    public int mEncodeType = 0;//0:H.264  1:H.265
    public int mAudioSRate = 0;// 0:48000 1:44100
    public int mChannels = 1;
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        mTitles = new String[2];
        mTitles[0] = getString(R.string.ttt_local_setting);
        mTitles[1] = getString(R.string.ttt_push_setting);
        View mHeadLy = findViewById(R.id.set_head);
        int statusBarHeight = DensityUtils.getStatusBarHeight(this);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mHeadLy.getLayoutParams();
        layoutParams.topMargin = statusBarHeight;
        mHeadLy.setLayoutParams(layoutParams);

        Intent intent = getIntent();
        mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
        mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
        mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
        mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
        mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
        mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
        mLocalIP = intent.getStringExtra("LIP");
        mLocalPort = intent.getIntExtra("LPORT", mLocalPort);
        mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
        mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
        mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
        mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
        mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
        mEncodeType = intent.getIntExtra("EDT", mEncodeType);
        mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);

        LocalFragment localFragment = new LocalFragment();
        mFragments.add(localFragment);
        PushFragment pushFragment = new PushFragment();
        mFragments.add(pushFragment);

        mTabLayout_1 = findViewById(R.id.tl_1);
        mTabLayout_1.setSelectedTabIndicator(null); // 去掉下划线

        TabLayout.Tab tab = mTabLayout_1.newTab();


        View rootView = View.inflate(this, R.layout.set_tab_ly, null);
        rootView.setBackgroundResource(R.drawable.setly_local_tab_bg);
        TextView view = rootView.findViewById(R.id.tab_text);
        view.setText(mTitles[0]);
        rootView.setTag(view);
        tab.setCustomView(rootView);

        TabLayout.Tab tab2 = mTabLayout_1.newTab();
        View rootView2 = View.inflate(this, R.layout.set_tab_ly, null);
        rootView2.setBackgroundResource(R.drawable.setly_push_tab_bg);
        TextView view2 = rootView2.findViewById(R.id.tab_text);
        view2.setText(mTitles[1]);
        rootView2.setTag(view2);
        tab2.setCustomView(rootView2);

        mTabLayout_1.addTab(tab);
        mTabLayout_1.addTab(tab2);
        tl_1();
    }


    private void tl_1() {
        final ViewPager vp_3 = findViewById(R.id.vp_2);
        vp_3.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mTabLayout_1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    Object tag = customView.getTag();
                    if (tag != null) {
                        ((TextView) tag).setTextColor(Color.WHITE);
                    }
                }
                vp_3.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    Object tag = customView.getTag();
                    if (tag != null) {
                        ((TextView) tag).setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        vp_3.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tabAt = mTabLayout_1.getTabAt(position);
                if (tabAt != null) {
                    tabAt.select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout.Tab tabAt = mTabLayout_1.getTabAt(0);
        if (tabAt != null) {
            View customView = tabAt.getCustomView();
            if (customView != null) {
                Object tag = customView.getTag();
                if (tag != null) {
                    ((TextView) tag).setTextColor(Color.WHITE);
                }
            }
        }
        vp_3.setCurrentItem(0);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {


        MyPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
        exit(false);
    }

    @Override
    public void onBackPressed() {
        exit(false);
        super.onBackPressed();
    }

    public void onOkButtonClick(View v) {
        LocalFragment localFragment = (LocalFragment) mFragments.get(0);
        boolean params = localFragment.getParams();
        if (!params) {
            return;
        }

        PushFragment pushFragment = (PushFragment) mFragments.get(1);
        boolean params2 = pushFragment.getParams();
        if (!params2) {
            return;
        }
        exit(true);
    }

    private void exit(boolean saveSetting) {
        mFragments.clear();
        mFragments = null;
        Intent intent = new Intent();
        if (saveSetting) {
            intent.putExtra("LVP", mLocalVideoProfile);
            intent.putExtra("PVP", mPushVideoProfile);
            intent.putExtra("LWIDTH", mLocalWidth);
            intent.putExtra("LHEIGHT", mLocalHeight);
            intent.putExtra("LBRATE", mLocalBitRate);
            intent.putExtra("LFRATE", mLocalFrameRate);
            intent.putExtra("LIP", mLocalIP);
            intent.putExtra("LPORT", mLocalPort);
            intent.putExtra("PWIDTH", mPushWidth);
            intent.putExtra("PHEIGHT", mPushHeight);
            intent.putExtra("PBRATE", mPushBitRate);
            intent.putExtra("PFRATE", mPushFrameRate);
            intent.putExtra("HQA", mUseHQAudio);
            intent.putExtra("EDT", mEncodeType);
            intent.putExtra("ASR", mAudioSRate);
        }
        setResult(ACTIVITY_SETTING, intent);
        finish();
    }
}
