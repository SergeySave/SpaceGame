package com.sergey.spacegame.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode;
import com.sergey.spacegame.common.SpaceGame;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains the main method for the desktop client
 *
 * @author sergeys
 */
public class DesktopLauncher {
    
    /**
     * The main method of the desktop client
     *
     * @param arg - the command line arguments to the program
     */
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        config.setTitle("SpaceGame");
        config.setHdpiMode(HdpiMode.Pixels);
        config.useVsync(true);
        config.setWindowedMode(1600, 900);
        
        List<String> args = Arrays.asList(arg);
        
        new Lwjgl3Application(new SpaceGame(SpaceGameClient.INSTANCE), config);
    }
}
