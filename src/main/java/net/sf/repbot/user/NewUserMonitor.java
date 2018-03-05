package net.sf.repbot.user;

import java.time.Instant;

import net.sf.repbot.db.UserDB;
import net.sf.repbot.db.UserDBException;
import net.sf.repbot.server.Monitor;
import net.sf.repbot.services.Logger;

public class NewUserMonitor extends Monitor.DefaultListener {

	private final UserManager reaper;
	private final UserDB db;
	private final Logger logger;

	public NewUserMonitor(UserManager reaper, UserDB db, Logger logger) {
		this.logger = logger;
		this.db = db;
		this.reaper = reaper;
	}

	/**
	 * Called on a new user. If the nick already exists in the DB, we have to delete
	 * it.
	 */
	public void onNewUser(String name) {
		if (reaper.isKnownUser(name)) {
			reaper.deleteUser(name);
		}
		// We do not call onLogin(user) here, MonitorImpl will do
	}

	/** Called on used login. */
	public void onLogin(String name) {
		try {
			db.setLastVerified(name, Instant.now());
		} catch (UserDBException e) {
			logger.logTimestampedLine(
					NewUserMonitor.class.getCanonicalName() + " failed to set verification timestamp for user " + name,
					e);
		}
	}
}
