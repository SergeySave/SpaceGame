package com.sergey.spacegame.common.game.orders;

import java.util.Optional;
import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.Level;

/**
 * 
 * An order used to build a new building. Uses a finite state machine to control the process of the building.
 * 
 * @author sergeys
 *
 */
public class BuildBuildingOrder implements IOrder {
	private String entity;
	private float time;
	private float x;
	private float y;
	private Entity planet;
	private Entity building;
	private State state;
	
	public BuildBuildingOrder(String entityName, float time, float x, float y) {
		this.entity = entityName;
		this.time = time;
		this.x = x;
		this.y = y;
		this.state = State.UNINITIALIZED;
	}

	@Override
	public void update(Entity e, float deltaTime, Level level) {
		if (time < 0) return;
		
		if (state == State.UNINITIALIZED) {
			Optional<Entity> closestPlanet = StreamSupport.stream(level.getPlanets().spliterator(), false)
				.filter(PositionComponent.MAPPER::has)
				.map((p)->new Object[] {p, PositionComponent.MAPPER.get(p)})
				.map((p)->new Object[] {p[0], (((PositionComponent)p[1]).x-x)*(((PositionComponent)p[1]).x-x) + (((PositionComponent)p[1]).y-y)*(((PositionComponent)p[1]).y-y)})
				.min((l,r)->Double.compare((Double)l[1], (Double)r[1]))
				.map((c)->(Entity)c[0]);
			
			if (closestPlanet.isPresent()) {
				state = State.BUILDING;
				planet = closestPlanet.get();
				
				building = level.getEntities().get(entity).createEntity(level); //Copy of building
				building.add(new InContructionComponent());
				
				PositionComponent pPos = PositionComponent.MAPPER.get(planet);
				BuildingComponent buildingC = new BuildingComponent(planet, pPos.createVector().sub(x, y).scl(-1).angle());
				
				building.add(buildingC);
				
				state = State.BUILDING;
				
				level.getECS().getEngine().addEntity(building);
			} else {
				//No planet fail to build
				time = -1;
				return;
			}
		}
		
		if (ShipComponent.MAPPER.has(e) && PositionComponent.MAPPER.has(e)) {
			PositionComponent pos = PositionComponent.MAPPER.get(e);
			VelocityComponent vel = VelocityComponent.MAPPER.get(e);
			if (vel == null) {
				vel = new VelocityComponent();
				e.add(vel);
			}
			ShipComponent ship = ShipComponent.MAPPER.get(e);
			float speed = ship.moveSpeed;
			PositionComponent desired = PositionComponent.MAPPER.get(building);
			
			double dx = desired.x-pos.x;
			double dy = desired.y-pos.y;
			double dist = Math.hypot(dx, dy);
			if (dist < speed*deltaTime) {
				pos.x = (float)desired.x;
				pos.y = (float)desired.y;
				e.remove(VelocityComponent.class);
				time -= deltaTime;
			} else {
				vel.vx = (float) (speed*dx/dist);
				vel.vy = (float) (speed*dy/dist);
			}
		} else {
			time -= deltaTime;
		}
		
		if (time < 0) {
			building.remove(InContructionComponent.class);
		}
	}

	@Override
	public boolean isValidFor(Entity e) {
		return true;
	}

	@Override
	public boolean completed() {
		return time < 0;
	}
	
	@Override
	public void onCancel(Entity e, Level level) {
		if (building != null) level.getECS().getEngine().removeEntity(building);
	}
	
	public Optional<PositionComponent> getBuilding() {
		return Optional.ofNullable(PositionComponent.MAPPER.get(building));
	}
	
	private static enum State {
		UNINITIALIZED,
		BUILDING;
	}
}
