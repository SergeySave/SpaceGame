package com.sergey.spacegame.common.game;

import java.util.HashMap;

import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.game.command.MoveCommand;

public class Level {
	private HashMap<String, Command> commands = new HashMap<>();
	
	{
		commands.put("move", new Command(new MoveCommand(), true, true, "Move", "missingTexture", "missingTexture"));
	}
	
	public HashMap<String, Command> getCommands() {
		return commands;
	}
}
