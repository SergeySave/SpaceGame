package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

@FunctionalInterface
public interface CommandExecutable {
	public void issue(Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end);
}
