package com.sergey.spacegame.common.util

/**
 * This class represents a 4-Tuple of any objects
 *
 * @param T - the type of the first object
 * @param U - the type of the second object
 * @param V - the type of the third object
 * @param W - the type of the fourth object
 *
 * @author sergeys
 *
 * @constructor Create a new Quadruple with the given four objects
 *
 * @property obj1 - the first object
 * @property obj2 - the second object
 * @property obj3 - the third object
 * @property obj4 - the fourth object
 */
data class Quadruple<out T, out U, out V, out W>(val obj1: T, val obj2: U, val obj3: V, val obj4: W)