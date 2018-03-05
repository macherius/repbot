package net.sf.repbot.preferences;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public interface Preferences {
	default Duration getDuration(String propertyKey) throws IllegalArgumentException {
		return Duration.ofMillis(getTimeUnit(propertyKey).toMillis(getLong(propertyKey)));
	}

	default long getLong(String propertyKey) throws IllegalArgumentException {
		try {
			return Long.parseLong(getString(propertyKey));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid number: "  + propertyKey + '=' + getString(propertyKey), nfe);
		}
	}

	default int getUnsignedInteger(String propertyKey) throws IllegalArgumentException {
		try {
			return Integer.parseUnsignedInt(getString(propertyKey));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid number for "  + propertyKey + '=' + getString(propertyKey), nfe);
		}
	}

	default String getString(String propertyKey) throws IllegalArgumentException {
		final String value = getUserProperties().getProperty(propertyKey);
		if (value == null) {
			throw new IllegalArgumentException("Unknown property key: " + propertyKey);
		}
		return value;
	}

	default boolean getBoolean(String propertyKey) throws IllegalArgumentException {
		return Boolean.parseBoolean(getString(propertyKey));
	}

	default TimeUnit getTimeUnit(String propertyKey) throws IllegalArgumentException {
		String unitString = getString(propertyKey + ".unit").toUpperCase(Locale.US);
		for (TimeUnit tu : TimeUnit.values()) {
			if (tu.name().equals(unitString)) {
				return tu;
			}
		}
		throw new IllegalArgumentException("Unknwon TimeUnit for " + propertyKey + ".unit=" + unitString);
	}

	default void list(PrintStream out) {
		getDefaultProperties().keySet().stream().sorted()
				.forEachOrdered(key -> out.println(key + "=" + getUserProperties().getProperty((String) key)));
	}

	default InetAddress getInetAddress(String propertyKey) throws IllegalArgumentException {
		try {
			return InetAddress.getByName(getString(propertyKey));
		} catch (UnknownHostException uhe) {
			throw new IllegalArgumentException("Unknown host for " + propertyKey + '=' + getString(propertyKey), uhe);
		}
	}
	
	default String[] getCommaSeparated(String propertyKey) {
		try {
			return getString(propertyKey).split("\\s*,\\s*");
		} catch (IllegalArgumentException iae) {
			return new String[0];
		}
	}

	Properties getUserProperties();

	Properties getDefaultProperties();
}
