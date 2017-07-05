package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.command.Command;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ControllableComponent implements ClonableComponent {
    
    public static final ComponentMapper<ControllableComponent> MAPPER = ComponentMapper.getFor(ControllableComponent.class);
    
    public List<Command> commands;
    
    public ControllableComponent(Command... commands) {
        this();
        this.commands.addAll(Arrays.asList(commands));
    }
    
    public ControllableComponent() {
        this.commands = new LinkedList<>();
    }
    
    @Override
    public Component copy() {
        ControllableComponent controllableComponent = new ControllableComponent();
        controllableComponent.commands.addAll(commands);
        return controllableComponent;
    }
    
    public static class Adapter
            implements JsonSerializer<ControllableComponent>, JsonDeserializer<ControllableComponent> {
        
        @Override
        public ControllableComponent deserialize(JsonElement json, Type typeOfT,
                                                 JsonDeserializationContext context) throws JsonParseException {
            JsonArray arr = json.getAsJsonArray();
            
            ControllableComponent control = new ControllableComponent();
            
            Level level = Level.deserializing();
            
            arr.forEach((element) -> {
                String str = element.getAsString();
                if (level.getCommands().containsKey(str)) {
                    control.commands.add(level.getCommands().get(str));
                } else {
                    throw new JsonParseException("Could not find command " + str);
                }
            });
            
            return control;
        }
        
        @Override
        public JsonElement serialize(ControllableComponent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            
            for (Command cmd : src.commands) {
                arr.add(cmd.getId());
            }
            
            return arr;
        }
    }
}
