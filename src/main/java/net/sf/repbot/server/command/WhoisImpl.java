package net.sf.repbot.server.command;

import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;
import net.sf.repbot.util.WhoisDateTimeParser;
import java.util.regex.*;
import java.time.Instant;
import lombok.Getter;
import lombok.ToString;

/**
 * Implementation of an asynchronous whois database.
 * 
 * <p>
 * Apparently fibs can only handle one request at a time, so we queue the
 * requests and issue them one at a time.
 * 
 * @author Avi Kivity
 */
public class WhoisImpl implements Whois {

	CommandQueue queue;

	private final static Pattern p_negative = Pattern.compile("No information found on user ([^\\s]+)\\.");
	private final static Pattern p_affirmative = Pattern.compile("Information about ([^\\s]+):");
	private final static Pattern p_login = Pattern.compile("  Last login:  (.+) from ([^\\s]+)");
	private final static Pattern p_logout = Pattern.compile("  Last logout: (.+)");
	private final static Pattern p_online = Pattern.compile("  Still logged in\\. ([:\\d]+) (second|minute)s idle\\.");
	private final static Pattern p_ready = Pattern.compile("  ([^\\s]+) is (not )?ready to play.*");
	private final static Pattern p_rating = Pattern.compile("  Rating: (-?[\\d\\.]+) Experience: (\\d+)");
	private final static Pattern p_playing = Pattern.compile("  (\\w+) is playing with (\\w+).");

	/** Creates a new instance of WhoisImpl */
	public WhoisImpl(CommandQueue queue) {
		this.queue = queue;
	}

	/** Submits an asynchronous request to the whois database. */
	@Override
	public void query(String name, ReplyListener replyListener, long timeout) {
		Request request = new Request(name, replyListener);
		CommandQueue.Handle handle = queue.send("whois " + name, request);
		request.setHandle(handle);
	}

	/** A whois request. */
	private static class Request implements CommandQueue.Listener {

		private String name;
		private ReplyListener listener;
		private Reply reply;
		private CommandQueue.Handle handle;

		public Request(String name, ReplyListener listener) {
			this.name = name;
			this.listener = listener;
			this.reply = new Reply();
		}

		/** Called on a received line. */
		public void onLine(String line, FibsListener connection) {
			try {
				Matcher m;
				if ((m =  p_negative.matcher(line)).matches()) {
					if (m.group(1).equals(name))
						listener.onNoUser(name);
					else
						listener.onTimeout(name);
					handle.acknowledge();
					return;
				}
				if ((m = p_affirmative.matcher(line)).matches()) {
					if (!m.group(1).equalsIgnoreCase(name)) {
						listener.onTimeout(name);
						handle.acknowledge();
						return;
					}
					reply.name = m.group(1);
					return;
				}
				/*
				 * Short circuit logic, any interesting line starts with two spaces.
				 */
				if (line.length() < 2 || line.charAt(0) != ' ' || line.charAt(1) != ' ') {
					return;
				}
				if ((m = p_login.matcher(line)).matches()) {
					reply.login = WhoisDateTimeParser.parseDate(m.group(1));
					reply.host = m.group(2);
					return;
				}
				if ((m = p_logout.matcher(line)).matches()) {
					reply.logout = WhoisDateTimeParser.parseDate(m.group(1));
					return;
				}
				if (p_online.matcher(line).matches()) {
					reply.online = true;
					return;
				}
				if ((m = p_ready.matcher(line)).matches()) {
					reply.ready = m.group(1).isEmpty();
					return;
				}
				if (p_playing.matcher(line).matches()) {
					reply.playing = true;
					return;
				}
				if ((m = p_rating.matcher(line)).matches()) {
					reply.rating = Double.parseDouble(m.group(1));
					reply.experience = Integer.parseUnsignedInt(m.group(2));
					// FIBS appears not to generate logout lines sometimes
					// so don't check for them
					if (reply.name != null && reply.login != null && reply.rating != 0)
						listener.onReply(name, reply);
					else
						listener.onTimeout(name);
					handle.acknowledge();
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				listener.onTimeout(name);
				handle.acknowledge();
			}
		}

		/** Called when the command has been unacknowledged for too long. */
		public void onTimeout() {
			listener.onTimeout(name);
		}

		/** Sets the handle into the command queue. */
		public void setHandle(CommandQueue.Handle handle) {
			this.handle = handle;
		}

	}

	/** A whois reply. */
	@Getter
	@ToString
	private static class Reply implements Info {
		String name;
		int experience;
		double rating;
		String host;
		Instant login;
		Instant logout;
		String opponent;
		boolean online;
		boolean playing;
		boolean ready;
	}
}
