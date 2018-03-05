package net.sf.repbot.command;

import net.sf.repbot.server.Server;
import net.sf.repbot.server.TimeoutListener;
import net.sf.repbot.services.Executor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DefaultCommand implements Command {

	private Server server;
	private Executor cron;
	private long ignoreSpammerTimerMillis;
	private int ignoreSpammerCount;

	private Map<String, SpammerTimer> spammers = new ConcurrentHashMap<>(2);

	public DefaultCommand(Server server, Executor cron, Duration ignoreSpammerTimer,
			int ignoreSpammerCount) {
		this.ignoreSpammerTimerMillis = ignoreSpammerTimer.toMillis();
		this.ignoreSpammerCount = ignoreSpammerCount;
		this.server = server;
		this.cron = cron;
	}

	@Override
	public void execute(String user, String command, String[] arguments) {
		// We do not talk to spammers
		if (isSpammer(user))
			return;
		// Everybody else gets a hint
		server.tell(user, "I don't understand you. Try 'tell RepBot help'.");
	}

	private boolean isSpammer(String name) {
		SpammerTimer timer = spammers.get(name);
		if (timer == null) {
			timer = new SpammerTimer(name);
			spammers.put(name, timer);
		} else {
			timer.bump();
		}
		return timer.isSpammer();
	}

	// keep track of people who "tell" incorrect command very often. they're
	// likely to be buggy bots in a loop.

	class SpammerTimer implements TimeoutListener {

		private String name;
		private int nTells;

		SpammerTimer(String name) {
			this.name = name;
			cron.addTimeout(this, ignoreSpammerTimerMillis, TimeUnit.MILLISECONDS);
		}

		void bump() {
			++nTells;
			cron.removeTimeout(this);
			cron.addTimeout(this, ignoreSpammerTimerMillis, TimeUnit.MILLISECONDS);
		}

		boolean isSpammer() {
			return nTells >= ignoreSpammerCount;
		}

		@Override
		public void onTimeout() {
			// de-register ourself
			spammers.remove(name);
		}
	}
}
