// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.minimap.overlays;

import java.util.Collection;
import java.util.Collections;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.nui.Color;
import org.terasology.utilities.Assets;

/**
 * Useful helper methods for overlays
 */
public final class MinimapOverlays {

    private MinimapOverlays() {
        // no instances
    }

    /**
     * @return a new overlays that renders a small icon at (0/0)
     */
    public static MinimapIconOverlay createOriginIconOverlay() {
        Color iconColor = new Color(192, 192, 192);
        Texture icon = Assets.getTexture("Minimap:maps-center-direction").get();
        Collection<ImmutableVector2i> points = Collections.singleton(BaseVector2i.ZERO);
        MinimapIconOverlay ovly = new MinimapIconOverlay(points, icon);
        ovly.setColor(iconColor);
        ovly.setIconSize(new Vector2f(4, 4));
        return ovly;
    }
}
