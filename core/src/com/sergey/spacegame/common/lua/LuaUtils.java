package com.sergey.spacegame.common.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.luajc.LuaJC;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class LuaUtils {

	private static final String FILE = "file://";
	private static final String RAW = "lua://";

	public static Globals newStandard() {
		Globals globals = new Globals();
		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new TableLib());
		globals.load(new StringLib());
		globals.load(new JseMathLib());
		LoadState.install(globals);
		LuaC.install(globals);	
		LuaJC.install(globals);
		return globals;
	}

	public static String getLUACode(String rawValue, FileSystem fs) throws IOException {
		if (rawValue.startsWith(FILE)) {
			String fileName = rawValue.substring(FILE.length());
			try {
				return Files.readAllLines(fs.getPath(fileName)).stream().collect(Collectors.joining("\n"));
			} catch (IOException e) {
				throw new IOException("Unable to load lua file " + fileName);
			}
		}
		if (rawValue.startsWith(RAW)) {
			return rawValue.substring(RAW.length());
		}
		return rawValue;
	}
}
