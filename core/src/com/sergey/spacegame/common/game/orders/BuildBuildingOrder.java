package com.sergey.spacegame.common.game.orders;

import java.util.Optional;
import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
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
	private PositionComponent planetPos;
	private Vector2 desired;

	public BuildBuildingOrder(String entityName, float time, float x, float y) {
		this.entity = entityName;
		this.time = time;
		this.x = x;
		this.y = y;
	}

	@Override
	public void init(Entity e, Level level) {
		Optional<Entity> closestPlanet = StreamSupport.stream(level.getPlanets().spliterator(), false)
			.filter(PositionComponent.MAPPER::has)
			.map((p)->new Object[] {p, PositionComponent.MAPPER.get(p)})
			.map((p)->new Object[] {p[0], (((PositionComponent)p[1]).x-x)*(((PositionComponent)p[1]).x-x) + (((PositionComponent)p[1]).y-y)*(((PositionComponent)p[1]).y-y)})
			.min((l,r)->Float.compare((Float)l[1], (Float)r[1]))
			.map((c)->(Entity)c[0]);

		if (closestPlanet.isPresent()) {
			planet = closestPlanet.get();

			Entity building = level.getEntities().get(entity).createEntity(level); //Copy of building
			building.add(new InContructionComponent());

			planetPos = PositionComponent.MAPPER.get(planet);
			
			float buildingPos = planetPos.createVector().sub(x, y).scl(-1).angle();
			
			float[] minMax = PlanetSystem.getMinMax(building, planet, buildingPos);
			
			if (!PlanetComponent.MAPPER.get(planet).isFree(minMax[0], minMax[1])) {
				//If placement is invalid
				time = -1;
				//Fail to build
				return;
			}
			
			BuildingComponent buildingC = new BuildingComponent();
			
			buildingC.init(planet, buildingPos);

			building.add(buildingC);

			level.getECS().getEngine().addEntity(building);
			this.building = building;
		} else {
			//No planet fail to build
			time = -1;
			return;
		}
	}

	@Override
	public void update(Entity e, float deltaTime, Level level) {
		if (time < 0) return;

		if (ShipComponent.MAPPER.has(e) && PositionComponent.MAPPER.has(e)) {
			PositionComponent pos = PositionComponent.MAPPER.get(e);
			VelocityComponent vel = VelocityComponent.MAPPER.get(e);
			if (vel == null) {
				vel = new VelocityComponent();
				e.add(vel);
			}
			ShipComponent ship = ShipComponent.MAPPER.get(e);
			float speed = ship.moveSpeed;
			desired = PositionComponent.MAPPER.get(building).createVector().sub(planetPos.x, planetPos.y).scl(1.5f).add(planetPos.x, planetPos.y);

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
		if (building != null) {
			level.getECS().getEngine().removeEntity(building);
		}
	}

	public Optional<Vector2> getPosition() {
		if (building == null) return Optional.empty();
		return Optional.of(PositionComponent.MAPPER.get(building).createVector().sub(planetPos.x, planetPos.y).scl(1.5f).add(planetPos.x, planetPos.y));
	}
}
