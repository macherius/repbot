package net.sf.repbot.user;

import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import net.sf.repbot.server.command.Whois;
import net.sf.repbot.services.Logger;

public abstract class AbstractUserGarbageCollector implements UserGarbageCollector {

	@Getter
	Logger logger;
	UserManager reaper;

	Duration periodicExecutionDelay;

	AbstractUserGarbageCollector(UserManager reaper, Duration periodicExecutionDelay) {
		this.periodicExecutionDelay = periodicExecutionDelay;
		this.reaper = reaper;
	}

	/** Schedules the next invocation of the reaper. */
	public void schedule() {
		schedule(periodicExecutionDelay);
	}

	private void schedule(Duration delay) {
		reaper.schedule(this, delay);
	}
	
	@Override
	/** Picks the least-recently-verified user to re-verify. */
	public void onTimeout() {
		String user = poll();
		if (user == null) {
			schedule();
		} else {
			reaper.whois(user, new Verifier());
		}
	}


	/* Verifies a user's existence, (removing on nonexistence). */
	private class Verifier implements Whois.ReplyListener {

		/** Called on no user found. */
		@Override
		public void onNoUser(String name) {
			reaper.deleteUser(name);
			// on success, reschedule early
			schedule(Duration.ofSeconds(10));
		}

		/** Called on normal reply. */
		@Override
		public void onReply(String name, Whois.Info info) {
			Instant logout = info.getLogout();
			// IN some whois messages, logout is missing
			// for no obvious reason. Be defensive.
			if (info.isOnline()) {
				logout = info.getLogin();
			}
			reaper.updateUser(name, info.getExperience(), logout);
			schedule();
		}

		/** Called on timeout, usually due to an error. */
		@Override
		public void onTimeout(String name) {
			schedule();
		}
	}
}
