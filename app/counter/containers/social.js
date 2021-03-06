
/**
 * 第三方分享和登录
 */
'use strict';

import React, { Component, PropTypes } from 'react';
import {
    StyleSheet,
    Text,
    View,
    TextInput,
    Dimensions,
    Image,
    TouchableOpacity,
    Modal,
    TouchableHighlight
} from 'react-native';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as WeChat from '../componets/RCTWechatAPI';
import * as QQ from '../componets/RCTQQAPI';
import * as Weibo from '../componets/RCTWeiboAPI';

const { width, height } = Dimensions.get('window');

class social extends Component {
    constructor(props) {
        super(props);
        this.state = {
            wbApiVersion: 'waiting...',
            isWBInstalled: 'waiting...',
            isWBSupportApi: 'waiting...',
            qqApiVersion: 'waiting...',
            isQQInstalled: 'waiting...',
            isQQSupportApi: 'waiting...',
            wxApiVersion: 'waiting...',
            isWXAppSupportApi: 'waiting...',
            isWXAppInstalled: 'waiting...',
            callbackStr: '',
        };
    }

    async componentDidMount() {
        try {
            this.setState({
                wbApiVersion: await Weibo.getApiVersion(),
                isWBInstalled: await Weibo.isAppInstalled(),
                isWBSupportApi: await Weibo.isAppSupportApi(),
                qqApiVersion: await QQ.getApiVersion(),
                isQQInstalled: await QQ.isAppInstalled(),
                isQQSupportApi: await QQ.isAppSupportApi(),
                wxApiVersion: await WeChat.getApiVersion(),
                isWXAppSupportApi: await WeChat.isAppSupportApi(),
                isWXAppInstalled: await WeChat.isAppInstalled()
            });
            console.log(this.state);
        } catch (e) {
            console.error(e);
        }
        console.log(WeChat);
        // console.log('getApiVersion', typeof WeChat.getApiVersion);
        // console.log('getWXAppInstallUrl', typeof WeChat.getWXAppInstallUrl);
        // console.log('sendRequest', typeof WeChat.sendRequest);
        // console.log('registerApp', typeof WeChat.registerApp);
        // console.log('sendErrorCommonResponse', typeof WeChat.sendErrorCommonResponse);
        // console.log('sendErrorUserCancelResponse', typeof WeChat.sendErrorUserCancelResponse);
        // console.log('sendAuthRequest', typeof WeChat.sendAuthRequest);
        // console.log('getWXAppInstallUrl', typeof WeChat.getWXAppInstallUrl);
        // console.log('openWXApp', typeof WeChat.openWXApp);
        // console.log('registerAppWithDescription', typeof WeChat.registerAppWithDescription);
        // console.log('isWXAppSupportApi', typeof WeChat.isWXAppSupportApi);
        // console.log('isWXAppInstalled', typeof WeChat.isWXAppInstalled);
    }

    //微博登陆
    loginToWeibo = () => {
        Weibo.login({
            scope: 'all', // 默认 'all'
        }).catch(error => {
            console.log(error.message);
        }).then(resp => {
            console.log(resp);
            this.setState({
                callbackStr: JSON.stringify(resp)
            });
        });
        // Weibo.isAppInstalled()
        //     .then((isInstalled) => {
        //         if (isInstalled) {
        //             Weibo.login({
        //                 scope: 'all', // 默认 'all'
        //             })
        //                 .catch(error => {
        //                     console.log(error.message);
        //                 }).then(resp => {
        //                     console.log(resp);
        //                     this.setState({
        //                         callbackStr: JSON.stringify(resp)
        //                     });
        //                 });
        //         } else {
        //             alert('没有安装微博，请您安装微博之后再试');
        //         }
        //     });
    }

    //微博分享
    shareToWeibo = () => {
        Weibo.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    Weibo.shareToWeibo({
                        type: Weibo.SHARE_TYPE_NEWS,
                        thumbImage: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        imageUrl: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        title: '乐视云开放框架介绍',
                        description: '应用工厂演示QQ分享实例',
                        text: '应用工厂演示QQ分享实例。LeValley是一套提供组件化开放功能的跨平台客户端框架，采用前端开发模式来定义移动应用，功能分解化，让模块开发变得快速而可维护。',
                        webpageUrl: 'http://www.lecloud.com/zh-cn/'
                    }).catch((error) => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        this.setState({
                            callbackStr: JSON.stringify(resp)
                        });
                    });

                } else {
                    alert('没有安装微博，请您安装微博之后再试');
                }
            });
    }

    //QQ登陆
    loginToQQ = () => {

        QQ.login('get_simple_userinfo')
            .catch(error => {
                console.log(error.message);
            }).then(resp => {
                console.log(resp);
                this.setState({
                    callbackStr: JSON.stringify(resp)
                });
            });


        // QQ.isAppInstalled()
        //     .then((isInstalled) => {
        //         if (isInstalled) {
        //             QQ.login('get_simple_userinfo')
        //                 .catch(error => {
        //                     console.log(error.message);
        //                 }).then(resp => {
        //                     console.log(resp);
        //                     this.setState({
        //                         callbackStr: JSON.stringify(resp)
        //                     });
        //                 });
        //         } else {
        //             alert('没有安装QQ，请您安装QQ之后再试');
        //         }
        //     });
    }


    //QQ分享给好友
    shareToQQ = () => {
        QQ.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    QQ.shareToQQ({
                        type: QQ.SHARE_TYPE_NEWS,
                        title: '乐视云开放框架介绍',
                        thumbImage: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        description: '应用工厂演示QQ分享实例，LeValley框架值得期待',
                        webpageUrl: 'http://www.lecloud.com/zh-cn/',
                        appName: '应用工厂演示',
                        cflag: 2
                    }).catch((error) => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        this.setState({
                            callbackStr: JSON.stringify(resp)
                        });
                    });

                } else {
                    alert('没有安装QQ，请您安装QQ之后再试');
                }
            });

    }

    //QQ分享给QZone
    shareToQzone = () => {
        QQ.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    QQ.shareToQzone({
                        type: QQ.SHARE_TYPE_NEWS,
                        title: '乐视云开放框架介绍',
                        thumbImage: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        imageUrl: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg,http://s2.51cto.com/wyfs02/M00/86/23/wKioL1e1psOjhcELAAMgatkMzjE767.png',
                        description: '应用工厂演示QQ分享实例，LeValley框架值得期待',
                        webpageUrl: 'http://www.lecloud.com/zh-cn/',
                        appName: '应用工厂演示',
                        cflag: 1
                    }).catch((error) => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        this.setState({
                            callbackStr: JSON.stringify(resp)
                        });
                    });

                } else {
                    alert('没有安装QQ，请您安装QQ之后再试');
                }
            });
    }

    //微信登陆
    loginToWeixin = () => {
        WeChat.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    WeChat.sendAuth({
                        config: { scope: 'snsapi_userinfo', }
                    }).catch(error => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        if (resp && resp.errCode == 0) {
                            WeChat.getToken(resp)
                                .then(json => {
                                    console.log(json);
                                    this.setState({
                                        callbackStr: JSON.stringify(json)
                                    });
                                });
                        } else {
                            this.setState({
                                callbackStr: JSON.stringify(resp)
                            });
                        }
                    });

                } else {
                    alert('没有安装微信，请您安装微信之后再试');
                }
            });

    }

    //微信分享给朋友
    shareToFrends = () => {
        WeChat.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    WeChat.shareToSession({
                        title: '应用工厂演示', // WeChat app treat title as file name
                        thumbImage: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        type: WeChat.SHARE_TYPE_NEWS,
                        description: '应用工厂演示微信分享范例',
                        webpageUrl: 'http://www.lecloud.com/zh-cn/',
                    }).catch((error) => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        this.setState({
                            callbackStr: JSON.stringify(resp)
                        });
                    });

                } else {
                    alert('没有安装微信，请您安装微信之后再试');
                }
            });

    }

    //微信分享到朋友圈
    shareToPyq = () => {
        WeChat.isAppInstalled()
            .then((isInstalled) => {
                if (isInstalled) {
                    WeChat.shareToTimeline({
                        title: '应用工厂演示',
                        thumbImage: 'http://cdn.huodongxing.com/file/20160426/11E69610D2AC0F75D7EB61C48EDEA840FB/30132422640007503.jpg',
                        type: WeChat.SHARE_TYPE_VIDEO,
                        description: '应用工厂演示微信分享范例',
                        videoUrl: 'http://www.lecloud.com/zh-cn/',
                        webpageUrl: 'http://www.lecloud.com/zh-cn/'
                    }).catch((error) => {
                        console.log(error.message);
                    }).then(resp => {
                        console.log(resp);
                        this.setState({
                            callbackStr: JSON.stringify(resp)
                        });
                    });
                } else {
                    alert('没有安装微信，请您安装微信之后再试');
                }
            });
    }


    render() {
        const { selectedDevice, posts, isFetching, lastUpdated } = this.props;
        const isEmpty = posts === null;

        let { deviceInfo } = this.props;
        return (
            <View style={[styles.container]}>

                <View style={{ top: 20, left: 10, right: 10, position: 'absolute' }}>
                    <Text>微博：SDK版本{this.state.wbApiVersion} |{String(this.state.isWBInstalled ? '已安装' : '未安装')} |{String(this.state.isWBSupportApi ? '支持' : '不支持')}</Text>
                    <Text>QQ ：SDK版本{this.state.qqApiVersion} |{String(this.state.isQQInstalled ? '已安装' : '未安装')} |{String(this.state.isQQSupportApi ? '支持' : '不支持')}</Text>
                    <Text>微信：SDK版本{this.state.wxApiVersion} |{String(this.state.isWXAppInstalled ? '已安装' : '未安装')} |{String(this.state.isWXAppSupportApi ? '支持' : '不支持')}</Text>
                    {this.state.callbackStr ? <Text>回调结果：{'\r\n'}{String(this.state.callbackStr)}</Text> : null}
                </View>

                <View style={[styles.wbContainer]}>
                    <TouchableOpacity onPress={this.loginToWeibo}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/loginWeibo.png')} style={styles.bigcodeimage} />
                            <Text>微博登录</Text>
                        </View>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.shareToWeibo}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/shareWeibo.png')} style={styles.bigcodeimage} />
                            <Text>分享到微博</Text>
                        </View>
                    </TouchableOpacity>
                </View>

                <View style={[styles.qqContainer]}>
                    <TouchableOpacity onPress={this.loginToQQ}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/loginQQ.png')} style={styles.bigcodeimage} />
                            <Text>QQ登录</Text>
                        </View>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.shareToQQ}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/shareQQ.png')} style={styles.bigcodeimage} />
                            <Text>分享到好友</Text>
                        </View>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.shareToQzone}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/shareQzone.png')} style={styles.bigcodeimage} />
                            <Text>分享到空间</Text>
                        </View>
                    </TouchableOpacity>
                </View>
                <View style={[styles.wxContainer]}>
                    <TouchableOpacity onPress={this.loginToWeixin}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/weixindenglu.png')} style={styles.bigcodeimage} />
                            <Text>微信登录</Text>
                        </View>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.shareToFrends}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/shareWeixin.png')} style={styles.bigcodeimage} />
                            <Text>分享到好友</Text>
                        </View>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.shareToPyq}>
                        <View style={{ alignItems: 'center' }}>
                            <Image source={require('../../img/sharePyq.png')} style={styles.bigcodeimage} />
                            <Text>分享到朋友圈</Text>
                        </View>
                    </TouchableOpacity>
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center'
    },
    wbContainer: {
        borderRadius: 10,
        justifyContent: 'space-around',
        alignItems: 'center',
        width: width / 3 * 2,
        height: height / 5,
        flexDirection: 'row',
        position: 'absolute',
        bottom: 200,
        left: width / 6
    },
    qqContainer: {
        borderRadius: 10,
        justifyContent: 'space-around',
        alignItems: 'center',
        width: width / 3 * 2,
        height: height / 5,
        flexDirection: 'row',
        position: 'absolute',
        bottom: 100,
        left: width / 6
    },
    wxContainer: {
        borderRadius: 10,
        justifyContent: 'space-around',
        alignItems: 'center',
        width: width / 3 * 2,
        height: height / 5,
        flexDirection: 'row',
        position: 'absolute',
        bottom: 10,
        left: width / 6
    },
    innerContainerCancel: {
        marginTop: 6,
        borderRadius: 10,
        justifyContent: 'center',
        alignItems: 'center',
        width: width / 3 * 2,
        height: 30,
        backgroundColor: '#fff',

    },
    bigcodeimage: {
        width: width / 6,
        height: width / 6,
        marginBottom: 6
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
    buttonContainer: {
        flex: 0,
        flexDirection: 'row',
        justifyContent: 'space-around'
    },
    button: {
        padding: 5,
        margin: 5,
        borderWidth: 1,
        borderColor: 'white',
        borderRadius: 3,
        backgroundColor: 'grey',
    }
});



//连接Redux
export default social;