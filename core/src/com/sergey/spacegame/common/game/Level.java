package com.sergey.spacegame.common.game;

import java.util.HashMap;

import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.game.command.Command;

public class Level {
	private HashMap<String, Command> commands = new HashMap<>();
	
	public static Level tempLevelGet() {
		String json = "{\"commands\":{\"move\":{\"type\":\"java\",\"class\":\"com.sergey.spacegame.common.game.command.MoveCommandExecutable\",\"requiresInput\":true,\"requiresTwoInput\":true,\"name\":\"Move\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"},\"test\":{\"type\":\"lua\",\"lua\":\"local entities = selected.iterator()\nwhile entities.hasNext() do\nprint(entities.next())\nend\",\"requiresInput\":false,\"requiresTwoInput\":false,\"name\":\"Test\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"}}}";
		return SpaceGame.getInstance().getGson().fromJson(json, Level.class);
		//commands.put("move", new Command(new MoveCommandExecutable(), true, true, "Move", "missingTexture", "missingTexture"));
	}
	
	public HashMap<String, Command> getCommands() {
		return commands;
	}
}
