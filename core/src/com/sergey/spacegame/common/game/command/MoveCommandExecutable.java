package com.sergey.spacegame.common.game.command;

import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.orders.FaceOrder;
import com.sergey.spacegame.common.game.orders.MoveOrder;
import com.sergey.spacegame.common.game.orders.TimeMoveOrder;

public final class MoveCommandExecutable implements CommandExecutable {

	@Override
	public void issue(Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end, Level level) {
		//Gets the center of all of the ships by averaging their coordinates
		Vector2 center = StreamSupport.stream(entitySource.spliterator(), true).collect(Vector2::new, (v,s)->v.add(new Vector2(PositionComponent.MAPPER.get(s).x,PositionComponent.MAPPER.get(s).y)), (v1,v2)->v1.add(v2));
		center.scl(1f/numEntities);
		
		//The direction we want the fleet to move
		float fleetMoveDir = start.cpy().sub(center).angle();
		
		//Change in position
		float dx = start.x-center.x;
		float dy = start.y-center.y;
		
		//The total movement distance
		double ds = Math.sqrt(dx*dx + dy*dy);
		
		//The final facing direction
		float dr = new Vector2(end.x-start.x, end.y-start.y).angle();
		
		//Is this a fleet command or a simple command
		boolean fleetOrder = numEntities>1;
		if (fleetOrder) {
			Vector2 farCenter = center.cpy().add(dx, dy);

			//Average Angle
			float fleetDir = StreamSupport.stream(entitySource.spliterator(), true).filter(RotationComponent.MAPPER::has).map((e)->new Vector2(1,0).rotate(RotationComponent.MAPPER.get(e).r)).collect(Vector2::new, (v1,v2)->v1.add(v2), (v1,v2)->v1.add(v2)).angle();
			//First fleet rotation angle
			float dr1 = fleetMoveDir-fleetDir;
			//Second fleet rotation angle
			float dr2 = dr-fleetMoveDir;

			//Time to rotate fleet, move fleet, and rotate the fleet again
			double[] maxTimes = StreamSupport.stream(entitySource.spliterator(), true).map((e)->{
				boolean doesTurn = RotationComponent.MAPPER.has(e);
				float ang;
				ShipComponent ship = ShipComponent.MAPPER.get(e);
				Vector2 startPos, endPos, deltaPos;
				double[] times = new double[3];

				startPos = PositionComponent.MAPPER.get(e).createVector();
				endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
				deltaPos = endPos.cpy().sub(startPos);
				ang = deltaPos.angle();
				//Turn time 1
				times[0] = (doesTurn ? getThroughRotateDistance(ang,RotationComponent.MAPPER.get(e).r)/ship.rotateSpeed : 0) + deltaPos.len()/ship.moveSpeed + (doesTurn ? getThroughRotateDistance(fleetMoveDir,ang)/ship.rotateSpeed : 0);

				startPos.add(dx, dy);
				times[1] = ds/ship.moveSpeed;

				startPos = endPos;
				endPos = startPos.cpy().sub(center).rotate(dr2).add(center);
				deltaPos = endPos.cpy().sub(startPos);
				ang = deltaPos.angle();
				//Turn time 2
				times[2] = (doesTurn ? getThroughRotateDistance(ang,fleetMoveDir)/ship.rotateSpeed : 0) + deltaPos.len()/ship.moveSpeed + (doesTurn ? getThroughRotateDistance(dr,ang)/ship.rotateSpeed : 0);

				return times;
			}).reduce(new double[3], (r,e)->new double[]{Math.max(r[0], e[0]),Math.max(r[1], e[1]),Math.max(r[2], e[2])});

			float time = (float) (maxTimes[1]);

			entitySource.forEach((e)->{
				boolean doesTurn = RotationComponent.MAPPER.has(e);
				ShipComponent ship = ShipComponent.MAPPER.get(e);
				float ang;
				OrderComponent ord;
				if (OrderComponent.MAPPER.has(e)) {
					ord = OrderComponent.MAPPER.get(e);
					ord.clearOrders(e, level);
				} else {
					ord = new OrderComponent();
				}
				Vector2 startPos, endPos, deltaPos;

				if (doesTurn) {
					startPos = PositionComponent.MAPPER.get(e).createVector();
					endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
					deltaPos = endPos.cpy().sub(startPos);
					ang = deltaPos.angle();

					//Fleet rotate 1
					ord.addOrder(new FaceOrder(ang, ship.rotateSpeed));
					double maxTime = maxTimes[0];
					float d1 = getThroughRotateDistance(ang,fleetMoveDir);
					float d2 = getThroughRotateDistance(ang,RotationComponent.MAPPER.get(e).r);
					float dt = d1 + d2;
					float tr = dt/ship.rotateSpeed;
					float mT = (float) (maxTime - tr);
					ord.addOrder(new TimeMoveOrder(endPos.x, endPos.y, mT));
					ord.addOrder(new FaceOrder(fleetMoveDir, ship.rotateSpeed)); 

					//Fleet move
					ord.addOrder(new TimeMoveOrder(endPos.x+dx, endPos.y+dy, time));

					startPos = endPos.add(dx, dy);
					endPos = startPos.cpy().sub(farCenter).rotate(dr2).add(farCenter);
					deltaPos = endPos.cpy().sub(startPos);
					ang = deltaPos.angle();

					//Fleet rotate 2
					ord.addOrder(new FaceOrder(ang, ship.rotateSpeed));
					mT = (float) (maxTimes[2]-(getThroughRotateDistance(ang,fleetMoveDir)+getThroughRotateDistance(ang,dr))/ship.rotateSpeed);
					ord.addOrder(new TimeMoveOrder(endPos.x, endPos.y, mT));
					ord.addOrder(new FaceOrder(dr, ship.rotateSpeed));
				} else {
					startPos = PositionComponent.MAPPER.get(e).createVector();
					endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
					deltaPos = endPos.cpy().sub(startPos);

					ord.addOrder(new TimeMoveOrder(endPos.x, endPos.y, (float) (maxTimes[0])));
					ord.addOrder(new TimeMoveOrder(endPos.x+dx, endPos.y+dy, time));

					startPos = endPos.add(dx, dy);
					endPos = startPos.cpy().sub(farCenter).rotate(dr2).add(farCenter);
					deltaPos = endPos.cpy().sub(startPos);

					ord.addOrder(new TimeMoveOrder(endPos.x, endPos.y, (float) (maxTimes[2])));
				}

				e.add(ord);
			});
		} else {
			float speed = (float) StreamSupport.stream(entitySource.spliterator(), true).mapToDouble((e)->ShipComponent.MAPPER.get(e).moveSpeed).min().getAsDouble();
			entitySource.forEach((e)->{
				OrderComponent ord = new OrderComponent(
						new FaceOrder(fleetMoveDir, 45),
						new MoveOrder(PositionComponent.MAPPER.get(e).x+dx, PositionComponent.MAPPER.get(e).y+dy, speed),
						new FaceOrder(dr, 45));
				
				if (OrderComponent.MAPPER.has(e)) {
					OrderComponent.MAPPER.get(e).clearOrders(e, level);
				}

				e.add(ord);
			});
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof MoveCommandExecutable;
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	private static float getThroughRotateDistance(float angle1, float angle2) {
		float dr = Math.abs(angle1 - angle2)%360;
		if (dr > 180) return 360-dr;
		return dr;
	}
}
