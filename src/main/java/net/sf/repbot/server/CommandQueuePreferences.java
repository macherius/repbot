package net.sf.repbot.server;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.ToString;
import net.sf.repbot.preferences.Preferences;

@Getter
@ToString
public class CommandQueuePreferences {
	private TimeUnit unit;
	private long timeout;

	public CommandQueuePreferences(Preferences prefs) {
		timeout = prefs.getLong("net.sf.repbot.fibs.command.queue.timeout");
		unit = prefs.getTimeUnit("net.sf.repbot.fibs.command.queue.timeout");
	}
}
