package net.sf.repbot.server.command;

import lombok.Getter;
import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;

public class MessageImpl implements Message {

	private final CommandQueue queue;
	
	/** Creates a new instance of MessageImpl */
	public MessageImpl(CommandQueue queue) {
		this.queue = queue;
	}

	/** Message a user something. */
	@Override
	public void message(String who, String what, ReplyListener listener) {
		String command = "message " + who + " " + what;
		Request request = new Request(listener);
		CommandQueue.Handle handle = queue.send(command, request);
		request.setHandle(handle);
	}

	private static class Request extends AbstractCommandQueueListener {
		
		private final static String P_DELIVERED = "Message delivered:";
		private final static String P_SAVED = "Message saved:";
		private final static String P_NOSUCHUSER = "** Don't know user";

		@Getter
		private ReplyListener listener;

		public Request(ReplyListener listener) {
			this.listener = listener;
		}

		/** Called on a received line. */
		@Override
		public void onLine(String line, FibsListener connection) {
			if (line.startsWith(P_DELIVERED, 0)) {
				handle.acknowledge();
				listener.onDelivered();
				return;
			} else if (line.startsWith(P_SAVED, 0)) {
				handle.acknowledge();
				listener.onSaved();
				return;
			} else if (line.startsWith(P_NOSUCHUSER, 0)) {
				handle.acknowledge();
				listener.onNoUser();
				return;
			}
		}
	}
}
