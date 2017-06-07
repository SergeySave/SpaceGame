package com.sergey.spacegame.common.game.command;

public class Command {
	private CommandExecutable executable;
	private boolean requiresInput;
	private boolean requiresTwoInput;
	private String name;
	private String drawableName;
	private String drawableCheckedName;
	
	public Command(CommandExecutable executable, boolean requiresInput, boolean requiresTwoInput, String name, String drawableName, String drawableCheckedName) {
		this.executable = executable;
		this.requiresInput = requiresInput;
		this.requiresTwoInput = requiresTwoInput;
		this.name = name;
		this.drawableName = drawableName;
		this.drawableCheckedName = drawableCheckedName;
	}

	/**
	 * @return the executable
	 */
	public CommandExecutable getExecutable() {
		return executable;
	}

	/**
	 * @return the requiresInput
	 */
	public boolean isRequiresInput() {
		return requiresInput;
	}

	/**
	 * @return the requiresTwoInput
	 */
	public boolean isRequiresTwoInput() {
		return requiresTwoInput;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDrawableName() {
		return drawableName;
	}
	
	public String getDrawableCheckedName() {
		return drawableCheckedName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Command)) return false;
		Command other = (Command)obj;
		return executable.equals(other.executable) && requiresInput == other.requiresInput && requiresTwoInput == other.requiresTwoInput && name.equals(other.name) && drawableName.equals(other.drawableName) && drawableCheckedName.equals(other.drawableCheckedName);
	}
	
	@Override
	public int hashCode() {
		return executable.hashCode() << 2 + (requiresInput ? 2 : 0) + (requiresTwoInput ? 1 : 0) + name.hashCode() + drawableName.hashCode() + drawableCheckedName.hashCode();
	}
}
