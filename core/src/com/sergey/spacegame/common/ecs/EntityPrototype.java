package com.sergey.spacegame.common.ecs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sergey.spacegame.common.ecs.component.ClonableComponent;
import com.sergey.spacegame.common.game.Level;

public final class EntityPrototype {
	
	private ClonableComponent[] components;
	
	public Entity createEntity(Level level) {
		Entity e = level.getECS().newEntity();
		
		for (ClonableComponent comp : components) {
			e.add(comp.copy());
		}
		
		return e;
	}
	
	public static class Adapter implements JsonSerializer<EntityPrototype>, JsonDeserializer<EntityPrototype>{

		@Override
		public EntityPrototype deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			ArrayList<ClonableComponent> components = new ArrayList<>();
			
			Set<Entry<String, JsonElement>> entries = obj.entrySet();
			
			for (Entry<String, JsonElement> entry : entries) {
				String className = entry.getKey();
				try {
					Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
					components.add(context.deserialize(entry.getValue(), clazz));
				} catch (ClassNotFoundException e) {
					throw new JsonParseException("Class " + className + " not found. ", e);
				}
			}
			
			EntityPrototype proto = new EntityPrototype();
			
			proto.components = components.toArray(new ClonableComponent[]{});
			
			return proto;
		}

		@Override
		public JsonElement serialize(EntityPrototype src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			
			for (ClonableComponent comp : src.components) {
				obj.add(comp.getClass().getName(), context.serialize(comp));
			}
			
			return obj;
		}

	}
}
