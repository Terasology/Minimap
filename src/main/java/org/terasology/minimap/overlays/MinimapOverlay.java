// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.minimap.overlays;

import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;

/**
 * An overlay for the minimap
 */
public interface MinimapOverlay {

    /**
     * @param canvas the canvas to use for rendering
     * @param worldRect the world rect that is currently covered
     */
    void render(Canvas canvas, Rectanglei worldRect);

    /**
     * @return the z position (will be rendering according to this value)
     */
    int getZOrder();
}
