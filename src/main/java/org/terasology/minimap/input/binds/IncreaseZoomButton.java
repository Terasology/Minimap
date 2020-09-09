// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.minimap.input.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;

/**
 * Registers a binding to increase the zoom level.
 */
@RegisterBindButton(id = "increaseMinimapViewingAxisOffset", description = "Increase Minimap Viewing Axis Offset")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.NUMPAD_PLUS)
public class IncreaseZoomButton extends BindButtonEvent {
}
