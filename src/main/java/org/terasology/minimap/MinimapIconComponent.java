/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.minimap;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * Information about an icon to be drawn on the mini map.
 *
 * Attach this component to an entity that should be drawn on the map.
 */
public class MinimapIconComponent implements Component {
    /**
     * The {@link org.terasology.assets.ResourceUrn} as string for the icon texture to be drawn on the mini map.
     *
     * <strong>Note:</strong> The resource urn MUST point to a {@link org.terasology.engine.rendering.assets.texture.Texture},
     * sub-textures from atlas are currently not supported (the URN is of the form "{moduleName}:{resourceName}").
     */
    @Replicate
    public String iconUrn;
}
