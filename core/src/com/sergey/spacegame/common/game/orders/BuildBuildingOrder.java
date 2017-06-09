package com.sergey.spacegame.common.game.orders;

import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.game.Level;

public class BuildBuildingOrder implements IOrder {
	private String entity;
	private float time;
	//private float position;
	
	public BuildBuildingOrder(String entityName, float time) {
		this.entity = entityName;
		this.time = time;
		//this.position = position;
	}

	@Override
	public void update(Entity e, float deltaTime, Level level) {
		if (time < 0) return;
		
		time -= deltaTime;
		
		if (time < 0) {
			Entity newEntity = level.getEntities().get(entity).createEntity(level);
			if (PositionComponent.MAPPER.has(e)) {
				PositionComponent pos = PositionComponent.MAPPER.get(e);
				//Find closest planet to the position and then add the building component to the entity
				StreamSupport.stream(level.getPlanets().spliterator(), false)
					.filter(PositionComponent.MAPPER::has)
					.map((p)->new Object[] {p, PositionComponent.MAPPER.get(p)})
					.map((p)->new Object[] {p[0], (((PositionComponent)p[1]).x-pos.x)*(((PositionComponent)p[1]).x-pos.x) + (((PositionComponent)p[1]).y-pos.y)*(((PositionComponent)p[1]).y-pos.y)})
					.min((l,r)->Double.compare((Double)l[1], (Double)r[1]))
					.map((c)->(Entity)c[0])
					.ifPresent((p)->{
						PositionComponent pPos = PositionComponent.MAPPER.get(p);
						BuildingComponent building = new BuildingComponent(p, pos.createVector().sub(pPos.x, pPos.y).angle());
						
						newEntity.add(building);
					});
			}
			
			level.getECS().getEngine().addEntity(newEntity);
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
}
