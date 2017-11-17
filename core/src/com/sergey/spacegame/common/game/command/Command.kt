package com.sergey.spacegame.common.game.command

import com.sergey.spacegame.common.lua.LuaPredicate

/**
 * @author sergeys
 */
abstract class Command(val executable: CommandExecutable, val allowMulti: Boolean, val requiresInput: Boolean,
                       val requiresTwoInput: Boolean, val id: String, val drawableName: String,
                       val req: Map<String, LuaPredicate>?,
                       val drawableCheckedName: String?, val orderTag: String?) {
    
    val name = "command.$id.name"
    val desc = "command.$id.desc"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Command) return false
        
        if (executable != other.executable) return false
        if (allowMulti != other.allowMulti) return false
        if (requiresInput != other.requiresInput) return false
        if (requiresTwoInput != other.requiresTwoInput) return false
        if (id != other.id) return false
        if (drawableName != other.drawableName) return false
        if (req != other.req) return false
        if (drawableCheckedName != other.drawableCheckedName) return false
        if (orderTag != other.orderTag) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = executable.hashCode()
        result = 31 * result + allowMulti.hashCode()
        result = 31 * result + requiresInput.hashCode()
        result = 31 * result + requiresTwoInput.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + drawableName.hashCode()
        result = 31 * result + (req?.hashCode() ?: 0)
        result = 31 * result + (drawableCheckedName?.hashCode() ?: 0)
        result = 31 * result + (orderTag?.hashCode() ?: 0)
        return result
    }
}

