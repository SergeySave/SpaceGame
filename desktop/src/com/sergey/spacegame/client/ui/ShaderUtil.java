package com.sergey.spacegame.client.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * This class contains various Shader utility functions
 *
 * @author sergeys
 */
public class ShaderUtil {
    
    /**
     * Create a shader program from two Strings containing the code for the vertex and fragment shaders
     *
     * @param vertex   - the code of the vertex shader
     * @param fragment - the code of the fragment shader
     *
     * @return a compiled ShaderProgram
     *
     * @throws IllegalArgumentException if the shader failed to compile
     */
    public static ShaderProgram compileShader(String vertex, String fragment) {
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
    
    /**
     * Create a shader program from two FileHandles to text files
     * containing the code for the vertex and fragment shaders
     *
     * @param vertex   - the file containing the vertex shader
     * @param fragment - the file containing the fragment shader
     *
     * @return a compiled ShaderProgram
     *
     * @throws IllegalArgumentException if the shader failed to compile
     */
    public static ShaderProgram compileShader(FileHandle vertex, FileHandle fragment) {
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
}