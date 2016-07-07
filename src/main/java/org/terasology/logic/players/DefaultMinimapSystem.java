/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.minimap.DecreaseZoomButton;
import org.terasology.input.binds.minimap.IncreaseZoomButton;
import org.terasology.input.binds.minimap.ToggleMinimapButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector3i;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.minimap.rendering.nui.layers.MinimapHUDElement;
import org.terasology.registry.In;
import org.terasology.registry.Share;
//import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.generator.WorldGenerator;

/**
 * This class represents the connection to the event system and maintains the HUD element.
 */
@RegisterSystem
@Share(MinimapSystem.class)
public class DefaultMinimapSystem extends BaseComponentSystem implements MinimapSystem {

    public static final String HUD_ELEMENT_MAP_ID = "minimap";

    private MinimapHUDElement minimapHUDElement;

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private WorldProvider worldProvider;

    @In
    private WorldGenerator worldGenerator;

    @Override
    public void initialise() {
        Rect2f rc = Rect2f.createFromMinAndSize(0, 0, 1, 1);
        minimapHUDElement = nuiManager.getHUD().addHUDElement(HUD_ELEMENT_MAP_ID, MinimapHUDElement.class, rc);
        minimapHUDElement.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {

            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });

        // TODO: get the sea level height from elsewhere (WorldInfo/GameInfo/GameManifest?)
        int seaLevel = worldGenerator == null ? 10 : worldGenerator.getWorld().getSeaLevel();
        minimapHUDElement.setHeightRange(seaLevel, seaLevel + 64);
        minimapHUDElement.setWorldProvider(worldProvider);
    }

    @Override
    public void addOverlay(MinimapOverlay overlay) {
        minimapHUDElement.addOverlay(overlay);
    }

    @Override
    public void removeOverlay(MinimapOverlay overlay) {
        minimapHUDElement.removeOverlay(overlay);
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onToggleMinimapButton(ToggleMinimapButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.setVisible(!minimapHUDElement.isVisible());
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onIncreaseZoomButton(IncreaseZoomButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.changeZoom(1);
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDecreaseZoomButton(DecreaseZoomButton event, EntityRef entity) {
        if (event.isDown()) {
            minimapHUDElement.changeZoom(-1);
            event.consume();
        }
    }

    @ReceiveEvent
    public void onDestroyBlock(DoDestroyEvent event, EntityRef entity, LocationComponent locationComp) {
        minimapHUDElement.updateLocation(new Vector3i(locationComp.getWorldPosition()));
    }

    @ReceiveEvent
    public void onPlaceBlock(PlaceBlocks event, EntityRef entity) {
        for (Vector3i pos : event.getBlocks().keySet()) {
            minimapHUDElement.updateLocation(pos);
        }
    }
}
