package net.sf.repbot.preferences;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LoggerPreferences {

	private final boolean logToStdout;
	private final String filePathFormat;
	private final String timestampFormat;

	public LoggerPreferences(Preferences prefs) throws IllegalArgumentException {
		logToStdout = prefs.getBoolean("net.sf.repbot.debug");
		filePathFormat = prefs.getString("net.sf.repbot.logging.file");
		timestampFormat = prefs.getString("net.sf.repbot.logging.timestamp");
	}
}
