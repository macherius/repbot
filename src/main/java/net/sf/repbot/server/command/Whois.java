package net.sf.repbot.server.command;

import java.time.Instant;

/** Interface into the fibs whois database.
 *
 * @author  Avi Kivity
 */
public interface Whois {
    
    /** User information. */
    interface Info {
        
        /** Gets the user' name as given by the server (capitalization!). */
        String getName();
        
        /** Gets the user's exprerience. */
        int getExperience();
        
        /** Gets the user's rating. */
        double getRating();
        
        /** Is the user currently logged in? */
        boolean isOnline();
        
        /** Is the user ready to play? */
        boolean isReady();
        
        /** Is the user playing right now? */
        boolean isPlaying();
        
        /** Gets the name of the current opponent. */
        String getOpponent();
        
        /** Gets the date of the last login. */
        Instant getLogin();
        
        /** Gets the date of the last logout, if not logged in right now. */
        Instant getLogout();
        
        /** Gets the host name from which the user last connected. */
        String getHost();
    }
    
    /** Interface for deliverying a whois response asynchronously. */
    interface ReplyListener {
        
        /** Called on notmal reply. */
        void onReply(String name, Info info);
        
        /** Called on no user found. */
        void onNoUser(String name);
        
        /** Called on timeout, usually due to an error. */
        void onTimeout(String name);
    }
    
    /** Submits an asynchronous request to the whois database. */
    void query(String name, ReplyListener replyListener, long timeout);
    
}
