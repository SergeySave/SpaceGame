package com.sergey.spacegame.client.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.component.ClonableComponent;

import java.lang.reflect.Type;

public class VisualComponent implements ClonableComponent {
	public static final ComponentMapper<VisualComponent> MAPPER = ComponentMapper.getFor(VisualComponent.class);
	
	private TextureRegion region;
	private String name;
	
	public VisualComponent() {}

	public VisualComponent(String name) {
		setRegion(name);
	}

	@Override
	public Component copy() {
		return new VisualComponent(name);
	}
	
	public TextureRegion getRegion() {
		return region;
	}
	
	public void setRegion(String name) {
		this.region = SpaceGame.getInstance().getRegion(name);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static class Adapter implements JsonSerializer<VisualComponent>, JsonDeserializer<VisualComponent> {

		@Override
		public VisualComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			
			return new VisualComponent(obj.get("image").getAsString());
		}

		@Override
		public JsonElement serialize(VisualComponent src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			
			obj.addProperty("image", src.name);

			return obj;
		}
	}
}
