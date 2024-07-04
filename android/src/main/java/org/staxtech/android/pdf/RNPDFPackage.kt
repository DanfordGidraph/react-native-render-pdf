/**
 * Original Author: Wonday (@wonday.org) Copyright (c) 2017-present
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package org.staxtech.android.pdf

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext

class RNPDFPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return emptyList()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext) = listOf(PdfManager(reactContext))
}
