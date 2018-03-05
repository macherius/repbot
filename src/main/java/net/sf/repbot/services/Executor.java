package net.sf.repbot.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.repbot.server.TimeoutListener;

public class Executor {

	private ConcurrentMap<TimeoutListener, ScheduledFuture<?>> timeoutEntries = new ConcurrentHashMap<>();
	private ScheduledThreadPoolExecutor executor;

	public Executor() {
		ThreadFactory tf = (r) -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setName("repbot-" + t.getName());
			return t;
		};
		executor = new ScheduledThreadPoolExecutor(2, tf);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setRemoveOnCancelPolicy(true);
	}
	/*
	 * Timers
	 */

	public void addTimeout(TimeoutListener callback, long delay, TimeUnit timeUnit) {
		if (timeoutEntries.containsKey(callback))
			throw new IllegalStateException("Timeout already registered");
		// System.out.printf("Add Callback=%s, Delay=%d, TimeUnit=%s \n", callback,
		// delay, timeUnit.toString());
		Runnable r = () -> {
			// System.out.printf("Execute Callback=%s, Delay=%d, TimeUnit=%s \n", callback,
			// delay, timeUnit.toString());
			timeoutEntries.remove(callback);
			callback.onTimeout();
		};
		ScheduledFuture<?> sf = executor.schedule(r, delay, timeUnit);
		timeoutEntries.put(callback, sf);
	}

	public void removeTimeout(TimeoutListener callback) {
		ScheduledFuture<?> sf = timeoutEntries.remove(callback);
		if (sf == null)
			throw new IllegalStateException("Timeout not registered");
		// System.out.printf("Remove Callback=%s, ScheduledFuture=%s \n", callback,
		// sf.toString());
		sf.cancel(false);
	}
	
	/*
	 * Periodic tasks
	 */

	public void scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		ScheduledFuture<?> sf = executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/*
	 * Long running tasks
	 */

	public void execute(Runnable run) {
		executor.execute(run);
	}
}
