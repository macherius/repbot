package net.sf.repbot.server.command;

import net.sf.repbot.server.CommandQueue.Handle;

public abstract class AbstractCommandQueueListener implements CommandQueueListener {

	protected Handle handle;

	@Override
	public void setHandle(Handle handle) {
		this.handle = handle;
	}
	
	/** Called when the command has been unacknowledged for too long. */
	@Override
	public void onTimeout() {
		getListener().onTimeout();
	}
}
