#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
void main()
{
  gl_FragColor = v_color * texture2D(u_texture, v_texCoords) * vec4(0.5, 0.5, 0.5, 0.5) + vec4(0.5, 0.0, 0.0, 0.0);
}
