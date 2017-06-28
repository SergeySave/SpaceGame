#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_multTint;
varying LOWP vec4 v_addTint;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
	//vec4 v = texture2D(u_texture, v_texCoords);
	gl_FragColor = texture2D(u_texture, v_texCoords) * v_multTint + v_addTint;
}
