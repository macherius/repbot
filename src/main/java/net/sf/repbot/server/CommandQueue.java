package net.sf.repbot.server;

/** A queue for sending commands to fibs. Fibs, apparently, cannot handle
 *  more than one unacknowledged command at a time, so we serialize commands
 *  via this interface.
 *
 *  @author  Avi Kivity
 */
public interface CommandQueue extends LineListener, TimeoutListener {
    
    /** A listener for responses to commands, including time out 
     *  notifications. */
    interface Listener extends LineListener, TimeoutListener {
    	void setHandle(CommandQueue.Handle handle);
    }
    
    /** Represents a queued command. */
    interface Handle {
        
        /** Signals that the command has been acknowledged by fibs. */
        void acknowledge();
    }
    
    /** Enqueues a command. 
     *  @param command   complete fibs command
     *  @param Listener  will receive fibs responses to the command
     *  @return queue handle, which must be acknowledged when the command
     *          completes. */
    Handle send(String command, Listener listener);
}
