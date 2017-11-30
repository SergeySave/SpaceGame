@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("Utils")

package com.sergey.spacegame.common.util

import java.util.Random

/**
 * @author sergeys
 *
 * A file containing various utility functions
 */

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will return true that proportion of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 *
 * @return a boolean describing the results of the test
 */
fun Random.test(trueChance: Float): Boolean = nextFloat() <= trueChance

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will run the given function the given proportion of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 * @param trueFunc - a function to call when true is returned
 */
inline fun Random.test(trueChance: Float, trueFunc: () -> Unit) = if (nextFloat() <= trueChance) trueFunc() else Unit

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will run the given function with the value true the given proporiton of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 * @param func - a function to call with the result of the test
 *
 * @return whatever the function returns
 */
inline fun <reified T> Random.test(trueChance: Float, func: (Boolean) -> T): T = func(nextFloat() <= trueChance)

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will return true that proportion of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 *
 * @return a boolean describing the results of the test
 */
fun Random.test(trueChance: Double): Boolean = nextDouble() <= trueChance

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will run the given function the given proportion of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 * @param trueFunc - a function to call when true is returned
 */
inline fun Random.test(trueChance: Double, trueFunc: () -> Unit) = if (nextDouble() <= trueChance) trueFunc() else Unit

/**
 * Checks if a random float is less than or equal to the given value.
 * When passed a value in [0,1] it will run the given function with the value true the given proporiton of the time
 *
 * @param trueChance - the proportion of the time that the function will return true
 * @param func - a function to call with the result of the test
 *
 * @return whatever the function returns
 */
inline fun <reified T> Random.test(trueChance: Double, func: (Boolean) -> T): T = func(nextDouble() <= trueChance)

/**
 * Returns the first value in an iterator that meets a given criteria
 *
 * @param predicate - a function of T to a boolean determining the criteria that needs to be met
 *
 * @return the first item in the iterator that meets the given criteria
 */
inline fun <T> Iterator<T>.first(predicate: (T) -> Boolean): T? {
    while (hasNext()) {
        val obj = next()
        if (predicate(obj))
            return obj
    }
    return null
}

/**
 * Returns the first value in an sequence that meets a given criteria
 *
 * @param predicate - a function of T to a boolean determining the criteria that needs to be met
 *
 * @return the first item in the sequence that meets the given criteria
 */
inline fun <T> Sequence<T>.first(predicate: (T) -> Boolean): T? = this.iterator().first(predicate)

/**
 * Round the double down to the nearest int
 *
 * @return the floor of this double value
 */
fun Double.floor(): Int = Math.floor(this).toInt()

/**
 * Round the double up to the nearest int
 *
 * @return the ceiling of this double value
 */
fun Double.ceil(): Int = Math.ceil(this).toInt()

/**
 * Clamp the float to a given range
 *
 * @param min - the minimum value to clamp to
 * @param max - the maximum value to clamp to
 *
 * @return the value of this float clamped to a given range
 */
fun Float.clamp(min: Float, max: Float): Float = if (this >= max) max else if (this <= min) min else this

/**
 * Clamp the double to a given range
 *
 * @param min - the minimum value to clamp to
 * @param max - the maximum value to clamp to
 *
 * @return the value of this double clamped to a given range
 */
fun Double.clamp(min: Double, max: Double): Double = if (this >= max) max else if (this <= min) min else this