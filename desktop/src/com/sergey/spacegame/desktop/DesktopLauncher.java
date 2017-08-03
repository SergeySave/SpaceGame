package com.sergey.spacegame.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode;
import com.sergey.spacegame.SpaceGame;

import java.util.Arrays;
import java.util.List;

public class DesktopLauncher {
	public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        config.setTitle("SpaceGame");
        config.setHdpiMode(HdpiMode.Pixels);
        config.useVsync(true);
        config.setWindowedMode(1600, 900);
        
        List<String> args = Arrays.asList(arg);
        
        new Lwjgl3Application(new SpaceGame(), config);
    }
}
