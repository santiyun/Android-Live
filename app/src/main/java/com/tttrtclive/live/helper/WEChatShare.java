package com.tttrtclive.live.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class WEChatShare implements IWXAPIEventHandler {

    private Context mContext;
    private IWXAPI api;

    public WEChatShare(Context context) {
        mContext = context;
        String APP_ID;
        if (LocalConfig.VERSION_FLAG == LocalConstans.VERSION_WHITE) {
            APP_ID = "wx39992374fe94311c";
        } else {
            APP_ID = "wxf49788754a287d14";
        }
        api = WXAPIFactory.createWXAPI(context, APP_ID, true);
        api.registerApp(APP_ID);
    }

    public void sendText(int targetScene, long roomid, String message) {
        if (message == null || message.length() == 0) {
            return;
        }

        WXVideoObject video = new WXVideoObject();
        video.videoUrl = message;

        WXMediaMessage msg = new WXMediaMessage(video);
        msg.title = "连麦直播";
        Bitmap bmp;
        if (LocalConfig.VERSION_FLAG == LocalConstans.VERSION_WHITE) {
            msg.description = "欢迎加入直播间:\n" + roomid;
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_white);
        } else {
            msg.description = "三体云联邀请你加入直播间:\n" + roomid;
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_white);
        }
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 200, 200, true);
        bmp.recycle();
        msg.thumbData = bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "video" + System.currentTimeMillis();
        req.message = msg;
        req.scene = targetScene;

        boolean isOk = api.sendReq(req);
        Log.d("zhx", "sendText: " + isOk);
    }

    public byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp resp) {
        String result;

        Toast.makeText(mContext, "baseresp.getType = " + resp.getType(), Toast.LENGTH_SHORT).show();

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "OK";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "USER_CANCEL";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "AUTH_DENIED";
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = "UNSUPPORT";
                break;
            default:
                result = "OTHER";
                break;
        }

        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
    }

    public byte[] getBytesByBitmap(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        return buffer.array();
    }


}
