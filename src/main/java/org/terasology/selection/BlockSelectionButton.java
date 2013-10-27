package org.terasology.selection;

import org.lwjgl.input.Keyboard;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;

/**
 * @author synopia
 */
@RegisterBindButton(id = "blockSelection", description = "Select an area of blocks")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_LCONTROL)
public class BlockSelectionButton extends BindButtonEvent {
}
