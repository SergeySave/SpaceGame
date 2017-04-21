package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Engine;

public class ECSManager {
	private Engine engine;
	
	public ECSManager() {
		engine = new Engine();
	}
	
	public Engine getEngine() {
		return engine;
	}
}
