package net.sf.repbot.server.command;

import net.sf.repbot.server.NoUserListener;
import net.sf.repbot.server.TimeoutListener;

public interface Message {

	/** Message result listener. */
	interface ReplyListener extends TimeoutListener, NoUserListener {

		/** Called when the message was delivered directly. */
		default void onDelivered() {};

		/** Called when the message was saved. */
		default void onSaved() {};
	}

	/** Tell a user something. */
	void message(String who, String what, ReplyListener listener);

	/** A default implementation of ReplyListener. Does nothing. */
	class DefaultReplyListener implements ReplyListener {
		@Override
		public void onTimeout() {
		}
		@Override
		public void onNoUser() {
		}
	}
}
