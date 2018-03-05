package net.sf.repbot.user;

import net.sf.repbot.server.Server;
import net.sf.repbot.server.TimeoutListener;
import net.sf.repbot.server.command.*;
import net.sf.repbot.services.Executor;
import net.sf.repbot.services.Logger;
import net.sf.repbot.db.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Scans for users deleted on the fibs server and deletes them locally as well.
 *
 * @author Avi Kivity
 */
public class UserManager {

	private final UserDB db;
	private final Logger logger;
	private final Server server;
	private final Executor cron;
	
	/** Creates a new instance of Reaper */
	public UserManager(UserDB db, Logger logger, Executor cron, Server server) {
		this.db = db;
		this.logger = logger;
		this.cron = cron;
		this.server = server;
	}

	public void addGarbageCollector(UserGarbageCollector queue) {
		queue.schedule();
	}

	public boolean isKnownUser(String name) {
		try {
			return db.isKnownUser(name);
		} catch (UserDBException e) {
			logger.logTimestampedLine(NewUserMonitor.class.getCanonicalName() + " failed to look up user " + name, e);
			return false;
		}
	}

	/** Called when a user is in the database but not in fibs. */
	public void deleteUser(String user) {
		logger.logTimestampedLine("Reaper: " + user + " is dead.");
		try {
			db.deleteUser(user);
		} catch (UserDBException e) {
			logger.logTimestampedLine("Reaper failed to delete user " + user, e);
		}
	}

	/**
	 * Called when a user has been verified, if their experience and logout date is
	 * known
	 */
	public void updateUser(String user, int lastExperience, Instant lastLogout) {
		if (lastLogout == null) {
			lastLogout = Instant.now();
		}
		try {
			db.setLastVerifiedExpLogout(user, Instant.now(), lastExperience, lastLogout);
		} catch (UserDBException e) {
			logger.logTimestampedLine("Reaper failed to set extended verification data for user " + user, e);
		}
	}
	
	// FIXME: hack
	
	void schedule(TimeoutListener listener, Duration delay) {
		cron.addTimeout(listener, delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	void whois(String user, Whois.ReplyListener listener) {
		server.whois(user, listener);
	}
	
	UserDB getDB() {
		return db;
	}
}
