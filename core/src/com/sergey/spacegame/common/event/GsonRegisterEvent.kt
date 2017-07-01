package com.sergey.spacegame.client.event

import com.google.gson.GsonBuilder
import com.sergey.spacegame.common.event.Event

class GsonRegisterEvent(val gson: GsonBuilder) : Event()