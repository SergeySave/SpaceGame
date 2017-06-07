package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public class CommandExecutorService {
	public CommandExecutorService() {
	}
	
	public void executeCommand(CommandExecutable executable, Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end) {
		if (numEntities == 0) return;
		executable.issue(entitySource, numEntities, start, end);
		//If replay is wanted it can be implemented by remembering the times that this was called
	}
}
