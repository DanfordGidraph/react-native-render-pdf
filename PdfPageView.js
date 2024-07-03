/**
 * Original Author: Wonday (@wonday.org) Copyright (c) 2017-present
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

// TODO: Convert to functional component

'use strict';

import React, { PureComponent } from 'react';

import PropTypes from 'prop-types';
import { ViewPropTypes } from 'deprecated-react-native-prop-types';
import {
    requireNativeComponent,
} from 'react-native';

export default class PdfPageView extends PureComponent {
    _getStylePropsProps = () => {
        const { width, height } = this.props;
        if (width || height) {
            return { width, height };
        }
        return {};
    };

    render() {
        const {
            style,
            ...restProps
        } = this.props;
        return (
            <PdfPageViewCustom
                {...restProps}
                style={[style, this._getStylePropsProps()]}
            />
        );

    }
}

PdfPageView.propTypes = {
    ...ViewPropTypes,
    fileNo: PropTypes.number,
    page: PropTypes.number,
    width: PropTypes.number,
    height: PropTypes.number
};

PdfPageView.defaultProps = {
    style: {}
};

let PdfPageViewCustom = requireNativeComponent('RCTPdfPageView', PdfPageView, { nativeOnly: {} });
