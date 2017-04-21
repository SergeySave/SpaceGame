package com.sergey.spacegame.client.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class VisualComponent implements Component {
	public TextureRegion region;
	
	public VisualComponent() {}

	public VisualComponent(TextureRegion region) {
		this.region = region;
	}
}
