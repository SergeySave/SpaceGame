package com.sergey.spacegame.common.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/**
 * @author sergeys
 */
class LuaPredicate(val original: String, val lua: String, val name: String) {
    private val code: LuaValue = LUA_GLOBALS.load(lua)
    
    fun test(): Boolean = code.call().checkboolean()
    
    private companion object {
        private val LUA_GLOBALS: Globals = LuaUtils.newStandard()
        
        init {
            LUA_GLOBALS.load(SpaceGameLuaLib.INSTANCE)
        }
    }
}