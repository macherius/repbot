package net.sf.repbot.server.command;

import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.TimeoutListener;

/**
 * A listener for responses to commands, including time out notifications.
 */

public interface CommandQueueListener extends CommandQueue.Listener {
	TimeoutListener getListener();
}
