package com.sergey.spacegame.common.game.command;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sergey.spacegame.client.ui.cursor.CursorOverride;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.lua.LuaUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

public final class Command {

	private CommandExecutable executable;
	private boolean requiresInput;
	private boolean requiresTwoInput;
	private String name;
	private String drawableName;
	private String drawableCheckedName;
	private String id;
	private CursorOverride cursor;
	
	public Command(CommandExecutable executable, boolean requiresInput, boolean requiresTwoInput, String name, String drawableName, String drawableCheckedName, CursorOverride cursor) {
		this.executable = executable;
		this.requiresInput = requiresInput;
		this.requiresTwoInput = requiresTwoInput;
		this.name = name;
		this.drawableName = drawableName;
		this.drawableCheckedName = drawableCheckedName;
		this.cursor = cursor;
	}

	/**
	 * @return the executable
	 */
	public CommandExecutable getExecutable() {
		return executable;
	}

	/**
	 * @return the requiresInput
	 */
	public boolean isRequiresInput() {
		return requiresInput;
	}

	/**
	 * @return the requiresTwoInput
	 */
	public boolean isRequiresTwoInput() {
		return requiresTwoInput;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDrawableName() {
		return drawableName;
	}
	
	public String getDrawableCheckedName() {
		return drawableCheckedName;
	}
	
	public String getId() {
		return id;
	}
	
	public CursorOverride getCursor() {
		return cursor;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Command)) return false;
		Command other = (Command)obj;
		return executable.equals(other.executable) && requiresInput == other.requiresInput && requiresTwoInput == other.requiresTwoInput && name.equals(other.name) && drawableName.equals(other.drawableName) && (drawableCheckedName == null ? other.drawableCheckedName == null : drawableCheckedName.equals(other.drawableCheckedName));
	}
	
	@Override
	public int hashCode() {
		return executable.hashCode() << 2 + (requiresInput ? 2 : 0) + (requiresTwoInput ? 1 : 0) + name.hashCode() + drawableName.hashCode() + (drawableCheckedName == null ? 0 : drawableCheckedName.hashCode());
	}
	
	public static class Adapter implements JsonSerializer<Command>, JsonDeserializer<Command> {

		@Override
		public Command deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			
			String type = obj.getAsJsonPrimitive("type").getAsString();
			CommandExecutable executable;
			if (type.equals("lua")) {

				String lua = obj.get("lua").getAsString();

				try {
					lua = LuaUtils.getLUACode(lua, Level.deserializingFileSystem());
				} catch (IOException e) {
					throw new JsonParseException(e.getMessage(), e);
				}

				executable = new LuaCommandExecutable(lua);
			} else {
				String className = obj.getAsJsonPrimitive("class").getAsString();
				try {
					Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
					executable = (CommandExecutable) clazz.newInstance();
				} catch (ClassNotFoundException e) {
					throw new JsonParseException("Class " + className + " not found. ", e);
				} catch (InstantiationException | IllegalAccessException e) {
					throw new JsonParseException("Class " + className + " unable to be instantiated. ", e);
				} catch (ClassCastException e) {
					throw new JsonParseException("Class " + className + " does not implement CommandExecutable. ", e);
				}
			}
			
			boolean requiresInput = obj.getAsJsonPrimitive("requiresInput").getAsBoolean();
			boolean requiresTwoInput = obj.getAsJsonPrimitive("requiresTwoInput").getAsBoolean();
			String name = obj.getAsJsonPrimitive("name").getAsString();
			String drawableName = obj.getAsJsonPrimitive("iconName").getAsString();
			String drawableCheckedName = Optional.ofNullable(obj.getAsJsonPrimitive("pressedIconName")).map(JsonPrimitive::getAsString).orElse(null);
			
			CursorOverride cursor = null;
			
			if (obj.has("cursor")) {
				JsonObject cursorObj = obj.getAsJsonObject("cursor");
				String className = cursorObj.get("class").getAsString();
				try {
					Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
					cursor = context.deserialize(cursorObj, clazz);
				} catch (ClassNotFoundException e) {
					throw new JsonParseException("Class " + className + " not found. ", e);
				}
			}
			
			return new Command(executable, requiresInput, requiresTwoInput, name, drawableName, drawableCheckedName, cursor);
		}

		@Override
		public JsonElement serialize(Command src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			if (src.getExecutable() instanceof LuaCommandExecutable) {
				obj.addProperty("type", "lua");
				obj.addProperty("lua", ((LuaCommandExecutable)src.getExecutable()).getLua());
			} else {
				obj.addProperty("type", "java");
				obj.addProperty("class", src.getExecutable().getClass().getName());
			}
			obj.addProperty("requiresInput", src.requiresInput);
			obj.addProperty("requiresTwoInput", src.requiresTwoInput);
			obj.addProperty("name", src.name);
			obj.addProperty("iconName", src.drawableName);
			obj.addProperty("pressedIconName", src.drawableCheckedName);
			return obj;
		}

	}

}
