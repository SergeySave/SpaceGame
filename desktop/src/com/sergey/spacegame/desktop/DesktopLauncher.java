package com.sergey.spacegame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sergey.spacegame.SpaceGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 1600;
		config.height = 900;
		config.useHDPI = true;
		config.vSyncEnabled = true;

		new LwjglApplication(new SpaceGame(), config);
	}
}
