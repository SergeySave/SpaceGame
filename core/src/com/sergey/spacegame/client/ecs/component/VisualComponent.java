package com.sergey.spacegame.client.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class VisualComponent implements Component {
	public static final ComponentMapper<VisualComponent> MAPPER = ComponentMapper.getFor(VisualComponent.class);
	
	public TextureRegion region;
	
	public VisualComponent() {}

	public VisualComponent(TextureRegion region) {
		this.region = region;
	}
}
