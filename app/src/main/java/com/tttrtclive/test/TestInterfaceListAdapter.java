package com.tttrtclive.test;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tttrtclive.R;

import java.util.List;

/**
 * Created by wangzhiguo on 18/3/30.
 */

public class TestInterfaceListAdapter extends RecyclerView.Adapter<TestInterfaceListAdapter.TestInterfaceListViewHeadHolder> {

    private final LayoutInflater mInflater;
    protected Activity mContext;
    public List<String> mDatas;

    public TestInterfaceListAdapter(final Activity mContext) {
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public TestInterfaceListAdapter.TestInterfaceListViewHeadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rooView = mInflater.inflate(R.layout.adapter_test_item, parent, false);
        return new TestInterfaceListViewHeadHolder(rooView);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(final TestInterfaceListAdapter.TestInterfaceListViewHeadHolder baseHolder, int position) {
        String mTestLog = mDatas.get(position);
        SpannableStringBuilder builder = new SpannableStringBuilder(mTestLog);
        String[] split = mTestLog.split("->");
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.BLUE);
        builder.setSpan(redSpan ,mTestLog.indexOf(split[1]) , mTestLog.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        baseHolder.mTextLogName.setText(builder);
    }

    class TestInterfaceListViewHeadHolder extends RecyclerView.ViewHolder {

        TextView mTextLogName;

        TestInterfaceListViewHeadHolder(View itemView) {
            super(itemView);
            mTextLogName = itemView.findViewById(R.id.adapter_test_tv);
        }
    }
}
