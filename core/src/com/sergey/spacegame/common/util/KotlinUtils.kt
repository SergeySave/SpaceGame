@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("KotlinUtils")

package com.sergey.spacegame.common.util

import java.util.Random

/**
 * @author sergeys
 *
 * A file containing various kotlin utility functions
 */

fun Random.test(trueChance: Float): Boolean = nextFloat() <= trueChance

inline fun Random.test(trueChance: Float, trueFunc: () -> Unit) = if (nextFloat() <= trueChance) trueFunc() else Unit
inline fun <reified T> Random.test(trueChance: Float, func: (Boolean) -> T): T = func(nextFloat() <= trueChance)

fun Random.test(trueChance: Double): Boolean = nextDouble() <= trueChance
inline fun Random.test(trueChance: Double, trueFunc: () -> Unit) = if (nextDouble() <= trueChance) trueFunc() else Unit
inline fun <reified T> Random.test(trueChance: Double, func: (Boolean) -> T): T = func(nextDouble() <= trueChance)
