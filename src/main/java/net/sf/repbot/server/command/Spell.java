package net.sf.repbot.server.command;

/** Fast interface into the fibs whois database, giving only user 
 *  existence and username spelling information.
 *
 * @author  Avi Kivity
 */
@FunctionalInterface
public interface Spell {
    
    /** Interface for deliverying a spell response asynchronously. */
    interface ReplyListener {
        
        /** Called on notmal reply. */
        void onReply(String name, String spelledName);
        
        /** Called on no user found. */
        void onNoUser(String name);
        
        /** Called on timeout, usually due to an error. */
        void onTimeout(String name);
    }
    
    /** Submits an asynchronous request to the whois database. */
    void query(String name, ReplyListener replyListener, long timeout);
    
}
