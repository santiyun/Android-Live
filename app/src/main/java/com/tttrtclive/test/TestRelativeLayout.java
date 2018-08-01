package com.tttrtclive.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tttrtclive.R;

/**
 * Created by wangzhiguo on 18/5/17.
 */

public class TestRelativeLayout extends RelativeLayout {
    private int mLastTouchX;
    private int mLastTouchY;
    private int screenHeight;
    private int screenWidth;
    private FrameLayout.LayoutParams params;

    private TextView mTestHeadView;
    private RecyclerView mTestListView;
    private boolean mTestIsExpand;

    public TestRelativeLayout(Context context) {
        super(context);
        init();
    }

    public TestRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTestHeadView = findViewById(R.id.main_test_title);
        mTestHeadView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                updateLocation(event);
                return false;
            }
        });
        mTestHeadView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTestIsExpand) {
                    mTestIsExpand = false;
                    mTestListView.setVisibility(View.INVISIBLE);
                    mTestHeadView.setText(getResources().getString(R.string.title_name_close));
                } else {
                    mTestIsExpand = true;
                    mTestListView.setVisibility(View.VISIBLE);
                    mTestHeadView.setText(getResources().getString(R.string.title_name_open));
                }
            }
        });
        mTestListView = findViewById(R.id.main_test_list);
    }

    public RecyclerView getTestListView() {
        return mTestListView;
    }

    private void updateLocation(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchX = (int) event.getRawX();//设定移动的初始位置相对位置
                mLastTouchY = (int) event.getRawY();
                params = new FrameLayout.LayoutParams(getWidth(), getHeight());
                break;
            case MotionEvent.ACTION_MOVE://移动
                //event.getRawX()事件点距离屏幕左上角的距离
                int dx = ((int) event.getRawX() - mLastTouchX);
                int dy = ((int) event.getRawY() - mLastTouchY);

                int left = TestRelativeLayout.this.getLeft() + dx;
                int top = TestRelativeLayout.this.getTop() + dy;
                int right = TestRelativeLayout.this.getRight() + dx;
                int bottom = TestRelativeLayout.this.getBottom() + dy;
                if (left < 0) { //最左边
                    left = 0;
                    right = left + TestRelativeLayout.this.getWidth();
                }
                if (right > screenWidth) { //最右边
                    right = screenWidth;
                    left = right - TestRelativeLayout.this.getWidth();
                }
                if (top < 0) {  //最上边
                    top = 0;
                    bottom = top + TestRelativeLayout.this.getHeight();
                }
//                if (bottom > screenHeight) {//最下边
//                    bottom = screenHeight;
//                    top = bottom - TestRelativeLayout.this.getHeight();
//                }

                params.leftMargin = left;
                params.topMargin = top;
                TestRelativeLayout.this.setLayoutParams(params);
//                this.layout(left, top, right, bottom);//设置控件的新位置
                mLastTouchX = (int) event.getRawX();//再次将滑动其实位置定位
                mLastTouchY = (int) event.getRawY();
                break;
        }
    }

    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }
}
