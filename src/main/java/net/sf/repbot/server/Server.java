package net.sf.repbot.server;

import java.util.Collection;

import net.sf.repbot.server.command.Message;
import net.sf.repbot.server.command.Shout;
import net.sf.repbot.server.command.Spell;
import net.sf.repbot.server.command.Tell;
import net.sf.repbot.server.command.Version;
import net.sf.repbot.server.command.Who;
import net.sf.repbot.server.command.Whois;

/** A representation of the fibs server.
 *
 *  @author  Avi Kivity
 */
public interface Server extends ConnectListener {
    
    /** Tells a user something. */
    void tell(String who, String what, Tell.ReplyListener listener);

    /** Tells a user something. */
    void tell(String who, String what);

    /** Tells a user something. */
    void tell(String who, Collection<String> messages, Tell.ReplyListener listener);

    /** Tells a user something. */
    void tell(String who, Collection<String> messages);
    
    
    /** Messages a user something. */
    void message(String who, String what, Message.ReplyListener listener);    

    /** Messages a user something. */
    void message(String who, String what);    

    
    /** Shout something. */
    void shout(String what, Shout.ReplyListener listener);

    /** Shout something. */
    void shout(String what);


    /** Queries user info. */
    void whois(String who, Whois.ReplyListener listener);
    
    /** Correct spelling of a user's name. Faster than whois. */
    void spell(String who, Spell.ReplyListener listener);
    
    /** Gets information about all online users. */
    void who(Who.ReplyListener listener);
    
    /** Gets information about the server's version. */
    void version();

    /** Gets information about the server's version. */
    public void version(Version.ReplyListener listener);
}
