// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.minimap.input.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;

/**
 * Registers a binding to decrease the zoom level.
 */
@RegisterBindButton(id = "decreaseMinimapViewingAxisOffset", description = "Decrease Minimap Viewing Axis Offset")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.NUMPAD_MINUS)
public class DecreaseZoomButton extends BindButtonEvent {
}
