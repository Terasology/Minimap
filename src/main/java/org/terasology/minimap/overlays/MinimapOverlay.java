/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.minimap.overlays;

import org.terasology.math.geom.Rect2f;
import org.terasology.rendering.nui.Canvas;

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
