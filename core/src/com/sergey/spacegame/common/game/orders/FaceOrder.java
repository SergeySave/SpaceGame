package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.game.Level;

public class FaceOrder implements IOrder {
	private double r;
	private double rotateSpeed;
	private boolean done;
	
	public FaceOrder(double r, float speed) {
		this.r = r;
		this.rotateSpeed = speed;
	}

	@Override
	public void update(Entity e, float deltaTime, Level level) {
		if (done) return;
		
		RotationComponent rot = RotationComponent.MAPPER.get(e);
		RotationVelocityComponent vel = RotationVelocityComponent.MAPPER.get(e);
		if (vel == null) {
			vel = new RotationVelocityComponent();
			e.add(vel);
		}

		float dr = (float) (r-rot.r);
		//Fix the angle
		while (dr < -180) {
			dr += 360;
		}
		//Fix the angle
		while (dr > 180) {
			dr -= 360;
		}
		
		if (Math.abs(dr) < rotateSpeed*deltaTime) {
			done = true;
			rot.r = (float) r;
			e.remove(RotationVelocityComponent.class);
			
		} else {
			vel.vr = (float) (Math.signum(dr)*rotateSpeed);
		}
	}

	@Override
	public boolean isValidFor(Entity e) {
		return RotationComponent.MAPPER.has(e) && ShipComponent.MAPPER.has(e);
	}

	@Override
	public boolean completed() {
		return done;
	}
}
