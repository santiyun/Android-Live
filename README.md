## 简介
此Demo为展示三体云基于SaaS SDK服务的连麦直播产品 -  3T lova （Live-streaming Over App）的功能而开发。
具体功能为，主播创建直播间后，参与连麦的副播进入到与主播相同的直播频道（房间ID）中, 主播和副播通过三体连麦直播（3T Lova）进行实时音视频交互;同时主播与副播的的音视频数据在视频云进行混流后推送到CDN供观众端观看，实现连麦直播。

## 运行环境
* Android Studio 3.0 +
* 真实 Android 设备 (Nexus 5X 或者其它设备)
* 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 接口变动
### 2018.08.01
* 添加回调 public void onSpeakingMuted(long uid, boolean muted)
</br>说明：此接口为被禁言回调，原本无论被禁言还是自己主动禁言都回调onUserMuteAudio。
