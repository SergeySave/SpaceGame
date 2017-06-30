package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.game.Level;

public interface IOrder {
	void update(Entity e, float deltaTime, Level level);
	boolean isValidFor(Entity e);
	boolean completed();
	
	default void init(Entity e, Level level) {}
	default void onCancel(Entity e, Level level){}
}
