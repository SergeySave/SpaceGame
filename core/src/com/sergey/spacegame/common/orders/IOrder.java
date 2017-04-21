package com.sergey.spacegame.common.orders;

import com.badlogic.ashley.core.Entity;

public interface IOrder {
	public void update(Entity e, float deltaTime);
	public boolean isValidFor(Entity e);
	public boolean completed();
}
