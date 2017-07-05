package com.sergey.spacegame.client.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class UIUtil {
    
    public static ShaderProgram compileShader(String vertex, String fragment) {
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
    
    public static ShaderProgram compileShader(FileHandle vertex, FileHandle fragment) {
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
}