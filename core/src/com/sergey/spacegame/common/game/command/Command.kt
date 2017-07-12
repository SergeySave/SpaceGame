package com.sergey.spacegame.common.game.command

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.client.ui.cursor.CursorOverride
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.lua.LuaUtils
import java.io.IOException
import java.lang.reflect.Type

/**
 * @author sergeys
 */
data class Command(val executable: CommandExecutable, val allowMulti: Boolean, val requiresInput: Boolean,
                   val requiresTwoInput: Boolean, val name: String, val id: String, val drawableName: String,
                   val drawableCheckedName: String?, val cursor: CursorOverride?) {
    class Adapter : JsonSerializer<Command>, JsonDeserializer<Command> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Command {
            val obj = json.asJsonObject
            
            val executable = when (obj["type"].asString) {
                "lua" -> {
                    val original = obj["lua"].asString
                    
                    val lua = try {
                        LuaUtils.getLUACode(original, Level.deserializingFileSystem())
                    } catch (e: IOException) {
                        throw JsonParseException(e.message, e)
                    }
                    
                    LuaCommandExecutable(lua, original)
                }
                else  -> {
                    val className = obj["class"].asString
                    
                    try {
                        val clazz = ClassLoader.getSystemClassLoader().loadClass(className)
                        clazz.newInstance() as CommandExecutable
                    } catch (e: ClassNotFoundException) {
                        throw JsonParseException("Class $className not found. ", e)
                    } catch (e: InstantiationException) {
                        throw JsonParseException("Class $className unable to be instantiated. ", e)
                    } catch (e: IllegalAccessException) {
                        throw JsonParseException("Class $className unable to be instantiated. ", e)
                    } catch (e: ClassCastException) {
                        throw JsonParseException("Class $className does not implement CommandExecutable. ", e)
                    }
                }
            }
            val allowMulti = obj["allowsMulti"]?.asBoolean ?: true
            val requiresInput = obj["requiresInput"]?.asBoolean ?: false
            val requiresTwoInput = obj["requiresTwoInput"]?.asBoolean ?: false
            val name = obj["name"]?.asString ?: throw JsonParseException("Command name not set")
            val id = obj["id"]?.asString ?: throw JsonParseException("Command id not set") // Should never occur as set programmatically
            val drawableName = obj["iconName"]?.asString ?: throw JsonParseException("Command iconName not set")
            val drawableCheckedName = obj["pressedIconName"]?.asString //Nullable type
            val cursor: CursorOverride? = when (obj.has("cursor")) {
                true  -> {
                    val cursorObj = obj["cursor"].asJsonObject
                    val className = cursorObj["class"].asString
                    try {
                        val clazz = ClassLoader.getSystemClassLoader().loadClass(className)
                        val cursorVal = context.deserialize<CursorOverride>(cursorObj, clazz)
                        cursorVal.load(Level.deserializingFileSystem())
                        cursorVal!!
                    } catch (e: ClassNotFoundException) {
                        throw JsonParseException("Class $className not found", e)
                    } catch (e: IOException) {
                        throw JsonParseException("Class $className IO Error", e)
                    }
                }
                false -> null
            }
    
            return Command(executable, allowMulti, requiresInput, requiresTwoInput, name, id, drawableName, drawableCheckedName, cursor)
        }
        
        override fun serialize(src: Command, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonObject().apply {
                if (src.executable is LuaCommandExecutable) {
                    addProperty("type", "lua")
                    addProperty("lua", src.executable.original)
                } else {
                    addProperty("type", "java")
                    addProperty("class", src.executable::class.java.name)
                }
    
                addProperty("allowsMulti", src.allowMulti)
                addProperty("requiresInput", src.requiresInput)
                addProperty("requiresTwoInput", src.requiresTwoInput)
                addProperty("name", src.name)
                addProperty("iconName", src.drawableName)
                if (src.drawableCheckedName != null) addProperty("pressedIconName", src.drawableCheckedName)
                
                if (src.cursor != null) {
                    add("cursor", context.serialize(src.cursor).apply {
                        asJsonObject.addProperty("class", this::class.java.name)
                    })
                }
            }
        }
    }
}