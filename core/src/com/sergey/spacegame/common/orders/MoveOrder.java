package com.sergey.spacegame.common.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;

public class MoveOrder implements IOrder {
	private double x;
	private double y;
	private boolean done;

	public MoveOrder(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void update(Entity e, float deltaTime) {
		if (done) return;
		
		PositionComponent pos = PositionComponent.MAPPER.get(e);
		VelocityComponent vel = VelocityComponent.MAPPER.get(e);
		if (vel == null) {
			vel = new VelocityComponent();
			e.add(vel);
		}
		ShipComponent ship = ShipComponent.MAPPER.get(e);

		double dx = x-pos.x;
		double dy = y-pos.y;
		double dist = Math.hypot(dx, dy);
		if (dist < ship.moveSpeed*deltaTime) {
			done = true;
			pos.x = (float)x;
			vel.vx = 0;
			pos.y = (float)y;
			vel.vy = 0;
		} else {
			vel.vx = (float) (ship.moveSpeed*dx/dist);
			vel.vy = (float) (ship.moveSpeed*dy/dist);
		}
	}

	@Override
	public boolean isValidFor(Entity e) {
		return PositionComponent.MAPPER.has(e) && ShipComponent.MAPPER.has(e);
	}

	@Override
	public boolean completed() {
		return done;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
}
