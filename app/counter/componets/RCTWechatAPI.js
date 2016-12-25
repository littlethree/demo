/*************************************************************************
 * Description: 微信API组件
 * Author: raojia
 * Mail: raojia@le.com
 * Created Time: 2016-12-20
 * Modified Time: 2016-12-20
 ************************************************************************/
'use strict';

import { NativeModules, NativeAppEventEmitter } from 'react-native';
import promisify from 'es6-promisify';

const WeChatAPI = NativeModules.WeChatModule;

function translateError(err, result) {
    if (!err) {
        return this.resolve(result);
    }
    if (typeof err === 'object') {
        if (err instanceof Error) {
            return this.reject(ret);
        }
        return this.reject(Object.assign(new Error(err.message), { errCode: err.errCode }));
    } else if (typeof err === 'string') {
        return this.reject(new Error(err));
    }
    this.reject(Object.assign(new Error(), { origin: err }));
}

// Save callback and wait for future event.
let savedCallback = undefined;
function waitForResponse(type) {
    return new Promise((resolve, reject) => {
        if (savedCallback) {
            savedCallback('User canceled.');
        }
        savedCallback = result => {
            if (result.type !== type) {
                // if (__DEV__) {
                //  throw new Error('Unsupported response type: ' + resp.type);
                // }
                return;
            }
            savedCallback = undefined;
            // if (result.errCode !== 0) {
            //     const err = new Error(result.errMsg);
            //     err.errCode = result.errCode;
            //     reject(err);
            // } else {
            //     resolve(result);
            // }
            resolve(result);
        };
    });
}

NativeAppEventEmitter.addListener('WeChat_Resp', resp => {
    const callback = savedCallback;
    savedCallback = undefined;
    callback && callback(resp);
});


function wrapCheckApi(nativeFunc) {
    if (!nativeFunc) {
        return undefined;
    }
    const promisified = promisify(nativeFunc, translateError);
    return (...args) => {
        return promisified(...args);
    };
}

/**
 * 微信是否安装
 * @method isWXAppInstalled
 * @return {Promise}
 */
export const isWXAppInstalled = wrapCheckApi(WeChatAPI.isWXAppInstalled);

/**
 * 微信版本是否支持API
 * @method isWXAppSupportApi
 * @return {Promise}
 */
export const isWXAppSupportApi = wrapCheckApi(WeChatAPI.isWXAppSupportApi);

/**
 * 获得微信版本
 * @method getApiVersion
 * @return {String} the api version string
 */
export const getApiVersion = wrapCheckApi(WeChatAPI.getApiVersion);

/**
 * 调起微信APP
 * @method openWXApp
 * @return {Promise}
 */
export const openWXApp = wrapCheckApi(WeChatAPI.openWXApp);


function wrapApi(nativeFunc) {
    if (!nativeFunc) {
        return undefined;
    }

    const promisified = promisify(nativeFunc, translateError);
    return async function (...args) {
        if (!WeChatAPI.isAppRegistered) {
            throw new Error('注册应用失败');
        }
        const checkInstalled = await isWXAppInstalled();
        if (!checkInstalled) {
            throw new Error('没有安装微信!');
        }
        const checkSupport = await isWXAppSupportApi();
        if (!checkSupport) {
            throw new Error('微信版本不支持');
        }
        return await promisified(...args);
    };
}

const nativeSendAuthRequest = wrapApi(WeChatAPI.sendAuth);
const nativeShareToTimelineRequest = wrapApi(WeChatAPI.shareToTimeline);
const nativeShareToSessionRequest = wrapApi(WeChatAPI.shareToSession);
const nativePayRequest = wrapApi(WeChatAPI.pay);

/**
 * 微信登陆
 * @method sendAuth
 * @return {Promise}
 */
export function sendAuth(config) {
    const scope = (config && config.scope) || 'snsapi_userinfo';
    return nativeSendAuthRequest({ scope })
        .then(() => waitForResponse("SendAuth.Resp"));
}

/**
 * 获取TOKEN
 * @method getToken
 * @return {Promise}
 */
export function getToken(data) {
    if (!(data && data.appid && data.secret && data.code)) return;
    return fetch(`https://api.weixin.qq.com/sns/oauth2/access_token?appid=${data.appid}&secret=${data.secret}&code=${data.code}&grant_type=authorization_code`)        
        .then(response => response.json()).catch(error => reject(error));

}

/**
 * 微信分享朋友圈
 * @method shareToTimeline
 * @return {Promise}
 */
export function shareToTimeline(data) {
    return nativeShareToTimelineRequest(data)
        .then(() => waitForResponse("SendMessageToWX.Resp"));
}

/**
 * 微信分享好友
 * @method shareToSession
 * @return {Promise}
 */
export function shareToSession(data) {
    return nativeShareToSessionRequest(data)
        .then(() => waitForResponse("SendMessageToWX.Resp"));
}

/**
 * 微信支付
 * @method pay
 * @return {Promise}
 */
export function pay(data) {
    return nativePayRequest(data)
        .then(() => waitForResponse("Pay.Resp"));
}