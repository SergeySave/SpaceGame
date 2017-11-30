package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sergey.spacegame.common.ecs.component.ClonableComponent;
import com.sergey.spacegame.common.ecs.component.HealthComponent;
import com.sergey.spacegame.common.ecs.component.Team1Component;
import com.sergey.spacegame.common.ecs.component.Team2Component;
import com.sergey.spacegame.common.game.Level;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents an prototype for an entity
 *
 * @author sergeys
 */
public final class EntityPrototype {
    
    private ClonableComponent[] components;
    private ClonableComponent   team;
    
    private EntityPrototype() {}
    
    /**
     * Create an entity for a given entity
     *
     * @param level - the level that the entity should be made in
     *
     * @return a new entity made from this prototype
     */
    public Entity createEntity(Level level) {
        Entity e = level.getECS().newEntity();
        
        for (ClonableComponent comp : components) {
            e.add(comp.copy());
        }
        
        return e;
    }
    
    /**
     * Get the team component for this prototype
     *
     * @return the team component
     */
    public ClonableComponent getTeam() {
        return team;
    }
    
    public static class Adapter implements JsonSerializer<EntityPrototype>, JsonDeserializer<EntityPrototype> {
        
        @Override
        public EntityPrototype deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            JsonObject                   obj        = json.getAsJsonObject();
            ArrayList<ClonableComponent> components = new ArrayList<>();
            
            Set<Entry<String, JsonElement>> entries = obj.entrySet();
            
            boolean           hasHealth = false;
            ClonableComponent team      = null;
            
            for (Entry<String, JsonElement> entry : entries) {
                String className = entry.getKey();
                try {
                    Class<?>          clazz     = ClassLoader.getSystemClassLoader().loadClass(className);
                    ClonableComponent component = context.deserialize(entry.getValue(), clazz);
                    components.add(component);
                    
                    if (component instanceof HealthComponent) hasHealth = true;
                    if (component instanceof Team1Component || component instanceof Team2Component) {
                        team = component;
                    }
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Class " + className + " not found. ", e);
                }
            }
            
            if ((team == null) == hasHealth) { //xor on if it has a team and it has a health
                System.err.println("Entity has only one of HealthComponent and TeamComponent");
            }
            
            EntityPrototype proto = new EntityPrototype();
            
            proto.components = components.toArray(new ClonableComponent[]{});
            proto.team = team;
            
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
