package com.sergey.spacegame.client.ui.cursor;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.game.Level;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;

/**
 * This interface represents an override to the default cursor
 *
 * It can return an optional cursor to replace the system cursor or it can just be used to draw extra details
 *
 * @author sergeys
 */
public interface CursorOverride {
    
    /**
     * The cursor returned by this method should always be the same instance
     * It should be disposed in the dispose method implementation
     *
     * @return an optional cursor object if a cursor is requested
     */
    Optional<Cursor> getRequestedCursor();
    
    /**
     * Called when this cursor should do it's loading
     *
     * @param fileSystem - the filesystem for the path to the current level
     *
     * @throws IOException if there was an error accessing files
     */
    default void load(FileSystem fileSystem) throws IOException                                         {}
    
    /**
     * Called when this cursor should do its initialization
     */
    default void init()                                                                                 {}
    
    /**
     * Called to draw extra details to the world
     *
     * @param level   - the level containing the world's details
     * @param batch   - the drawing batch to use to draw
     * @param camera  - the camera used by the current screen
     * @param enabled - is this cursor override's command enabled
     */
    default void drawExtra(Level level, DrawingBatch batch, OrthographicCamera camera, boolean enabled) {}
    
    /**
     * Does this cursor override need to be initialized
     *
     * @return whether this cursor ride needs to be initialized
     */
    default boolean needsInitialization() {
        return false;
    }
    
    /**
     * Called to release any resources being held by this cursor
     */
    void dispose();
}
