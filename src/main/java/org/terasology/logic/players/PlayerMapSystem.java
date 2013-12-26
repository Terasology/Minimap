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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.inventory.ToolbarNextButton;
import org.terasology.input.binds.minimap.HideMapButton;
import org.terasology.input.binds.minimap.ToggleMapAxisButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.event.SelectItemRequest;
import org.terasology.rendering.hudElement.HUDElementMap;
import org.terasology.rendering.logic.manager.HUD;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * @author mkienenb
 */
@RegisterSystem
public class PlayerMapSystem implements ComponentSystem {

	String HUD_ELEMENT_MAP_ID = "terrain:map";

    @In
    private LocalPlayer localPlayer;

    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity) {
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        int nextSlot = (character.selectedItem + 1) % 10;
        localPlayer.getCharacterEntity().send(new SelectItemRequest(nextSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onHideMapButton(HideMapButton event, EntityRef entity) {
        if (event.isDown()) {
            HUD hud = CoreRegistry.get(HUD.class);
			HUDElement hudElement = hud.getHUDElementByHUDElementId(HUD_ELEMENT_MAP_ID);
            HUDElementMap hudElementMap;
            if (null == hudElement) {
            	hudElementMap = new HUDElementMap();
            	hud.addHUDElement(hudElementMap);
            } else {
            	hudElementMap = (HUDElementMap)hudElement;
                hudElementMap.toggleVisibility();
            }
            
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onToggleMapAxisButton(ToggleMapAxisButton event, EntityRef entity) {
        if (event.isDown()) {
            HUD hud = CoreRegistry.get(HUD.class);
            HUDElement hudElement = hud.getHUDElementByHUDElementId(HUD_ELEMENT_MAP_ID);
            if (null == hudElement) {
        		return;
            }
            
            HUDElementMap hudElementMap = (HUDElementMap)hudElement;

            hudElementMap.toggleMapGridAxis();

            event.consume();
        }
    }
}
