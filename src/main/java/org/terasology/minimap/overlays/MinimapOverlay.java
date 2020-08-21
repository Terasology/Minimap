// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.minimap.overlays;

import org.terasology.math.geom.Rect2f;
import org.terasology.nui.Canvas;

/**
 * An overlay for the minimap
 */
public interface MinimapOverlay {

    /**
     * @param canvas the canvas to use for rendering
     * @param worldRect the world rect that is currently covered
     */
    void render(Canvas canvas, Rect2f worldRect);

    /**
     * @return the z position (will be rendering according to this value)
     */
    int getZOrder();
}
