package net.sf.repbot.server.command;
import lombok.Getter;
import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;

/**
 * Implementation of the async tell command.
 *
 * @author Avi Kivity
 */
public class TellImpl implements Tell {

	CommandQueue queue;

	/** Creates a new instance of TellImpl */
	public TellImpl(CommandQueue queue) {
		this.queue = queue;
	}

	/** Tell a user something. */
	@Override
	public void tell(String who, String what, ReplyListener listener) {
		String command = "tell " + who + ' ' + what;
		Request request = new Request(listener);
		CommandQueue.Handle handle = queue.send(command, request);
		request.setHandle(handle);
	}

	private static class Request extends AbstractCommandQueueListener {
		
		// private Pattern p_affirmative = Pattern.compile("\\*\\* You tell (\\w+):
		// (.*)");
		// private Pattern p_negative = Pattern.compile("\\*\\* There is no one called
		// (\\w+)");
		private final static String s_affirmative = "** You tell ";
		private final static String s_negative = "** There is no one called ";

		@Getter
		private ReplyListener listener;

		public Request(ReplyListener listener) {
			this.listener = listener;
		}

		/** Called on a received line. */
		@Override
		public void onLine(String line, FibsListener connection) {
			// if (p_affirmative.matcher(line).matches()) {
			if (line.startsWith(s_affirmative)) {
				handle.acknowledge();
				listener.onTell();
				return;
			}
			// if (p_negative.matcher(line).matches()) {
			if (line.startsWith(s_negative)) {
				handle.acknowledge();
				listener.onNoUser();
				return;
			}
		}
	}
}
