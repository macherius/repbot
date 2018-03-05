package net.sf.repbot.util;

import lombok.Getter;
import lombok.ToString;
import net.sf.repbot.preferences.Preferences;

@Getter
@ToString
public class LoggerPreferences {

	private boolean logToStdout;

	public LoggerPreferences(Preferences prefs) throws IllegalArgumentException {
		logToStdout = prefs.getBoolean("net.sf.repbot.debug");
	}
}
