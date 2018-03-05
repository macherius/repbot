package net.sf.repbot.server.command;

import java.util.regex.*;

import lombok.Getter;
import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;

/**
 * Implementation of the async tell command.
 *
 * @author Avi Kivity
 */
public class ShoutImpl implements Shout {

	CommandQueue queue;

	/** Creates a new instance of ShoutImpl */
	public ShoutImpl(CommandQueue queue) {
		this.queue = queue;
	}

	/** Shouts something. */
	@Override
	public void shout(String what, ReplyListener listener) {
		String command = "shout " + what;
		Request request = new Request(listener);
		CommandQueue.Handle handle = queue.send(command, request);
		request.setHandle(handle);
	}

	private static class Request extends AbstractCommandQueueListener {

		private final static String p_affirmative = "You shout: ";

		@Getter
		private ReplyListener listener;

		public Request(ReplyListener listener) {
			this.listener = listener;
		}

		/** Called on a received line. */
		@Override
		public void onLine(String line, FibsListener connection) {
			if (line.startsWith(p_affirmative, 0)) {
				handle.acknowledge();
				listener.onShout();
				return;
			}
		}
	}
}
