package com.sergey.spacegame.common.game.command

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.lua.LuaPredicate
import com.sergey.spacegame.common.lua.LuaUtils
import java.io.IOException
import java.lang.reflect.Type

class ServerCommand(executable: CommandExecutable, allowMulti: Boolean, requiresInput: Boolean,
                    requiresTwoInput: Boolean, id: String, drawableName: String, req: Map<String, LuaPredicate>?,
                    drawableCheckedName: String?,
                    orderTag: String?) : Command(executable, allowMulti, requiresInput, requiresTwoInput, id,
                                                 drawableName, req, drawableCheckedName, orderTag) {
    
    
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
            val id = obj["id"]?.asString ?: throw JsonParseException("Command id not set") // Should never occur as set programmatically
            val drawableName = obj["iconName"]?.asString ?: throw JsonParseException("Command iconName not set")
            val drawableCheckedName = obj["pressedIconName"]?.asString //Nullable type
            val orderTag = if (!allowMulti) obj["orderTag"].asString!! else null //Nullable but cannot be null if allowMulti is false
            val req = obj["req"]?.asJsonObject?.run {
                val map = HashMap<String, LuaPredicate>()
                
                for (entry in entrySet()) {
                    val original = entry.value.asString
                    val code = try {
                        LuaUtils.getLUACode(original, Level.deserializingFileSystem())
                    } catch (e: IOException) {
                        throw JsonParseException(e.message, e)
                    }
                    map.put(entry.key, LuaPredicate(original, code))
                }
                
                map
            }
            
            return ServerCommand(executable, allowMulti, requiresInput, requiresTwoInput, id, drawableName, req, drawableCheckedName, orderTag)
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
                addProperty("iconName", src.drawableName)
                if (src.drawableCheckedName != null) addProperty("pressedIconName", src.drawableCheckedName)
                if (!src.allowMulti) addProperty("orderTag", src.orderTag)
                src.req?.let { req ->
                    val reqObj = JsonObject()
                    
                    for (entry in req) {
                        reqObj.addProperty(entry.key, entry.value.original)
                    }
                    
                    add("req", reqObj)
                }
            }
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerCommand) return false
        return super.equals(other)
    }
    
    override fun hashCode(): Int = super.hashCode() * 31 + javaClass.hashCode()
}