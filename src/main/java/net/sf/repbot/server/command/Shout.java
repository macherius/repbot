package net.sf.repbot.server.command;

import net.sf.repbot.server.TimeoutListener;

/** An interface for executing the tell command asynchronously.
 *
 * @author  Avi Kivity
 */
public interface Shout {
    
    /** Tell result listener. */
    interface ReplyListener extends TimeoutListener {
        
        /** Called when the shout has been executed. */
        default void onShout() {};
    }
    
    /** Tell a user something. */
    void shout(String what, ReplyListener listener);

    /** A default implementation of ReplyListener. Does nothing. */
    class DefaultReplyListener implements ReplyListener {
    	@Override
    	public void onTimeout() {
    	}
    }
}
