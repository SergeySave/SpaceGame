package com.sergey.spacegame.common.game

/**
 * This class represents an objective for the player
 *
 * @author sergeys
 *
 * @constructor Creates a new Objective object
 *
 * @property id - the id of this objective
 * @property title - the title of this objective
 * @property description - the description of this objective
 * @property completed - is this objective completed
 */
data class Objective(var id: String, var title: String, var description: String, var completed: Boolean = false)