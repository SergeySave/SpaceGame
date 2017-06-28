
attribute vec4 a_position;

attribute vec4 a_multTint;
attribute vec4 a_addTint;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_multTint;
varying vec4 v_addTint;
varying vec2 v_texCoords;

void main()
{
   v_texCoords = a_texCoord0;
   v_multTint = a_multTint;
   v_addTint = a_addTint;
   gl_Position =  u_projTrans * a_position;
}
