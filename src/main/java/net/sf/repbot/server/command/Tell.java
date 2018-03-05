package net.sf.repbot.server.command;

import net.sf.repbot.server.NoUserListener;
import net.sf.repbot.server.TimeoutListener;

/** An interface for executing the tell command asynchronously.
 *
 * @author  Avi Kivity
 */

@FunctionalInterface
public interface Tell {
    
    /** Tell result listener. */
    interface ReplyListener extends TimeoutListener, NoUserListener {
        
        /** Called when the tell has been executed. */
        default void onTell() {};
    }
    
    /** Tell a user something. */
    void tell(String who, String what, ReplyListener listener);

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
