package net.sf.repbot.server;

/** A listener for timeout events.
 *
 *  @author  Avi Kivity
 */
@FunctionalInterface
public interface TimeoutListener {

    /** Called on a timeout. */
    void onTimeout();    
}
