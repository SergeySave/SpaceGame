package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.game.Level;

public interface IOrder {
	public void update(Entity e, float deltaTime, Level level);
	public boolean isValidFor(Entity e);
	public boolean completed();
}
