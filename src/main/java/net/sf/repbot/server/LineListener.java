package net.sf.repbot.server;

/** A listener for the server data stream.
 *
 * @author  avi
 */
@FunctionalInterface
public interface LineListener {
    
    /** Called on a received line. 
     *  The line is guaranteed to be not null or empty.
     * */
    void onLine(String line, FibsListener connection);
    
}
