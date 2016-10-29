'use strict';

import React, {Component} from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from 'react-native';

import RCTLeVideoView from '../componets/RCTLeVideoView';

class VideoPlayer extends Component {
    constructor(props) {
        super(props);
        this.onLoad = this.onLoad.bind(this);
        this.onProgress = this.onProgress.bind(this);
    }

    state = {
        paused: false,
        volume: 1,
        duration: 0.0,
        currentTime: 0.0,
    };

    onLoad(data) {
        this.setState({ duration: data.duration });
    }

    onProgress(data) {
        this.setState({ currentTime: data.currentTime });
    }

    getCurrentTimePercentage() {
        if (this.state.currentTime > 0) {
            return parseFloat(this.state.currentTime) / parseFloat(this.state.duration);
        } else {
            return 0;
        }
    }


    renderVolumeControl(volume) {
        const isSelected = (this.state.volume == volume);

        return (
            <TouchableOpacity onPress={() => { this.setState({ volume: volume }); } }>
                <Text style={[styles.controlOption, { fontWeight: isSelected ? "bold" : "normal" }]}>
                    {volume * 100}%
                </Text>
            </TouchableOpacity>
        );
    }

    render() {
        const flexCompleted = this.getCurrentTimePercentage() * 100;
        const flexRemaining = (1 - this.getCurrentTimePercentage()) * 100;

        //网络地址
        const uri = "http://cache.utovr.com/201601131107187320.mp4";
        //标准点播
        const vod = { playMode: 10000, uuid: "838389", vuid: "200271100", businessline: "102", saas: true, pano: false, hasSkin: false };
        //活动直播
        const live = { playMode: 10002, actionId: "A2016062700000gx", usehls: false, customerId: "838389", businessline: "102", cuid: "", utoken: "", pano: false, hasSkin: false };

        return (
            <View style={styles.container}>
                <TouchableOpacity style={styles.fullScreen} onPress={() => { this.setState({ paused: !this.state.paused }); } }>
                    <RCTLeVideoView source={vod}
                        style={styles.fullScreen}
                        paused={this.state.paused}
                        volume={this.state.volume}
                        onLoad={this.onLoad}
                        onProgress={this.onProgress}
                        onEnd={() => { console.log('Done!'); } }
                        repeat={true} />
                </TouchableOpacity>

                <View style={styles.controls}>
                    <View style={styles.generalControls}>
                        <View style={styles.volumeControl}>
                            {this.renderVolumeControl(0.5) }
                            {this.renderVolumeControl(1) }
                            {this.renderVolumeControl(1.5) }
                        </View>
                    </View>

                    <View style={styles.trackingControls}>
                        <View style={styles.progress}>
                            <View style={[styles.innerProgressCompleted, { flex: flexCompleted }]} />
                            <View style={[styles.innerProgressRemaining, { flex: flexRemaining }]} />
                        </View>
                    </View>
                </View>
            </View>
        );
    }
}


const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'black',
    },
    fullScreen: {
        position: 'absolute',
        top: 0,
        left: 0,
        bottom: 0,
        right: 0,
    },
    controls: {
        backgroundColor: "transparent",
        borderRadius: 5,
        position: 'absolute',
        bottom: 20,
        left: 20,
        right: 20,
    },
    progress: {
        flex: 1,
        flexDirection: 'row',
        borderRadius: 3,
        overflow: 'hidden',
    },
    innerProgressCompleted: {
        height: 20,
        backgroundColor: '#cccccc',
    },
    innerProgressRemaining: {
        height: 20,
        backgroundColor: '#2C2C2C',
    },
    generalControls: {
        flex: 1,
        flexDirection: 'row',
        borderRadius: 4,
        overflow: 'hidden',
        paddingBottom: 10,
    },
    rateControl: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'center',
    },
    volumeControl: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'center',
    },
    resizeModeControl: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
    },
    controlOption: {
        alignSelf: 'center',
        fontSize: 11,
        color: "white",
        paddingLeft: 2,
        paddingRight: 2,
        lineHeight: 12,
    },
});


export default VideoPlayer;