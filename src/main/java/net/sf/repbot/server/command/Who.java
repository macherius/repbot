package net.sf.repbot.server.command;

import java.util.Collection;

import net.sf.repbot.server.TimeoutListener;

/** Interface into the fibs whois database.
 *
 * @author  Avi Kivity
 */
public interface Who {
    
    /** User information. */
    interface Info {
        
        /** Gets the user' name as given by the server (capitalization!). */
        String getName();
        
        /** Is the user ready? */
        boolean isReady();

        /** Is the user playing? */
        boolean isPlaying();
        
        /** Is the user watching? */
        boolean isWatching();
        
        /** Is the user away? */
        boolean isAway();
        
        /** Gets the user's rating. */
        double getRating();
        
        /** Gets the user's exprerience. */
        int getExperience();
        
        /** Gets the host name from which the user last connected. */
        String getHost();
    }
    
    /** Interface for deliverying a whois response asynchronously. */
    interface ReplyListener  extends TimeoutListener {
        
        /** Called on notmal reply. */
        void onReply(Collection<Who.Info> info);
    }
    
    /** Submits an asynchronous request to the whois database.
     * TODO: No timeout support yet */
    void query(ReplyListener replyListener);
    
}
