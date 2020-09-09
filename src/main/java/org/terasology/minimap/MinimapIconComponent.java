// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.minimap;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * Information about an icon to be drawn on the mini map.
 * <p>
 * Attach this component to an entity that should be drawn on the map.
 */
public class MinimapIconComponent implements Component {
    /**
     * The {@link org.terasology.assets.ResourceUrn} as string for the icon texture to be drawn on the mini map.
     *
     * <strong>Note:</strong> The resource urn MUST point to a {@link org.terasology.rendering.assets.texture.Texture},
     * sub-textures from atlas are currently not supported (the URN is of the form "{moduleName}:{resourceName}").
     */
    @Replicate
    public String iconUrn;
}
