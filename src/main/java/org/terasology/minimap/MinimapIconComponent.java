// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.minimap;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Information about an icon to be drawn on the mini map.
 *
 * Attach this component to an entity that should be drawn on the map.
 */
public class MinimapIconComponent implements Component<MinimapIconComponent> {
    /**
     * The {@link org.terasology.gestalt.assets.ResourceUrn} as string for the icon texture to be drawn on the mini map.
     *
     * <strong>Note:</strong> The resource urn MUST point to a {@link org.terasology.engine.rendering.assets.texture.Texture},
     * sub-textures from atlas are currently not supported (the URN is of the form "{moduleName}:{resourceName}").
     */
    @Replicate
    public String iconUrn;

    @Override
    public void copyFrom(MinimapIconComponent other) {
        this.iconUrn = other.iconUrn;
    }
}
