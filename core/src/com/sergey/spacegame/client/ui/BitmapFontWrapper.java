package com.sergey.spacegame.client.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class BitmapFontWrapper extends BitmapFont {
	
	private BitmapFont wrapped;
	
	public BitmapFontWrapper(BitmapFont font) {
		super();
		super.dispose();
		wrapped = font;
	}
	
	/**
	 * Set the wrapped BitmapFont
	 * 
	 * @param wrapped the new bitmap font to wrap
	 * @return the old bitmap font
	 */
	public BitmapFont setWrapped(BitmapFont wrapped) {
		BitmapFont old = this.wrapped;
		this.wrapped = wrapped;
		return old;
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#draw(com.badlogic.gdx.graphics.g2d.Batch, java.lang.CharSequence, float, float)
	 */
	@Override
	public GlyphLayout draw(Batch batch, CharSequence str, float x, float y) {
		return wrapped.draw(batch, str, x, y);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#draw(com.badlogic.gdx.graphics.g2d.Batch, java.lang.CharSequence, float, float, float, int, boolean)
	 */
	@Override
	public GlyphLayout draw(Batch batch, CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
		return wrapped.draw(batch, str, x, y, targetWidth, halign, wrap);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#draw(com.badlogic.gdx.graphics.g2d.Batch, java.lang.CharSequence, float, float, int, int, float, int, boolean)
	 */
	@Override
	public GlyphLayout draw(Batch batch, CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap) {
		return wrapped.draw(batch, str, x, y, start, end, targetWidth, halign, wrap);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#draw(com.badlogic.gdx.graphics.g2d.Batch, java.lang.CharSequence, float, float, int, int, float, int, boolean, java.lang.String)
	 */
	@Override
	public GlyphLayout draw(Batch batch, CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap, String truncate) {
		return wrapped.draw(batch, str, x, y, start, end, targetWidth, halign, wrap, truncate);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#draw(com.badlogic.gdx.graphics.g2d.Batch, com.badlogic.gdx.graphics.g2d.GlyphLayout, float, float)
	 */
	@Override
	public void draw(Batch batch, GlyphLayout layout, float x, float y) {
		wrapped.draw(batch, layout, x, y);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getColor()
	 */
	@Override
	public Color getColor() {
		return wrapped.getColor();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#setColor(com.badlogic.gdx.graphics.Color)
	 */
	@Override
	public void setColor(Color color) {
		wrapped.setColor(color);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#setColor(float, float, float, float)
	 */
	@Override
	public void setColor(float r, float g, float b, float a) {
		wrapped.setColor(r, g, b, a);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getScaleX()
	 */
	@Override
	public float getScaleX() {
		return wrapped.getScaleX();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getScaleY()
	 */
	@Override
	public float getScaleY() {
		return wrapped.getScaleY();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getRegion()
	 */
	@Override
	public TextureRegion getRegion() {
		return wrapped.getRegion();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getRegions()
	 */
	@Override
	public Array<TextureRegion> getRegions() {
		return wrapped.getRegions();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getRegion(int)
	 */
	@Override
	public TextureRegion getRegion(int index) {
		return wrapped.getRegion(index);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getLineHeight()
	 */
	@Override
	public float getLineHeight() {
		return wrapped.getLineHeight();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getSpaceWidth()
	 */
	@Override
	public float getSpaceWidth() {
		return wrapped.getSpaceWidth();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getXHeight()
	 */
	@Override
	public float getXHeight() {
		return wrapped.getXHeight();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getCapHeight()
	 */
	@Override
	public float getCapHeight() {
		return wrapped.getCapHeight();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getAscent()
	 */
	@Override
	public float getAscent() {
		return wrapped.getAscent();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getDescent()
	 */
	@Override
	public float getDescent() {
		return wrapped.getDescent();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#isFlipped()
	 */
	@Override
	public boolean isFlipped() {
		return wrapped.isFlipped();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#dispose()
	 */
	@Override
	public void dispose() {
		wrapped.dispose();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#setFixedWidthGlyphs(java.lang.CharSequence)
	 */
	@Override
	public void setFixedWidthGlyphs(CharSequence glyphs) {
		wrapped.setFixedWidthGlyphs(glyphs);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#setUseIntegerPositions(boolean)
	 */
	@Override
	public void setUseIntegerPositions(boolean integer) {
		wrapped.setUseIntegerPositions(integer);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#usesIntegerPositions()
	 */
	@Override
	public boolean usesIntegerPositions() {
		return wrapped.usesIntegerPositions();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getCache()
	 */
	@Override
	public BitmapFontCache getCache() {
		return wrapped.getCache();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#getData()
	 */
	@Override
	public BitmapFontData getData() {
		return wrapped.getData();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#ownsTexture()
	 */
	@Override
	public boolean ownsTexture() {
		return wrapped.ownsTexture();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#setOwnsTexture(boolean)
	 */
	@Override
	public void setOwnsTexture(boolean ownsTexture) {
		wrapped.setOwnsTexture(ownsTexture);
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#newFontCache()
	 */
	@Override
	public BitmapFontCache newFontCache() {
		if (wrapped == null) return super.newFontCache();
		return wrapped.newFontCache();
	}

	/**
	 * @see com.badlogic.gdx.graphics.g2d.BitmapFont#toString()
	 */
	@Override
	public String toString() {
		return wrapped.toString();
	}
}
