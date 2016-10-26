
import React, { Component } from 'react';
import {
    StyleSheet,
    Text,
    View,
} from 'react-native';
import { connect } from 'react-redux';

import { plus } from '../actions/action';

class Counter02 extends Component {

    handleClick = () => {
        this.props.dispatch(plus(-1));
    }

    render() {
        const { dispatch, value } = this.props;
        return (
            <View style={{ flexDirection: 'row' }} >
                <Text style={{ fontSize: 20, marginRight: 20 }} >计数器：{this.props.value}</Text>
                <Text style={{ fontSize: 20 }} onPress={() => this.handleClick()} >点击  -1</Text>
            </View>
        );
    }
}

const mapStateToProps = state => {
    const { value } = state.calculate;
    return { value };
};

export default connect(mapStateToProps)(Counter02);