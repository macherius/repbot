package net.sf.repbot.server;

import net.sf.repbot.server.FibsListener.LoginConnection;

/** A listener for the server connections/disconnections.
 *
 * @author  Avi Kivity
 */
public interface ConnectListener {
    
    /** Called on connection establishment. */
    void onConnect(LoginConnection connection);
    
    /** Called on disconnection. */
    void onDisconnect(LoginConnection connection);
    
}
