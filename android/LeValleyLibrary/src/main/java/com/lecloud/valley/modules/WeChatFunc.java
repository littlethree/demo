package com.lecloud.valley.modules;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.lecloud.valley.common.Events;
import com.lecloud.valley.utils.LogUtils;
import com.letv.lepaysdk.wxpay.WXPay;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXFileObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.lecloud.valley.common.Constants.AUTH_RESULT_CODE_CANCEL;
import static com.lecloud.valley.common.Constants.AUTH_RESULT_CODE_FAILED;
import static com.lecloud.valley.common.Constants.AUTH_RESULT_CODE_SUCCESSFUL;
import static com.lecloud.valley.common.Constants.AUTH_RESULT_MSG_CANCEL;
import static com.lecloud.valley.common.Constants.AUTH_RESULT_MSG_FAILED;
import static com.lecloud.valley.common.Constants.AUTH_RESULT_MSG_SUCCESSFUL;
import static com.lecloud.valley.common.Constants.CODE_INVALID_ARGUMENT;
import static com.lecloud.valley.common.Constants.CODE_INVOKE_FAILED;
import static com.lecloud.valley.common.Constants.CODE_NOT_REGISTERED;
import static com.lecloud.valley.common.Constants.EVENT_PROP_SOCIAL_CODE;
import static com.lecloud.valley.common.Constants.EVENT_PROP_SOCIAL_MSG;
import static com.lecloud.valley.common.Constants.EVENT_PROP_SOCIAL_TYPE;
import static com.lecloud.valley.common.Constants.MSG_INVALID_ARGUMENT;
import static com.lecloud.valley.common.Constants.MSG_INVOKE_FAILED;
import static com.lecloud.valley.common.Constants.MSG_NOT_REGISTERED;
import static com.lecloud.valley.common.Constants.REACT_CLASS_WECHAT_MODULE;
import static com.lecloud.valley.common.Constants.SHARE_PROP_AUDIO;
import static com.lecloud.valley.common.Constants.SHARE_PROP_DESP;
import static com.lecloud.valley.common.Constants.SHARE_PROP_TARGET;
import static com.lecloud.valley.common.Constants.SHARE_PROP_THUMB_IMAGE;
import static com.lecloud.valley.common.Constants.SHARE_PROP_TITLE;
import static com.lecloud.valley.common.Constants.SHARE_PROP_TYPE;
import static com.lecloud.valley.common.Constants.SHARE_PROP_VIDEO;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_CODE_CANCEL;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_CODE_FAILED;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_CODE_SUCCESSFUL;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_MSG_CANCEL;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_MSG_FAILED;
import static com.lecloud.valley.common.Constants.SHARE_RESULT_MSG_SUCCESSFUL;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_AUDIO;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_FILE;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_IMAGE;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_IMAGE_FILE;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_NEWS;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_TEXT;
import static com.lecloud.valley.common.Constants.SHARE_TYPE_VIDEO;
import static com.lecloud.valley.utils.LogUtils.TAG;

/**
 * Created by raojia on 2017/2/8.
 */
class WeChatFunc implements ReactBaseFunc, IWXAPIEventHandler {

    private final ReactApplicationContext mReactContext;
    private final RCTNativeAppEventEmitter mEventEmitter;

    private static String appId = null;
    private static String secret = null;
    private static IWXAPI api = null;
    private static WXPay wxPay;
    private static WeChatFunc gModule = null;
    private static boolean gIsAppRegistered = false;

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;


    WeChatFunc(ReactApplicationContext reactContext, RCTNativeAppEventEmitter eventEmitter) {
        mReactContext = reactContext;
        mEventEmitter = eventEmitter;

        initialize();
    }

    public void initialize() {

        if (appId == null) {
            ApplicationInfo appInfo;
            try {
                appInfo = mReactContext.getPackageManager().getApplicationInfo(mReactContext.getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                throw new Error(e);
            }
            if (!appInfo.metaData.containsKey("WX_APPID")) {
                throw new Error("meta-data WX_APPID not found in AndroidManifest.xml");
            }
            appId = appInfo.metaData.get("WX_APPID").toString();

            if (!appInfo.metaData.containsKey("WX_SECRET")) {
                throw new Error("meta-data WX_SECRET not f ound in AndroidManifest.xml");
            }
            secret = appInfo.metaData.get("WX_SECRET").toString();
        }

        if (!gIsAppRegistered) {
            // 通过WXAPIFactory工厂，获取IWXAPI的实例
            api = WXAPIFactory.createWXAPI(mReactContext, appId, false);
            // 将该app注册到微信
            gIsAppRegistered = api.registerApp(appId);

        }
        gModule = this;
    }

    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("isAppRegistered", gIsAppRegistered);
        constants.put("APP_ID", appId);
        constants.put("APP_SECRET", secret);
        constants.put("SHARE_TYPE_NEWS", SHARE_TYPE_NEWS);
        constants.put("SHARE_TYPE_IMAGE", SHARE_TYPE_IMAGE);
        constants.put("SHARE_TYPE_IMAGE_FILE", SHARE_TYPE_IMAGE_FILE);
        constants.put("SHARE_TYPE_TEXT", SHARE_TYPE_TEXT);
        constants.put("SHARE_TYPE_VIDEO", SHARE_TYPE_VIDEO);
        constants.put("SHARE_TYPE_AUDIO", SHARE_TYPE_AUDIO);
        constants.put("SHARE_TYPE_FILE", SHARE_TYPE_FILE);
        return constants;
    }


    public void destroy() {
//        if (api != null) {
//            api = null;  //加了重新加载会崩溃
//        }
        gModule = null;
    }


    /**
     * 微信是否安装
     */
    void isAppInstalled(Promise promise) {
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        promise.resolve(api.isWXAppInstalled());
    }

    /**
     * 微信版本是否支持API
     */
    void isAppSupportApi(Promise promise) {
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        promise.resolve(api.isWXAppSupportAPI());
    }

    /**
     * 获得微信版本
     */
    void getApiVersion(Promise promise) {
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        int wxSdkVersion = api.getWXAppSupportAPI();
//        if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
//            Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline supported", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline not supported", Toast.LENGTH_LONG).show();
//        }
        promise.resolve(Integer.toHexString(wxSdkVersion));
//        callback.invoke(null, Integer.toHexString(wxSdkVersion));
    }

    /**
     * 调起微信APP
     */
    void openApp(Promise promise) {
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        promise.resolve(api.openWXApp());
    }

    /**
     * 微信登陆
     */
    void sendAuth(ReadableMap config, Promise promise) {
        Log.d(TAG, LogUtils.getTraceInfo() + "微信登陆——— config：" + config.toString());
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }

        SendAuth.Req req = new SendAuth.Req();
        if (config.hasKey("scope")) {
            req.scope = config.getString("scope");
        }
        if (config.hasKey("state")) {
            req.state = config.getString("state");
        } else {
            req.state = new Date().toString();
        }
//        callback.invoke(api.sendReq(req) ? null : MSG_INVOKE_FAILED);
        if (api.sendReq(req))
            promise.resolve(null);
        else
            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
    }


    /**
     * 微信分享朋友圈
     */
    void shareToTimeline(ReadableMap data, Promise promise) {
        Log.d(TAG, LogUtils.getTraceInfo() + "微信分享朋友圈——— data：" + data.toString());
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        _share(SendMessageToWX.Req.WXSceneTimeline, data, promise);
    }

    /**
     * 微信分享好友
     */
    void shareToSession(ReadableMap data, Promise promise) {
        Log.d(TAG, LogUtils.getTraceInfo() + "微信分享好友——— data：" + data.toString());
        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        _share(SendMessageToWX.Req.WXSceneSession, data, promise);
    }

    /**
     * 微信支付
     */
    void pay(ReadableMap data, Promise promise) {
        Log.d(TAG, LogUtils.getTraceInfo() + "微信支付——— data：" + data.toString());

        if (api == null) {
            promise.reject(CODE_NOT_REGISTERED, MSG_NOT_REGISTERED);
            return;
        }
        PayReq payReq = new PayReq();
        if (data.hasKey("partnerId")) {
            payReq.partnerId = data.getString("partnerId");
        }
        if (data.hasKey("prepayId")) {
            payReq.prepayId = data.getString("prepayId");
        }
        if (data.hasKey("nonceStr")) {
            payReq.nonceStr = data.getString("nonceStr");
        }
        if (data.hasKey("timeStamp")) {
            payReq.timeStamp = data.getString("timeStamp");
        }
        if (data.hasKey("sign")) {
            payReq.sign = data.getString("sign");
        }
        if (data.hasKey("package")) {
            payReq.packageValue = data.getString("package");
        }
        if (data.hasKey("extData")) {
            payReq.extData = data.getString("extData");
        }
        payReq.appId = appId;

        if (api.sendReq(payReq))
            promise.resolve(null);
        else
            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        WritableMap map = Arguments.createMap();
        map.putInt("wxCode", baseResp.errCode);
        map.putString("wxStr", baseResp.errStr);
        map.putString("transaction", baseResp.transaction);

        if (baseResp instanceof SendAuth.Resp) {
            SendAuth.Resp resp = (SendAuth.Resp) (baseResp);
            map.putString(EVENT_PROP_SOCIAL_TYPE, "SendAuth.Resp");
            if (resp.code != null) map.putString("code", resp.code);
            if (resp.state != null) map.putString("state", resp.state);
            if (resp.url != null) map.putString("url", resp.url);
            if (resp.lang != null) map.putString("lang", resp.lang);
            if (resp.country != null) map.putString("country", resp.country);

            if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
                map.putInt(EVENT_PROP_SOCIAL_CODE, AUTH_RESULT_CODE_SUCCESSFUL);
                map.putString(EVENT_PROP_SOCIAL_MSG, AUTH_RESULT_MSG_SUCCESSFUL);
            } else if (baseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL || baseResp.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                map.putInt(EVENT_PROP_SOCIAL_CODE, AUTH_RESULT_CODE_CANCEL);
                map.putString(EVENT_PROP_SOCIAL_MSG, AUTH_RESULT_MSG_CANCEL);
            } else {
                map.putInt(EVENT_PROP_SOCIAL_CODE, AUTH_RESULT_CODE_FAILED);
                map.putString(EVENT_PROP_SOCIAL_MSG, AUTH_RESULT_MSG_FAILED);
            }

        } else if (baseResp instanceof SendMessageToWX.Resp) {
            SendMessageToWX.Resp resp = (SendMessageToWX.Resp) (baseResp);
            map.putString(EVENT_PROP_SOCIAL_TYPE, "SendMessageToWX.Resp");
            if (resp.openId != null) map.putString("openId", resp.openId);
            if (resp.transaction != null) map.putString("transaction", resp.transaction);

            if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
                map.putInt(EVENT_PROP_SOCIAL_CODE, SHARE_RESULT_CODE_SUCCESSFUL);
                map.putString(EVENT_PROP_SOCIAL_MSG, SHARE_RESULT_MSG_SUCCESSFUL);
            } else if (baseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL || baseResp.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                map.putInt(EVENT_PROP_SOCIAL_CODE, SHARE_RESULT_CODE_CANCEL);
                map.putString(EVENT_PROP_SOCIAL_MSG, SHARE_RESULT_MSG_CANCEL);
            } else {
                map.putInt(EVENT_PROP_SOCIAL_CODE, SHARE_RESULT_CODE_FAILED);
                map.putString(EVENT_PROP_SOCIAL_MSG, SHARE_RESULT_MSG_FAILED);
            }

        } else if (baseResp instanceof PayResp) {
            PayResp resp = (PayResp) (baseResp);
            map.putString(EVENT_PROP_SOCIAL_TYPE, "Pay.Resp");
            map.putString("returnKey", resp.returnKey);

            wxPay.setResp(resp);
        }

        if (mEventEmitter != null)
            mEventEmitter.emit(Events.EVENT_WECHAT_RESP.toString(), map);

        Log.d(TAG, LogUtils.getTraceInfo() + "WeChat_Resp callback——— map：" + map.toString());
    }

    public static void handleIntent(Intent intent) {
        if (gModule != null) {
            api.handleIntent(intent, gModule);
        }
    }

    public static void getPayInstance(Context context) {
//        if (gModule != null) {
        // 创建WXPay接口
        wxPay = WXPay.getInstance(context);
//        }
    }

    private String _getErrorMsg(int errCode) {
        switch (errCode) {
            case BaseResp.ErrCode.ERR_OK:
                return "成功";
            case BaseResp.ErrCode.ERR_COMM:
                return "普通错误类型";
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                return "微信授权失败，用户取消";
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                return "微信请求失败，请稍后重试";
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                return "微信授权失败，用户拒绝";
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                return "微信不支持";
            case BaseResp.ErrCode.ERR_BAN:
                return "签名错误";
            default:
                return "失败";
        }
    }


    private void _share(final int scene, final ReadableMap data, final Promise promise) {
        Uri uri = null;
        if (data.hasKey(SHARE_PROP_THUMB_IMAGE)) {
            String imageUrl = data.getString(SHARE_PROP_THUMB_IMAGE);
            try {
                uri = Uri.parse(imageUrl);
                // Verify scheme is set, so that relative uri (used by static resources) are not handled.
                if (uri.getScheme() == null) {
                    uri = getResourceDrawableUri(mReactContext, imageUrl);
                }
            } catch (Exception e) {
                // ignore malformed uri, then attempt to extract resource ID.
                promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
            }
        }

        if (uri != null) {
            _getImage(uri, new ResizeOptions(100, 100), new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bitmap) {
                    _share(scene, data, bitmap, promise);
                }
            });
        } else {
            _share(scene, data, null, promise);
        }
    }

    private void _getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
        BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                bitmap = bitmap.copy(bitmap.getConfig(), true);
                imageCallback.invoke(bitmap);
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                imageCallback.invoke(null);
            }
        };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (resizeOptions != null) {
            builder = builder.setResizeOptions(resizeOptions);
        }
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private static Uri getResourceDrawableUri(Context context, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        name = name.toLowerCase().replace("-", "_");
        int resId = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());

        if (resId == 0) {
            return null;
        } else {
            return new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
        }
    }

    private void _share(final int scene, final ReadableMap data, final Bitmap thumbImage, final Promise promise) {
        if (!data.hasKey(SHARE_PROP_TYPE)) {
            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
            return;
        }
        String type = data.getString(SHARE_PROP_TYPE);
        WXMediaMessage.IMediaObject mediaObject = null;
        switch (type) {
            case SHARE_TYPE_NEWS:
                mediaObject = _jsonToWebpageMedia(data);
                break;
            case SHARE_TYPE_TEXT:
                mediaObject = _jsonToTextMedia(data);
                break;
            case SHARE_TYPE_IMAGE:
                __jsonToImageUrlMedia(data, new MediaObjectCallback() {
                    @Override
                    public void invoke(@Nullable WXMediaMessage.IMediaObject mediaObject) {
                        if (mediaObject == null) {
                            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
                        } else {
                            _share(scene, data, thumbImage, mediaObject, promise);
                        }
                    }
                });
                return;
            case SHARE_TYPE_IMAGE_FILE:
                __jsonToImageFileMedia(data, new MediaObjectCallback() {
                    @Override
                    public void invoke(@Nullable WXMediaMessage.IMediaObject mediaObject) {
                        if (mediaObject == null) {
                            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
                        } else {
                            _share(scene, data, thumbImage, mediaObject, promise);
                        }
                    }
                });
                return;
            case SHARE_TYPE_VIDEO:
                mediaObject = __jsonToVideoMedia(data);
                break;
            case SHARE_TYPE_AUDIO:
                mediaObject = __jsonToMusicMedia(data);
                break;
            case SHARE_TYPE_FILE:
                mediaObject = __jsonToFileMedia(data);
                break;
        }

        if (mediaObject == null) {
            promise.reject(CODE_INVALID_ARGUMENT, MSG_INVALID_ARGUMENT);
        } else {
            _share(scene, data, thumbImage, mediaObject, promise);
        }
    }

    private void _share(int scene, ReadableMap data, Bitmap thumbImage, WXMediaMessage.IMediaObject mediaObject, final Promise promise) {

        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = mediaObject;

        if (thumbImage != null) {
            message.setThumbImage(thumbImage);
        }

        if (data.hasKey(SHARE_PROP_TITLE)) {
            message.title = data.getString(SHARE_PROP_TITLE);
        }
        if (data.hasKey(SHARE_PROP_DESP)) {
            message.description = data.getString(SHARE_PROP_DESP);
        }
        if (data.hasKey("mediaTagName")) {
            message.mediaTagName = data.getString("mediaTagName");
        }
        if (data.hasKey("messageAction")) {
            message.messageAction = data.getString("messageAction");
        }
        if (data.hasKey("messageExt")) {
            message.messageExt = data.getString("messageExt");
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = message;
        req.scene = scene;
        req.transaction = UUID.randomUUID().toString();

        if (api.sendReq(req))
            promise.resolve(null);
        else
            promise.reject(CODE_INVOKE_FAILED, MSG_INVOKE_FAILED);
    }

    private WXTextObject _jsonToTextMedia(ReadableMap data) {
        if (!data.hasKey(SHARE_PROP_DESP)) {
            return null;
        }

        WXTextObject ret = new WXTextObject();
        ret.text = data.getString(SHARE_PROP_DESP);
        return ret;
    }

    private WXWebpageObject _jsonToWebpageMedia(ReadableMap data) {
        if (!data.hasKey(SHARE_PROP_TARGET)) {
            return null;
        }

        WXWebpageObject ret = new WXWebpageObject();
        ret.webpageUrl = data.getString(SHARE_PROP_TARGET);
        if (data.hasKey("extInfo")) {
            ret.extInfo = data.getString("extInfo");
        }
        return ret;
    }

    private void __jsonToImageMedia(String imageUrl, final MediaObjectCallback callback) {
        Uri imageUri;
        try {
            imageUri = Uri.parse(imageUrl);
            // Verify scheme is set, so that relative uri (used by static resources) are not handled.
            if (imageUri.getScheme() == null) {
                imageUri = getResourceDrawableUri(mReactContext, imageUrl);
            }
        } catch (Exception e) {
            imageUri = null;
        }

        if (imageUri == null) {
            callback.invoke(null);
            return;
        }

        _getImage(imageUri, null, new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                callback.invoke(bitmap == null ? null : new WXImageObject(bitmap));
            }
        });
    }

    private void __jsonToImageUrlMedia(ReadableMap data, MediaObjectCallback callback) {
        if (!data.hasKey(SHARE_PROP_THUMB_IMAGE)) {
            callback.invoke(null);
            return;
        }
        String imageUrl = data.getString(SHARE_PROP_THUMB_IMAGE);
        __jsonToImageMedia(imageUrl, callback);
    }

    private void __jsonToImageFileMedia(ReadableMap data, MediaObjectCallback callback) {
        if (!data.hasKey(SHARE_PROP_THUMB_IMAGE)) {
            callback.invoke(null);
            return;
        }

        String imageUrl = data.getString(SHARE_PROP_THUMB_IMAGE);
        if (!imageUrl.toLowerCase().startsWith("file://")) {
            imageUrl = "file://" + imageUrl;
        }
        __jsonToImageMedia(imageUrl, callback);
    }

    private WXMusicObject __jsonToMusicMedia(ReadableMap data) {
        if (!data.hasKey(SHARE_PROP_AUDIO)) {
            return null;
        }

        WXMusicObject ret = new WXMusicObject();
        ret.musicUrl = data.getString(SHARE_PROP_AUDIO);
        return ret;
    }

    private WXVideoObject __jsonToVideoMedia(ReadableMap data) {
        if (!data.hasKey(SHARE_PROP_VIDEO)) {
            return null;
        }

        WXVideoObject ret = new WXVideoObject();
        ret.videoUrl = data.getString(SHARE_PROP_VIDEO);
        return ret;
    }

    private WXFileObject __jsonToFileMedia(ReadableMap data) {
        if (!data.hasKey("filePath")) {
            return null;
        }
        return new WXFileObject(data.getString("filePath"));
    }

    // TODO: 实现sendRequest、sendSuccessResponse、sendErrorCommonResponse、sendErrorUserCancelResponse


    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

    private interface MediaObjectCallback {
        void invoke(@Nullable WXMediaMessage.IMediaObject mediaObject);
    }

}
