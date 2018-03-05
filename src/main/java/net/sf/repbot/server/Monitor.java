package net.sf.repbot.server;

/** Monitors user activities on fibs.
 *
 *  @author Avi Kivity
 */
public interface Monitor extends LineListener {

    /** Registers a new event listener. */
    void addEventListener(Listener listener);
    
    /** Deregisters an event listener. */
    void removeEventListener(Listener listener);
    
    /** Event listener for fibs user activities. */
    interface Listener {
        
        /** Called when a user shouts something. */
        default void onShout(String user, String message) {};
        
        /** Called when a user is first registered. */
        default void onNewUser(String user) {};
        
        /** Called when a user logs in. */
        default void onLogin(String user) {};
        
        /** Called when a user logs out. */
        default void onLogout(String user) {};
        
        /** Called when a user drops connection. */
        default void onDrop(String user) {};
        
        /** Called when a network error occurs with a user. */
        default void onNetworkError(String user) {};
        
        /** Called when a connection timeout occurs with a user. */
        default void onConnectionTimedOut(String user) {};
        
        /** Called when an old connection is closed. */
        default void onClosedOldConnection(String user) {};
        
        /** Called when a user's connection is closed by an administrator. */
        default void onAdminClose(String user, String admin) {};
        
        /** Called when a game is started. */
        default void onStartMatch(String user1, String user2, int length) {};
        
        /** Called when a game is resumed. */
        default void onResumeMatch(String user1, String user2, int length) {};
        
        /** Called when a game is finished. */
        default void onEndMatch(String user1, String user2, int length, int points1, int points2) {};
    }
    
    /** Default do-nothing event listener. */
    class DefaultListener implements Listener {        
    }
}
