package com.sergey.spacegame.client.game.command

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.client.ui.cursor.CursorOverride
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.command.Command
import com.sergey.spacegame.common.game.command.CommandExecutable
import com.sergey.spacegame.common.game.command.LuaCommandExecutable
import com.sergey.spacegame.common.lua.LuaPredicate
import com.sergey.spacegame.common.lua.LuaUtils
import java.io.IOException
import java.lang.reflect.Type

/**
 * Represents a Command for the desktop client side
 * Unlike other the common commands it has the client cursor overrides
 *
 * @author sergeys
 *
 * @constructor Creates a new ClientCommand
 *
 * @param executable - the command's executable
 * @param allowMulti - does this command allow multiple targets
 * @param requiresInput - does this command require an input position
 * @param requiresTwoInput - does this command require a secondary input position
 * @param id - the id of this command
 * @param drawableName - the name of the image to use for drawing the command's icon button
 * @param req - the requirements of this command to be enabled
 * @param drawableCheckedName - the name of the image to use for drawing the command's pressed icon button
 * @property cursor - the cursor override used for this command when it is selected or null
 * @param orderTag - the tag to use to determine the order count
 */
class ClientCommand(executable: CommandExecutable, allowMulti: Boolean, requiresInput: Boolean,
                    requiresTwoInput: Boolean, id: String, drawableName: String, req: Map<String, LuaPredicate>?,
                    drawableCheckedName: String?, val cursor: CursorOverride?,
                    orderTag: String?) : Command(executable, allowMulti, requiresInput, requiresTwoInput, id,
                                                 drawableName, req, drawableCheckedName, orderTag) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientCommand) return false
        if (!super.equals(other)) return false
        
        if (cursor != other.cursor) return false
        
        return super.equals(other)
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (cursor?.hashCode() ?: 0)
        return result
    }
    
    class Adapter : JsonSerializer<Command>, JsonDeserializer<Command> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Command {
            val obj = json.asJsonObject
    
            //Create the command's executable
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
            val cursor: CursorOverride? = when (obj.has("cursor")) {
                true  -> {
                    val cursorObj = obj["cursor"].asJsonObject
                    val className = cursorObj["class"].asString
                    try {
                        val clazz = ClassLoader.getSystemClassLoader().loadClass(className)
                        val cursorVal = context.deserialize<com.sergey.spacegame.client.ui.cursor.CursorOverride>(cursorObj, clazz)
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
            
            return ClientCommand(executable, allowMulti, requiresInput, requiresTwoInput, id, drawableName, req, drawableCheckedName, cursor, orderTag)
        }
        
        override fun serialize(src: Command, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonObject().apply {
                if (src.executable is LuaCommandExecutable) {
                    addProperty("type", "lua")
                    addProperty("lua", (src.executable as LuaCommandExecutable).original)
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
                if (src is ClientCommand) {
                    if (src.cursor != null) {
                        add("cursor", context.serialize(src.cursor).apply {
                            asJsonObject.addProperty("class", this::class.java.name)
                        })
                    }
                }
            }
        }
    }
    
}