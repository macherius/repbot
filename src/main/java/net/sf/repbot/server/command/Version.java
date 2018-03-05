package net.sf.repbot.server.command;

import net.sf.repbot.server.TimeoutListener;

public interface Version {
    /** Tell result listener. */
    interface ReplyListener extends TimeoutListener {
    	@Override
    	default void onTimeout() {};
    }
    
    void version(ReplyListener listener);
    
    /** A default implementation of ReplyListener. Does nothing. */
    class DefaultReplyListener implements ReplyListener {
    }
}
