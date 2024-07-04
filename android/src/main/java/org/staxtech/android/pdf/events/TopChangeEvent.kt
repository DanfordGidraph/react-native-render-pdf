/**
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package org.staxtech.android.pdf.events

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

internal class TopChangeEvent(surfaceId: Int, viewTag: Int, private val eventData: WritableMap) :
    Event<TopChangeEvent>(surfaceId, viewTag) {
    override fun getEventName(): String {
        return "topChange"
    }

    override fun getEventData(): WritableMap {
        return eventData
    }
}