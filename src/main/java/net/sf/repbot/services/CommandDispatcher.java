package net.sf.repbot.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.repbot.command.Command;
import net.sf.repbot.command.DefaultCommand;
import net.sf.repbot.db.UserDBException;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.preferences.AdminPreferences;
import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.LineListener;

/**
 *
 * @author avi, inim
 */
public class CommandDispatcher implements LineListener {

	private final static String[] NULLSTRINGARRAY = new String[0];

	private final static Pattern says = Pattern.compile("(\\S+) says: \\s*(\\S+)?\\s*(.*\\S)?\\s*");
	private final Collection<Entry> registeredCommands = new ArrayList<>();
	private final Command defaultCommand;
	private final UserDB db;
	private final Logger logger;
	private final AdminPreferences prefs;

	private static class Entry {
		Entry(Pattern pattern, Command command, boolean isAdminCommand) {
			this.pattern = pattern;
			this.command = command;
			this.isAdminCommand = isAdminCommand;
		}

		Pattern pattern;
		Command command;
		boolean isAdminCommand;
	}

	public CommandDispatcher(AdminPreferences prefs, DefaultCommand defaultCommand, UserDB db, Logger logger) {
		this.defaultCommand = defaultCommand;
		this.db = db;
		this.logger = logger;
		this.prefs = prefs;
	}

	/** Registers a command. */
	public CommandDispatcher register(String pattern, Command command) {
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		registeredCommands.add(new Entry(p, command, false));
		return this;
	}

	/** Registers a privileged command. */
	public CommandDispatcher registerPriviledged(String pattern, Command command) {
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		registeredCommands.add(new Entry(p, command, true));
		return this;
	}

	/** Called on a received line. */
	public void onLine(String line, FibsListener connection) {
		Matcher m;
		if (!(m = says.matcher(line)).matches())
			return;
		final String who = m.group(1);
		final String verb = Objects.toString(m.group(2), "");
		final String rest = Objects.toString(m.group(3), "");

		// Deny service to banned users
		if (who != null && contains(prefs.getBanned(), who)) {
			logger.logTimestampedLine("Ignored '" + verb + " " + rest + "' from banned user " + who + ".");
			// server.tell(who, "You are banned from using RepBot.", new
			// Tell.DefaultReplyListener());
			return;
		}

		try {
			db.markAsUser(who);
		} catch (UserDBException e) {
			logger.logTimestampedLine("Error marking user " + who + ": ", e);
		}
		String[] arguments = rest.isEmpty() ? NULLSTRINGARRAY : rest.split("\\s+");

		Command command = defaultCommand;

		for (Entry e : registeredCommands) {
			if (e.pattern.matcher(verb).matches()) {
				// Check execution privileges for admin commands
				// If missing privileges, treat as unknown (default) command
				if (e.isAdminCommand) {
					if (contains(prefs.getAdmin(), who))
						command = e.command;
				} else {
					command = e.command;
				}
				break;
			}
		}
		command.execute(who, verb, arguments);
	}

	private final static <T> boolean contains(final T[] array, final T v) {
		if (array.length == 0) {
			return false;
		}
		for (final T e : array) {
			if (e == v || v != null && v.equals(e))
				return true;
		}
		return false;
	}
}
