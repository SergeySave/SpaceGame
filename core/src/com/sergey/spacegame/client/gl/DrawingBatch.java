package com.sergey.spacegame.client.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.SpaceGame;

public class DrawingBatch {
	private static final float ZERO = Color.toFloatBits(0f, 0f, 0f, 0f);

	private final Vector2 tmp = new Vector2();

	//pos,tex, tintM , tintA
	//x,y,u,v,rgba pk,rgba pk
	private Mesh mesh;

	private Texture lastTexture;

	private float[] verticies;
	private short index;
	private boolean drawing;
	private boolean blending;
	private float multTint;
	private float addTint;
	private float lineWidth = 0.75f;
	private int blendSrcFunc;
	private int blendDstFunc;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private ShaderProgram shader;
	private boolean disposeShader;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;


	public DrawingBatch(int size, ShaderProgram shader, boolean disposeShader) {
		//8191 max size
		if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		this.shader = shader;
		this.disposeShader = disposeShader;

		//aliases reference variables in basic.vertex.glsl
		mesh = new Mesh(false, size * 4, size * 6,
				new VertexAttribute(Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"),
				new VertexAttribute(Usage.ColorPacked, 4, "a_multTint"),
				new VertexAttribute(Usage.ColorPacked, 4, "a_addTint"));

		//4 bytes per type @ 2 vals per position, 1 val per color, 2 val per coordinate, and 2 vals for both tints together
		verticies = new float[size * (4*(2+2+1+1))];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);
	}

	public void draw (TextureRegion region, float x, float y, float width, float height) {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before draw.");

		float[] vertices = this.verticies;

		Texture texture = region.getTexture();
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (index == vertices.length)
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.getU();
		final float v = region.getV2();
		final float u2 = region.getU2();
		final float v2 = region.getV();

		vertices[index++] = x;
		vertices[index++] = y;
		vertices[index++] = u;
		vertices[index++] = v;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = x;
		vertices[index++] = fy2;
		vertices[index++] = u;
		vertices[index++] = v2;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = fx2;
		vertices[index++] = fy2;
		vertices[index++] = u2;
		vertices[index++] = v2;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = fx2;
		vertices[index++] = y;
		vertices[index++] = u2;
		vertices[index++] = v;
		vertices[index++] = multTint;
		vertices[index++] = addTint;
	}

	public void draw (TextureRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before draw.");

		float[] vertices = this.verticies;

		Texture texture = region.getTexture();
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (index == vertices.length)
			flush();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		final float u = region.getU();
		final float v = region.getV2();
		final float u2 = region.getU2();
		final float v2 = region.getV();

		vertices[index++] = x1;
		vertices[index++] = y1;
		vertices[index++] = u;
		vertices[index++] = v;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = x2;
		vertices[index++] = y2;
		vertices[index++] = u;
		vertices[index++] = v2;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = x3;
		vertices[index++] = y3;
		vertices[index++] = u2;
		vertices[index++] = v2;
		vertices[index++] = multTint;
		vertices[index++] = addTint;

		vertices[index++] = x4;
		vertices[index++] = y4;
		vertices[index++] = u2;
		vertices[index++] = v;
		vertices[index++] = multTint;
		vertices[index++] = addTint;
	}

	public void setMultTint(Color multTint) {
		this.multTint = multTint.toFloatBits();
	}
	public void setMultTint(float multTint) {
		this.multTint = multTint;
	}

	public void setAddTint(Color addTint) {
		this.addTint = addTint.toFloatBits();
	}
	public void setAddTint(float addTint) {
		this.addTint = addTint;
	}

	public void setForceColor(Color forceColor) {
		this.multTint = ZERO;
		this.addTint = forceColor.toFloatBits();
	}
	public void setForceColor(float forceColor) {
		this.multTint = ZERO;
		this.addTint = forceColor;
	}

	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void line(float x1, float y1, float x2, float y2) {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before draw.");

		if (index == verticies.length)
			flush();

		Vector2 t = tmp.set(y2 - y1, x1 - x2).nor();
		float width = 0.5f * lineWidth;
		float tx = t.x * width;
		float ty = t.y * width;

		verticies[index++] = x1 + tx;
		verticies[index++] = y1 + ty;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x1 - tx;
		verticies[index++] = y1 - ty;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2 - tx;
		verticies[index++] = y2 - ty;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2 + tx;
		verticies[index++] = y2 + ty;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;
	}

	public void rect(float x1, float y1, float x2, float y2) {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before draw.");

		if (index == verticies.length)
			flush();

		verticies[index++] = x1;
		verticies[index++] = y1;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x1;
		verticies[index++] = y2;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2;
		verticies[index++] = y2;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2;
		verticies[index++] = y1;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;
	}
	public void rectWH(float x, float y, float w, float h) {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before draw.");

		if (index == verticies.length)
			flush();

		float x1;
		float y1;
		float x2;
		float y2;

		if (w < 0) {
			x2 = x;
			x1 = x+w;
		} else {
			x1 = x;
			x2 = x+w;
		}
		if (h < 0) {
			y2 = y;
			y1 = y+h;
		} else {
			y1 = y;
			y2 = y+h;
		}

		verticies[index++] = x1;
		verticies[index++] = y1;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x1;
		verticies[index++] = y2;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2;
		verticies[index++] = y2;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;

		verticies[index++] = x2;
		verticies[index++] = y1;
		verticies[index++] = 0;
		verticies[index++] = 0;
		verticies[index++] = multTint;
		verticies[index++] = addTint;
	}

	public void begin () {
		if (drawing) throw new IllegalStateException("DrawingBatch.end must be called before begin.");
		renderCalls = 0;

		Gdx.gl.glDepthMask(false);
		shader.begin();
		setupMatrices();

		drawing = true;
	}

	public void flush () {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before flush.");
		if (index == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int quads = index / 24; // 6 values per vertex * 4 vertexes per quad
		int count = quads * 6; //6 indicies per quad

		if (lastTexture == null) {
			lastTexture = SpaceGame.getInstance().getAtlas().getTextures().first();
		}

		lastTexture.bind();
		Mesh mesh = this.mesh;
		mesh.setVertices(verticies, 0, index);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(count);
		
		if (blending) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
		} else {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}

		mesh.render(shader, GL20.GL_TRIANGLES, 0, count);

		index = 0;
	}

	public void end () {
		if (!drawing) throw new IllegalStateException("DrawingBatch.begin must be called before end.");
		if (index > 0) flush();
		drawing = false;

		Gdx.gl.glDepthMask(true);
		shader.end();
	}

	protected void switchTexture (Texture texture) {
		flush();
		lastTexture = texture;
		//invTexWidth = 1.0f / texture.getWidth();
		//invTexHeight = 1.0f / texture.getHeight();
	}

	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) flush();
		transformMatrix.set(transform);
		if (drawing) setupMatrices();
	}

	private void setupMatrices () {
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		shader.setUniformMatrix("u_projTrans", combinedMatrix);
		shader.setUniformi("u_texture", 0);
	}

	public void disableBlending () {
		if (!blending) return;
		if (drawing) flush();
		blending = false;
	}

	public void enableBlending () {
		if (blending) return;
		if (drawing) flush();
		blending = true;
	}

	public void setBlendFunction (int srcFunc, int dstFunc) {
		if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return;
		if (drawing) flush();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	public void dispose() {
		mesh.dispose();
		mesh = null;
		lastTexture = null;//Should be disposed elsewhere may even still be in use
		verticies = null;
		if (disposeShader) shader.dispose();
		shader = null;
	}

}