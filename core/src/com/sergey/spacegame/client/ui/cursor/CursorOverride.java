package com.sergey.spacegame.client.ui.cursor;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.game.Level;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;

public interface CursorOverride {
    
    /**
     * The cursor returned by this method should always be the same instance
     * It should be disposed in the dispose method implementation
     *
     * @return an optional cursor object if a cursor is requested
     */
    Optional<Cursor> getRequestedCursor();
    
    default void load(FileSystem fileSystem) throws IOException                        {}
    
    default void init()                                                                {}
    
    default void drawExtra(Level level, DrawingBatch batch, OrthographicCamera camera) {}
    
    default boolean needsInitialization() {
        return false;
    }
    
    void dispose();
}
