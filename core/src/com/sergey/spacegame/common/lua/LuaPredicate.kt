package com.sergey.spacegame.common.lua

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/**
 * Represents a LUA backed predicate
 *
 * @author sergeys
 *
 * @constructor Create a new LuaPredicate
 *
 * @property original - the original json string that was converted to LUA
 * @property lua - the lua code that will be run
 */
class LuaPredicate(val original: String, val lua: String) {
    private val code: LuaValue = LUA_GLOBALS.load(lua)
    
    /**
     * Test the predicate
     *
     * @return whether the lua code returned true
     */
    fun test(): Boolean = code.call().checkboolean()
    
    private companion object {
        private val LUA_GLOBALS: Globals = LuaUtils.newStandard()
        
        init {
            LUA_GLOBALS.load(SpaceGameLuaLib)
        }
    }
}