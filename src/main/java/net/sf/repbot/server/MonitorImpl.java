/*
 * MonitorImpl.java
 *
 * Created on July 4, 2002, 11:09 PM
 */

package net.sf.repbot.server;

import java.util.*;
import java.util.regex.*;

/**
 * Implementation of the fibs monitor.
 *
 * @author avi
 */
public class MonitorImpl implements Monitor, Monitor.Listener, LineListener {

	private final Collection<Listener> listeners = new ArrayList<>();

	private final static Pattern shouts = Pattern.compile("(\\S+) shouts: (.*)");
	private final static Pattern logsin = Pattern.compile("(\\S+) logs in( again)?.");
	private final static Pattern logsout = Pattern.compile("(\\S+) logs out.");
	private final static Pattern drops = Pattern.compile("(\\S+) drops connection.");
	private final static Pattern closeold = Pattern.compile("Closed old connection with user (\\S+).");
	private final static Pattern timedout = Pattern.compile("Connection with (\\S+) timed out.");
	private final static Pattern admin = Pattern.compile("(\\S+) closes connection with (\\S),");
	private final static Pattern neterr = Pattern.compile("Network error with (\\S+).");
	private final static Pattern start = Pattern.compile("(\\S+) and (\\S+) start a (\\d+) point match.");
	private final static Pattern resume = Pattern.compile("(\\S+) and (\\S+) are resuming their (\\d+)-point match.");
	private final static Pattern end = Pattern.compile("(\\S+) wins a (\\d+) point match against (\\S+)  (\\d+)-(\\d+) .");
	private final static Pattern newuser = Pattern.compile("(\\S+) just registered and logs in\\.");

	/** Creates a new instance of MonitorImpl */
	public MonitorImpl() {
	}
	
	/*
	 * Monitor interface
	 */

	/** Registers a new event listener. */
	@Override
	public void addEventListener(Listener listener) {
		listeners.add(listener);
	}

	/** Deregisters an event listener. */
	@Override
	public void removeEventListener(Listener listener) {
		listeners.remove(listener);
	}

	/*
	 * LineListener interface
	 */

	/** Called on a received line. */
	@Override
	public void onLine(String line, FibsListener connection) {
		Matcher m;
		if ((m = shouts.matcher(line)).matches())
			onShout(m.group(1), m.group(2));
		else if ((m = newuser.matcher(line)).matches()) {
			onNewUser(m.group(1));
			onLogin(m.group(1));
		} else if ((m = logsin.matcher(line)).matches())
			onLogin(m.group(1));
		else if ((m = logsout.matcher(line)).matches())
			onLogout(m.group(1));
		else if ((m = drops.matcher(line)).matches())
			onDrop(m.group(1));
		else if ((m = admin.matcher(line)).matches())
			onAdminClose(m.group(2), m.group(1));
		else if ((m = neterr.matcher(line)).matches())
			onNetworkError(m.group(1));
		else if ((m = closeold.matcher(line)).matches())
			onClosedOldConnection(m.group(1));
		else if ((m = timedout.matcher(line)).matches())
			onConnectionTimedOut(m.group(1));
		else if ((m = start.matcher(line)).matches())
			onStartMatch(m.group(1), m.group(2), Integer.parseUnsignedInt(m.group(3)));
		else if ((m = resume.matcher(line)).matches())
			onResumeMatch(m.group(1), m.group(2), Integer.parseUnsignedInt(m.group(3)));
		else if ((m = end.matcher(line)).matches())
			onEndMatch(m.group(1), m.group(3), Integer.parseUnsignedInt(m.group(2)),
					Integer.parseUnsignedInt(m.group(4)), Integer.parseUnsignedInt(m.group(5)));
	}

	/*
	 * Monitor.Listener interface
	 */

	/** Called when a user drops connection. */
	@Override
	public void onDrop(String user) {
		for (Listener listener : listeners)
			listener.onDrop(user);
	}

	/** Called when a game is finished. */
	@Override
	public void onEndMatch(String user1, String user2, int length, int points1, int points2) {
		for (Listener listener : listeners)
			listener.onEndMatch(user1, user2, length, points1, points2);
	}

	/** Called when a user logs in. */
	@Override
	public void onNewUser(String user) {
		for (Listener listener : listeners)
			listener.onNewUser(user);
	}

	/** Called when a user logs in. */
	@Override
	public void onLogin(String user) {
		for (Listener listener : listeners)
			listener.onLogin(user);
	}

	/** Called when a user logs out. */
	@Override
	public void onLogout(String user) {
		for (Listener listener : listeners)
			listener.onLogout(user);
	}

	/** Called when a game is resumed. */
	@Override
	public void onResumeMatch(String user1, String user2, int length) {
		for (Listener listener : listeners)
			listener.onResumeMatch(user1, user2, length);
	}

	/** Called when a user shouts something. */
	@Override
	public void onShout(String user, String message) {
		for (Listener listener : listeners)
			listener.onShout(user, message);
	}

	/** Called when a game is started. */
	@Override
	public void onStartMatch(String user1, String user2, int length) {
		for (Listener listener : listeners)
			listener.onStartMatch(user1, user2, length);
	}

	/** Called when a network error occurs with a user. */
	@Override
	public void onNetworkError(String user) {
		for (Listener listener : listeners)
			listener.onNetworkError(user);
	}

	/** Called when a user's connection is closed by an administrator. */
	@Override
	public void onAdminClose(String user, String admin) {
		for (Listener listener : listeners)
			listener.onAdminClose(user, admin);
	}

	/** Called when an old connection is closed. */
	@Override
	public void onClosedOldConnection(String user) {
		for (Listener listener : listeners)
			listener.onClosedOldConnection(user);
	}

	/** Called when a connection timeout occurs with a user. */
	@Override
	public void onConnectionTimedOut(String user) {
		for (Listener listener : listeners)
			listener.onConnectionTimedOut(user);
	}
}
