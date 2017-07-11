package com.sergey.spacegame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sergey.spacegame.SpaceGame;

import java.util.Arrays;
import java.util.List;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = 1600;
		config.height = 900;
		config.useHDPI = true;
		config.vSyncEnabled = true;
        
        List<String> args = Arrays.asList(arg);

		new LwjglApplication(new SpaceGame(), config);
	}
}
