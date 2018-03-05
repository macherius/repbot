package net.sf.repbot.preferences;

import java.net.InetAddress;
import java.time.Duration;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FibsListenerPreferences {

	private final InetAddress host;
	private final int port;
	private final String user;
	private final String password;
	private final Duration loginRetryWaitMin;
	private final Duration loginRetryWaitMax;
	private final Duration loginTimeout;

	public FibsListenerPreferences(Preferences prefs) throws IllegalArgumentException {
		host = prefs.getInetAddress("net.sf.repbot.fibs.host");
		port = prefs.getUnsignedInteger("net.sf.repbot.fibs.port");
		if (port <1 || port>65535) {
			throw new IllegalArgumentException("0 < \"net.sf.repbot.fibs.port\" < 65535, got " + port); 
		}
		user = prefs.getString("net.sf.repbot.fibs.user");
		password = prefs.getString("net.sf.repbot.fibs.password");
		loginTimeout = prefs.getDuration("net.sf.repbot.fibs.login.timeout");
		loginRetryWaitMin = prefs.getDuration("net.sf.repbot.fibs.login.retry.timer.min");
		loginRetryWaitMax = prefs.getDuration("net.sf.repbot.fibs.login.retry.timer.max");
	}
}
