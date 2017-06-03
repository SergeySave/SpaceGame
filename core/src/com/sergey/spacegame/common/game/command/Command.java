package com.sergey.spacegame.common.game.command;

public class Command {
	private CommandExecutable executable;
	private boolean requiresInput;
	private boolean requiresTwoInput;
	
	public Command(CommandExecutable executable, boolean requiresInput, boolean requiresTwoInput) {
		this.executable = executable;
		this.requiresInput = requiresInput;
		this.requiresTwoInput = requiresTwoInput;
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
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Command)) return false;
		Command other = (Command)obj;
		return executable.equals(other.executable) && requiresInput == other.requiresInput && requiresTwoInput == other.requiresTwoInput;
	}
	
	@Override
	public int hashCode() {
		return executable.hashCode() << 2 + (requiresInput ? 2 : 0) + (requiresTwoInput ? 1 : 0);
	}
}
