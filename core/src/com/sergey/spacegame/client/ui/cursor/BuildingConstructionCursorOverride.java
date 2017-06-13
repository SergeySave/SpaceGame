package com.sergey.spacegame.client.ui.cursor;

import java.util.Optional;
import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.ui.UIUtil;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.game.Level;

public final class BuildingConstructionCursorOverride implements CursorOverride {
	
	private String entity;
	private transient boolean entityDirty;
	private transient Entity building;
	private transient ShaderProgram green;
	private transient ShaderProgram red;
	
	public BuildingConstructionCursorOverride() {}
	
	@Override
	public void init() {
		green = UIUtil.compileShader(Gdx.files.internal("shaders/basic.vertex.glsl"), Gdx.files.internal("shaders/constructionOverrideGreen.fragment.glsl"));
		red = UIUtil.compileShader(Gdx.files.internal("shaders/basic.vertex.glsl"), Gdx.files.internal("shaders/constructionOverrideRed.fragment.glsl"));
	}

	@Override
	public Optional<Cursor> getRequestedCursor() {
		return Optional.empty();
	}
	
	@Override
	public void drawExtra(Level level, SpriteBatch batch) {
		float x = Gdx.input.getX();
		float y = Gdx.graphics.getHeight() - Gdx.input.getY();
		StreamSupport.stream(level.getPlanets().spliterator(), false)
				.filter(PositionComponent.MAPPER::has)
				.map((p)->new Object[] {p, PositionComponent.MAPPER.get(p)})
				.map((p)->new Object[] {p[0], (((PositionComponent)p[1]).x-x)*(((PositionComponent)p[1]).x-x) + (((PositionComponent)p[1]).y-y)*(((PositionComponent)p[1]).y-y)})
				.min((l,r)->Float.compare((Float)l[1], (Float)r[1]))
				.map((c)->(Entity)c[0])
				.ifPresent((planet)->{
					if (entityDirty || building == null) {
						building = level.getEntities().get(entity).createEntity(level);
						entityDirty = false;
						if (building == null) return;
					}
					
					if (!PositionComponent.MAPPER.has(building)) return;
					if (!SizeComponent.MAPPER.has(building)) return;
					
					PositionComponent planetPos = PositionComponent.MAPPER.get(planet);
					BuildingComponent buildingC = new BuildingComponent(planet, planetPos.createVector().sub(x, y).scl(-1).angle());
					
					building.add(buildingC);
					
					BuildingSystem.doSetBuildingPosition(building);
					
					PositionComponent posVar;
					SizeComponent sizeVar;
					VisualComponent visVar;

					RotationComponent rotVar;
					
					boolean validPlacement = true;
					
					batch.setShader(validPlacement ? green : red);

					posVar = PositionComponent.MAPPER.get(building);
					sizeVar = SizeComponent.MAPPER.get(building);
					visVar = VisualComponent.MAPPER.get(building);

					if (RotationComponent.MAPPER.has(building)) {
						rotVar = RotationComponent.MAPPER.get(building); 
						float oX = rotVar.originX * sizeVar.w;
						float oY = rotVar.originY * sizeVar.h;
						batch.draw(visVar.getRegion(), posVar.x-oX, posVar.y-oY, oX, oY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
					} else {
						batch.draw(visVar.getRegion(), posVar.x-sizeVar.w/2, posVar.y-sizeVar.h/2, sizeVar.w, sizeVar.h);
					}
				});
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
		if (!entity.equals(this.entity)) entityDirty = true;
	}
	
	@Override
	public boolean needsInitialization() {
		return green == null;
	}

	@Override
	public void dispose() {
		green.dispose();
		red.dispose();
		green = null;
		red = null;
	}
}
