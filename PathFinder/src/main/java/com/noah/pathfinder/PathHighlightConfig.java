package com.noah.pathfinder;

import net.runelite.client.config.*;
import net.runelite.client.util.OSType;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup("pathfinder")
public interface PathHighlightConfig extends Config
{
	@Alpha
	@ConfigItem(
			keyName = "highlightPathColor",
			name = "Guide Color",
			description = "Configures the color of the path"
	)
	default Color highlightPathColor()
	{
		return new Color(0, 127, 0, 127);
	}

	@Alpha
	@ConfigItem(
			keyName = "secondaryPathColor",
			name = "Path Color",
			description = "Configures the secondary path color"
	)
	default Color secondaryPathColor()
	{
		return new Color(0, 127, 0, 127);
	}

	@ConfigItem(
			keyName = "skipJumpedTiles",
			name = "Hide Tiles Jumped Over",
			description = "Hides tiles you would jump over when you run"
	)
	default boolean skipJumpedTiles() { return true; }

	@ConfigItem(
			keyName = "displaySetting",
			name = "Display Setting",
			description = "Configures when the path should be displayed"
	)
	default PathDisplaySetting displaySetting() { return PathDisplaySetting.ALWAYS_DISPLAY; }

	@ConfigItem(
			keyName = "displayKeybind",
			name = "Keybind",
			description = "Sets the keybind if configured to display the path on toggle or while a key is pressed.\nCan be combined with Shift, Ctrl and Alt as well as Command on Mac."
	)
	default Keybind displayKeybind() {
		OSType osType = OSType.getOSType();
		int modifier;
		if (osType == OSType.MacOS){
			modifier = InputEvent.META_DOWN_MASK; //Command on Mac keyboard
		} else {
			modifier = InputEvent.CTRL_DOWN_MASK;
		}
		return new Keybind(KeyEvent.VK_Z, modifier);
	}

	@ConfigItem(
			keyName = "keepOnClick",
			name = "Keep Path on Click",
			description = "Maintain the finder path after click until destination is reached."
	)
	default boolean keepOnClick() { return true; }

	@ConfigItem(
			keyName = "disableGuide",
			name = "Disable guide when moving",
			description = "Maintain the finder path after click until destination is reached."
	)
	default boolean disableGuide() { return true; }
}
