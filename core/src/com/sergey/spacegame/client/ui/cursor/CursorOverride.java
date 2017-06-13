package com.sergey.spacegame.client.ui.cursor;

import java.util.Optional;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sergey.spacegame.common.game.Level;

public interface CursorOverride {
	/**
	 * The cursor returned by this method should always be the same instance
	 * It should be disposed in the dispose method implementation
	 * 
	 * @return an optional cursor object if a cursor is requested
	 */
	public Optional<Cursor> getRequestedCursor();
	
	public default void init() {}
	
	public default void drawExtra(Level level, SpriteBatch batch) {}
	
	public default boolean needsInitialization() {
		return false;
	}
	
	public void dispose();
}
