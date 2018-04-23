package com.tttrtclive.dialog;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tttrtclive.LocalConfig;
import com.tttrtclive.R;

import static com.tttrtclive.LocalConfig.mAudience;
import static com.tttrtclive.LocalConfig.mAuthorSize;

/**
 * Created by wangzhiguo on 17/10/11.
 */

public class MoreInfoDialog extends Dialog {

    private boolean mIsVisibileInfo = true;

    private ImageView mControlShowInfo;
    private DataInfoShowCallback mDataInfoShowCallback;
    private TextView mWatchViewrs;
    private TextView mAuthorViewrs;

    public MoreInfoDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainly_more_dialog);
        mControlShowInfo = (ImageView) findViewById(R.id.mainly_dialog_show_info);
        EditText mCdnEdit = (EditText) findViewById(R.id.mainly_dialog_cdn);
        mControlShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsVisibileInfo) {
                    mControlShowInfo.setImageResource(R.drawable.guanbi);
                    mIsVisibileInfo = false;
                    if (mDataInfoShowCallback != null) {
                        mDataInfoShowCallback.showDataInfo(false);
                    }
                } else {
                    mControlShowInfo.setImageResource(R.drawable.dakai);
                    mIsVisibileInfo = true;
                    if (mDataInfoShowCallback != null) {
                        mDataInfoShowCallback.showDataInfo(true);
                    }
                }
            }
        });

        findViewById(R.id.mainly_dialog_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(LocalConfig.mCDNAddress);
            }
        });

        mCdnEdit.setText(LocalConfig.mCDNAddress);
        mCdnEdit.setTextIsSelectable(true);
        mCdnEdit.setSelection(mCdnEdit.length());

        mWatchViewrs = (TextView) findViewById(R.id.main_btn_viewers);
        mAuthorViewrs = (TextView) findViewById(R.id.main_btn_secondAnchor);
        setTextViewContent(mWatchViewrs, R.string.main_viewer, String.valueOf(mAudience));
        setTextViewContent(mAuthorViewrs, R.string.main_secondAnchor, String.valueOf(mAuthorSize));
    }

    public void setDataInfoShowCallback(DataInfoShowCallback mDataInfoShowCallback) {
        this.mDataInfoShowCallback = mDataInfoShowCallback;
    }

    private void setTextViewContent(TextView textView, int resourceID, String value) {
        String string = this.getContext().getResources().getString(resourceID);
        String result = String.format(string, value);
        textView.setText(result);
    }

    @Override
    public void show() {
        super.show();
        setTextViewContent(mWatchViewrs, R.string.main_viewer, String.valueOf(mAudience));
        setTextViewContent(mAuthorViewrs, R.string.main_secondAnchor, String.valueOf(mAuthorSize));
    }
}
