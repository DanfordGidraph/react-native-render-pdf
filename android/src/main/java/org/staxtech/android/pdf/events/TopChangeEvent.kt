/**
 * Original Author: Wonday (@wonday.org) Copyright (c) 2017-present
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.staxtech.android.pdf.events;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;

public class TopChangeEvent extends Event<TopChangeEvent> {
    private final WritableMap eventData;

    public TopChangeEvent(int surfaceId, int viewTag, WritableMap data) {
        super(surfaceId, viewTag);
        eventData = data;
    }

    @Override
    public String getEventName() {
        return "topChange";
    }

    @Nullable
    @Override
    protected WritableMap getEventData() {
        return eventData;
    }
}