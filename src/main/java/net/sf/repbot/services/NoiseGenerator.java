package net.sf.repbot.services;

import java.util.concurrent.TimeUnit;

import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.LineListener;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.TimeoutListener;

/**
 * This LineListener generates noise if we're too quiet, so fibs won't log us
 * out.
 *
 * @author Avi Kivity
 */
public class NoiseGenerator implements LineListener, TimeoutListener {

	/*
	 * Command sent whenever we are at the risk of timing out Make sure request and
	 * response are in sync.
	 */
	private long timeout;
	private TimeUnit timeoutUnit;
	private Server server;
	private Executor cron;

	/** Creates a new instance of NoiseGenerator */
	public NoiseGenerator(long timeout, TimeUnit timeoutUnit, Server server, Executor cron) {
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.server = server;
		this.cron = cron;
		cron.addTimeout(this, timeout, timeoutUnit);
	}

	/** Called on a received line. Reschedules the timeout. */
	@Override
	public void onLine(String line, FibsListener connection) {
		cron.removeTimeout(this);
		cron.addTimeout(this, timeout, timeoutUnit);
	}

	/** Called on a timeout. Sends some dummy command. */
	@Override
	public void onTimeout() {
		server.version();
		cron.addTimeout(this, timeout, timeoutUnit);
	}
}
