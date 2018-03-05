// TODO
//	unit test
//	OOM debug
//	logging control/level?
//	avoid local alloc in loop of int l
//	re-structure/modularize processLoop
//		use some regexp package (rio?), Only do FIRST pattern matched
//			not ALL
//		action's response processing is a private method call
//	note RFC comment embedded in loop below
//	stop via cgi method?

package net.sf.repbot.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

import javax.security.auth.login.FailedLoginException;

import lombok.Getter;
import net.sf.repbot.preferences.CommandQueuePreferences;
import net.sf.repbot.preferences.FibsListenerPreferences;
import net.sf.repbot.preferences.Preferences;
import net.sf.repbot.services.Executor;
import net.sf.repbot.services.Logger;
import net.sf.repbot.util.CharStateMachine;
import net.sf.repbot.util.CharStateMachineRecorder;
import net.sf.repbot.util.LastLoginInfo;

public class FibsListener implements Runnable {

	private Logger logger;

	private FibsListenerPreferences fibsListenerPrefs;
	private CommandQueuePreferences commandQueueListenerPrefs;

	// See {@link SocketOptions#SO_TIMEOUT SO_TIMEOUT}
	// Must be > 0 for this code to work
	private final static Duration FIBS_SO_TIMEOUT = Duration.ofSeconds(20);
	private final static Charset FIBS_CHARSET = Charset.forName("US-ASCII");

	/*
	 * Socket to fibs and the two (synchronous) streams
	 */

	private NetworkConnection networkConnection;
	@Getter
	private LoginConnection loginConnection;
	private ListenerMultiplexer listener;
	private Executor cron;

	/**
	 * true if we should execute the main loop. Set to false via stop() method to
	 * exit loop.
	 */
	private boolean done;

	/*
	 * Constructor
	 */

	public FibsListener(Preferences prefs, Executor cron, Logger logger) {
		this.fibsListenerPrefs = new FibsListenerPreferences(prefs);
		this.commandQueueListenerPrefs = new CommandQueuePreferences(prefs);
		this.logger = logger;
		this.cron = cron;
		this.listener = new ListenerMultiplexer();
	}

	@Override
	public void run() {

		// String line = null;
		done = false;

		while (!done) {
			logger.logTimestampedLine("FibsListener.run:top of Login/processLoop loop");
			Thread.currentThread().setName(toString());

			Duration waitBetweenAttempts = fibsListenerPrefs.getLoginRetryWaitMin();

			// login attempt loop
			while (true) {
				try {
					// attempt. if successful, break out to processLoop
					logger.logTimestampedLine("Attempting network connection with " + fibsListenerPrefs.getHost() + ':'
							+ fibsListenerPrefs.getPort());
					networkConnection = new NetworkConnection(fibsListenerPrefs.getHost(), fibsListenerPrefs.getPort(),
							listener);
					logger.logTimestampedLine("... object created, login attempt ");
					networkConnection.connect(FIBS_SO_TIMEOUT);
					logger.logTimestampedLine("network connection to " + networkConnection + " established.");
					logger.logTimestampedLine("Attempting login as user " + fibsListenerPrefs.getUser());
					loginConnection = new LoginConnection(networkConnection, fibsListenerPrefs.getUser(),
							fibsListenerPrefs.getPassword(), listener);
					logger.logTimestampedLine("... object created ");
					loginConnection.login(FIBS_SO_TIMEOUT);
					logger.logTimestampedLine("login with " + loginConnection + " established.");
					loginConnection.send("toggle telnet");
					break;
				} catch (IOException e) {
					e.printStackTrace();
					// wait progressively longer, but not longer than 2 minutes
					if (waitBetweenAttempts.compareTo(fibsListenerPrefs.getLoginRetryWaitMax()) < 0) {
						waitBetweenAttempts = waitBetweenAttempts.plus(waitBetweenAttempts);
					}
					logger.logTimestampedLine("login failed, retry in " + waitBetweenAttempts + ".");
					for (Instant now = Instant.now(), deadline = now.plus(waitBetweenAttempts); now
							.isBefore(deadline); now = Instant.now()) {
						try {
							Thread.sleep(now.until(deadline, ChronoUnit.MILLIS));
						} catch (InterruptedException interruptedException) {
						}
					}
				} catch (FailedLoginException fle) {
					// FIXME: Get out
					fle.printStackTrace();
				}
			}

			Thread.currentThread().setName(toString());

			// now we're in. process until socket breaks
			try {
				logger.logTimestampedLine("FibsListener.run:processLoop enter");
				processLoop();
				logger.logTimestampedLine("FibsListener.run:processLoop exit");
			} catch (Exception e) {
				e.printStackTrace();
				logger.logTimestampedLine("FibsListener.run:processLoop Exception");
				reconnect();
				logger.logTimestampedLine("FibsListener.run:done cleaning up socket, done=" + done);
			}
		}
	}

	private void processLoop() throws IOException {
		while (!done) {
			try {
				String line = networkConnection.getFrom().readLine();
				if (null != line && !line.isEmpty()) {
					// cron.execute(() -> {
					logger.logTimestampedLine(line);
					listener.onLine(line, this);
					// });
				}
			} catch (SocketTimeoutException e) {
				// Ignored. We read from a socket with timeout, so
				// waking up periodically guarantees that
				// the "done" flag is checked
			}
		}
	}

	public void stop() {
		this.done = true;
	}

	/** Signals a reconnect request by closing the socket. */
	public void reconnect() {
		try {
			if (loginConnection != null) {
				loginConnection.close();
			}
			if (networkConnection != null) {
				networkConnection.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.currentThread().setName(toString());
	}

	public void addLineListener(LineListener lineListener) {
		listener.add(lineListener);
	}

	public void addConnectListener(ConnectListener connectlistener) {
		listener.add(connectlistener);
	}

	public class ListenerMultiplexer implements LineListener, ConnectListener {

		private Collection<LineListener> lineListeners = new HashSet<>();
		private Collection<ConnectListener> connectListeners = new HashSet<>();

		public void add(LineListener lineListener) {
			if (lineListeners.contains(lineListener)) {
				throw new IllegalStateException(
						LineListener.class.getCanonicalName() + lineListener + "already registered.");
			}
			lineListeners.add(lineListener);
		}

		public void remove(LineListener lineListener) {
			if (!lineListeners.contains(lineListener)) {
				throw new IllegalStateException(
						LineListener.class.getCanonicalName() + lineListener + "not registered.");
			}
			lineListeners.remove(lineListener);
		}

		public void add(ConnectListener listener) {
			if (connectListeners.contains(listener)) {
				throw new IllegalStateException(
						ConnectListener.class.getCanonicalName() + listener + "already registered.");
			}
			connectListeners.add(listener);
		}

		@Override
		public void onConnect(LoginConnection connection) {
			for (ConnectListener connectionListener : connectListeners)
				connectionListener.onConnect(loginConnection);
		}

		@Override
		public void onDisconnect(LoginConnection connection) {
			for (ConnectListener connectionListener : connectListeners)
				connectionListener.onDisconnect(loginConnection);
		}

		@Override
		/** Notify any {@link LineListerer}s that we received a line. */
		public void onLine(String line, FibsListener connection) {
			for (LineListener ll : lineListeners) {
				try {
					ll.onLine(line, connection);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Getter
	public class NetworkConnection implements AutoCloseable {
		InetSocketAddress host;
		Socket socket;
		BufferedReader from;
		OutputStream to;
		boolean connected;
		ListenerMultiplexer listener;

		public NetworkConnection(InetAddress host, int port, ListenerMultiplexer listener) {
			this.host = new InetSocketAddress(host, port);
			socket = new Socket();
			this.listener = listener;
		}

		public boolean connect(Duration timeout) throws UnknownHostException, IOException {
			logger.logTimestampedLine("connect trace enter" + timeout);

			// FIXME Java 9: timeout.toMillisPart();
			int timeoutMillis = (int) timeout.toMillis();

			// DNS-resolve host again, as FIBS DNS outages and changes are quite common
			// We already resolved at preferences reading time, it meanwhile may have
			// vanished.Let's provoke an error in this case.
			logger.logTimestampedLine("connect trace dns");
			InetAddress address = InetAddress.getByName(getHost().getAddress().getHostName());
			// UnknownHostException

			// if (getHost().getAddress().isReachable(timeoutMillis)) {
			logger.logTimestampedLine("connect trace socket.connect");
			socket.connect(host, timeoutMillis);
			// Make IO non-blocking, 1 sec during login
			socket.setSoTimeout(timeoutMillis);
			from = new BufferedReader(new InputStreamReader(socket.getInputStream(), "US-ASCII"));
			to = socket.getOutputStream();
			connected = true;
			// }

			// NoRouteToHostException
			// ConnectException
			// SocketException
			// IllegalArgumentException
			// IOException
			logger.logTimestampedLine("connect trace leave");
			return connected;
		}

		@Override
		public String toString() {
			return host.toString();
		}

		@Override
		public void close() throws Exception {
			connected = false;
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
	}

	@Getter
	public class LoginConnection implements AutoCloseable {

		private final char[] FIBS_LOGIN_PROMPT = new char[] { 'l', 'o', 'g', 'i', 'n', ':', ' ' };
		private final String FIBS_LOGIN_AUTHENTIFICATED = "** User %s authenticated.\r\n";

		NetworkConnection network;
		String user;
		String password;
		LastLoginInfo lastLogin;
		boolean authorized;
		ListenerMultiplexer listener;
		CommandQueue queue;

		LoginConnection(NetworkConnection network, String user, String password, ListenerMultiplexer listener) {
			this.network = network;
			this.user = user;
			this.password = password;
			this.listener = listener;
		}

		public boolean login(Duration timeout) throws IOException, FailedLoginException {

			try {
				CharStateMachineRecorder fsm = skipToLoginPrompt(timeout);
				for (String line : fsm.getRecording().split("\n")) {
					listener.onLine(line, FibsListener.this);
				}
				if (fsm.isFinalState()) {
					sendLoginCredentials();
				}

				authorized = verifyAuthorization(timeout);
				if (!authorized) {
					throw new FailedLoginException("Login as" + toString() + "failed.");
				}

				// Correct socket timeout from login mode back to normal
				network.getSocket().setSoTimeout((int) FIBS_SO_TIMEOUT.toMillis());

				queue = new CommandQueueImpl(commandQueueListenerPrefs, this, cron);
				listener.add(queue);

				String lastLoginLine = network.getFrom().readLine();
				lastLogin = new LastLoginInfo(lastLoginLine);
				listener.onConnect(this);
				listener.onLine(lastLoginLine, FibsListener.this);
				return authorized;
			} catch (IOException ioe) {
				throw new IOException("IO Error while login in as " + toString(), ioe);
			}
		}

		private void readCharsUntil(Function<Character, Boolean> nextChar, Duration timeout) throws IOException {
			Instant deadline = Instant.now().plus(timeout);

			while (true) {
				try {
					final char readChar = (char) network.getFrom().read();
					if (readChar == -1) {
						throw new IOException("EOF while trying to login " + toString() + '.');
					} else if (nextChar.apply(readChar)) {
						break;
					} else if (Instant.now().isAfter(deadline)) {
						throw new IOException(
								"Timeout while trying to login " + toString() + ", waited " + timeout + '.');
					}
				} catch (SocketTimeoutException sto) {
					// spurious read timeout, safe to ignore
				}
			}
		}

		private CharStateMachineRecorder skipToLoginPrompt(Duration timeout) throws IOException {
			CharStateMachineRecorder fsm = new CharStateMachineRecorder(FIBS_LOGIN_PROMPT);
			readCharsUntil(c -> fsm.transition(c), timeout);
			return fsm;
		}

		private boolean verifyAuthorization(Duration timeout) throws IOException {
			String authorizationConfirmed = FIBS_LOGIN_AUTHENTIFICATED.replace("%s", getUser());

			CharStateMachine authFsm = new CharStateMachine(authorizationConfirmed);
			CharStateMachine promptFsm = new CharStateMachine(FIBS_LOGIN_PROMPT);
			readCharsUntil(c -> authFsm.transition(c) || promptFsm.transition(c), timeout);

			if (promptFsm.isFinalState()) {
				throw new IOException("Authorization failed for " + toString() + ". Wrong user or password.");
			}

			if (authFsm.isFinalState()) {
				listener.onLine(authorizationConfirmed, FibsListener.this);
				return true;
			} else {
				throw new IOException("Timeout while waiting for authorization confirmation " + toString() + ", waited "
						+ timeout + '.');
			}
		}

		private void sendLoginCredentials() throws IOException {
			getNetwork().getTo().write(("connect " + user + ' ' + password + "\r\n").getBytes("US-ASCII"));
			getNetwork().getTo().flush();
		}

		/*
		 * Send a line to the server
		 */

		public void send(String line) throws IOException {
			logger.logTimestampedLine("toFIBS:" + line);
			if (!network.isConnected())
				throw new IOException("Not connected.");
			if (!authorized)
				throw new IOException("Not authorized.");
			int length = line.length();
			byte[] bytes = Arrays.copyOf(line.getBytes(FIBS_CHARSET), length + 2);
			bytes[length] = '\r';
			bytes[length + 1] = '\n';
			network.getTo().write(bytes);
			network.getTo().flush();
		}

		@Override
		public String toString() {
			return getUser() + '@' + getNetwork();
		}

		@Override
		public void close() throws Exception {
			// FIXME: clean fibs logout
			if (authorized) {
				send("exit");
			}
			listener.onDisconnect(this);
			listener.remove(queue);
			authorized = false;
			if (network != null) {
				network.close();
				network = null;
			}
		}
	}

	/*
	 * Utility
	 */

	// @Override
	// public String toString() {
	// return FibsListener.class.getCanonicalName() + " " +
	// getConnectionDescription() + " "
	// + (networkConnection.isConnected() ? "(" : "(dis") + "connected" + ", "
	// + (loginConnection.isAuthorized() ? "" : "not ") + "authorized" + ")";
	// }
	//
	// private final String getConnectionDescription() {
	// return loginConnection.toString();
	// }
}
