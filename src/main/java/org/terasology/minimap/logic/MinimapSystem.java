// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.minimap.logic;

import org.terasology.minimap.overlays.MinimapOverlay;

/**
 * This class represents the connection to the event system and maintains the HUD element.
 */
public interface MinimapSystem {

    /**
     * @param overlay the overlay to add
     */
    void addOverlay(MinimapOverlay overlay);

    /**
     * @param overlay the overlay to remove
     */
    void removeOverlay(MinimapOverlay overlay);
}
